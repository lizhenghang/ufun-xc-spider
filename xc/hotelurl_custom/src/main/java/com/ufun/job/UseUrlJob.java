package com.ufun.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ufun.bean.Hotel;
import com.ufun.util.CalendarUtils;
import com.ufun.util.HttpClientUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/***
 * @author lizenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/22 13:02
 */

public class UseUrlJob implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(UseUrlJob.class);

    private HttpClientUtil httpClientUtil = new HttpClientUtil();

    private JedisPool jedisPool;

    //private SQLManager sqlManager;

    public HttpHost proxy;

    public Jedis jedisTool;

    public HikariDataSource hikariDataSource;

    private boolean flag;

    /**
     * 多线程共享的资源
     * @param jedisPool             redis连接池
     * @param hikariDataSource      mysql连接池
     */
    public UseUrlJob(JedisPool jedisPool, HikariDataSource hikariDataSource) {
        this.jedisPool = jedisPool;
        this.hikariDataSource = hikariDataSource;
    }

    /**
     * 任务执行代码
     */
    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        jedisTool = jedisPool.getResource();
        int count=0;
        while (!flag) {
            log.info(threadName + "__________________本次循环开始__________________");
            List<String> urlList = jedisTool.blpop(200, "xc:hotel");//如果xc:hotel为空那么当前线程就会阻塞在此
            if (urlList == null || urlList.size() < 1)
                continue;
            try {
                if(count>2){
                    proxy=null;
                    count=0;
                }
                use(urlList);
            } catch (Exception e) {
                log.warn("操作异常"+e.toString());
                e.printStackTrace();
                continue;
            }finally {
                count++;
            }
        }
        jedisTool.close();
        log.info(threadName + "___工作结束死亡！！__");
    }

    /**
     * 根据从redis拿到的url进行消费使用
     *
     * @param urlList
     * @throws IOException
     */
    public void use(List<String> urlList) throws Exception {
        try {
            String redisKey = urlList.get(0);//获得redis生产内容的key
            JSONObject jsonObject = JSON.parseObject(urlList.get(1));//获得redis生产内容的value(post内容)
            log.info("从redis获取url");
            getProxyHost();
            log.info("获取proxy代理对象" + proxy);
            HttpPost httpPost = setPost(jsonObject, proxy);//设置请求url
            log.info("设置httpPost请求对象");
            String dataJsonStr = sendPost(httpPost);//发送请求url，得到结果
            log.info("发送httpPost成功返回数据dataJsonStr");
            dataJsonStr = dataJsonStr.replace("\\D", "");//解析结果对象
            log.info("替换dataJsonStr中的无效字符：\\D");
            JSONObject resultJson = JSON.parseObject(dataJsonStr);
            log.info("解析dataJsonStr成功");
            JSONArray hotelArr = resultJson.getJSONArray("hotelPositionJSON");//拿到了成都市下的某一页的酒店列表数据
            log.info("解析hotelAll成功");
            insertDB(hotelArr);
            log.info("批量入库成功");
        } catch (Exception e) {
            throw new Exception("use方法操作异常:" + e.toString());
        }
    }

    /**
     * 获取代理ip
     * 从redis中获取一个代理，如果没有就阻塞等待，知道有可用的代理
     * 如果代理对象为空就创建一个，其他地方发生相关异常，就设置为Null即可
     *
     * @return 返回设置好的代理对象
     */
    private void getProxyHost() {
        if(proxy==null){
            List<String> list = jedisTool.blpop(500, "xc:proxy");
            String redisKey = list.get(0);
            String proxyStr = list.get(1);
            String[] arr = proxyStr.split(":");
            proxy = new HttpHost(arr[0], Integer.valueOf(arr[1]));
        }
    }

    /**
     * 将某一页的数据批量插入DB
     * 插入数据到当日的表中:hotel_yyyy-MM-dd
     * @param hotelArr
     */
    public void insertDB(JSONArray hotelArr) throws SQLException {
        //TODO 入库,这里为了降低代码复杂性就直接入库了，后期优化可以用同步屏障让线程卡主，等待所有线程执行完毕，同意批量入库
        if (hotelArr == null && hotelArr.size() < 1)
            return;
        int[] rs = null;
        PreparedStatement pst = null;
        Connection conn = null;
        String table = "hotel_" + CalendarUtils.getDateString("yyyy_MM_dd");
        String sql = "INSERT INTO `ufun`.`" + table + "`(" +
                "`id`, `sno`, `address`, `name`, `url`, `score`, `star`, `short_name`, `star_desc`, `dp_count`, `dp_score`, " +
                "`is_single_rec`, `lat`, `lon`, `tel`, `create_time`, `update_time`, `type`" +
                ") VALUES (null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            conn = hikariDataSource.getConnection();
            pst = conn.prepareStatement(sql);
            for (int j = 0; j < hotelArr.size(); j++) {
                JSONObject hotelJsonObj = hotelArr.getJSONObject(j);
                Hotel hotel = new Hotel();
                hotel.setSno(hotelJsonObj.getString("id"));
                hotel.setName(hotelJsonObj.getString("name"));
                hotel.setLat(hotelJsonObj.getString("lat"));
                hotel.setLon(hotelJsonObj.getString("lon"));
                hotel.setUrl(hotelJsonObj.getString("url"));
                hotel.setAddress(hotelJsonObj.getString("address"));
                hotel.setStarDesc(hotelJsonObj.getString("stardesc"));
                hotel.setStar(hotelJsonObj.getString("star"));
                hotel.setDpScore(hotelJsonObj.getString("dpscore"));
                hotel.setDpCount(hotelJsonObj.getString("dpcount"));
                hotel.setScore(hotelJsonObj.getString("score"));
                hotel.setShortName(hotelJsonObj.getString("shortname"));
                hotel.setCreateTime(new Date());
                hotel.setType("携程");
                pst.setObject(1, hotel.getSno());
                pst.setObject(2, hotel.getAddress());
                pst.setObject(3, hotel.getName());
                pst.setObject(4, hotel.getUrl());
                pst.setObject(5, hotel.getScore());
                pst.setObject(6, hotel.getStar());
                pst.setObject(7, hotel.getShortName());
                pst.setObject(8, hotel.getStarDesc());
                pst.setObject(9, hotel.getDpCount());
                pst.setObject(10, hotel.getDpScore());
                pst.setObject(11, hotel.getIsSingleRec());
                pst.setObject(12, hotel.getLat());
                pst.setObject(13, hotel.getLon());
                pst.setObject(14, hotel.getTel());
                pst.setObject(15, hotel.getCreateTime());
                pst.setObject(16, hotel.getUpdateTime());
                pst.setObject(17, hotel.getType());
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (SQLException e) {
            throw new SQLException("批量插入异常：" + e.toString());
        } finally {
            try {
                pst.close();
                hikariDataSource.evictConnection(conn);
            } catch (SQLException e) {
                pst = null;
                conn = null;
            }
        }
    }

    /**
     * 发送httpPost请求
     *
     * @param httpPost
     * @return 返回content内容
     * @throws IOException
     */
    public String sendPost(HttpPost httpPost) throws Exception {
        CloseableHttpClient httpClient=null;
        CloseableHttpResponse response=null;
        HttpEntity entity=null;
        try{
            SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(false).setSoLinger(1).setSoReuseAddress(true).setSoTimeout(5566).setTcpNoDelay(true).build();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5566).setConnectTimeout(5566).setConnectionRequestTimeout(5566).setProxy(proxy).build();
            httpClient  = HttpClientBuilder.create().setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(requestConfig).build();
            response = httpClient.execute(httpPost);

            entity = response.getEntity();
            String body = "";
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }

            return body;
        }catch(Exception e){
            throw new Exception(e.toString());
        }finally{
            if(entity!=null)
                EntityUtils.consume(entity);
            if(response!=null)
                response.close();
            if(httpClient!=null)
                httpClient.close();
        }

    }


    /**
     * 设置请求url,
     * 设置连接超时和socket超时
     * 设置请求行
     * 设置请求参数
     *
     * @param jsonObject
     * @param proxy
     * @return 设置好的post
     * @throws UnsupportedEncodingException
     */
    public HttpPost setPost(JSONObject jsonObject, HttpHost proxy) throws UnsupportedEncodingException {

        HttpPost httpPost = new HttpPost(jsonObject.getString("url"));

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)                 //设置socket超时时间
                .setConnectTimeout(5000)                //设置连接超时时间
                .setConnectionRequestTimeout(3000)      //设置连接请求超时时间
                .setProxy(proxy)                        //设置代理对象（为快代理池中的代理服务器）
                .build();//设置请求和传输超时时间
        httpPost.setConfig(requestConfig);

        JSONObject headMap = jsonObject.getJSONObject("headMap");
        Set<String> keySet = headMap.keySet();
        for (String key : keySet) {
            httpPost.setHeader(key, headMap.getString(key));
        }

        JSONObject paramMap = jsonObject.getJSONObject("paramMap");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        keySet = paramMap.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, paramMap.getString(key)));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

        return httpPost;
    }


}

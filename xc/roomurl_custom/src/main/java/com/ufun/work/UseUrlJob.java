package com.ufun.work;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ufun.bean.Hotel;
import com.ufun.bean.Room;
import com.ufun.util.CalendarUtils;
import com.ufun.util.HttpClientUtil;
import com.zaxxer.hikari.HikariDataSource;
import jdk.nashorn.internal.runtime.ParserException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.beetl.sql.core.SQLManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.init.ScriptException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/***
 * @author lizhenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/22 13:02
 */

public class UseUrlJob implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(UseUrlJob.class);

    private HttpClientUtil httpClientUtil = new HttpClientUtil();

    private JedisPool jedisPool;

    private SQLManager sqlManager;

    public HikariDataSource hikariDataSource;

    private HttpHost proxy;

    private Jedis jedisTool;

    private int spiderDay;

    public UseUrlJob(SQLManager sqlManager, JedisPool jedisPool,int spiderDay, HikariDataSource hikariDataSource) {
        this.jedisPool = jedisPool;
        this.sqlManager = sqlManager;
        this.spiderDay=spiderDay;
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public void run() {

        jedisTool = jedisPool.getResource();
        int count = 0;

        while (true) {
            log.info("____________开始循环____________");
            try {
                String redisName= "xc:room:"+CalendarUtils.getDateString("yyyy-MM-dd");
                List<String> urlList = jedisTool.blpop(100, redisName);//如果key为没有value，则当前线程阻塞
                if (urlList == null || urlList.size() < 1)
                    continue;
                //进入方法正常执行则每次将count清0，否则异常就递增，如果递增到5那么就可能是验证问题，就清空代理，然后获取新的代理
                if (count > 2) {
                    proxy = null;
                    count = 0;
                }
                use(urlList);
                log.info("线程执行use成功");
            } catch (JedisException e) {
                log.warn("redis操作异常："+e.toString());
                jedisTool.close();
                jedisTool = jedisPool.getResource();
            } catch (Exception e) {
                log.warn("发生异常：" + e.toString());
                e.printStackTrace();
            }finally {
                count++;
            }
            log.info("____________本次循环结束______________");
        }
    }

    /**
     * 消费url
     * @param urlList 二级缓存队列中的url
     * @throws IOException
     * @throws URISyntaxException
     */
    private void use(List<String> urlList) throws Exception {
        String redisKey = urlList.get(0);//获得redis生产内容的key
        JSONObject jsonObject = JSON.parseObject(urlList.get(1));//获得redis生产内容的value(post内容)
        getProxyHost();
        HttpGet httpGet = setHttpGet(jsonObject, proxy);
        log.info("设置代理对象");
        String hotelId = jsonObject.getJSONObject("paramMap").getString("hotel");
        String content = sendGet(httpGet);
        log.info("返回content=" + content.substring(0, 30));
        JSONObject resultJsonObject = JSON.parseObject(content);
        log.info("解析content成功");
        List<Room> roomList = parseData(resultJsonObject, hotelId);
        log.info("解析成功roomList=" + roomList.size());
        insertDB(roomList);
        log.info("#############################批量插入数据到mqsql成功##############################");
    }

    /**
     * 批量插入DB操作
     *
     * @param roomList 数据列表
     */
    private void insertDB(List<Room> roomList) throws SQLException {
        if (roomList == null)
            return;
        int[] rs = null;
        PreparedStatement pst = null;
        Connection conn = null;
        String table = "room_" + CalendarUtils.getDateString("yyyy_MM_dd");//room_yyyy-MM-dd
        String sql ="INSERT INTO `ufun`.`"+table+"`(`id`, `room_id`, `room_type`, `bed_count`, `bed_type`, `break_fask`, " +
                "`services`, `peoples`, `data_policy`, `room_price`, `residue_room`, `book_way`, `area`, `floor_level`, " +
                "`data_price`, `data_pricedisplay`, `hotel_id`, `hotel_sno`, `hotel_address`, `hotel_name`, `lat`, `lon`, " +
                "`tel`, `create_time`, `update_time`, `type`, `source`, `start_Time`, `end_Time`, `start_time_str`, " +
                "`end_time_str`, `roomId2`, `roomName`, `networkwifi`, `networklan`, `baseroominfo`,`room_total`) " +
                "VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            conn = hikariDataSource.getConnection();
            pst = conn.prepareStatement(sql);
            for (Room room:roomList) {
                pst.setObject(1, room.getRoomId());pst.setObject(2, room.getRoomType());pst.setObject(3, room.getBedCount());
                pst.setObject(4, room.getBedType());pst.setObject(5, room.getBreakfast());pst.setObject(6, room.getServices());
                pst.setObject(7, room.getPeoples());pst.setObject(8, room.getDataPolicy());pst.setObject(9, room.getRoomPrice());
                pst.setObject(10, room.getResidueRoom());pst.setObject(11, room.getBookWay()); pst.setObject(12, room.getArea());
                pst.setObject(13, room.getFloorLevel());pst.setObject(14, room.getDataPrice());pst.setObject(15, room.getDataPricedisplay());
                pst.setObject(16, room.getHotelId());pst.setObject(17, room.getHotelSno());pst.setObject(18, room.getHotelAddress());
                pst.setObject(19, room.getHotelName());pst.setObject(20, room.getLat());pst.setObject(21, room.getLon());
                pst.setObject(22, room.getTel());pst.setObject(23, room.getCreateTime());pst.setObject(24, room.getUpdateTime());
                pst.setObject(25, room.getType()); pst.setObject(26, room.getSource());pst.setObject(27, room.getStartTime());
                pst.setObject(28, room.getEndTime());pst.setObject(29, room.getStartTimeStr());pst.setObject(30, room.getEndTimeStr());
                pst.setObject(31, room.getRoomId2());pst.setObject(32,room.getRoomName());pst.setObject(33,room.getNetworkwifi());
                pst.setObject(34, room.getNetworklan());pst.setObject(35,room.getBaseroominfo());pst.setObject(36,room.getRoomTotal());

                pst.addBatch();
            }
            pst.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("批量插入异常：" + e.toString());
        } finally {
            try {
                pst.close();
                hikariDataSource.evictConnection(conn);
            } catch (SQLException e) {
                pst = null;
                conn = null;
                e.printStackTrace();
            }
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
        if (proxy == null) {
            List<String> list = jedisTool.blpop(200, "xc:proxy");
            String redisKey = list.get(0);
            String proxyStr = list.get(1);
            String[] arr = proxyStr.split(":");
            proxy = new HttpHost(arr[0], Integer.valueOf(arr[1]));
        }
    }

    /**
     * 解析数据，关联酒店
     *
     * @param jsonObject 带解析的room数据
     * @param hotelId    待关联的酒店ID
     * @return
     */
    private List<Room> parseData(JSONObject jsonObject, String hotelId) {
        String html = jsonObject.getString("html");
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        Elements timeElement=body.getElementsByClass("btns_base22 J_hotel_order");
        /**获取当前数据的时间参数*/
        String str=timeElement.get(0).attr("bookparam");
        JSONObject json=JSON.parseObject(str);
        String startTime=json.getString("StartDate");
        String endTime=json.getString("DepDate");

        /*遍历每个房间信息*/
        Elements elements = body.getElementsByClass("child_name J_Col_RoomName");
        List<Room> roomList = new ArrayList<Room>();
        for (Element element : elements) {
            Room room = new Room();
            room.setDataPrice(element.attr("data-price"));
            room.setDataPricedisplay(element.attr("data-pricedisplay"));
            room.setPolicy(element.attr("data-policy"));
            room.setRoomType(element.attr("data-baseroomname"));
            room.setBedCount(element.attr("data-bed"));
            room.setHotelSno(hotelId);
            room.setStartTimeStr(startTime);
            room.setEndTimeStr(endTime);
            room.setRoomId(element.attr("data-roomid"));
            room.setPeoples(room.getBedCount()+"人");
            room.setNetworklan(element.attr("data-networklan"));
            room.setNetworkwifi(element.attr("data-networkwifi"));
            room.setSource("友房科技");
            room.setType("携程网");
            room.setBaseroominfo(element.attr("data-baseroominfo"));
            String infoStr=room.getBaseroominfo();
            JSONObject info=JSON.parseObject(infoStr);
            if(info!=null&&!"".equals(info)){
                String baseInfo=info.getString("BaseRoomInfo");
                if(!"".equals(baseInfo)&&baseInfo!=null){
                    room.setBaseroominfo(baseInfo);
                    String[] arr=baseInfo.split("<span class=\"line\">|</span>");
                    StringBuilder sb=new StringBuilder();
                    for (String s : arr) {
                        sb.append(s);
                        if(s.contains("平方米"))
                            room.setArea(s);
                        if(s.contains("层"));
                            room.setFloorLevel(s);
                        if(s.contains("床")&&s.contains("张"))
                            room.setBedType(s);
                        room.setPeoples(arr[arr.length-1]);
                    }
                    room.setBaseroominfo(sb.toString());
                }
                room.setRoomName(info.getString("RoomName"));
                room.setRoomId2(info.getString("RoomID"));
                room.setRoomTotal(info.getString("RoomTotalNum"));
            }
            roomList.add(room);
        }
        return roomList;
    }


    /**
     * 设置httpGet请求
     *
     * @param jsonObject
     * @param proxy
     * @return
     * @throws URISyntaxException
     */
    private HttpGet setHttpGet(JSONObject jsonObject, HttpHost proxy) throws URISyntaxException {

        StringBuilder sb = new StringBuilder();                           /** 设置url字符串 */
        String url = jsonObject.getString("url");                     /** 设置url字符串 */
        sb.append(url + "?kc_=xxt");                                      /** 设置url字符串 */

        JSONObject paramMap = jsonObject.getJSONObject("paramMap");     /** 设置请求参数 */
        Set<String> keyParamSet = paramMap.keySet();                      /** 设置请求参数 */
        Iterator<String> it = keyParamSet.iterator();                   /** 设置请求参数 */
        while (it.hasNext()) {                                            /** 设置请求参数 */
            sb.append("&");                                             /** 设置请求参数 */
            String key = it.next();                                       /** 设置请求参数 */
            sb.append(key + "=" + paramMap.getString(key));                 /** 设置请求参数 */
        }                                                               /** 设置请求参数 */

        URI uri = new URI(sb.toString());                                 /**设置请求url*/
        HttpGet httpGet = new HttpGet(uri);                               /**设置请求url*/

        JSONObject headMap = jsonObject.getJSONObject("headMap");       /** 设置请求头 */
        Set<String> keyHeadSet = headMap.keySet();                      /** 设置请求头 */
        for (String key : keyHeadSet) {                                 /** 设置请求头 */
            httpGet.setHeader(key, headMap.getString(key));             /** 设置请求头 */
        }                                                               /** 设置请求头 */
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(3000).setProxy(proxy).build();//设置请求和传输超时时间
        httpGet.setConfig(requestConfig);
        return httpGet;
    }


    /**
     * 发送httpGet请求
     *
     * @param httpGet
     * @return
     * @throws IOException
     */
    private String sendGet(HttpGet httpGet) throws Exception {
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        CloseableHttpClient httpClient = null;
        try {
            SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(false).setSoLinger(1).setSoReuseAddress(true).setSoTimeout(4444).setTcpNoDelay(true).build();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(4444).setConnectTimeout(4444).setConnectionRequestTimeout(4444).setProxy(proxy).build();//设置请求和传输超时时间
            httpGet.setConfig(requestConfig);

            httpClient = HttpClientBuilder.create().setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(requestConfig).build();
            log.info("初始化一个httpClient");
            response = httpClient.execute(httpGet);
            log.info("发送httpGet成功，返回response");
            entity = response.getEntity();
            log.info("从response中获得entity");
            String content = EntityUtils.toString(entity);
            return content;
        } catch (Exception e) {
            throw new Exception("发送httpGet异常" + e.toString());
        } finally {
            try {
                if (entity != null)
                    EntityUtils.consume(entity);
                if (response != null)
                    response.close();
                if (httpClient != null)
                    httpClient.close();
                log.info("**释放请求资源成功**");
            } catch (IOException ioe) {
                log.error("!!释放请求资源发生了异常!!" + ioe);
                log.error("!!!暴力释放资源，交给垃圾回收器!!!");
                entity = null;
                response = null;
                httpClient = null;
                throw new Exception("释放请求资源发生了异常：" + ioe.toString());
            }
            log.warn(Thread.currentThread().getName() + "释放资源成功");
        }
    }

    /**
     * getter and setter
     */
    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public void setSqlManager(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    public int getSpiderDay() {
        return spiderDay;
    }

    public void setSpiderDay(int spiderDay) {
        this.spiderDay = spiderDay;
    }
}

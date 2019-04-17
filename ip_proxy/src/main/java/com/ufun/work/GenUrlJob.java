package com.ufun.work;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ufun.bean.ErrorCode;
import com.ufun.bean.Request;
import com.ufun.util.HttpClientUtil;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.*;

/***
 * @author lizhneghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/23 15:44
 */
@Component
public class GenUrlJob extends QuartzJobBean {

    private final static Logger log = LoggerFactory.getLogger(GenUrlJob.class);

    @Autowired
    private Request request;//在ProxyConfig配置类中返回实例

    @Autowired
    private JedisPool jedisPool;

    private Jedis jedis;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("----------------==========定时任务执行=============----------------");
        jedis = jedisPool.getResource();
        log.info("定时采集IP任务执行，获取jedis客户端工具");
        jedis.del("xc:proxy");//每次执行都将之前的旧数据删除，因为快代理官方要求指定时间内务必消耗完代理，否则会失效
        log.info("开始获取新的IP代理---------->xc:proxy");

        try {
            String url = genUrl(request);//从配置文件中的指定参数生成url
            log.info("从配置中获取快代理url：" + url);
            String content = sendUrl(url); //发送url
            log.info("发送url成功返回！");
            JSONObject jsonObject = JSON.parseObject(content);
            validate(jsonObject);//验证快代理返回结果
            List<String> list = parseJSON(jsonObject);
            log.info("返回list：" + list);
            enRedisQueue(list);
            log.info("代理ip存入redis队列");
        } catch (Exception e) {
            log.warn("采集ip异常:"+e.toString());
            e.printStackTrace();
            return;
        }finally {
            jedis.close();
            jedis=null;
        }
        log.info("----------------==========定时任务结束=============----------------");
    }

    /**
     * 存入redis
     * @param list
     */
    private void enRedisQueue(List<String> list) {
        /*为了保证每个ip都充分利用，将他们循环存储N次*/
        for(int i=0;i<1000;i++){
            for (String s : list) {
                jedis.rpush("xc:proxy", s);
            }
        }
    }

    /**
     * 解析返回内容
     *
     * @param jsonObject
     * @return
     */
    private List<String> parseJSON(JSONObject jsonObject) {
        JSONObject dataObject = jsonObject.getJSONObject("data");
        JSONArray proxyArr = dataObject.getJSONArray("proxy_list");
        List<String> list = new ArrayList<>();
        Iterator it = proxyArr.iterator();
        while (it.hasNext()) {
            list.add(it.next().toString());
        }
        return list;
    }

    /**
     * 验证信息
     *
     * @param jsonObject
     * @throws Exception
     */
    private void validate(JSONObject jsonObject) throws Exception {
        int code = jsonObject.getInteger("code");
        if (code != 0) {
            String error = ErrorCode.getContent(code);
            throw new Exception(error);
        }
    }

    /**
     * 从request对象生成url
     *
     * @param request
     * @return
     */
    private String genUrl(Request request) {

        StringBuilder sb = new StringBuilder();
        sb.append(request.getUrl()).append("?");
        if (request.getOrderId() != null && !"".equals(request.getOrderId())) {
            sb.append("orderid=" + request.getOrderId());
            sb.append("&");
        }
        if (request.getSignType() != null && !"".equals(request.getSignType())) {
            sb.append("sign_type=" + request.getSignType());
            sb.append("&");
        }
        if (request.getSignature() != null && !"".equals(request.getSignature())) {
            sb.append("signature=" + request.getSignature());
            sb.append("&");
        }
        if (request.getNum() != null && !"".equals(request.getNum())) {
            sb.append("num=" + request.getNum());
            sb.append("&");
        }
        if (request.getFormat() != null && !"".equals(request.getFormat())) {
            sb.append("format=" + request.getFormat());
            sb.append("&");
        }
        if (request.getUt() != null && !"".equals(request.getUt())) {
            sb.append("ut=" + request.getUt());
            sb.append("&");
        }
        if(request.getArea()!=null&&!"".equals(request.getArea())){
            sb.append("area=" + request.getArea());
            sb.append("&");
        }
        sb.append("sep=1");
        return sb.toString();
    }

    /**
     * 发送URL
     * @param url
     * @return
     * @throws IOException
     */
    private String sendUrl(String url) throws Exception {
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        CloseableHttpClient httpClient=null;
        try {
            HttpClientUtil httpClientUtil = HttpClientUtil.getInstance();
            HttpGet httpGet = httpClientUtil.getHttpGet(url);
            httpClient = httpClientUtil.getClient();
            response = httpClient.execute(httpGet);
            entity = response.getEntity();
            String content = EntityUtils.toString(entity, "UTF-8");
            return content;
        } catch (Exception e) {
            throw new Exception(e.toString());
        } finally {
            if(entity!=null)
                EntityUtils.consume(entity);
            if(response!=null)
                response.close();
            if(httpClient!=null)
                httpClient.close();
            log.info("___________释放资源成功_____________");
        }
    }
}

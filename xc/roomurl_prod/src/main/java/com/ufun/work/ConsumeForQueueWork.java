package com.ufun.work;

import com.alibaba.fastjson.JSON;
import com.ufun.bean.Hotel;
import com.ufun.bean.UrlJsonObject;
import com.ufun.util.CalendarUtils;
import com.ufun.util.HttpClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;


import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/***
 * @author lizhneghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/25 11:18
 */
public class ConsumeForQueueWork implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ConsumeForQueueWork.class);

    private BlockingQueue<Hotel> queue;

    private JedisPool jedisPool;

    private Jedis jedis;

    private HttpHost proxy;

    private boolean flag;

    private int spiderDay;

    /**
     * 多线程封闭执行代码，共享queue和redis连接池
     */
    @Override
    public void run() {
        jedis = jedisPool.getResource();
        int count=0;
        while (!flag) {
            log.info("_________________开始循环_____________________");
            try {
                Hotel hotel = queue.take();
                log.info("从queue中拿一个酒店:"+hotel.getSno());
                if (!hotel.getFlag()) {//如果线程读取到无效的hotel那么就跳出循环，任务结束
                    flag = true;
                    log.info("!!!!!!!.........缓存队列消耗空break,跳出循环线程终止........!!!!!!!!!");
                    break;
                }
                if(count>2){        //如果异常计数大于3则重新获取proxy
                    proxy=null;
                    count=0;
                }
                genUrl(hotel);
                log.info("******生成url成功*********");
            } catch (JedisException e) {
                log.warn("redis操作异常："+e.toString());
                jedis.close();
                jedis = jedisPool.getResource();
            } catch (Exception e) {
                log.warn("发生异常:" + e.toString());
                e.printStackTrace();
            }finally {
                count++;
            }
        }
        jedis.close();
        log.info("==========================死亡==========================");
    }

    private void genUrl(Hotel hotel) throws Exception {
        //可以排个序,从最后的日期开始，
        String[] arr = CalendarUtils.getCallendarString(spiderDay, "yyyy-MM-dd");
        String redisName="xc:room:"+arr[0];//以当天的日期作为redis的表名,xc:room:yyyy-MM-dd

        //TODO 后期试验一下用一个eleven去生成若天天房间详情url，看看能不能访问到数据
        for (int i = arr.length - 1; i >0 ; i--) {
            getProxyHost();
            String eleven = getElevenParam(hotel);
            log.info("获取eleven成功：" + eleven);
            UrlJsonObject urlJsonObject = encap(hotel, eleven,  arr[i-1],arr[i]);//封住指定开始时间和结束时间的url
            log.info("封装url成功---[酒店："+hotel.getSno()+",开始时间："+arr[0]+",结束时间："+arr[1]+"]");
            enRedisQueue(urlJsonObject, redisName);//以开始时间为redis缓存名称
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
            List<String> list = jedis.blpop(500, "xc:proxy");
            //String redisKey = list.get(0);
            String proxyStr = list.get(1);
            String[] arr = proxyStr.split(":");
            proxy = new HttpHost(arr[0], Integer.valueOf(arr[1]));
        }
    }

    /**
     * 将url对象加入redis队列
     *
     * @param urlJsonObject
     */
    private void enRedisQueue(UrlJsonObject urlJsonObject, String redisName) {
        String str = JSON.toJSONString(urlJsonObject);
        jedis.rpush(redisName, str);
    }

    private UrlJsonObject encap(Hotel hotel, String eleven, String start, String end) {

        UrlJsonObject urlJsonObject = new UrlJsonObject();
        Map<String, String> paramMap = urlJsonObject.getParamMap();
        Map<String, String> headerMap = urlJsonObject.getHeadMap();

        urlJsonObject.setUrl("http://hotels.ctrip.com/Domestic/tool/AjaxHote1RoomListForDetai1.aspx");

        headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        headerMap.put("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
        headerMap.put("If-Modified-Since", "Thu, 01 Jan 1970 00:00:00 GMT");
        headerMap.put("Referer", "http://hotels.ctrip.com/hotel/" + hotel.getSno() + ".html?isFull=F");
        headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");

        paramMap.put("MasterHotelID", hotel.getSno());
        paramMap.put("hotel", hotel.getSno());
        paramMap.put("city", "28");
        paramMap.put("showspothotel", "T");
        paramMap.put("IsDecoupleSpotHotelAndGroup", "F");
        paramMap.put("startDate", start);
        log.info("设置开始时间:"+start);
        paramMap.put("depDate", end);
        log.info("设置结束时间"+end);
        paramMap.put("IsFlash", "F");
        paramMap.put("RequestTravelMoney", "F");
        paramMap.put("contyped", "0");
        paramMap.put("priceInfo", "-1");
        paramMap.put("defaultLoad", "T");
        paramMap.put("Currency", "RMB");
        paramMap.put("Exchange", "1");
        paramMap.put("TmFromList", "F");
        paramMap.put("RoomGuestCount", "1,1,0");
        paramMap.put("eleven", eleven);
        paramMap.put("callback", getcallback(15));
        paramMap.put("_", System.currentTimeMillis() + "");

        return urlJsonObject;
    }


    //此处规则可能随时会变，不做任何代码优化，
    private String getElevenParam(Hotel hotel) throws Exception {
        HttpEntity entity = null;
        String ocean = "";
        String eleven = "";
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            String callBack = getcallback(15);
            long timeStrap = System.currentTimeMillis();
            HttpClientUtil httpClientUtil = HttpClientUtil.getInstance();
            HttpGet httpGet = httpClientUtil.getHttpGet("http://hotels.ctrip.com/domestic/cas/oceanball?callback=" + callBack + "&_=" + timeStrap);
            httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");//www接口
            httpGet.setHeader("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
            httpGet.setHeader("If-Modified-Since", "Thu, 01 Jan 1970 00:00:00 GMT");
            httpGet.setHeader("Referer", "http://hotels.ctrip.com/hotel/" + hotel.getSno() + ".html?isFull=F");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");

            SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(false).setSoLinger(1).setSoReuseAddress(true).setSoTimeout(4444).setTcpNoDelay(true).build();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(4444).setConnectTimeout(4444).setConnectionRequestTimeout(4444).setProxy(proxy).build();//设置请求和传输超时时间
            httpGet.setConfig(requestConfig);

            httpClient = HttpClientBuilder.create().setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(requestConfig).build();

            log.info("拿到一个httpClient");
            response = httpClient.execute(httpGet);
            log.info("发送请求成功，返回response");

            entity = response.getEntity();
            if (entity != null) {
                //返回的是js格式的eval包含的代码,js引擎会计算其中的代码，如果是一个函数则返回函数对象（就是这个函数的样子），如果函数.()立即调用则返回调用后的结果
                ocean = EntityUtils.toString(entity, "UTF-8");
            }
            //将eval改为JSON.stringify，也就是说JS引擎会将JSON.stringify中的对象转换为json数据,调试js看到返回的就是一个字符串的代码
            ocean = ocean.replace("eval", "JSON.stringify");

            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("javascript");
            log.info("初始化一个javascript引擎engine");
            ocean = String.valueOf(engine.eval(ocean));
            ocean = ocean.replace(callBack, "var eleven=" + callBack);
            ocean = String.valueOf(engine.eval(new StringReader(ocean)));
            ScriptEngineManager manager1 = new ScriptEngineManager();
            ScriptEngine engine1 = manager1.getEngineByName("javascript");
            log.info("再创建一个js引擎engine1");
            engine1.eval(
                    "var hotel_id = \"" + hotel.getSno() + "\"; " +
                            "var site = {}; " + "site.getUserAgent = function(){}; " + "var Image = function(){}; " + "var window = {}; " +
                            "window.document = window.document = {body:{innerHTML:\"1\"}, documentElement:{attributes:{webdriver:\"1\"}}, createElement:function(x){return {innerHTML:\"1\"}}}; " +
                            "var document = window.document;" + "window.navigator = {\"appCodeName\":\"Mozilla\", \"appName\":\"Netscape\", \"language\":\"zh-CN\", \"platform\":\"Win\"}; " +
                            "window.navigator.userAgent = site.getUserAgent(); " + "var navigator = window.navigator; " + "window.location = {}; " +
                            "window.location.href = \"http://hotels.ctrip.com/hotel/\"+hotel_id+\".html\"; " + "var location = window.location;" +
                            "var navigator = {userAgent:{indexOf: function(x){return \"1\"}}, geolocation:\"1\"};" + "var getEleven = 'zgs';"
            );
            engine1.eval("var " + callBack + " = function(a){getEleven = a;};");
            engine1.eval(ocean);
            log.info("engine1执行ocean代码");
            if (engine instanceof Invocable) {
                Invocable invocable = (Invocable) engine1;
                eleven = (String) invocable.invokeFunction("getEleven");//4.使用 invocable.invokeFunction掉用js脚本里的方法，第一個参数为方法名，后面的参数为被调用的js方法的入参
            }

            if (eleven.contains("<"))
                eleven.replace("<", "");
            if (eleven.contains(">"))
                eleven.replace(">", "");
            return eleven;
        } catch (Exception e) {
            throw new Exception("获取eleven方法异常：" + e.toString());
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
                log.error("!!释放请求资源发生了异常!!");
                log.error("!!!暴力释放资源，交给垃圾回收器!!!");
                entity = null;
                response = null;
                httpClient = null;
            }
        }
    }


    /**
     * callback参数获取
     *
     * @param number 一般默认写15即可
     * @return
     */
    public static String getcallback(int number) {
        String s[] = {
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
                "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
        };
        String cal = "CAS";
        for (int i = 0; i < number; i++) {
            int t = (int) Math.ceil(51 * Math.random());
            cal = cal + s[t];
        }
        return cal;
    }

    public BlockingQueue<Hotel> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Hotel> queue) {
        this.queue = queue;
    }


    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public HttpHost getProxy() {
        return proxy;
    }

    public void setProxy(HttpHost proxy) {
        this.proxy = proxy;
    }

    public int getSpiderDay() {
        return spiderDay;
    }

    public void setSpiderDay(int spiderDay) {
        this.spiderDay = spiderDay;
    }
}

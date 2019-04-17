package com.ufun.util;


import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

/***
 * @author 李正杭
 * @mail 1475546247@qq.com
 * @TIME 2019/3/12 9:43
 */
@Component
public class HttpClientUtil {

    private  CloseableHttpClient client=HttpClients.createDefault();
    private HttpGet httpGet=new HttpGet();

    public static HttpClientUtil getInstance(){
        return new HttpClientUtil();
    }

    /**
     * 返回一个httpPost
     * @param url
     * @return
     */
    public HttpPost getHttpPost(String url){
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);
        return httpPost;
    }

    /**
     * 返回一个httpGet
     * @param url
     * @return
     */
    public HttpGet getHttpGet(String url){
        return new HttpGet(url);
        //return httpGet;
    }

    /**
     * @return 返回httpclient
     */
    public CloseableHttpClient getClient() {
        //return client;
        return client=HttpClients.createDefault();
    }


}

package com.ufun.config;

import com.ufun.bean.Request;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/28 16:20
 */
@Configuration
@ConfigurationProperties("proxy")
public class ProxyConfig {

    private String url;
    private String get;
    private String encode;
    private String orderId;
    private String signType;
    private String signature;
    private String num;
    private String format;
    private String ut;
    private String area;

    @Bean("request")
    public Request getRequest(){
        Request request=new Request();
        request.setUrl(url);
        request.setGet(get);
        request.setEncode(encode);
        request.setOrderId(orderId);
        request.setSignType(signType);
        request.setSignature(signature);
        request.setNum(num);
        request.setFormat(format);
        request.setUt(ut);
        request.setArea(area);
        return request;
    }

    public String getUt() {
        return ut;
    }

    public void setUt(String ut) {
        this.ut = ut;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGet() {
        return get;
    }

    public void setGet(String get) {
        this.get = get;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}

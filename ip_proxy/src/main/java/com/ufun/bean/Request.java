package com.ufun.bean;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/28 16:14
 */
public class Request {

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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getUt() {
        return ut;
    }

    public void setUt(String ut) {
        this.ut = ut;
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

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

package com.ufun.bean;

import java.util.List;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/28 15:55
 */
public class IPProxyResult {

    private String msg;
    private Integer code;
    private Integer count;
    private Integer dedupCount;
    private List<String> proxyList;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getDedupCount() {
        return dedupCount;
    }

    public void setDedupCount(Integer dedupCount) {
        this.dedupCount = dedupCount;
    }

    public List<String> getProxyList() {
        return proxyList;
    }

    public void setProxyList(List<String> proxyList) {
        this.proxyList = proxyList;
    }
}

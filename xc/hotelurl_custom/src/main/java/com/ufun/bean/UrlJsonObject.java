package com.ufun.bean;

import java.util.HashMap;
import java.util.Map;

/***
 * @author lizenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/22 13:02
 */
public class UrlJsonObject {

    private String url;

    private Map<String,String> paramMap=new HashMap<String,String>();

    private Map<String,String> headMap=new HashMap<String,String>();


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    public Map<String, String> getHeadMap() {
        return headMap;
    }

    public void setHeadMap(Map<String, String> headMap) {
        this.headMap = headMap;
    }
}
//
package com.ufun.bean;

import org.beetl.sql.core.annotatoin.Table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/12 16:00
 */
@Table(name="hotel")
public class Hotel {

    private Integer id;             //id
    private String sno;         //酒店编号
    private String address;     //酒店地址
    private String name;        //酒店名称
    private String url;         //详情页url
    private String score;       //用户评分
    private String star;        //星级
    private String shortName;   //短名称
    private String starDesc;    //型号（如舒适型 高档型等）
    private String dpCount;     //参与点赞的用户
    private String dpScore;     //好评率
    private Integer isSingleRec;//
    private String lat;         //纬度
    private String lon;         //经度
    private String tel;         //酒店电话+
    private String type;        //类型（携程，途家）
    private Date createTime;    //创建时间
    private Date updateTime;    //更新时间
    private List<Room> list = new ArrayList<Room>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getStar() {
        return star;
    }

    public void setStar(String star) {
        this.star = star;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getStarDesc() {
        return starDesc;
    }

    public void setStarDesc(String starDesc) {
        this.starDesc = starDesc;
    }

    public String getDpCount() {
        return dpCount;
    }

    public void setDpCount(String dpCount) {
        this.dpCount = dpCount;
    }

    public String getDpScore() {
        return dpScore;
    }

    public void setDpScore(String dpScore) {
        this.dpScore = dpScore;
    }

    public Integer getIsSingleRec() {
        return isSingleRec;
    }

    public void setIsSingleRec(Integer isSingleRec) {
        this.isSingleRec = isSingleRec;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<Room> getList() {
        return list;
    }

    public void setList(List<Room> list) {
        this.list = list;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "id=" + id +
                ", sno='" + sno + '\'' +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", score='" + score + '\'' +
                ", star='" + star + '\'' +
                ", shortName='" + shortName + '\'' +
                ", starDesc='" + starDesc + '\'' +
                ", dpCount='" + dpCount + '\'' +
                ", dpScore='" + dpScore + '\'' +
                ", isSingleRec=" + isSingleRec +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", tel='" + tel + '\'' +
                ", type='" + type + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", list=" + list +
                '}';
    }
}

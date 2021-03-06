package com.ufun.bean;

import org.beetl.sql.core.annotatoin.Table;

import java.util.Arrays;
import java.util.Date;

/***
 * @author lizhenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/16 17:00
 */
@Table(name="hotel_room")
public class Room {

    private String id;
    private String roomId;
    private String roomType;            //房型
    private String bedType;             //床型
    private String bedCount;           //床数量
    private String breakfast;           //早餐
    private String[] services;            //设施服务
    private Integer peoples;             //入住人数
    private String dataPolicy;         //促销优惠减100的策略都是3，貌似3就代表=100元
    private String roomPrice;           //真实价格，即dataPrice减去政策优惠后的价格
    private Integer residueRoom;         //剩余房间
    private String bookWay;             //预订方式
    private double area;                //房间面积
    private String floorLevel;          //楼层
    private String isWindow;            //是否有窗户
    private String dataPrice;           //真实价格，用横线划掉的价格
    private String dataPricedisplay;    //
    private Integer hotelId;             //所属id
    private String hotelSno;         //所属酒店编号
    private String hotelAddress;     //所属酒店地址
    private String hotelName;        //所属酒店名称
    private String lat;
    private String lon;
    private Date createTime;
    private Date updateTime;
    private String type;
    private String source;

    public String getBedCount() {
        return bedCount;
    }

    public void setBedCount(String bedCount) {
        this.bedCount = bedCount;
    }

    public String getDataPolicy() {
        return dataPolicy;
    }

    public void setDataPolicy(String dataPolicy) {
        this.dataPolicy = dataPolicy;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getDataPrice() {
        return dataPrice;
    }

    public void setDataPrice(String dataPrice) {
        this.dataPrice = dataPrice;
    }

    public String getDataPricedisplay() {
        return dataPricedisplay;
    }

    public void setDataPricedisplay(String dataPricedisplay) {
        this.dataPricedisplay = dataPricedisplay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String rootType) {
        this.roomType = rootType;
    }

    public String getBedType() {
        return bedType;
    }

    public void setBedType(String bedType) {
        this.bedType = bedType;
    }

    public String getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(String breakfast) {
        this.breakfast = breakfast;
    }

    public String[] getServices() {
        return services;
    }

    public void setServices(String[] services) {
        this.services = services;
    }

    public Integer getPeoples() {
        return peoples;
    }

    public void setPeoples(Integer peoples) {
        this.peoples = peoples;
    }

    public String getPolicy() {
        return dataPolicy;
    }

    public void setPolicy(String policy) {
        this.dataPolicy = policy;
    }

    public String getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(String roomPrice) {
        this.roomPrice = roomPrice;
    }

    public Integer getResidueRoom() {
        return residueRoom;
    }

    public void setResidueRoom(Integer residueRoom) {
        this.residueRoom = residueRoom;
    }

    public String getBookWay() {
        return bookWay;
    }

    public void setBookWay(String bookWay) {
        this.bookWay = bookWay;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public String getFloorLevel() {
        return floorLevel;
    }

    public void setFloorLevel(String floorLevel) {
        this.floorLevel = floorLevel;
    }

    public String getIsWindow() {
        return isWindow;
    }

    public void setIsWindow(String isWindow) {
        this.isWindow = isWindow;
    }

    public Integer getHotelId() {
        return hotelId;
    }

    public void setHotelId(Integer hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelSno() {
        return hotelSno;
    }

    public void setHotelSno(String hotelSno) {
        this.hotelSno = hotelSno;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", roomId='" + roomId + '\'' +
                ", roomType='" + roomType + '\'' +
                ", bedType='" + bedType + '\'' +
                ", bedCount='" + bedCount + '\'' +
                ", breakfast='" + breakfast + '\'' +
                ", services=" + Arrays.toString(services) +
                ", peoples=" + peoples +
                ", dataPolicy='" + dataPolicy + '\'' +
                ", roomPrice='" + roomPrice + '\'' +
                ", residueRoom=" + residueRoom +
                ", bookWay='" + bookWay + '\'' +
                ", area=" + area +
                ", floorLevel='" + floorLevel + '\'' +
                ", isWindow='" + isWindow + '\'' +
                ", dataPrice='" + dataPrice + '\'' +
                ", dataPricedisplay='" + dataPricedisplay + '\'' +
                ", hotelId=" + hotelId +
                ", hotelSno='" + hotelSno + '\'' +
                ", hotelAddress='" + hotelAddress + '\'' +
                ", hotelName='" + hotelName + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", type='" + type + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}

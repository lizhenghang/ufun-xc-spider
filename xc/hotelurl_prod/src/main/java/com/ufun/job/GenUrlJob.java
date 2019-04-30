package com.ufun.job;

import com.alibaba.fastjson.JSON;
import com.ufun.bean.UrlJsonObject;
import com.ufun.util.CalendarUtils;

import com.zaxxer.hikari.HikariDataSource;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/***
 * @author lizhenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/22 10:17
 */
public class GenUrlJob extends QuartzJobBean {

    private final static Logger log = LoggerFactory.getLogger(GenUrlJob.class);

    @Autowired
    private JedisPool jedisPool;
    /*
         beetlSQL框架达不到后期需求，暂时废弃。但是还是建议在MVC模块中使用它，MCV中性能是仅次于JDBC的，而且完全可以替代mybatis
        @Autowired
        private SQLManager sqlManager;
    */
    @Autowired
    private HikariDataSource hikariDataSource;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("-------------===========定时任务开始============-------------");
        int page=1;
        int count=555;
        Jedis jedis=jedisPool.getResource();
        jedis.del("xc:hotel");//每次执行都删除陈旧的数据
        createCurrTable();//创建当天的hotel表
        createTommTable();
        while(page<count){
            try{
                log.info("生产第"+page+"页URL");
                UrlJsonObject urlJsonObject=new UrlJsonObject();
                Map<String,String> paramMap=urlJsonObject.getParamMap();
                Map<String,String> headMap=urlJsonObject.getHeadMap();
                urlJsonObject.setUrl("https://hotels.ctrip.com/Domestic/Tool/AjaxHotelList.aspx");
                setHeader(headMap);
                log.info("设置请求头到urlJsonObject对象");
                setParam(paramMap,page);
                log.info("设置参数到urlJsonObject对象");
                page++;
                String str = JSON.toJSONString(urlJsonObject);
                jedis.rpush("xc:hotel",str);
                log.info("保存json字符串到redis");
            }catch (Exception e){
                e.printStackTrace();
                log.warn("定时任务发生异常信息,跳出本次循环继续下一次");
                log.warn(e.getMessage());
                continue;
            }
        }
        jedis.close();
        log.info("while循环结束，关闭jedis客户端");
        log.info("-------------===========定时任务结束============-------------");
    }

    private void createTommTable(){
        Connection conn=null;
        PreparedStatement pst=null;
        try {
        String table="hotel_"+ CalendarUtils.getTomorrowDateString("yyyy_MM_dd");//hotel_yyyy-MM--dd
        String sql="CREATE TABLE IF NOT EXISTS `"+table+"` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',\n" +
                "  `sno` varchar(80) DEFAULT NULL COMMENT '酒店编号',\n" +
                "  `address` varchar(255) DEFAULT NULL COMMENT '酒店地址',\n" +
                "  `name` varchar(100) DEFAULT NULL COMMENT '酒店名称',\n" +
                "  `url` varchar(155) DEFAULT NULL COMMENT '酒店链接',\n" +
                "  `score` varchar(20) DEFAULT NULL COMMENT '酒店评分',\n" +
                "  `star` varchar(50) DEFAULT NULL COMMENT '酒店星级',\n" +
                "  `short_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '酒店简称',\n" +
                "  `star_desc` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '酒店型号（舒适型，高档型）',\n" +
                "  `dp_count` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '参与点赞用户',\n" +
                "  `dp_score` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '好评率',\n" +
                "  `is_single_rec` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,\n" +
                "  `lat` varchar(100) DEFAULT NULL COMMENT '纬度',\n" +
                "  `lon` varchar(100) DEFAULT NULL COMMENT '经度',\n" +
                "  `tel` varchar(50) DEFAULT NULL COMMENT '电话',\n" +
                "  `create_time` date DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_time` date DEFAULT NULL COMMENT '更新时间',\n" +
                "  `type` varchar(20) DEFAULT NULL COMMENT '类型',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='酒店信息表';";
            conn=hikariDataSource.getConnection();
            pst=conn.prepareStatement(sql);
            int result=pst.executeUpdate();
            log.info("创建表语句完成：result="+result);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                pst.close();
                hikariDataSource.evictConnection(conn);
            } catch (SQLException e) {
                pst=null;
                conn=null;
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建当日的表
     * 每天定时任务执行即创建当日的表hotel_yyyy-MM--dd
     */
    private void createCurrTable() {
        String table="hotel_"+ CalendarUtils.getDateString("yyyy_MM_dd");//hotel_yyyy-MM--dd
        String sql="CREATE TABLE IF NOT EXISTS `"+table+"` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',\n" +
                "  `sno` varchar(80) DEFAULT NULL COMMENT '酒店编号',\n" +
                "  `address` varchar(255) DEFAULT NULL COMMENT '酒店地址',\n" +
                "  `name` varchar(100) DEFAULT NULL COMMENT '酒店名称',\n" +
                "  `url` varchar(155) DEFAULT NULL COMMENT '酒店链接',\n" +
                "  `score` varchar(20) DEFAULT NULL COMMENT '酒店评分',\n" +
                "  `star` varchar(50) DEFAULT NULL COMMENT '酒店星级',\n" +
                "  `short_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '酒店简称',\n" +
                "  `star_desc` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '酒店型号（舒适型，高档型）',\n" +
                "  `dp_count` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '参与点赞用户',\n" +
                "  `dp_score` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '好评率',\n" +
                "  `is_single_rec` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,\n" +
                "  `lat` varchar(100) DEFAULT NULL COMMENT '纬度',\n" +
                "  `lon` varchar(100) DEFAULT NULL COMMENT '经度',\n" +
                "  `tel` varchar(50) DEFAULT NULL COMMENT '电话',\n" +
                "  `create_time` date DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_time` date DEFAULT NULL COMMENT '更新时间',\n" +
                "  `type` varchar(20) DEFAULT NULL COMMENT '类型',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='酒店信息表';";
        Connection connection=null;
        PreparedStatement ps=null;
        try {
            connection=hikariDataSource.getConnection();
            ps=connection.prepareStatement(sql);
            int result=ps.executeUpdate();
            log.info("创建表语句完成：result="+result);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if(ps!=null)
                    ps.close();
                if(connection!=null)
                    hikariDataSource.evictConnection(connection);
            } catch (SQLException e) {
                ps=null;
                connection=null;
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置参数
     * @param paramMap
     * @param page
     */
    private void setParam(Map<String, String> paramMap,int page) {
        String[] arr= CalendarUtils.getCallendarString(1,"yyyy-MM-dd");
        paramMap.put("__VIEWSTATEGENERATOR","DB1FBB6D");
        paramMap.put("cityName","%E6%88%90%E9%83%BD");
        paramMap.put("RoomGuestCount","1,1,0");
        paramMap.put("operationtype","NEWHOTELORDER");
        paramMap.put("IsOnlyAirHotel","F");
        paramMap.put("cityLat","30.663491162");
        paramMap.put("cityLng","104.0723267245");
        paramMap.put("ubt_price_key","htl_search_result_promotion");
        paramMap.put("page",""+page);
        paramMap.put("StartTime", arr[0]);
        paramMap.put("DepTime",arr[1]);
        paramMap.put("cityId","28");
        paramMap.put("cityPY","chengdu");
        paramMap.put("cityCode","028");
    }

    /**
     * 设置请求头
     * @param headMap
     */
    private void setHeader(Map<String, String> headMap) {

        headMap.put("accept","*/*");
        headMap.put("accept-encoding","gzip, deflate, br");
        headMap.put("accept-language","zh-CN,zh;q=0.9,en;q=0.8");
        headMap.put("cache-control","max-age=0");
        headMap.put("content-type","application/x-www-form-urlencoded; charset=UTF-8");
        headMap.put("if-modified-since","Thu, 01 Jan 1970 00:00:00 GMT");
        headMap.put("origin","https://hotels.ctrip.com");
        headMap.put("referer","https://hotels.ctrip.com/domestic/hotel/chengdu28");
        headMap.put("Content-type","application/x-www-form-urlencoded");
        headMap.put("user-agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.109 Safari/537.36");
    }
}

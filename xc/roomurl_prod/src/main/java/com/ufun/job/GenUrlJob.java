package com.ufun.job;


import com.sun.management.OperatingSystemMXBean;
import com.ufun.bean.ApplicationPropertis;
import com.ufun.bean.Hotel;
import com.ufun.util.CalendarUtils;
import com.ufun.util.SpringApplicationContextUtil;
import com.ufun.work.ConsumeForQueueWork;
import com.ufun.work.GenForQueueWork;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beetl.sql.core.SQLManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/***
 * @author lizhneghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/23 15:44
 */
@Component
public class GenUrlJob extends QuartzJobBean {

    public Log log = LogFactory.getLog(GenUrlJob.class);

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private SQLManager sqlManager;

    @Autowired
    private HikariDataSource hikariDataSource;

    @Autowired
    private ApplicationPropertis applicationPropertis;

    public ArrayBlockingQueue<Hotel> queue = new ArrayBlockingQueue(5000, true);//基于公平策略的queue

    public OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();




    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            queue.clear();//每次定时任务开始之前都先清空queue
            //TODO 尝试删除指定天内的reids的key
            createTable();
            log.info("——————==========定时任务开始执行===========—————");
            String table="hotel_"+ CalendarUtils.getDateString("yyyy_MM_dd");//得到当日操作的数据库表:hotel_yyyy-MM-dd
            log.info("当日要操作的数据库表:"+table);
            List<Hotel> hotelList = sqlManager.execute("select sno from "+table, Hotel.class, null);
            log.info("酒店数量:"+hotelList.size());
            ExecutorService executor = (ExecutorService) SpringApplicationContextUtil.getBean("executorService");
            enQueue(executor, hotelList, queue);//酒店数据入队--->queue
            deQueue(executor, queue);//酒店数据出队
            //monitor(executor);//后期可以去掉，使用JDK自带性能监控工具
        } catch (Exception e) {
            log.warn("异常操作："+e.toString());
            e.printStackTrace();
        }
    }

    private void createTable() throws Exception {
        String table="room_"+ CalendarUtils.getDateString("yyyy_MM_dd");//room_yyyy-MM-dd
        String sql="CREATE TABLE `"+table+"` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',\n" +
                "  `room_id` varchar(255) DEFAULT NULL COMMENT '房间主键',\n" +
                "  `room_type` varchar(255) DEFAULT NULL COMMENT '房型号',\n" +
                "  `bed_count` varchar(255) DEFAULT NULL COMMENT '床数量',\n" +
                "  `bed_type` varchar(255) DEFAULT NULL COMMENT '床型',\n" +
                "  `break_fask` varchar(255) DEFAULT NULL COMMENT '是否有早餐',\n" +
                "  `services` varchar(255) DEFAULT NULL COMMENT '服务设施',\n" +
                "  `peoples` varchar(255) DEFAULT NULL COMMENT '入住人数',\n" +
                "  `data_policy` varchar(255) DEFAULT NULL COMMENT '促销优惠政策',\n" +
                "  `room_price` varchar(255) DEFAULT NULL COMMENT '房间真实价格',\n" +
                "  `residue_room` varchar(255) DEFAULT NULL COMMENT '剩余房间',\n" +
                "  `book_way` varchar(255) DEFAULT NULL COMMENT '预订方式',\n" +
                "  `area` varchar(255) DEFAULT NULL COMMENT '面基',\n" +
                "  `floor_level` varchar(255) DEFAULT NULL COMMENT '楼层',\n" +
                "  `data_price` varchar(255) DEFAULT NULL COMMENT '原价',\n" +
                "  `data_pricedisplay` varchar(255) DEFAULT NULL COMMENT '吓人价',\n" +
                "  `hotel_id` varchar(255) DEFAULT NULL COMMENT '所属酒店id',\n" +
                "  `hotel_sno` varchar(255) DEFAULT NULL COMMENT '所属酒店编号',\n" +
                "  `hotel_address` varchar(255) DEFAULT NULL COMMENT '所属酒店地址',\n" +
                "  `hotel_name` varchar(255) DEFAULT NULL COMMENT '所属酒店名称',\n" +
                "  `lat` varchar(255) DEFAULT NULL COMMENT '所属酒店经纬度纬度',\n" +
                "  `lon` varchar(255) DEFAULT NULL COMMENT '所属酒店经纬度经度',\n" +
                "  `tel` varchar(255) DEFAULT NULL COMMENT '所属酒店电话',\n" +
                "  `create_time` varchar(255) DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_time` varchar(255) DEFAULT NULL COMMENT '更新时间',\n" +
                "  `type` varchar(255) DEFAULT NULL COMMENT '类型',\n" +
                "  `source` varchar(255) DEFAULT NULL,\n" +
                "  `start_Time` date DEFAULT NULL COMMENT '查询条件的开始时间',\n" +
                "  `end_Time` date DEFAULT NULL COMMENT '查询条件的结束时间',\n" +
                "  `start_time_str` varchar(255) DEFAULT NULL,\n" +
                "  `end_time_str` varchar(255) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=335 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='酒店房间详细表';";
        Connection connection=null;
        PreparedStatement ps=null;
        try {
            connection=hikariDataSource.getConnection();
            ps=connection.prepareStatement(sql);
            int result=ps.executeUpdate();
            System.out.println(result);
        } catch (SQLException e) {
            throw new Exception("创建表异常"+e.toString());
        }finally {
            try {
                if(ps!=null)
                    ps.close();
                if(connection!=null)
                    hikariDataSource.evictConnection(connection);
            } catch (SQLException e) {
                ps=null;
                connection=null;
            }
        }
    }

    /**
     * 监控线程池状态
     * @param executor
     */
    protected void monitor(ExecutorService executor) {
        int i=0;
        ThreadPoolExecutor tpe = ((ThreadPoolExecutor) executor);
        while (i<2000000000) {
            double cpuLoad = osmxb.getSystemCpuLoad();//获取形同cpu负载
            double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
            double totalMemory = osmxb.getTotalPhysicalMemorySize();//获取系统内存占用
            log.info("\r\n==================================================================================" +
                    "\r\n当前排队线程数:" + tpe.getQueue().size() +
                    "\r\n当前活动线程数：" + tpe.getActiveCount() +
                    "\r\n执行完成线程数：" + tpe.getCompletedTaskCount() +
                    "\r\n线程总数：" + tpe.getTaskCount() +
                    "\r\ncpu使用率：" + cpuLoad * 100 +
                    "\r\n内存占用：" + ((1 - freePhysicalMemorySize / totalMemory) * 100) +
                    "---------------------------------------------------------------------------------"+
                    "---------------------------------------------------------------------------------");
            try {
                Thread.sleep(1000*60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                i++;
            }
        }
    }


    /**
     * 出队
     *
     * @param executor 线程执行器
     * @param queue    共享阻塞队列
     */
    public void deQueue(ExecutorService executor, BlockingQueue<Hotel> queue) {
        ApplicationPropertis applicationPropertis = (ApplicationPropertis) SpringApplicationContextUtil.getBean("appProperty");
        log.info("SB配置："+applicationPropertis);
        //此处减1是因为GenForQueueWork占用了线程池的一个线程，如果此处不减也行，那么多余的任务将加入线程池内部维护的线程等待队列中
        int threadCount = applicationPropertis.getThreadCount()-1;
        for (int i = 0; i < threadCount; i++) {
            ConsumeForQueueWork consumeForQueue = new ConsumeForQueueWork();
            consumeForQueue.setJedisPool(jedisPool);
            consumeForQueue.setQueue(queue);
            consumeForQueue.setSpiderDay(applicationPropertis.getSpiderDay());//设置爬取天数,会根据天数进行url的生产
            executor.execute(consumeForQueue);
        }
    }

    /**
     * 入队
     *
     * @param executor 线程执行器
     */
    public void enQueue(ExecutorService executor, List<Hotel> hotelList, BlockingQueue<Hotel> queue) {
        GenForQueueWork genForQueue = new GenForQueueWork();
        genForQueue.setHotelList(hotelList);
        genForQueue.setQueue(queue);
        executor.execute(genForQueue);
    }


    /**
     * 经过测试，定时任务每次执行都会生成执行的对象
     */
    public GenUrlJob() {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>GenUrlJob被构造>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}

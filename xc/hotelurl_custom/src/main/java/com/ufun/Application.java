package com.ufun;

import com.sun.management.OperatingSystemMXBean;
import com.ufun.bean.ApplicationPropertis;
import com.ufun.job.UseUrlJob;
import com.ufun.util.SpringApplicationContextUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.beetl.sql.core.SQLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import redis.clients.jedis.JedisPool;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/***
 * @author lizenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/22 9:23
 */
@SpringBootApplication
public class Application {

    private final static Logger log = LoggerFactory.getLogger(Application.class);

    public static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    public static void main(String[] args) {
        /**
         * springboot启动入口,MVC相关功能。
         */
        ApplicationContext applicationContext= SpringApplication.run(Application.class, args);
        //获取线程执行器
        ExecutorService executor= (ExecutorService) applicationContext.getBean("executorService");
        //获取sb框架配置文件属性
        ApplicationPropertis applicationPropertis= (ApplicationPropertis) applicationContext.getBean("appProperty");
        //获取一个Redis连接池工具
        JedisPool jedisPool=applicationContext.getBean("jedisPool",JedisPool.class);
        //SQLManager sqlManager=applicationContext.getBean("sqlManager",SQLManager.class);
        //获取mysql连接池
        HikariDataSource hikariDataSource=applicationContext.getBean("hikariDataSource",HikariDataSource.class);
        //配置文件中ufun.thread-count=6配置了多少个线程就启动多少个线程
        for(int i=0;i<applicationPropertis.getThreadCount();i++){
            executor.execute(new UseUrlJob(jedisPool,hikariDataSource));//多线程共享redis连接池和mysql连接池
        }
        //monitor(executor);//此处改用JDK自带工具更为直观
    }

    /**
     * 此方法监控线程池中的线程状态，
     * @param executor
     */
    protected static void monitor(ExecutorService executor) {
        int i=0;
        while (i<2099999999) {
            ThreadPoolExecutor tpe = ((ThreadPoolExecutor) executor);
            double cpuLoad = osmxb.getSystemCpuLoad();//获取形同cpu负载
            double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
            double totalMemory = osmxb.getTotalPhysicalMemorySize();//获取系统内存占用
            log.info("\r\n=============================================" +
                    "\r\n当前排队线程数:" + tpe.getQueue().size() +
                    "\r\n当前活动线程数：" + tpe.getActiveCount() +
                    "\r\n执行完成线程数：" + tpe.getCompletedTaskCount() +
                    "\r\n线程总数：" + tpe.getTaskCount() +
                    "\r\ncpu使用率：" + cpuLoad * 100 +
                    "\r\n内存占用：" + ((1 - freePhysicalMemorySize / totalMemory) * 100));
            try {
                Thread.sleep(1000*120);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                i++;
            }
        }
    }
}

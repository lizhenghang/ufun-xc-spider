package com.ufun.config;

import com.ufun.bean.ApplicationPropertis;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/***
 * @author lizhneghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/23 15:44
 */

@Configuration
@ConfigurationProperties("ufun")
public class ThreadConfig {

    //public ThreadPoolExecutor threadPoolExecutor;
    public ExecutorService executorService;
    public int threadCount;

    /**
     * 将自定义的配置参数全部放置到ApplicationPropertis对象中使用
     * @return
     */
    @Bean("appProperty")
    public ApplicationPropertis getAppProperty(){
        ApplicationPropertis applicationPropertis=new ApplicationPropertis();
        applicationPropertis.setThreadCount(threadCount);
        return applicationPropertis;
    }

    @Bean("executorService")
    public ExecutorService getExecutor() {
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>(100);
        ThreadFactory threadFactory =Executors.defaultThreadFactory();
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(threadCount, 100, 1, TimeUnit.HOURS,
                taskQueue, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        return executorService;
        //return executorService= Executors.newFixedThreadPool(threadCount);
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}

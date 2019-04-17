package com.ufun.config;

import com.ufun.bean.ApplicationPropertis;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private int spiderDay;
    /**
     * 将自定义的配置参数全部放置到ApplicationPropertis对象中使用
     * @return
     */
    @Bean("appProperty")
    public ApplicationPropertis getAppProperty(){
        ApplicationPropertis applicationPropertis=new ApplicationPropertis();
        applicationPropertis.setThreadCount(threadCount);
        applicationPropertis.setSpiderDay(spiderDay);
        return applicationPropertis;
    }

    @Bean("executorService")
    public ExecutorService getExecutor() {
        return executorService= Executors.newFixedThreadPool(threadCount+3);
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getSpiderDay() {
        return spiderDay;
    }

    public void setSpiderDay(int spiderDay) {
        this.spiderDay = spiderDay;
    }
}

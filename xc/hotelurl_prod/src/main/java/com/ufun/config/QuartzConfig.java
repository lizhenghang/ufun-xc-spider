package com.ufun.config;

import com.ufun.job.GenUrlJob;
import org.quartz.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/22 10:37
 */
@Configuration
@ConfigurationProperties("ufun")
public class QuartzConfig {

    private String timer;

    @Bean
    public JobDetail myJobDetail(){
        JobDetail jobDetail = JobBuilder.newJob(GenUrlJob.class)
                .withIdentity("myJob1","myJobGroup1")
                //JobDataMap可以给任务execute传递参数
                .usingJobData("job_param","job_param1")
                .storeDurably()
                .build();
        return jobDetail;
    }
    @Bean
    public Trigger myTrigger(){
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(myJobDetail())
                .withIdentity("myTrigger1","myTriggerGroup1")
                .usingJobData("job_trigger_param","job_trigger_param1")
                .startNow()
                //.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5).repeatForever())
                .withSchedule(CronScheduleBuilder.cronSchedule(timer))//每天13点35执行
                //.withSchedule(CronScheduleBuilder.cronSchedule("/6 * * * * ?"))//每10秒执行
                .build();
        return trigger;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        System.out.println("timer="+timer);
        System.out.println("timer="+timer);
        System.out.println("timer="+timer);
        System.out.println("timer="+timer);
        System.out.println("timer="+timer);
        System.out.println("timer="+timer);
        System.out.println("timer="+timer);
        this.timer = timer;
    }
}

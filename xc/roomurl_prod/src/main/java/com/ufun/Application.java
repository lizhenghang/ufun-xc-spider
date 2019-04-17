package com.ufun;

import com.ufun.util.SpringApplicationContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/***
 * @author lizhneghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/23 15:44
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        /**
         * springboot启动入口,MVC相关功能。
         */
        ApplicationContext applicationContext=SpringApplication.run(Application.class, args);
        SpringApplicationContextUtil.setApplicationContext(applicationContext);

    }
}

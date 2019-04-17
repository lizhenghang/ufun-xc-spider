package com.ufun;

import com.ufun.util.SpringApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/***
 * @author lizhneghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/23 15:44
 */
@SpringBootApplication
public class Application {

    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        /**
         * springboot启动入口,MVC相关功能。
         */
        ApplicationContext applicationContext=SpringApplication.run(Application.class, args);
        SpringApplicationContextUtil.setApplicationContext(applicationContext);//

    }
}

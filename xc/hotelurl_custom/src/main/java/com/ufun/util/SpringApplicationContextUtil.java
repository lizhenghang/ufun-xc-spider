package com.ufun.util;

import org.springframework.context.ApplicationContext;

/***
 * @author lizenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/23 12:02
 */
public class SpringApplicationContextUtil {
    private static ApplicationContext applicationContext = null;

    public static void setApplicationContext(ApplicationContext applicationContext) {

        if (SpringApplicationContextUtil.applicationContext == null)
            SpringApplicationContextUtil.applicationContext = applicationContext;
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
}

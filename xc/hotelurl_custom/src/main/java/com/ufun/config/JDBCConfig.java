package com.ufun.config;

import com.zaxxer.hikari.HikariDataSource;
import org.beetl.sql.core.*;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.MySqlStyle;
import org.beetl.sql.ext.DebugInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/19 9:52
 */
@Configuration
@ConfigurationProperties("spring.datasource")
public class JDBCConfig {

    private String url;
    private String username;
    private String password;
    private String driverClassName;

    @Bean("hikariDataSource")
    public HikariDataSource getHikariDataSource(){
        //配置HikariDataSource数据源，号称史上最快的数据源
        HikariDataSource hikariDataSource=new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setUsername(username);
        hikariDataSource.setPassword(password);
        hikariDataSource.setDriverClassName(driverClassName);
        return hikariDataSource;
    }


    @Bean("sqlManager")
    public SQLManager getSQLManager(){
        ConnectionSource source = ConnectionSourceHelper.getSimple(driverClassName, url, username, password);
        DBStyle mysql = new MySqlStyle();
        // sql语句放在classpagth的/sql 目录下
        SQLLoader loader = new ClasspathLoader("/sql");
        // 数据库命名跟java命名一样，所以采用DefaultNameConversion，还有一个是UnderlinedNameConversion，下划线风格的，
        UnderlinedNameConversion nc = new UnderlinedNameConversion();
        // 最后，创建一个SQLManager,DebugInterceptor 不是必须的，但可以通过它查看sql执行情况
        SQLManager sqlManager = new SQLManager(mysql, loader, source, nc, new Interceptor[]{new DebugInterceptor()});
        return sqlManager;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
}

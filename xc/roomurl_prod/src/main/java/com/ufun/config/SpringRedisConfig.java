package com.ufun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/***
 * @author lizhenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/29 11:22
 */
@Configuration
@ConfigurationProperties("spring.redis")
public class SpringRedisConfig {

    private String host;
    private Integer port;


    @Bean("jedisPool")
    public JedisPool getJedisClient(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(50);
        jedisPoolConfig.setBlockWhenExhausted(false);
        jedisPoolConfig.setFairness(true);
        JedisPool jedisPool=new JedisPool(jedisPoolConfig,host,port,1000);
        return jedisPool;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}

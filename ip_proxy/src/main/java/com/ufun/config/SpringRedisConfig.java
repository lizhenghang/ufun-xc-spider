package com.ufun.config;

import com.ufun.bean.RedisInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/***
 * @author lizhenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/28 17:14
 */
@Configuration
@ConfigurationProperties("spring.redis")
public class SpringRedisConfig {

    private String host;
    private Integer port;

    @Bean("redisInfo")
    public RedisInfo getRedisInfo(){
        RedisInfo redisInfo=new RedisInfo();
        redisInfo.setHost(host);
        redisInfo.setPort(port);
        return redisInfo;
    }

    @Bean("jedisPool")
    public JedisPool getJedisClient(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
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

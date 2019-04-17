package com.ufun.bean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/***
 * @author lizhneghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/23 15:44
 */
public class ConnectPool {

    private static JedisPoolConfig jedisPoolConfig ;

    public static JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public static void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        ConnectPool.jedisPoolConfig = jedisPoolConfig;
    }
}

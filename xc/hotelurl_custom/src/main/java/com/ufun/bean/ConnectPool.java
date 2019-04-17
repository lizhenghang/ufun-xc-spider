package com.ufun.bean;

import redis.clients.jedis.JedisPool;

/***
 * @author lizenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/22 13:02
 */
public class ConnectPool {

    private static JedisPool jedisPool;

    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    public static void setJedisPool(JedisPool jedisPool) {
        ConnectPool.jedisPool = jedisPool;
    }
}

package com.ufun.bean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/21 16:13
 */
public class ConnectPool {

    private JedisPool jedisPool;

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
}

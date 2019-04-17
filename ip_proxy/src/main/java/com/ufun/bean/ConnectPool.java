package com.ufun.bean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/***
 * @author lizenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/22 13:02
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

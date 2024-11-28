package ru.t1.java.demo.config;

import redis.clients.jedis.UnifiedJedis;

public class JedisClient extends UnifiedJedis {

    public JedisClient(String url) {
        super(url);
    }

}

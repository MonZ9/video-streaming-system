package com.video.util;

import redis.clients.jedis.Jedis;

import java.util.Set;

public class RedisUtil {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 6379;

    public static Jedis getJedis() {
        return new Jedis(HOST, PORT);
    }

    // ================= 基础操作 =================

    public static void set(String key, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.set(key, value);
        }
    }

    public static void setex(String key, int seconds, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.setex(key, seconds, value);
        }
    }

    public static String get(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.get(key);
        }
    }

    public static void del(String key) {
        try (Jedis jedis = getJedis()) {
            jedis.del(key);
        }
    }

    // ================= ⭐批量删除（兼容版） =================

    public static void delByPattern(String pattern) {

        try (Jedis jedis = getJedis()) {

            Set<String> keys = jedis.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    jedis.del(key);
                }
            }
        }
    }
}
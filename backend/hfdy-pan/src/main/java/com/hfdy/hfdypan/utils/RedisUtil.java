package com.hfdy.hfdypan.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author hf-dy
 * @date 2024/10/21 17:47
 */
@Component
public class RedisUtil<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    /**
     * 根据keys删除
     *
     * @param keys
     */
    public Long delete(String... keys) {
        if (keys != null && keys.length > 0) {
            return redisTemplate.delete(Arrays.asList(keys));
        }
        return 0L;
    }

    /**
     * 根据key获取value
     *
     * @param key
     * @return
     */
    public V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     */
    public void set(String key, V value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 添加并设置过期时间
     *
     * @param key
     * @param value
     * @param time  秒
     */
    public void set(String key, V value, long time) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 获取key的过期时间
     *
     * @param key
     * @return
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
}

package com.hfdy.hfdypan.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.List;

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

    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
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
        if (time > 0) {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
        } else {
            redisTemplate.persist(key);
        }
    }

    /**
     * +1
     *
     * @param key
     */
    public void incr(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    /**
     * -1
     *
     * @param key
     */
    public void decr(String key) {
        redisTemplate.opsForValue().decrement(key);
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


    /**
     * 集合添加元素
     *
     * @param key
     * @param value
     * @return
     */
    public void setAdd(String key, V value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public void setAdd(String key, V value, Long expire) {
        redisTemplate.opsForSet().add(key, value);
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    /**
     * 集合中是否存在key value
     *
     * @param key
     * @param value
     */
    public Boolean setExists(String key, V value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 添加所有元素到集合
     *
     * @param key
     * @param values
     * @return
     */
    public void setAddAll(String key, List<V> values) {
        for (V v : values) {
            setAdd(key, v);
        }
    }

    public void setAddAll(String key, List<V> values, Long expire) {
        for (V v : values) {
            setAdd(key, v);
        }
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    /***
     * 集合删除元素
     * @param key
     * @param value
     */
    public Long setRemove(String key, V value) {
        return redisTemplate.opsForSet().remove(key, value);
    }

    /**
     * 获取所有set的值
     *
     * @param key
     * @return
     */
    public Set<V> setGetAll(String key) {
        return redisTemplate.opsForSet().members(key);
    }


    /**
     * 保存hash类型
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public void hashPut(String key, String hashKey, V value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }


    /**
     * 获取hash的所有k v
     *
     * @param key
     * @return
     */
    public Map<String, V> hashGetAll(String key) {
        return redisTemplate.<String, V>opsForHash().entries(key);
    }

    /**
     * 删除hash类型
     *
     * @param key
     * @param hashKey
     */
    public void hashDelete(String key, String hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }


}

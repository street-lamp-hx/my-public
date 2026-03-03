package com.yiyitech.mf.util;

import com.yiyitech.support.redis.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author hx
 * @version 1.0.0
 * @ClassName RedisUtils.java
 * @Description
 * @createTime 2023年11月28日 13:47:00
 */
@Component
@SuppressWarnings({"unchecked", "all"})
public class RedisUtils {
    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private RedisCache redisCache;

    public RedisUtils(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setStringSerializer(new StringRedisSerializer());
    }

    /**
     * 尝试获取分布式锁，返回锁的唯一标识UUID，失败返回null
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    public String tryLock(String key, long timeout, TimeUnit unit) {
        try {
            String value = UUID.randomUUID().toString();
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
            if (Boolean.TRUE.equals(success)) {
                return value;
            }
            return null;
        } catch (Exception e) {
            log.error("尝试获取锁失败，key={}, err={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 释放分布式锁脚本
     */
    private static final String UNLOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";
    /**
     * 释放分布式锁，只有锁的值和传入值匹配才释放，避免误删
     * @param key
     * @param value
     * @return
     */
    public boolean releaseLock(String key, String value) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_LUA);
            script.setResultType(Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(key), value);
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("释放锁失败，key={}, err={}", key, e.getMessage());
            return false;
        }
    }

}

package com.yiyitech.support.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 轻量版 RedisCache：替代原 com.yiyitech.support.redis.RedisCache
 * 目的：去掉 yiyitech-support 后先让项目可编译/可运行
 */
@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class RedisCache {

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    // ======================= String Key - Value =======================

    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public <T> void setCacheObject(final String key, final T value, final long timeout, final TimeUnit unit) {
        if (timeout > 0) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } else {
            setCacheObject(key, value);
        }
    }

    public <T> T getCacheObject(final String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public Boolean expire(final String key, final long timeout, final TimeUnit unit) {
        if (timeout <= 0) {
            return Boolean.FALSE;
        }
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(final String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public Boolean hasKey(final String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    public Long deleteObject(final Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 注意：keys(pattern) 在大 key 空间会比较重；但为了兼容老代码先保留
     */
    public Collection<String> keys(final String pattern) {
        Set set = redisTemplate.keys(pattern);
        if (set == null) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>(set.size());
        for (Object o : set) {
            list.add(String.valueOf(o));
        }
        return list;
    }

    public Long increment(final String key, final long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Double increment(final String key, final double delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ======================= List =======================

    public <T> long setCacheList(final String key, final List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0L;
        }
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0L : count;
    }

    public <T> List<T> getCacheList(final String key) {
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size <= 0) {
            return Collections.emptyList();
        }
        return (List<T>) redisTemplate.opsForList().range(key, 0, size - 1);
    }

    // ======================= Set =======================

    public <T> long setCacheSet(final String key, final Set<T> dataSet) {
        if (dataSet == null || dataSet.isEmpty()) {
            return 0L;
        }
        Long count = redisTemplate.opsForSet().add(key, dataSet.toArray());
        return count == null ? 0L : count;
    }

    public <T> Set<T> getCacheSet(final String key) {
        Set set = redisTemplate.opsForSet().members(key);
        return set == null ? Collections.emptySet() : (Set<T>) set;
    }

    // ======================= Hash(Map) =======================

    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return;
        }
        redisTemplate.opsForHash().putAll(key, dataMap);
    }

    public <T> Map<String, T> getCacheMap(final String key) {
        Map map = redisTemplate.opsForHash().entries(key);
        return map == null ? Collections.emptyMap() : (Map<String, T>) map;
    }

    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    public <T> T getCacheMapValue(final String key, final String hKey) {
        return (T) redisTemplate.opsForHash().get(key, hKey);
    }

    public void deleteCacheMapValue(final String key, final String hKey) {
        redisTemplate.opsForHash().delete(key, hKey);
    }

    // ======================= 兼容原yiyitech-support的带数据库索引方法 =======================
    // 注意：Spring Boot默认的Redis连接池通常使用固定database，这里忽略dbIndex参数
    // 但保持方法签名兼容以避免修改调用代码

    /**
     * 检查key是否存在
     * 
     * @param key     键
     * @param dbIndex 数据库索引（当前忽略）
     * @return 1存在，0不存在
     */
    public int exist(final String key, int dbIndex) {
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists) ? 1 : 0;
    }

    /**
     * 删除key
     * 
     * @param key     键
     * @param dbIndex 数据库索引（当前忽略）
     */
    public void del(final String key, int dbIndex) {
        redisTemplate.delete(key);
    }

    /**
     * 设置key-value（如果不存在）
     * 
     * @param key     键
     * @param value   值
     * @param dbIndex 数据库索引（当前忽略）
     * @return 设置成功返回true
     */
    public Boolean setNx(final String key, final String value, int dbIndex) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 设置带过期时间的key-value
     * 
     * @param key     键
     * @param value   值
     * @param timeout 过期时间（秒）
     * @param dbIndex 数据库索引（当前忽略）
     */
    public void set(final String key, final String value, long timeout, int dbIndex) {
        if (timeout > 0) {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 获取key的value
     * 
     * @param key     键
     * @param dbIndex 数据库索引（当前忽略）
     * @return 值
     */
    public String get(final String key, int dbIndex) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }
}

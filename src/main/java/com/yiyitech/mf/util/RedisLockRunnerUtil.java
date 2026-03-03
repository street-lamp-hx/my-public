package com.yiyitech.mf.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName RedisLockRunnerUtil.java
 * @Description
 * @createTime 2025年08月28日 13:13:00
 */
@Slf4j
public class RedisLockRunnerUtil implements AutoCloseable{
    private final String lockKey;
    private final String lockValue;
    private final Duration expire;
    private final long renewIntervalSeconds;
    private final RedisUtils redisUtils;
    private final RedisTemplate<String, String> redisTemplate;

    private ScheduledExecutorService renewScheduler;

    private RedisLockRunnerUtil(String lockKey,
                            Duration expire,
                            long renewIntervalSeconds,
                            RedisUtils redisUtils,
                            RedisTemplate<String, String> redisTemplate,
                            String lockValue) {
        this.lockKey = lockKey;
        this.expire = expire;
        this.renewIntervalSeconds = renewIntervalSeconds;
        this.redisUtils = redisUtils;
        this.redisTemplate = redisTemplate;
        this.lockValue = lockValue;
    }

    /** 获取分布式锁（带续期），失败返回 null */
    public static RedisLockRunnerUtil tryAcquire(String lockKey,
                                             Duration expire,
                                             long renewIntervalSeconds,
                                             RedisUtils redisUtils,
                                             RedisTemplate<String, String> redisTemplate) {
        String value = redisUtils.tryLock(lockKey, expire.getSeconds(), TimeUnit.SECONDS);


//        if (value == null) return null;
        if (value == null) {
            //新增：未获取到锁时，打印持有者与剩余TTL（毫秒）
            String holder = null;
            Long ttlMs = null;
            try { holder = redisTemplate.opsForValue().get(lockKey); } catch (Exception ignore) {}
            try { ttlMs = redisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS); } catch (Exception ignore) {}
            log.info("未获取到锁：key={}, holder={}, ttl={}", lockKey, holder, formatTtl(ttlMs));
            return null;
        }


        RedisLockRunnerUtil runner = new RedisLockRunnerUtil(lockKey, expire, renewIntervalSeconds, redisUtils, redisTemplate, value);
        runner.startRenew();
        log.info("成功获取锁：key={}, value={}", lockKey, value);
        return runner;
    }

    /** 便捷执行：拿锁 → 执行业务 → 释放锁 */
    public static void runWithLock(String lockKey,
                                   Duration expire,
                                   long renewIntervalSeconds,
                                   RedisUtils redisUtils,
                                   RedisTemplate<String, String> redisTemplate,
                                   Runnable work) {
        RedisLockRunnerUtil lock = null;
        try {
            lock = RedisLockRunnerUtil.tryAcquire(lockKey, expire, renewIntervalSeconds, redisUtils, redisTemplate);
            if (lock == null) {
                log.info("已有同类任务在执行，跳过：{}", lockKey);
                return;
            }
            work.run();
        } catch (Throwable t) {
            log.error("任务执行异常：{}", t.getMessage(), t);
        } finally {
            if (lock != null) lock.close();
        }
    }

    private void startRenew() {
        //配置自检：续期间隔不能 >= 过期时间，否则可能来不及续期
        long expSec = expire.getSeconds();
        if (expSec <= renewIntervalSeconds) {
            log.warn("锁配置不合理：expire={}s <= renewInterval={}s，可能导致到期前无法续期", expSec, renewIntervalSeconds);
        }

        //（可选）安全保证：续期间隔明显小于过期时间
//        long interval = Math.min(renewIntervalSeconds, Math.max(1, expSec / 2));
        this.renewScheduler = Executors.newScheduledThreadPool(1);
        Runnable renewTask = () -> {
            try {
                String current = redisTemplate.opsForValue().get(lockKey);
                if (Objects.equals(current, lockValue)) {
                    redisTemplate.expire(lockKey, expire.getSeconds(), TimeUnit.SECONDS);
                    log.info("锁续期成功：{}", lockKey);
                } else {
                    log.warn("锁已被释放或被他人持有，停止续期：{}", lockKey);
                    stopRenew();
                }
            } catch (Throwable t) {
                log.warn("锁续期异常：{}", t.getMessage(), t);
            }
        };
        //用安全保证时：用interval替代下边的两个renewIntervalSeconds
        renewScheduler.scheduleAtFixedRate(renewTask, renewIntervalSeconds, renewIntervalSeconds, TimeUnit.SECONDS);
    }

    private void stopRenew() {
        if (renewScheduler != null) {
            renewScheduler.shutdownNow();
        }
    }

    /** 释放锁 + 停续期 */
//    @Override
//    public void close() {
//        try {
//            boolean released = redisUtils.releaseLock(lockKey, lockValue);
//            if (released) log.info("锁已释放：{}", lockKey);
//            else log.warn("锁释放失败或已被释放：{}", lockKey);
//        } finally {
//            stopRenew();
//        }
//    }
    @Override
    public void close() {
        try {
            boolean released = redisUtils.releaseLock(lockKey, lockValue);
            if (released) {
                log.info("锁已释放：{}", lockKey);
            } else {
                // 新增：释放失败时打印当前持有者与 TTL，便于判断是不是被别人抢占/替换
                String cur = null;
                Long ttlMs = null;
                try { cur = redisTemplate.opsForValue().get(lockKey); } catch (Exception ignore) {}
                try { ttlMs = redisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS); } catch (Exception ignore) {}
                log.warn("锁释放失败或已被释放：key={}, myValue={}, currentHolder={}, ttl={}", lockKey, lockValue, cur, formatTtl(ttlMs));
            }
        } finally {
            stopRenew();
        }
    }

    // 辅助：把 TTL 友好打印
    private static String formatTtl(Long ttlMs) {
        if (ttlMs == null) return "unknown";
        if (ttlMs == -2) return "no-key";     // Redis：键不存在
        if (ttlMs == -1) return "no-expire";  // Redis：永不过期
        return ttlMs + "ms";
    }

}

package com.yiyitech.mf.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class TermSummaryRedisDb2Config {

    // ====== 默认 Redis（DB1）：锁/其他业务用 ======
    @Value("${spring.redis.host}")
    private String defaultHost;

    @Value("${spring.redis.port}")
    private int defaultPort;

    @Value("${spring.redis.password:}")
    private String defaultPassword;

    @Value("${spring.redis.database:1}")
    private int defaultDb;

    @Value("${spring.redis.timeout:5000}")
    private long defaultTimeoutMs;

    // ====== term_summary 专用 Redis（DB2）：三类大缓存用 ======
    @Value("${termSummaryRedis.host:${spring.redis.host}}")
    private String termHost;

    @Value("${termSummaryRedis.port:${spring.redis.port}}")
    private int termPort;

    @Value("${termSummaryRedis.password:${spring.redis.password:}}")
    private String termPassword;

    @Value("${termSummaryRedis.database:2}")
    private int termDb;

    @Value("${termSummaryRedis.timeout:${spring.redis.timeout:5000}}")
    private long termTimeoutMs;

    /** DB1 连接工厂（Primary） */
    @Bean("redisConnectionFactory")
    @Primary
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
        conf.setHostName(defaultHost);
        conf.setPort(defaultPort);
        if (defaultPassword != null && !defaultPassword.trim().isEmpty()) {
            conf.setPassword(RedisPassword.of(defaultPassword));
        }
        conf.setDatabase(defaultDb); // <<< DB1

        LettuceClientConfiguration clientCfg = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(defaultTimeoutMs))
                .build();

        return new LettuceConnectionFactory(conf, clientCfg);
    }

    /** DB2 连接工厂（不要 Primary） */
    @Bean("termSummaryRedisConnectionFactory")
    public LettuceConnectionFactory termSummaryRedisConnectionFactory() {
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
        conf.setHostName(termHost);
        conf.setPort(termPort);
        if (termPassword != null && !termPassword.trim().isEmpty()) {
            conf.setPassword(RedisPassword.of(termPassword));
        }
        conf.setDatabase(termDb); // <<< DB2

        LettuceClientConfiguration clientCfg = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(termTimeoutMs))
                .build();

        return new LettuceConnectionFactory(conf, clientCfg);
    }

    // ====== redisTemplate 和 stringRedisTemplate 是补齐DB1默认模板用的（旧代码有大量使用它们往db1里加缓存的，为了不删除旧代码） ======

    /**
     * DB1 默认 redisTemplate（名字就叫 redisTemplate，且 Primary）
     * 现在项目里大量：@Autowired RedisTemplate<String,String> redisTemplate，会自动回DB1
     */
    @Bean("redisTemplate")
    @Primary
    public RedisTemplate<Object, Object> redisTemplate(
            @Qualifier("redisConnectionFactory") RedisConnectionFactory cf) {

        RedisTemplate<Object, Object> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);

        StringRedisSerializer str = new StringRedisSerializer();
        t.setKeySerializer(str);
        t.setHashKeySerializer(str);
        t.setValueSerializer(str);
        t.setHashValueSerializer(str);

        t.afterPropertiesSet();
        return t;
    }

    /**
     * DB1 默认 stringRedisTemplate（名字就叫 stringRedisTemplate，且 Primary）
     * 现在项目里大量 controller / service 里 @Autowired StringRedisTemplate stringRedisTemplate; 会自动回 DB1
     */
    @Bean("stringRedisTemplate")
    @Primary
    public StringRedisTemplate stringRedisTemplate(
            @Qualifier("redisConnectionFactory") RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    //DB2专用模板（必须显式 Qualifier 才会用到）
    @Bean("termSummaryRedisTemplate")
    public StringRedisTemplate termSummaryRedisTemplate(
            @Qualifier("termSummaryRedisConnectionFactory") RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

}

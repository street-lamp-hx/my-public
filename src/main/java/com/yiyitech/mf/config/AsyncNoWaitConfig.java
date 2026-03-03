package com.yiyitech.mf.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AsyncNoWaitConfig.java
 * @Description
 * @createTime 2025年11月13日 11:09:00
 */
@Configuration
@EnableAsync
public class AsyncNoWaitConfig {
    /**
     * 通用后台执行器
     */
    @Bean("backgroundExecutor")
    public ThreadPoolTaskExecutor backgroundExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("bg-");
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(20);
        ex.setKeepAliveSeconds(60);
        ex.setAllowCoreThreadTimeOut(true);
        ex.setTaskDecorator(mdcTaskDecorator());
        // 拒绝策略：保护服务，不要把任务回压到HTTP线程
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(1800);
        ex.initialize();
        return ex;
    }

    /** 日志MDC（如traceId/accountId）在异步线程中也可用*/
    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return (runnable) -> {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                if (contextMap != null) MDC.setContextMap(contextMap);
                try {
                    runnable.run();
                } finally {
                    if (previous != null) MDC.setContextMap(previous);
                    else MDC.clear();
                }
            };
        };
    }
}

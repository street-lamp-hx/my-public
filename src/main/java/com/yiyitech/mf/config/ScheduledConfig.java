package com.yiyitech.mf.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName ScheduledConfig.java
 * @Description
 * @createTime 2023年12月20日 17:37:00
 */
@Slf4j
@Configuration
@EnableScheduling
//@EnableAsync
public class ScheduledConfig implements SchedulingConfigurer, AsyncConfigurer {

    /** 最大线程数 */
    private static final int TASK_POOL_SIZE = 15;
    /** 线程池名前缀（字符串有长度限制） */
    private static final String TASK_THREAD_PREFIX = "ads-scheduled-";


    @Bean("taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(TASK_POOL_SIZE);
        executor.setThreadNamePrefix(TASK_THREAD_PREFIX);
        executor.setAwaitTerminationSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setTaskScheduler(this.taskScheduler());
    }

//    @Bean("asyncTaskExecutor")
//    public ThreadPoolTaskExecutor asyncTaskExecutor() {
//        ThreadPoolTaskExecutor asyncTaskExecutor = new ThreadPoolTaskExecutor();
//        asyncTaskExecutor.setThreadNamePrefix("Async-Thread-");
//        asyncTaskExecutor.setCorePoolSize(3);
//        asyncTaskExecutor.setQueueCapacity(100);
//        asyncTaskExecutor.setMaxPoolSize(10);
//        asyncTaskExecutor.setKeepAliveSeconds(6);
//        asyncTaskExecutor.setAllowCoreThreadTimeOut(true);
//        asyncTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        asyncTaskExecutor.setWaitForTasksToCompleteOnShutdown(false);
//        return asyncTaskExecutor;
//    }
//
//    @Override
//    public Executor getAsyncExecutor() {
//        return this.asyncTaskExecutor();
//    }
//
//    @Override
//    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//        return new SimpleAsyncUncaughtExceptionHandler();
//    }

}

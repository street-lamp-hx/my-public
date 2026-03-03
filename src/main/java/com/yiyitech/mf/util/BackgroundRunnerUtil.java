package com.yiyitech.mf.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
/**
 * @author hx
 * @version 1.0.0
 * @ClassName BackgroundRunnerUtil.java
 * @Description
 * @createTime 2025年11月13日 11:15:00
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BackgroundRunnerUtil {

    @Resource(name = "backgroundExecutor")
    private TaskExecutor executor;

    /** 提交一个fire-and-forget任务（不等待结果） */
    public void submit(String name, Runnable task) {
        try {
            executor.execute(() -> {
                long start = System.currentTimeMillis();
                try {
                    log.info("[BG:{}] start", name);
                    task.run();
                    log.info("[BG:{}] done, cost={}ms", name, System.currentTimeMillis() - start);
                } catch (Throwable t) {
                    log.error("[BG:{}] failed", name, t);
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException rex) {
            //队列满了就记录异常（后续看需要：可根据需要改成抛出业务异常/返回提示）
            log.error("[BG:{}] rejected: executor queue is full", name, rex);
        }
    }
}

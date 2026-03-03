package com.yiyitech.mf.util;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AsyncTaskUtil.java
 * @Description
 * @createTime 2025年07月16日 17:55:00
 */
public class AsyncTaskUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskUtil.class);
    private static final ThreadPoolExecutor executor;

    static {
        int core = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(
                core * 2,
                core * 4,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("autoNeg-task-" + t.getId());
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()//避免丢任务
        );
    }

    public static Object[] runAll(List<AsyncTask> asyncTaskList) {
        if (CollectionUtils.isEmpty(asyncTaskList)) {
            throw new RuntimeException("AsyncTask list is empty");
        }

        int size = asyncTaskList.size();
        Object[] resultArr = new Object[size];
        CompletableFuture<Void>[] futures = new CompletableFuture[size];

        for (int i = 0; i < size; i++) {
            AsyncTask task = asyncTaskList.get(i);
            int index = i;

            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    Object result = task.run();
                    resultArr[index] = result;
                    task.callback(result);
                } catch (Exception e) {
                    LOGGER.error("AsyncTask [{}] failed", index, e);
                }
            }, executor);
        }
        try {
            CompletableFuture.allOf(futures).get(); // 等待所有任务完成
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("AsyncTaskUtil.runAll encountered error", e);
        }
        return resultArr;
    }
}

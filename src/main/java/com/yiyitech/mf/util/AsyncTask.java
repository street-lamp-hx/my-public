package com.yiyitech.mf.util;

import java.text.ParseException;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AsyncTask.java
 * @Description
 * @createTime 2025年07月16日 17:55:00
 */
public abstract class AsyncTask {
    private int taskOrder;

    public AsyncTask() {
    }

    public AsyncTask(int taskOrder) {
        this.taskOrder = taskOrder;
    }

    public abstract Object run() throws ParseException;

    public void callback(Object returnValue) {
    }

    public int getTaskNum() {
        return this.taskOrder;
    }
}

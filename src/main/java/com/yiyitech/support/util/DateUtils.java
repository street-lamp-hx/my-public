package com.yiyitech.support.util;

import cn.hutool.core.date.DateUtil;

import java.util.Date;

/**
 * 轻量版 DateUtils：替代原 com.yiyitech.support.util.DateUtils
 * 目的：去掉 yiyitech-support 后先让项目可编译/可运行
 */
public class DateUtils {

    private DateUtils() {
        // 工具类不允许实例化
    }

    /**
     * 格式化日期
     *
     * @param date    日期
     * @param pattern 格式 (如 "yyyy-MM-dd")
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return DateUtil.format(date, pattern);
    }
}

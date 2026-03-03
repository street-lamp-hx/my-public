package com.yiyitech.support.util;

import cn.hutool.core.bean.BeanUtil;

/**
 * 轻量版 BeanCopyUtil：替代原 com.yiyitech.support.util.BeanCopyUtil
 * 目的：去掉 yiyitech-support 后先让项目可编译/可运行
 */
public class BeanCopyUtil {

    private BeanCopyUtil() {
        // 工具类不允许实例化
    }

    /**
     * 复制 Bean 属性
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyBean(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target);
    }

    /**
     * 复制 Bean 属性（忽略指定属性）
     *
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreProperties 忽略的属性名
     */
    public static void copyBean(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target, ignoreProperties);
    }
}

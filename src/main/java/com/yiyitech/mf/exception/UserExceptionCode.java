package com.yiyitech.mf.exception;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName UserExceptionCode.java
 * @Description
 * @createTime 2025年06月13日 14:46:00
 */
public class UserExceptionCode {
    public static final String USER_ALREADY_EXIST_CODE = "100501100";
    public static final String USER_ALREADY_EXIST_MSG = "用户已注册";

    public static final String USER_INFO_IS_EMPTY_CODE = "100502100";
    public static final String USER_INFO_IS_EMPTY_MSG = "用户名或密码不能为空";

    public static final String USER_NOT_EXIST_CODE = "100503100";
    public static final String USER_NOT_EXIST_MSG = "用户不存在";

    public static final String PASSWORD_ERROR_CODE = "100504100";
    public static final String PASSWORD_ERROR__MSG = "密码错误！";
}

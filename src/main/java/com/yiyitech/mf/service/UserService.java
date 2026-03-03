package com.yiyitech.mf.service;


import java.util.Map;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsUserService.java
 * @Description
 * @createTime 2024年01月18日 16:49:00
 */
public interface UserService {

    Map<String, Object> getUserInfo(Long userId, boolean isAdmin);
}

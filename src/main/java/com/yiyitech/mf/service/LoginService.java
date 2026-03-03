package com.yiyitech.mf.service;

import java.util.Map;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsLoginService.java
 * @Description
 * @createTime 2025年06月11日 17:45:00
 */
public interface LoginService {

    Map<String, Object> register(String phone, String password, String code);

    Map<String, Object> login(String phone, String password);

}

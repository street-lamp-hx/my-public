package com.yiyitech.mf.util;

import com.yiyitech.mf.model.dobj.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName SecurityUtil.java
 * @Description
 * @createTime 2025年06月16日 16:06:00
 */
public class SecurityUtil {
    public static LoginUser getCurrentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
}



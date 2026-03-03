package com.yiyitech.mf.model.dobj;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName LoginUserModel.java
 * @Description
 * @createTime 2025年06月16日 16:16:00
 */
@Data
@AllArgsConstructor
public class LoginUser {
    private String id;
    private String userName;
    private String parentId;
    private String phone;
    private List<String> roles;
    private List<String> permissions;

    public boolean isSuperAdmin() {
        return roles != null && !roles.isEmpty() && roles.contains("ADMIN");
    }

    public boolean isAdmin() {
        return roles != null && !roles.isEmpty() && (roles.contains("ADMIN") || roles.contains("SUB:ADMIN"));
    }
}

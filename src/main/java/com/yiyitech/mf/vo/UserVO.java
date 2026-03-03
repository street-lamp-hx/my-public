package com.yiyitech.mf.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsUserVO.java
 * @Description
 * @createTime 2025年06月25日 15:57:00
 */
@Getter
@Setter
public class UserVO {
    private String userId;
    private String token;
    private String username;
    private String name;
    private String phone;
    private String email;
    private Boolean alreadyLogin;
    private List<String> roleList;
}

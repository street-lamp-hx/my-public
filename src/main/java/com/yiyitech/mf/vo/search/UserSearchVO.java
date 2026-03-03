package com.yiyitech.mf.vo.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsUserSearchVO.java
 * @Description
 * @createTime 2025年06月12日 17:26:00
 */
@Getter
@Setter
public class UserSearchVO {
    private String parentId;
    private String username;
    private String name;
    @Schema(description = "手机号")
    private String phone;
    private String email;
    @Schema(description = "密码")
    private String password;
    private String code;
}

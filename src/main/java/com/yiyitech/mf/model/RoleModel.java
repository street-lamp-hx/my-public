package com.yiyitech.mf.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsRoleModel.java
 * @Description
 * @createTime 2025年06月20日 16:42:00
 */
@Data
@Accessors(chain = true)
@TableName("ads_role")
public class RoleModel implements Serializable {
    private static final long serialVersionUID = 7137030108867115524L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "role_name")
    private String roleName;

    @TableField(value = "role_code")
    private String roleCode;

    @TableField(value = "description")
    private String description;

    @TableField(value = "status")
    private Boolean status;

    // 创建者
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    // 更新者
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    // 更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

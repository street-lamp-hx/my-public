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
 * @ClassName AdsRolePermissionModel.java
 * @Description
 * @createTime 2025年06月20日 16:44:00
 */
@Data
@Accessors(chain = true)
@TableName("ads_role_permission")
public class RolePermissionModel implements Serializable {
    private static final long serialVersionUID = 8313272861058434348L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "role_id")
    private Long roleId;

    @TableField(value = "permission_id")
    private Long permissionId;

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

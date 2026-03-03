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
 * @ClassName AdsPermissionModel.java
 * @Description
 * @createTime 2025年06月20日 16:43:00
 */
@Data
@Accessors(chain = true)
@TableName("ads_permission")
public class PermissionModel implements Serializable {
    private static final long serialVersionUID = 5489403685422616324L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "permission_name")
    private String permissionName;

    @TableField(value = "permission_code")
    private String permissionCode;

    @TableField(value = "permission_type")
    private Integer permissionType;

    @TableField(value = "parent_id")
    private Long parentId;

    @TableField(value = "path")
    private String path;

    @TableField(value = "icon")
    private String icon;

    @TableField(value = "sort")
    private Integer sort;

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

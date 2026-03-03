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
 * @ClassName AdsUserRoleModel.java
 * @Description
 * @createTime 2025年06月20日 16:43:00
 */
@Data
@Accessors(chain = true)
@TableName("ads_user_role")
public class UserRoleModel implements Serializable {
    private static final long serialVersionUID = 6792559146770366257L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "role_id")
    private Long roleId;

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

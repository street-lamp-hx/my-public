package com.yiyitech.mf.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Date;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsUserModel.java
 * @Description
 * @createTime 2025年06月11日 15:16:00
 */
@Data
@Accessors(chain = true)
@TableName("ads_user")
public class UserModel implements Serializable {
    private static final long serialVersionUID = -8525639745232094L;

    @TableId(type = IdType.AUTO)
    private Long id;
    //父级id
    @TableField(value = "parent_id")
    private Long parentId;
    //用户名
    @TableField(value = "username")
    private String username;
    //昵称
    @TableField(value = "name")
    private String name;
    //手机号
    @TableField(value = "phone")
    private String phone;
    //邮箱
    @TableField(value = "email")
    private String email;
    //密码
    @TableField(value = "password")
    private String password;
    //用户状态（0无效、1有效）
    @TableField(value = "status")
    private Boolean status;
    //是否已经登录过系统（0没有登陆过，1已经登陆过）
    @TableField(value = "already_login")
    private Boolean alreadyLogin;
    //创建者
    @TableField(value = "create_by")
    private String createBy;
    //更新者
    @TableField(value = "update_by")
    private String updateBy;
    //创建时间
    @TableField(value = "create_time")
    private Date createTime;
    //更新时间
    @TableField(value = "update_time")
    private Date updateTime;
}

package com.yiyitech.mf.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiyitech.mf.model.UserRoleModel;
import com.yiyitech.support.mybatis.AbstractBaseMapper;

import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsUserRoleMapper.java
 * @Description
 * @createTime 2025年06月20日 16:13:00
 */
public interface UserRoleMapper extends AbstractBaseMapper<UserRoleModel> {

    default List<UserRoleModel> searchByUserId(Long userId) {
        QueryWrapper<UserRoleModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return selectList(queryWrapper);
    }


}

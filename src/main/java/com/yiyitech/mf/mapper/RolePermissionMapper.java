package com.yiyitech.mf.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiyitech.mf.model.RolePermissionModel;
import com.yiyitech.support.mybatis.AbstractBaseMapper;

import java.util.Collections;
import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsRolePermissionMapper.java
 * @Description
 * @createTime 2025年06月20日 16:13:00
 */
public interface RolePermissionMapper extends AbstractBaseMapper<RolePermissionModel> {

    default List<RolePermissionModel> searchByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<RolePermissionModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", roleIds);
        return selectList(queryWrapper);
    }
}

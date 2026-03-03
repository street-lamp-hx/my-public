package com.yiyitech.mf.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiyitech.mf.model.RoleModel;
import com.yiyitech.support.mybatis.AbstractBaseMapper;

import java.util.Collections;
import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsRoleMapper.java
 * @Description
 * @createTime 2025年06月20日 17:46:00
 */
public interface RoleMapper extends AbstractBaseMapper<RoleModel> {

    default List<RoleModel> searchByIds(List<Long> Ids) {
        if (Ids == null || Ids.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<RoleModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", Ids);
        return selectList(queryWrapper);
    }
}

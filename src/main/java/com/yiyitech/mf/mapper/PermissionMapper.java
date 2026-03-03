package com.yiyitech.mf.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiyitech.mf.model.PermissionModel;
import com.yiyitech.support.mybatis.AbstractBaseMapper;

import java.util.Collections;
import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsPermissionMapper.java
 * @Description
 * @createTime 2025年06月20日 17:49:00
 */
public interface PermissionMapper extends AbstractBaseMapper<PermissionModel> {

    default List<PermissionModel> searchByIds(List<Long> Ids) {
        if (Ids == null || Ids.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<PermissionModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", Ids);
        return selectList(queryWrapper);
    }
}

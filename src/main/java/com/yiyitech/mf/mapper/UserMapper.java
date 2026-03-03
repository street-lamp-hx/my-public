package com.yiyitech.mf.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiyitech.mf.model.UserModel;
import com.yiyitech.support.mybatis.AbstractBaseMapper;

import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsUserModelMapper.java
 * @Description
 * @createTime 2025年06月13日 14:22:00
 */
public interface UserMapper extends AbstractBaseMapper<UserModel> {

    default List<UserModel> searchByPhone(String phone) {
        QueryWrapper<UserModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return selectList(queryWrapper);
    }
}

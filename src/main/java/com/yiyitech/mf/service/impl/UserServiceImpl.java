package com.yiyitech.mf.service.impl;

import cn.hutool.http.HttpStatus;
import com.yiyitech.mf.mapper.*;
import com.yiyitech.mf.model.RoleModel;
import com.yiyitech.mf.model.UserModel;
import com.yiyitech.mf.model.UserRoleModel;
import com.yiyitech.mf.service.UserService;
import com.yiyitech.mf.vo.UserVO;
import com.yiyitech.support.util.BeanCopyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author hx
 * @version 1.0.0
 * @ClassName AdsUserServiceImpl.java
 * @Description
 * @createTime 2024年01月18日 16:49:00
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper adsUserMapper;
    @Autowired
    private UserRoleMapper adsUserRoleMapper;
    @Autowired
    private RoleMapper adsRoleMapper;

    @Override
    public Map<String, Object> getUserInfo(Long userId, boolean isAdmin) {
        Map<String, Object> map = new HashMap<>();
        UserVO Vo = new UserVO();
        UserModel userModel = adsUserMapper.selectById(userId);
        BeanCopyUtil.copyBean(userModel, Vo);
        Vo.setUserId(userId.toString());

        List<UserRoleModel> roleModels = adsUserRoleMapper.searchByUserId(userModel.getId());
        List<Long> roleIds = roleModels.stream().map(UserRoleModel::getRoleId).collect(Collectors.toList());
        List<RoleModel> roleLst = roleIds.size() < 0 ? new ArrayList<>() : adsRoleMapper.selectBatchIds(roleIds);
        List<String> roleList = roleLst.stream().map(RoleModel::getRoleCode).collect(Collectors.toList());
        Vo.setRoleList(roleList);
        if(isAdmin && !Vo.getAlreadyLogin()){
            userModel.setAlreadyLogin(true);
            adsUserMapper.updateById(userModel);
        }
        map.put("data", Vo);
        map.put("status", HttpStatus.HTTP_OK);
        return map;
    }
}

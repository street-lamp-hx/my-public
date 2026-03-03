package com.yiyitech.mf.service.impl;

import cn.hutool.http.HttpStatus;
import com.yiyitech.mf.exception.UserExceptionCode;
import com.yiyitech.mf.mapper.*;
import com.yiyitech.mf.model.*;
import com.yiyitech.mf.service.LoginService;
import com.yiyitech.mf.util.TokenUtil;
import com.yiyitech.mf.vo.UserVO;
import com.yiyitech.support.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper adsUserMapper;
    @Autowired
    private UserRoleMapper adsUserRoleMapper;
    @Autowired
    private RolePermissionMapper adsRolePermissionMapper;
    @Autowired
    private RoleMapper adsRoleMapper;
    @Autowired
    private PermissionMapper adsPermissionMapper;

    @Override
    public Map<String, Object> register(String phone, String password, String code) {
        Map<String, Object> map = new HashMap<>();

//        AdsUserModel model = new AdsUserModel().setPhone(phone).setPassword(passwordEncoder.encode(password)).setStatus(true);
//        System.out.println(model.getPassword());

        if(CollectionUtils.isNotEmpty(adsUserMapper.searchByPhone(phone))){
            log.error("用户{"+phone+"}已注册");
            throw new BusinessException(UserExceptionCode.USER_ALREADY_EXIST_CODE, UserExceptionCode.USER_ALREADY_EXIST_MSG);
        }
        UserModel model = new UserModel().setPhone(phone).setPassword(passwordEncoder.encode(password)).setStatus(true);
        adsUserMapper.insert(model);
        map.put("status", HttpStatus.HTTP_OK);
        return map;
    }

    @Override
    public Map<String, Object> login(String phone, String password) {
        Map<String, Object> map = new HashMap<>();
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(password)) {
            throw new BusinessException(UserExceptionCode.USER_INFO_IS_EMPTY_CODE, UserExceptionCode.USER_INFO_IS_EMPTY_MSG);
        }
        List<UserModel> userModelLst  = adsUserMapper.searchByPhone(phone);
        if (CollectionUtils.isEmpty(userModelLst)) {
            throw new BusinessException(UserExceptionCode.USER_NOT_EXIST_CODE, UserExceptionCode.USER_NOT_EXIST_MSG);
        }
        UserModel user = userModelLst.get(0);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(UserExceptionCode.PASSWORD_ERROR_CODE, UserExceptionCode.PASSWORD_ERROR__MSG);
        }
        //TODO 权限角色放入redis取用
        //查询用户拥有的角色和权限
        List<UserRoleModel> roleModels = adsUserRoleMapper.searchByUserId(user.getId());
        List<Long> roleIds = roleModels.stream().map(UserRoleModel::getRoleId).collect(Collectors.toList());
        List<RolePermissionModel> permissionModels = adsRolePermissionMapper.searchByRoleIds(roleIds);
        List<Long> permissionIds = permissionModels.stream().map(RolePermissionModel::getPermissionId).collect(Collectors.toList());
        List<RoleModel> roleLst = roleIds.size() < 0 ? new ArrayList<>() : adsRoleMapper.selectBatchIds(roleIds);
        List<PermissionModel> permissionLst = permissionIds.size() < 0 ? new ArrayList<>() :  adsPermissionMapper.selectBatchIds(permissionIds);
        //角色和权限信息放入token
        List<String> roleList = roleLst.stream().map(RoleModel::getRoleCode).collect(Collectors.toList());
        List<String> permissionList = permissionLst.stream().map(PermissionModel::getPermissionCode).collect(Collectors.toList());
        String token = TokenUtil.generateToken(user.getId().toString(), user.getUsername(), user.getParentId().toString(), phone, roleList, permissionList);
        UserVO Vo = new UserVO();
        Vo.setToken(token);
        Vo.setRoleList(roleList);
        map.put("data", Vo);
        map.put("status", HttpStatus.HTTP_OK);
        return map;
    }

}

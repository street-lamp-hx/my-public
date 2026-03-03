package com.yiyitech.mf.controller;

import com.yiyitech.mf.model.dobj.LoginUser;
import com.yiyitech.mf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName UserController.java
 * @Description
 * @createTime 2024年01月18日 16:46:00
 */
@RestController
@RequestMapping("/v1/user")
public class AdsUserController {
    @Autowired
    private UserService userService;

    @RequestMapping(path = {"/info"}, method = {RequestMethod.POST})
    @CrossOrigin(originPatterns = "*", allowCredentials = "true")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
        return new ResponseEntity<>(userService.getUserInfo(Long.valueOf(loginUser.getId()), loginUser.isAdmin()), HttpStatus.OK);
    }
}

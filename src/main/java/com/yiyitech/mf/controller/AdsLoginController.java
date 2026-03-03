package com.yiyitech.mf.controller;

import com.yiyitech.mf.service.LoginService;
import com.yiyitech.mf.vo.search.UserSearchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName LoginController.java
 * @Description
 * @createTime 2025年06月11日 13:58:00
 */
@RestController
@RequestMapping("/v1/enter")
public class AdsLoginController {
    @Autowired
    private LoginService loginService;

    @RequestMapping(path = {"/register"}, method = {RequestMethod.POST})
    @CrossOrigin(originPatterns = "*", allowCredentials = "true")
//    @CrossOrigin(origins = "http://localhost:8091",allowCredentials = "true")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserSearchVO param) {
        return new ResponseEntity<>(loginService.register(param.getPhone(), param.getPassword(), param.getCode()), HttpStatus.OK);
    }

    @RequestMapping(path = {"/login"}, method = {RequestMethod.POST})
    @CrossOrigin(originPatterns = "*", allowCredentials = "true")
//    @CrossOrigin(origins = "http://localhost:8091",allowCredentials = "true")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserSearchVO param) {
        return new ResponseEntity<>(loginService.login(param.getPhone(), param.getPassword()), HttpStatus.OK);
    }

}


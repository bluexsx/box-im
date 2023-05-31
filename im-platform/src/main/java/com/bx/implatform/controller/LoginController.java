package com.bx.implatform.controller;


import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.vo.LoginVO;
import com.bx.implatform.vo.RegisterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;



@Api(tags = "用户登录和注册")
@RestController
public class LoginController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    @ApiOperation(value = "用户注册",notes="用户注册")
    public Result register(@Valid @RequestBody LoginVO vo){
        String token = userService.login(vo);
        return ResultUtils.success(token,ResultCode.SUCCESS.getMsg());
    }


    @PostMapping("/register")
    @ApiOperation(value = "用户注册",notes="用户注册")
    public Result register(@Valid @RequestBody RegisterVO vo){
        userService.register(vo);
        return ResultUtils.success();
    }
}

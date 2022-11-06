package com.bx.implatform.controller;


import com.bx.common.result.Result;
import com.bx.common.result.ResultUtils;
import com.bx.implatform.vo.RegisterVO;
import com.bx.implatform.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;



@Api(tags = "用户注册")
@RestController
public class RegisterController {

    @Autowired
    private IUserService userService;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册",notes="用户注册")
    public Result register(@Valid @RequestBody RegisterVO dto){
        userService.register(dto);
        return ResultUtils.success();
    }
}

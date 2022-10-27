package com.lx.implatform.controller;


import com.lx.common.result.Result;
import com.lx.common.result.ResultUtils;
import com.lx.implatform.service.IUserService;
import com.lx.implatform.vo.RegisterVO;
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

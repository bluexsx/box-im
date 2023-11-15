package com.bx.implatform.controller;


import com.bx.implatform.dto.ModifyPwdDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.dto.LoginDTO;
import com.bx.implatform.dto.RegisterDTO;
import com.bx.implatform.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;



@Api(tags = "用户登录和注册")
@RestController
@AllArgsConstructor
public class LoginController {

    private IUserService userService;

    @PostMapping("/login")
    @ApiOperation(value = "用户注册",notes="用户注册")
    public Result register(@Valid @RequestBody LoginDTO dto){
        LoginVO vo = userService.login(dto);
        return ResultUtils.success(vo);
    }


    @PutMapping("/refreshToken")
    @ApiOperation(value = "刷新token",notes="用refreshtoken换取新的token")
    public Result refreshToken(@RequestHeader("refreshToken")String refreshToken){
        LoginVO vo = userService.refreshToken(refreshToken);
        return ResultUtils.success(vo);
    }


    @PostMapping("/register")
    @ApiOperation(value = "用户注册",notes="用户注册")
    public Result register(@Valid @RequestBody RegisterDTO dto){
        userService.register(dto);
        return ResultUtils.success();
    }

    @PutMapping("/modifyPwd")
    @ApiOperation(value = "修改密码",notes="修改用户密码")
    public Result update(@Valid @RequestBody ModifyPwdDTO dto){
        userService.modifyPassword(dto);
        return ResultUtils.success();
    }

}

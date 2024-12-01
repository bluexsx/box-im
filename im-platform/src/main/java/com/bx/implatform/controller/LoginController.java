package com.bx.implatform.controller;

import com.bx.implatform.dto.LoginDTO;
import com.bx.implatform.dto.ModifyPwdDTO;
import com.bx.implatform.dto.RegisterDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.UserService;
import com.bx.implatform.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "注册登录")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return ResultUtils.success(vo);
    }

    @PutMapping("/refreshToken")
    @Operation(summary = "刷新token", description = "用refreshtoken换取新的token")
    public Result refreshToken(@RequestHeader("refreshToken") String refreshToken) {
        LoginVO vo = userService.refreshToken(refreshToken);
        return ResultUtils.success(vo);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册")
    public Result register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return ResultUtils.success();
    }

    @PutMapping("/modifyPwd")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result modifyPassword(@Valid @RequestBody ModifyPwdDTO dto) {
        userService.modifyPassword(dto);
        return ResultUtils.success();
    }

}

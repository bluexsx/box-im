package com.bx.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("用户登录DTO")
public class LoginDTO {

    @Max(value = 1,message = "登录终端类型取值范围:0,1")
    @Min(value = 0,message = "登录终端类型取值范围:0,1")
    @NotNull(message="登录终端类型不可为空")
    @ApiModelProperty(value = "登录终端 0:web  1:app")
    private Integer terminal;

    @NotEmpty(message="用户名不可为空")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @NotEmpty(message="用户密码不可为空")
    @ApiModelProperty(value = "用户密码")
    private String password;

}

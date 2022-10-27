package com.lx.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("用户注册VO")
public class RegisterVO {

    @NotEmpty(message="用户名不可为空")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @NotEmpty(message="用户密码不可为空")
    @ApiModelProperty(value = "用户密码")
    private String password;

    @NotEmpty(message="用户昵称不可为空")
    @ApiModelProperty(value = "用户昵称")
    private String nickName;


}

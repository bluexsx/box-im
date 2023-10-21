package com.bx.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("修改密码DTO")
public class ModifyPwdDTO {

    @NotEmpty(message="旧用户密码不可为空")
    @ApiModelProperty(value = "旧用户密码")
    private String oldPassword;

    @NotEmpty(message="新用户密码不可为空")
    @ApiModelProperty(value = "新用户密码")
    private String newPassword;

}

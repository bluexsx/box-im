package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description = "修改密码DTO")
public class ModifyPwdDTO {

    @NotEmpty(message = "旧用户密码不可为空")
    @Schema(description = "旧用户密码")
    private String oldPassword;

    @NotEmpty(message = "新用户密码不可为空")
    @Schema(description = "新用户密码")
    private String newPassword;

}

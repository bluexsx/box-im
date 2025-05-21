package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "用户注册DTO")
public class RegisterDTO {

    @Length(max = 20, message = "用户名不能大于20字符")
    @NotEmpty(message = "用户名不可为空")
    @Schema(description = "用户名")
    private String userName;

    @Length(min = 5, max = 20, message = "密码长度必须在5-20个字符之间")
    @NotEmpty(message = "用户密码不可为空")
    @Schema(description = "用户密码")
    private String password;

    @Length(max = 20, message = "昵称不能大于20字符")
    @NotEmpty(message = "用户昵称不可为空")
    @Schema(description = "用户昵称")
    private String nickName;


}

package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户登录VO")
public class LoginVO {

    @Schema(description = "每次请求都必须在header中携带accessToken")
    private String accessToken;

    @Schema(description = "accessToken过期时间(秒)")
    private Integer accessTokenExpiresIn;

    @Schema(description = "accessToken过期后，通过refreshToken换取新的token")
    private String refreshToken;

    @Schema(description = "refreshToken过期时间(秒)")
    private Integer refreshTokenExpiresIn;

}

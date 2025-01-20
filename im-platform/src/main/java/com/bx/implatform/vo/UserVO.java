package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "用户信息VO")
public class UserVO {

    @NotNull(message = "用户id不能为空")
    @Schema(description = "id")
    private Long id;

    @NotEmpty(message = "用户名不能为空")
    @Length(max = 20, message = "用户名不能大于20字符")
    @Schema(description = "用户名")
    private String userName;

    @NotEmpty(message = "用户昵称不能为空")
    @Length(max = 20, message = "昵称不能大于20字符")
    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "用户类型 1:普通用户 2:审核账户")
    private Integer type;

    @Length(max = 128, message = "个性签名不能大于128个字符")
    @Schema(description = "个性签名")
    private String signature;

    @Schema(description = "头像")
    private String headImage;

    @Schema(description = "头像缩略图")
    private String headImageThumb;

    @Schema(description = "是否在线")
    private Boolean online;

    @Schema(description = "账号是否被封禁")
    private Boolean isBanned;

    @Schema(description = "被封禁原因")
    private String reason;
}

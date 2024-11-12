package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "群信息VO")
public class GroupVO {

    @Schema(description = "群id")
    private Long id;

    @Length(max = 20, message = "群名称长度不能大于20")
    @NotEmpty(message = "群名称不可为空")
    @Schema(description = "群名称")
    private String name;

    @Schema(description = "群主id")
    private Long ownerId;

    @Schema(description = "头像")
    private String headImage;

    @Schema(description = "头像缩略图")
    private String headImageThumb;

    @Length(max = 1024, message = "群聊显示长度不能大于1024")
    @Schema(description = "群公告")
    private String notice;

    @Length(max = 20, message = "显示昵称长度不能大于20")
    @Schema(description = "用户在群显示昵称")
    private String remarkNickName;

    @Schema(description = "群内显示名称")
    private String showNickName;

    @Schema(description = "群名显示名称")
    private String showGroupName;

    @Length(max = 20, message = "群备注长度不能大于20")
    @Schema(description = "群名备注")
    private String remarkGroupName;

    @Schema(description = "是否已解散")
    private Boolean dissolve;

    @Schema(description = "是否已退出")
    private Boolean quit;


}

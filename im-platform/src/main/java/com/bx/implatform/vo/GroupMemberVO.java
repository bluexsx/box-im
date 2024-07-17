package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "群成员信息VO")
public class GroupMemberVO {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "群内显示名称")
    private String aliasName;

    @Schema(description = "头像")
    private String headImage;

    @Schema(description = "是否已退出")
    private Boolean quit;

    @Schema(description = "是否在线")
    private Boolean online;

    @Schema(description = "备注")
    private String remark;

}

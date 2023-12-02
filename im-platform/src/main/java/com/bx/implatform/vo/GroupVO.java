package com.bx.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("群信息VO")
public class GroupVO {

    @ApiModelProperty(value = "群id")
    private Long id;

    @Length(max = 20, message = "群名称长度不能大于20")
    @NotEmpty(message = "群名称不可为空")
    @ApiModelProperty(value = "群名称")
    private String name;

    @ApiModelProperty(value = "群主id")
    private Long ownerId;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;

    @Length(max = 1024, message = "群聊显示长度不能大于1024")
    @ApiModelProperty(value = "群公告")
    private String notice;

    @Length(max = 20, message = "群聊显示长度不能大于20")
    @ApiModelProperty(value = "用户在群显示昵称")
    private String aliasName;

    @Length(max = 20, message = "群聊显示长度不能大于20")
    @ApiModelProperty(value = "群聊显示备注")
    private String remark;

}

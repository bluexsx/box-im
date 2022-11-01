package com.lx.implatform.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel("群信息VO")
public class GroupVO {

    @NotNull(message = "群id不可为空")
    @ApiModelProperty(value = "群id")
    private Long id;

    @NotEmpty(message = "群名称不可为空")
    @ApiModelProperty(value = "群名称")
    private String name;

    @NotNull(message = "群主id不可为空")
    @ApiModelProperty(value = "群主id")
    private Long ownerId;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;

    @ApiModelProperty(value = "群公告")
    private String notice;

    @ApiModelProperty(value = "用户在群显示昵称")
    private String aliasName;

    @ApiModelProperty(value = "群聊显示备注")
    private String remark;

}

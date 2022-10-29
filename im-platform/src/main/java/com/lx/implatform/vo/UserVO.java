package com.lx.implatform.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("用户信息VO")
public class UserVO {

    @NotNull(message = "用户id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    @NotEmpty(message = "用户名不能为空")
    @Length(max = 64,message = "用户名不能大于64字符")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @NotEmpty(message = "用户昵称不能为空")
    @Length(max = 64,message = "昵称不能大于64字符")
    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "性别")
    private Integer sex;

    @Length(max = 64,message = "个性签名不能大于1024个字符")
    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;


    @ApiModelProperty(value = "是否在线")
    private Boolean online;

}

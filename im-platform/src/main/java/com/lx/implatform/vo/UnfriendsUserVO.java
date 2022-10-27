package com.lx.implatform.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("非好友用户信息VO")
public class UnfriendsUserVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "是否已是好友")
    private Boolean isFriend;

}

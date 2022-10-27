package com.lx.implatform.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("好友信息VO")
public class FriendsVO {

    @ApiModelProperty(value = "好友id")
    private Long friendId;


    @ApiModelProperty(value = "用户昵称")
    private String friendNickName;


    @ApiModelProperty(value = "用户头像")
    private String friendHeadImage;
}

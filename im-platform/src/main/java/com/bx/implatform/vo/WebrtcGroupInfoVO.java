package com.bx.implatform.vo;

import com.bx.implatform.session.WebrtcUserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author: Blue
 * @date: 2024-06-09
 * @version: 1.0
 */
@Data
@ApiModel("群通话信息VO")
public class WebrtcGroupInfoVO {


    @ApiModelProperty(value = "是否在通话中")
    private Boolean isChating;

    @ApiModelProperty(value = "通话发起人")
    WebrtcUserInfo host;

    @ApiModelProperty(value = "通话用户列表")
    private List<WebrtcUserInfo> userInfos;
}

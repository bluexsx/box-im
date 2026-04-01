package com.bx.imcommon.model;

import lombok.Data;

@Data
public class IMLoginInfo {

    /**
     * 登陆token
     */
    private String accessToken;

    /**
     * 设备id
     */
    private String devId;
}

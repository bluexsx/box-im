package com.bx.imcommon.model;

import lombok.Data;

/**
 * 强制下线推送给客户端的数据
 */
@Data
public class IMForceLogoutData {

    /**
     * 下线类型，见 {@link com.bx.imcommon.enums.IMForceLogoutType}
     */
    private Integer type;

    /**
     * 原因说明（封禁时由管理端传入）
     */
    private String reason;

}

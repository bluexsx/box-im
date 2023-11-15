package com.bx.imcommon.model;

import lombok.Data;

@Data
public class IMSessionInfo {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 终端类型
     */
    private Integer terminal;

}

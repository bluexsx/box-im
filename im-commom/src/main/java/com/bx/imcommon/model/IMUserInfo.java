package com.bx.imcommon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Blue
 * @date: 2023-09-24 09:23:11
 * @version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IMUserInfo {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户终端类型 IMTerminalType
     */
    private Integer terminal;


}

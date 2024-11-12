package com.bx.implatform.session;

import com.bx.imcommon.model.IMSessionInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSession extends IMSessionInfo {

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;
}

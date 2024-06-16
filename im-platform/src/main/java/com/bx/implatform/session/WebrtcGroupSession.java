package com.bx.implatform.session;

import com.bx.imcommon.model.IMUserInfo;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: Blue
 * @date: 2024-06-01
 * @version: 1.0
 */
@Data
public class WebrtcGroupSession {

    /**
     *  通话发起者
     */
    private IMUserInfo host;

    /**
     * 所有被邀请的用户列表
     */
    private List<WebrtcUserInfo> userInfos;

    /**
     * 已经进入通话的用户列表
     */
    private List<IMUserInfo> inChatUsers = new LinkedList<>();


}

package com.bx.implatform.session;

import lombok.Data;

/**
 * 离线通知session
 * @author: blue
 * @date: 2024-07-06
 * @version: 1.0
 */
@Data
public class NotifySession {
    /**
     *  接收方客户端id
     */
    private String cid;

    /**
     *  通知id
     */
    private Integer notifyId;

    /**
     *  发送方用户名
     */
    private String sendNickName;

    /**
     *  标题
     */
    private String title;

    /**
     *  显示图标
     */
    private String logo;

    /**
     *  消息数量
     */
    private Integer count;

}

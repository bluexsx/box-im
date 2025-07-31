package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 群消息
 * </p>
 *
 * @author blue
 * @since 2022-10-31
 */
@Data

@TableName("im_group_message")
public class GroupMessage {

    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 临时id,由前端生成
     * 作用:如果用户正在发送消息时掉线了，可以通过此字段获取该消息的实际发送状态
     */
    private String tmpId;

    /**
     * 群id
     */
    private Long groupId;

    /**
     * 发送用户id
     */
    private Long sendId;

    /**
     * 发送用户昵称
     */
    private String sendNickName;

    /**
     * 接受用户id,为空表示全体发送
     */
    private String recvIds;

    /**
     * @用户列表
     */
    private String atUserIds;
    /**
     * 发送内容
     */
    private String content;

    /**
     * 消息类型 MessageType
     */
    private Integer type;

    /**
     *  是否回执消息
     */
    private Boolean receipt;

    /**
     *  回执消息是否完成
     */
    private Boolean receiptOk;

    /**
     * 状态 MessageStatus
     */
    private Integer status;

    /**
     * 发送时间
     */
    private Date sendTime;

}

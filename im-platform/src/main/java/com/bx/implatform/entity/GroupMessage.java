package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
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
@EqualsAndHashCode(callSuper = false)
@TableName("im_group_message")
public class GroupMessage extends Model<GroupMessage> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 发送用户id
     */
    @TableField("send_id")
    private Long sendId;

    /**
     * 发送用户昵称
     */
    @TableField("send_nick_name")
    private String sendNickName;

    /**
     * @用户列表
     */
    @TableField("at_user_ids")
    private String atUserIds;
    /**
     * 发送内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型 MessageType
     */
    @TableField("type")
    private Integer type;

    /**
     *  是否回执消息
     */
    @TableField("receipt")
    private Boolean receipt;

    /**
     *  回执消息是否完成
     */
    @TableField("receipt_ok")
    private Boolean receiptOk;

    /**
     * 状态 MessageStatus
     */
    @TableField("status")
    private Integer status;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private Date sendTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}

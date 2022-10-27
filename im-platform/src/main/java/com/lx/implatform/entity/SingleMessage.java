package com.lx.implatform.entity;

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
 * 
 * </p>
 *
 * @author blue
 * @since 2022-10-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("single_message")
public class SingleMessage extends Model<SingleMessage> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送用户id
     */
    @TableField("send_user_id")
    private Long sendUserId;

    /**
     * 接收用户id
     */
    @TableField("recv_user_id")
    private Long recvUserId;

    /**
     * 发送内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型
     */
    @TableField("type")
    private Integer type;

    /**
     * 状态
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

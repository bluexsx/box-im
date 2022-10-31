package com.lx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
@TableName("group_message")
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
    @TableField("send_user_id")
    private Long sendUserId;

    /**
     * 发送内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型 0:文字 1:图片 2:文件
     */
    @TableField("type")
    private Boolean type;

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

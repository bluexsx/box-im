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
 *
 * </p>
 *
 * @author blue
 * @since 2022-10-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_friend")
public class Friend extends Model<Friend> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 好友id
     */
    @TableField("friend_id")
    private Long friendId;

    /**
     * 用户昵称
     */
    @TableField("friend_nick_name")
    private String friendNickName;

    /**
     * 用户头像
     */
    @TableField("friend_head_image")
    private String friendHeadImage;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}

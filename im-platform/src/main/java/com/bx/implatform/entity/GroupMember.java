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
 * 群成员
 * </p>
 *
 * @author blue
 * @since 2022-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group_member")
public class GroupMember extends Model<GroupMember> {

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
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 群内显示名称
     */
    @TableField("alias_name")
    private String aliasName;

    /**
     * 头像
     */
    @TableField("head_image")
    private String headImage;


    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否已离开群聊
     */
    @TableField("quit")
    private Boolean quit;

    /**
     * 退群时间
     */
    @TableField("quit_time")
    private Date quitTime;

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

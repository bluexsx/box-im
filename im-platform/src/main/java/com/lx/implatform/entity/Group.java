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
 * 群
 * @author blue
 * @since 2022-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group")
public class Group extends Model<Group> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群名字
     */
    @TableField("name")
    private String name;

    /**
     * 群主id
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 头像
     */
    @TableField("head_image")
    private String headImage;

    /**
     * 头像缩略图
     */
    @TableField("head_image_thumb")
    private String headImageThumb;

    /**
     * 群公告
     */
    @TableField("notice")
    private String notice;

    /**
     * 是否已删除
     */
    @TableField("deleted")
    private Boolean deleted;

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

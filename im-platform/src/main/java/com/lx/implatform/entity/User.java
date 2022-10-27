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
@TableName("user")
public class User extends Model<User> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户名
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 用户名
     */
    @TableField("head_image")
    private String headImage;

    /**
     * 密码(明文)
     */
    @TableField("password")
    private String password;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private Date lastLoginTime;

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

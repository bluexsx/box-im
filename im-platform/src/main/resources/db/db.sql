use `lx-im`;
create table `im_user`(
    `id` bigint not null auto_increment primary key  comment 'id',
    `user_name` varchar(255) not null comment '用户名',
    `nick_name` varchar(255) not null comment '用户昵称',
    `head_image` varchar(255) default '' comment '用户头像',
    `head_image_thumb` varchar(255) default '' comment '用户头像缩略图',
    `password` varchar(255) not null comment '密码(明文)',
    `sex`  tinyint(1) default 0 comment '性别 0:男 1::女',
    `signature` varchar(1024) default '' comment '个性签名',
    `last_login_time`  datetime DEFAULT null comment '最后登录时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    unique key `idx_user_name`(user_name),
    key `idx_nick_name`(nick_name)
) ENGINE=InnoDB CHARSET=utf8mb3 comment '用户';

create table `im_friend`(
    `id` bigint not null auto_increment primary key  comment 'id',
    `user_id` bigint not null  comment '用户id',
    `friend_id` bigint not null  comment '好友id',
    `friend_nick_name` varchar(255) not null comment '好友昵称',
    `friend_head_image` varchar(255) default '' comment '好友头像',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    key `idx_user_id` (`user_id`),
    key `idx_friend_id` (`friend_id`)
) ENGINE=InnoDB CHARSET=utf8mb3 comment '好友';

create table `im_private_message`(
    `id` bigint not null auto_increment primary key comment 'id',
    `send_id` bigint not null  comment '发送用户id',
    `recv_id` bigint not null  comment '接收用户id',
    `content` text   comment '发送内容',
    `type`  tinyint(1) NOT NULL  comment '消息类型 0:文字 1:图片 2:文件',
    `status` tinyint(1) NOT NULL   comment '状态 0:未读 1:已读 ',
    `send_time` datetime DEFAULT CURRENT_TIMESTAMP comment '发送时间',
    key `idx_send_recv_user_id` (`send_user_id`,`recv_user_id`)
)ENGINE=InnoDB CHARSET=utf8mb3 comment '私聊消息';


create table `im_group`(
    `id` bigint not null auto_increment primary key comment 'id',
    `name` varchar(255) not null comment '群名字',
    `owner_id` bigint not null  comment '群主id',
    `head_image` varchar(255) default '' comment '群头像',
    `head_image_thumb` varchar(255) default '' comment '群头像缩略图',
    `notice` varchar(1024)  default '' comment '群公告',
    `remark` varchar(255) default '' comment '群备注',
    `deleted` tinyint(1) DEFAULT 0   comment '是否已删除',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间'
)ENGINE=InnoDB CHARSET=utf8mb3 comment '群';

create table `im_group_member`(
    `id` bigint not null auto_increment primary key comment 'id',
    `group_id` bigint not null  comment '群id',
    `user_id` bigint not null  comment '用户id',
    `alias_name` varchar(255) DEFAULT '' comment '组内显示名称',
    `head_image` varchar(255) default '' comment '用户头像',
    `remark` varchar(255) DEFAULT '' comment '备注',
    `quit` tinyint(1) DEFAULT 0  comment '是否已退出',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    key `idx_group_id`(`group_id`),
    key `idx_user_id`(`user_id`)
)ENGINE=InnoDB CHARSET=utf8mb3 comment '群成员';

create table `im_group_message`(
    `id` bigint not null auto_increment primary key comment 'id',
    `group_id` bigint not null  comment '群id',
    `send_id` bigint not null  comment '发送用户id',
    `content` text   comment '发送内容',
    `type`  tinyint(1) NOT NULL  comment '消息类型 0:文字 1:图片 2:文件',
    `send_time` datetime DEFAULT CURRENT_TIMESTAMP comment '发送时间',
    key `idx_group_id` (group_id)
)ENGINE=InnoDB CHARSET=utf8mb3 comment '群消息';


create table `im_group_message_read_pos`(
     `id` bigint not null auto_increment primary key comment 'id',
     `group_id` bigint not null  comment '群id',
     `user_id` bigint not null  comment '用户id',
     `read_pos` bigint default 0  comment '已读取消息的最大消息id',
     `last_read_time` datetime DEFAULT CURRENT_TIMESTAMP comment '最后读取时间',
     key `idx_user_id`(`user_id`)
)ENGINE=InnoDB CHARSET=utf8mb3 comment '群消息读取位置';
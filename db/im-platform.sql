create table `im_user`
(
    `id`               bigint       not null auto_increment primary key comment 'id',
    `user_name`        varchar(255) not null comment '用户名',
    `nick_name`        varchar(255) not null comment '用户昵称',
    `head_image`       varchar(255)  default '' comment '用户头像',
    `head_image_thumb` varchar(255)  default '' comment '用户头像缩略图',
    `password`         varchar(255) not null comment '密码',
    `sex`              tinyint       default 0 comment '性别 0:男 1:女',
    `is_banned`        tinyint(1) default 0 comment '是否被封禁 0:否 1:是',
    `reason`           varchar(255)  default '' comment '被封禁原因',
    `type`             smallint      default 1 comment '用户类型 1:普通用户 2:审核账户',
    `signature`        varchar(1024) default '' comment '个性签名',
    `last_login_time`  datetime      default null comment '最后登录时间',
    `created_time`     datetime      default current_timestamp comment '创建时间',
    unique key `idx_user_name` (user_name),
    key                `idx_nick_name` (nick_name)
) engine = innodb charset = utf8mb4 comment '用户';

create table `im_friend`
(
    `id`                bigint       not null auto_increment primary key comment 'id',
    `user_id`           bigint       not null comment '用户id',
    `friend_id`         bigint       not null comment '好友id',
    `friend_nick_name`  varchar(255) not null comment '好友昵称',
    `friend_head_image` varchar(255) default '' comment '好友头像',
    `is_dnd`            tinyint      default 0 comment '免打扰标识(do not disturb)  0:关闭   1:开启',
    `is_top`            tinyint(1)   default 0 comment '是否置顶会话',
    `deleted`           tinyint      default 0 comment '删除标识  0：正常   1：已删除',
    `created_time`      datetime     default current_timestamp comment '创建时间',
    `version`           BIGINT       default 0 comment '版本号',
    UNIQUE KEY `idx_user_friend_id` (`user_id`, `friend_id`),
    key                 `idx_friend_id` (`friend_id`)
) engine = innodb charset = utf8mb4 comment '好友';

create table `im_private_message`
(
    `id`        bigint      not null auto_increment primary key comment 'id',
    `local_id`  varchar(32) comment '业务id,由前端生成',
    `seq_no`    int         not null comment '序列号,单个会话消息的序号连续递增',
    `send_id`   bigint      not null comment '发送用户id',
    `recv_id`   bigint      not null comment '接收用户id',
    `conv_key`  varchar(64) not null comment '会话key，格式:userId1_userId2',
    `content`   text character set utf8mb4 comment '发送内容',
    `type`      tinyint     not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示',
    `status`    tinyint     not null comment '状态 0:未读 1:已发送 2:撤回 3:已读',
    `send_time` datetime(3) default current_timestamp(3) comment '发送时间',
    key         `idx_conv_key_seq_no`(`conv_key`,`seq_no`),
    key         `idx_send_recv_id` (`send_id`, `recv_id`,`id`),
    key         `idx_recv_id` (`recv_id`)
) engine = innodb charset = utf8mb4 comment '私聊消息';

create table `im_group`
(
    `id`               bigint       not null auto_increment primary key comment 'id',
    `name`             varchar(255) not null comment '群名字',
    `owner_id`         bigint       not null comment '群主id',
    `head_image`       varchar(255)  default '' comment '群头像',
    `head_image_thumb` varchar(255)  default '' comment '群头像缩略图',
    `notice`           varchar(1024) default '' comment '群公告',
    `is_banned`        tinyint(1) default 0 comment '是否被封禁 0:否 1:是',
    `reason`           varchar(255)  default '' comment '被封禁原因',
    `dissolve`         tinyint(1) default 0 comment '是否已解散',
    `created_time`     datetime      default current_timestamp comment '创建时间'
) engine = innodb charset = utf8mb4 comment '群';

create table `im_group_member`
(
    `id`                bigint not null auto_increment primary key comment 'id',
    `group_id`          bigint not null comment '群id',
    `user_id`           bigint not null comment '用户id',
    `user_nick_name`    varchar(255) default '' comment '用户昵称',
    `remark_nick_name`  varchar(255) default '' comment '显示昵称备注',
    `head_image`        varchar(255) default '' comment '用户头像',
    `remark_group_name` varchar(255) default '' comment '显示群名备注',
    `is_dnd`            tinyint(1) comment '免打扰标识(do not disturb)  0:关闭   1:开启',
    `is_top`            tinyint(1) default 0 comment '是否置顶会话',
    `quit`              tinyint(1) default 0 comment '是否已退出',
    `quit_time`         datetime     default null comment '退出时间',
    `created_time`      datetime     default current_timestamp comment '创建时间',
    `version`           bigint       default 0 comment '版本号',
    key                 `idx_group_id` (`group_id`),
    key                 `idx_user_id` (`user_id`)
) engine = innodb charset = utf8mb4 comment '群成员';

create table `im_group_message`
(
    `id`             bigint  not null auto_increment primary key comment 'id',
    `local_id`       varchar(32) comment '业务id,由前端生成',
    `group_id`       bigint  not null comment '群id',
    `seq_no`         int     not null comment '序列号,单个会话消息的序号连续递增',
    `send_id`        bigint  not null comment '发送用户id',
    `send_nick_name` varchar(255) default '' comment '发送用户昵称',
    `content`        text character set utf8mb4 comment '发送内容',
    `at_user_ids`    varchar(1024) comment '被@的用户id列表，逗号分隔',
    `receipt`        tinyint(1) default 0 comment '是否回执消息',
    `receipt_ok`     tinyint(1) default 0 comment '回执消息是否完成',
    `type`           tinyint not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示',
    `status`         tinyint      default 0 comment '状态 0:未发出  2:撤回 ',
    `send_time`      datetime(3) default current_timestamp(3) comment '发送时间',
    key              `idx_group_id_seq_no` (`group_id`,`seq_no`),
    key              `idx_send_time` (`send_time`)
) engine = innodb charset = utf8mb4 comment '群消息';

create table `im_sensitive_word`
(
    `id`          bigint      not null auto_increment primary key comment 'id',
    `content`     varchar(64) not null comment '敏感词内容',
    `enabled`     tinyint(1) default 0 comment '是否启用 0:未启用 1:启用',
    `creator`     bigint   default null comment '创建者',
    `create_time` datetime default current_timestamp comment '创建时间'
) engine = innodb charset = utf8mb4 comment '敏感词';

create table `im_file_info`
(
    `id`              bigint       not null auto_increment primary key comment 'id',
    `file_name`       varchar(255) not null comment '文件名',
    `file_path`       varchar(255) not null comment '文件地址',
    `file_size`       integer      not null comment '文件大小',
    `file_type`       tinyint      not null comment '0:普通文件 1:图片 2:视频',
    `compressed_path` varchar(255) default null comment '压缩文件路径',
    `cover_path`      varchar(255) default null comment '封面文件路径，仅视频文件有效',
    `upload_time`     datetime     default current_timestamp comment '上传时间',
    `is_permanent`    tinyint(1) default 0 comment '是否永久文件',
    `md5`             varchar(64)  not null comment '文件md5',
    key               `idx_md5` (md5)
) engine = innodb charset = utf8mb4 comment '文件';

create table `im_message_deletion`
(
    `id`          bigint  not null auto_increment primary key comment 'id',
    `user_id`     bigint  not null comment '用户id',
    `chat_type`   tinyint not null comment '会话类型 1:私聊 2:群聊',
    `chat_id`     bigint  not null comment '好友id、群聊id',
    `message_id`  bigint(20) comment '消息id',
    `delete_type` tinyint not null comment '删除类型 1:按消息删除 2:按会话删除',
    `delete_time` datetime default current_timestamp comment '消息删除时间',
    key           `idx_user_id` (`user_id`)
) engine = innodb charset = utf8mb4 comment '消息删除记录';
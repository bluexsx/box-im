package com.bx.implatform.contant;

public final class Constant {

    /**
     * 系统用户id
     */
    public static final Long SYS_USER_ID = 0L;
    /**
     * 最大图片上传大小
     */
    public static final Long MAX_IMAGE_SIZE = 20 * 1024 * 1024L;
    /**
     * 最大上传文件大小
     */
    public static final Long MAX_FILE_SIZE = 20 * 1024 * 1024L;

    /**
     * 最大文件名长度
     */
    public static final Long MAX_FILE_NAME_LENGTH = 128L;

    /**
     * 大群人数上限
     */
    public static final Long MAX_GROUP_MEMBER = 3000L;


    /**
     * 文字消息最大长度
     */
    public static final Long MAX_MESSAGE_LENGTH = 1024L;

    /**
     * 离线消息最大拉取时间(天)
     */
    public static final Long MAX_OFFLINE_MESSAGE_DAYS = 60L;

    /**
     * 离线消息最大拉取数量
     */
    public static final Long MAX_OFFLINE_MESSAGE_SIZE = 10000L;

}

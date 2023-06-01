package com.bx.implatform.contant;


public class Constant {
    // 最大图片上传大小
    public static final long MAX_IMAGE_SIZE = 5*1024*1024;
    // 最大上传文件大小
    public static final long MAX_FILE_SIZE = 10*1024*1024;
    // 群聊最大人数
    public static final long MAX_GROUP_MEMBER = 500;
    // accessToken 过期时间(1小时)
    public static final Integer ACCESS_TOKEN_EXPIRE = 60 * 60;
    // refreshToken 过期时间(7天)
    public static final Integer REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60 ;
    // accessToken 加密秘钥
    public static final String ACCESS_TOKEN_SECRET = "MIIBIjANBgkq";
    // refreshToken 加密秘钥
    public static final String REFRESH_TOKEN_SECRET = "IKDiqVmn0VFU";

}

package com.bx.implatform.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件存储配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 存储厂商：minio | aliyun | tencent | huawei | qiniu
     */
    private String platform = "minio";

    /**
     * 对外访问根地址（拼文件 URL）
     */
    private String domain;

    /**
     * SDK 连接地址（MinIO 内网 / 阿里云 OSS / 华为云 OBS endpoint）
     */
    private String endpoint;

    /**
     * AccessKey / AccessKeyId / SecretId
     */
    private String accessKey;

    /**
     * SecretKey / AccessKeySecret
     */
    private String secretKey;

    /**
     * 桶名称（腾讯云格式一般为 bucket-appid）
     */
    private String bucketName;

    /**
     * 图片目录
     */
    private String imagePath;

    /**
     * 文件目录
     */
    private String filePath;

    /**
     * 视频目录
     */
    private String videoPath;

    /**
     * 非永久文件过期天数
     */
    private Integer expireIn;

    /**
     * 地域：腾讯云如 ap-guangzhou；七牛如 huadong/huabei/huanan/auto
     */
    private String region;

}

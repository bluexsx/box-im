package com.bx.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoragePlatformType {

    /**
     * MinIO
     */
    MINIO("minio", "MinIO"),
    /**
     * 阿里云 OSS
     */
    ALIYUN("aliyun", "阿里云OSS"),
    /**
     * 腾讯云 COS
     */
    TENCENT("tencent", "腾讯云COS"),
    /**
     * 华为云 OBS
     */
    HUAWEI("huawei", "华为云OBS"),
    /**
     * 七牛云 Kodo
     */
    QINIU("qiniu", "七牛云"),
    /**
     * 未知
     */
    UNKNOWN("unknown", "未知");

    private final String code;

    private final String desc;

    public static StoragePlatformType fromCode(String code) {
        for (StoragePlatformType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return StoragePlatformType.UNKNOWN;
    }

}

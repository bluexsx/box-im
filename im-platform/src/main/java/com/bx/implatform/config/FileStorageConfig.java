package com.bx.implatform.config;

import com.bx.implatform.config.props.StorageProperties;
import com.bx.implatform.enums.StoragePlatformType;
import com.bx.implatform.thirdparty.storage.AliyunOssFileStorageAPI;
import com.bx.implatform.thirdparty.storage.FileStorageAPI;
import com.bx.implatform.thirdparty.storage.HuaweiObsFileStorageAPI;
import com.bx.implatform.thirdparty.storage.MinioFileStorageAPI;
import com.bx.implatform.thirdparty.storage.QiniuFileStorageAPI;
import com.bx.implatform.thirdparty.storage.TencentCosFileStorageAPI;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 */
@Configuration
@RequiredArgsConstructor
public class FileStorageConfig {

    private final StorageProperties props;

    @Bean
    FileStorageAPI fileStorageAPI() {
        if (StringUtils.isAnyBlank(props.getAccessKey(), props.getSecretKey(), props.getBucketName(),
            props.getDomain())) {
            throw new IllegalStateException("storage 配置不完整，请检查 access-key、secret-key、bucket-name、domain");
        }
        StoragePlatformType platform = StoragePlatformType.fromCode(props.getPlatform());
        return switch (platform) {
            case MINIO -> {
                if (StringUtils.isBlank(props.getEndpoint())) {
                    throw new IllegalStateException("storage.platform=minio 时请配置 endpoint");
                }
                yield new MinioFileStorageAPI(props);
            }
            case ALIYUN -> {
                if (StringUtils.isBlank(props.getEndpoint())) {
                    throw new IllegalStateException("storage.platform=aliyun 时请配置 endpoint");
                }
                yield new AliyunOssFileStorageAPI(props);
            }
            case TENCENT -> {
                if (StringUtils.isBlank(props.getRegion())) {
                    throw new IllegalStateException("storage.platform=tencent 时请配置 region");
                }
                yield new TencentCosFileStorageAPI(props);
            }
            case HUAWEI -> {
                if (StringUtils.isBlank(props.getEndpoint())) {
                    throw new IllegalStateException("storage.platform=huawei 时请配置 endpoint");
                }
                yield new HuaweiObsFileStorageAPI(props);
            }
            case QINIU -> new QiniuFileStorageAPI(props);
            default -> throw new IllegalStateException(
                "storage.platform 配置无效，支持 minio、aliyun、tencent、huawei、qiniu");
        };
    }

}

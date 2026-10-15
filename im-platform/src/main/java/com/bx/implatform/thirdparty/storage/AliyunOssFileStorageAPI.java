package com.bx.implatform.thirdparty.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.bx.implatform.config.props.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 阿里云 OSS 存储实现
 */
@Slf4j
public class AliyunOssFileStorageAPI extends AbstractFileStorageAPI {

    private final OSS ossClient;

    public AliyunOssFileStorageAPI(StorageProperties props) {
        super(props);
        this.ossClient = new OSSClientBuilder().build(props.getEndpoint(), props.getAccessKey(), props.getSecretKey());
    }

    @Override
    public void ensureBucket() {
        String bucketName = props.getBucketName();
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                log.warn("阿里云 OSS 桶不存在，请先在控制台创建: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查阿里云 OSS 桶失败, bucket:{}", bucketName, e);
        }
    }

    @Override
    public String upload(String path, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new RuntimeException("文件名为空");
        }
        String objectName = buildObjectName(originalFilename);
        String objectKey = fullObjectKey(path, objectName);
        try (InputStream stream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (StringUtils.isNotBlank(file.getContentType())) {
                metadata.setContentType(file.getContentType());
            }
            ossClient.putObject(new PutObjectRequest(props.getBucketName(), objectKey, stream, metadata));
            return objectName;
        } catch (Exception e) {
            log.error("阿里云 OSS 上传文件失败", e);
            return null;
        }
    }

    @Override
    public String upload(String path, String name, byte[] data, String contentType) {
        String objectName = buildObjectName(name);
        String objectKey = fullObjectKey(path, objectName);
        try (InputStream stream = new ByteArrayInputStream(data)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            if (StringUtils.isNotBlank(contentType)) {
                metadata.setContentType(contentType);
            }
            ossClient.putObject(new PutObjectRequest(props.getBucketName(), objectKey, stream, metadata));
            return objectName;
        } catch (Exception e) {
            log.error("阿里云 OSS 上传文件失败", e);
            return null;
        }
    }

    @Override
    public boolean remove(String objectKey) {
        try {
            ossClient.deleteObject(props.getBucketName(), objectKey);
            return true;
        } catch (Exception e) {
            log.warn("阿里云 OSS 删除文件失败, objectKey:{}", objectKey, e);
            return false;
        }
    }

    @Override
    public boolean isExist(String objectKey) {
        try {
            return ossClient.doesObjectExist(props.getBucketName(), objectKey);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public byte[] getObject(String objectKey) {
        try (InputStream stream = ossClient.getObject(props.getBucketName(), objectKey).getObjectContent()) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.warn("阿里云 OSS 下载文件失败, objectKey:{}", objectKey, e);
            return null;
        }
    }

    @Override
    public String resolveObjectKey(String fileUrl) {
        return stripDomain(fileUrl);
    }

    @Override
    public String getFileUrl(String path, String objectName) {
        return buildVirtualHostUrl(path, objectName);
    }

}

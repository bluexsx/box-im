package com.bx.implatform.thirdparty.storage;

import com.bx.implatform.config.props.StorageProperties;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * MinIO 存储实现
 */
@Slf4j
public class MinioFileStorageAPI extends AbstractFileStorageAPI {

    private final MinioClient minioClient;

    public MinioFileStorageAPI(StorageProperties props) {
        super(props);
        this.minioClient = MinioClient.builder()
            .endpoint(props.getEndpoint())
            .credentials(props.getAccessKey(), props.getSecretKey())
            .build();
    }

    @Override
    public void ensureBucket() {
        String bucketName = props.getBucketName();
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                setBucketPublic(bucketName);
            }
        } catch (Exception e) {
            log.error("初始化 MinIO bucket 失败, bucket:{}", bucketName, e);
        }
    }

    private void setBucketPublic(String bucketName) {
        try {
            String sb = "{\"Version\":\"2012-10-17\","
                + "\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(sb).build());
        } catch (Exception e) {
            log.error("设置 MinIO bucket 公开只读策略失败, bucket:{}", bucketName, e);
        }
    }

    @Override
    public String upload(String path, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new RuntimeException("文件名为空");
        }
        String objectName = buildObjectName(originalFilename);
        try (InputStream stream = file.getInputStream()) {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                .bucket(props.getBucketName())
                .object(fullObjectKey(path, objectName))
                .stream(stream, file.getSize(), -1)
                .contentType(resolveContentType(originalFilename, file.getContentType()))
                .build();
            minioClient.putObject(objectArgs);
            return objectName;
        } catch (Exception e) {
            log.error("MinIO 上传文件失败", e);
            return null;
        }
    }

    @Override
    public String upload(String path, String name, byte[] data, String contentType) {
        String objectName = buildObjectName(name);
        try (InputStream stream = new ByteArrayInputStream(data)) {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                .bucket(props.getBucketName())
                .object(fullObjectKey(path, objectName))
                .stream(stream, data.length, -1)
                .contentType(resolveContentType(name, contentType))
                .build();
            minioClient.putObject(objectArgs);
            return objectName;
        } catch (Exception e) {
            log.error("MinIO 上传文件失败", e);
            return null;
        }
    }

    @Override
    public boolean remove(String objectKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(props.getBucketName()).object(objectKey).build());
            return true;
        } catch (Exception e) {
            log.warn("MinIO 删除文件失败, objectKey:{}", objectKey, e);
            return false;
        }
    }

    @Override
    public boolean isExist(String objectKey) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder().bucket(props.getBucketName()).object(objectKey).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public byte[] getObject(String objectKey) {
        try (InputStream stream = minioClient.getObject(
            GetObjectArgs.builder().bucket(props.getBucketName()).object(objectKey).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.warn("MinIO 下载文件失败, objectKey:{}", objectKey, e);
            return null;
        }
    }

    @Override
    public String resolveObjectKey(String fileUrl) {
        String marker = "/" + props.getBucketName() + "/";
        int idx = fileUrl.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        return fileUrl.substring(idx + marker.length());
    }

    @Override
    public String getFileUrl(String path, String objectName) {
        return buildPathStyleUrl(path, objectName);
    }

}

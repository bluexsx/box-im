package com.bx.implatform.thirdparty.storage;

import com.bx.implatform.config.props.StorageProperties;
import com.obs.services.ObsClient;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 华为云 OBS 存储实现
 */
@Slf4j
public class HuaweiObsFileStorageAPI extends AbstractFileStorageAPI {

    private final ObsClient obsClient;

    public HuaweiObsFileStorageAPI(StorageProperties props) {
        super(props);
        this.obsClient = new ObsClient(props.getAccessKey(), props.getSecretKey(), props.getEndpoint());
    }

    @Override
    public void ensureBucket() {
        String bucketName = props.getBucketName();
        try {
            if (!obsClient.headBucket(bucketName)) {
                log.warn("华为云 OBS 桶不存在，请先在控制台创建: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查华为云 OBS 桶失败, bucket:{}", bucketName, e);
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
            PutObjectRequest request = new PutObjectRequest(props.getBucketName(), objectKey, stream);
            if (StringUtils.isNotBlank(file.getContentType())) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                request.setMetadata(metadata);
            }
            obsClient.putObject(request);
            return objectName;
        } catch (Exception e) {
            log.error("华为云 OBS 上传文件失败", e);
            return null;
        }
    }

    @Override
    public String upload(String path, String name, byte[] data, String contentType) {
        String objectName = buildObjectName(name);
        String objectKey = fullObjectKey(path, objectName);
        try (InputStream stream = new ByteArrayInputStream(data)) {
            PutObjectRequest request = new PutObjectRequest(props.getBucketName(), objectKey, stream);
            if (StringUtils.isNotBlank(contentType)) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(contentType);
                request.setMetadata(metadata);
            }
            obsClient.putObject(request);
            return objectName;
        } catch (Exception e) {
            log.error("华为云 OBS 上传文件失败", e);
            return null;
        }
    }

    @Override
    public boolean remove(String objectKey) {
        try {
            obsClient.deleteObject(props.getBucketName(), objectKey);
            return true;
        } catch (Exception e) {
            log.warn("华为云 OBS 删除文件失败, objectKey:{}", objectKey, e);
            return false;
        }
    }

    @Override
    public boolean isExist(String objectKey) {
        try {
            return obsClient.doesObjectExist(props.getBucketName(), objectKey);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public byte[] getObject(String objectKey) {
        try {
            ObsObject obsObject = obsClient.getObject(props.getBucketName(), objectKey);
            try (InputStream stream = obsObject.getObjectContent()) {
                return stream.readAllBytes();
            }
        } catch (Exception e) {
            log.warn("华为云 OBS 下载文件失败, objectKey:{}", objectKey, e);
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

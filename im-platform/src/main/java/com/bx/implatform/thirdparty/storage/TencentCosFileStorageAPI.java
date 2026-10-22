package com.bx.implatform.thirdparty.storage;

import com.bx.implatform.config.props.StorageProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 腾讯云 COS 存储实现
 */
@Slf4j
public class TencentCosFileStorageAPI extends AbstractFileStorageAPI {

    private final COSClient cosClient;

    public TencentCosFileStorageAPI(StorageProperties props) {
        super(props);
        COSCredentials cred = new BasicCOSCredentials(props.getAccessKey(), props.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(props.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        this.cosClient = new COSClient(cred, clientConfig);
    }

    @Override
    public void ensureBucket() {
        String bucketName = props.getBucketName();
        try {
            if (!cosClient.doesBucketExist(bucketName)) {
                log.warn("腾讯云 COS 桶不存在，请先在控制台创建: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查腾讯云 COS 桶失败, bucket:{}", bucketName, e);
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
            metadata.setContentType(resolveContentType(originalFilename, file.getContentType()));
            cosClient.putObject(new PutObjectRequest(props.getBucketName(), objectKey, stream, metadata));
            return objectName;
        } catch (Exception e) {
            log.error("腾讯云 COS 上传文件失败", e);
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
            metadata.setContentType(resolveContentType(name, contentType));
            cosClient.putObject(new PutObjectRequest(props.getBucketName(), objectKey, stream, metadata));
            return objectName;
        } catch (Exception e) {
            log.error("腾讯云 COS 上传文件失败", e);
            return null;
        }
    }

    @Override
    public boolean remove(String objectKey) {
        try {
            cosClient.deleteObject(props.getBucketName(), objectKey);
            return true;
        } catch (Exception e) {
            log.warn("腾讯云 COS 删除文件失败, objectKey:{}", objectKey, e);
            return false;
        }
    }

    @Override
    public boolean isExist(String objectKey) {
        try {
            return cosClient.doesObjectExist(props.getBucketName(), objectKey);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public byte[] getObject(String objectKey) {
        try (InputStream stream = cosClient.getObject(props.getBucketName(), objectKey).getObjectContent()) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.warn("腾讯云 COS 下载文件失败, objectKey:{}", objectKey, e);
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

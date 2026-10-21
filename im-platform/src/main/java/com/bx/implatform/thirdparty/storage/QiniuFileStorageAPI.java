package com.bx.implatform.thirdparty.storage;

import cn.hutool.http.HttpUtil;
import com.bx.implatform.config.props.StorageProperties;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 七牛云 Kodo 存储实现
 */
@Slf4j
public class QiniuFileStorageAPI extends AbstractFileStorageAPI {

    private final Auth auth;
    private final UploadManager uploadManager;
    private final BucketManager bucketManager;

    public QiniuFileStorageAPI(StorageProperties props) {
        super(props);
        this.auth = Auth.create(props.getAccessKey(), props.getSecretKey());
        Configuration cfg = new Configuration(resolveRegion(props.getRegion()));
        this.uploadManager = new UploadManager(cfg);
        this.bucketManager = new BucketManager(auth, cfg);
    }

    private Region resolveRegion(String region) {
        if (StringUtils.isBlank(region) || "auto".equalsIgnoreCase(region)) {
            return Region.autoRegion();
        }
        return switch (region.toLowerCase()) {
            case "huadong", "z0", "region0" -> Region.huadong();
            case "huabei", "z1", "region1" -> Region.huabei();
            case "huanan", "z2", "region2" -> Region.huanan();
            case "beimei", "na0" -> Region.beimei();
            case "xinjiapo", "as0" -> Region.xinjiapo();
            default -> {
                log.warn("未知七牛 region:{}，将使用 autoRegion", region);
                yield Region.autoRegion();
            }
        };
    }

    @Override
    public void ensureBucket() {
        try {
            bucketManager.getBucketInfo(props.getBucketName());
        } catch (Exception e) {
            log.warn("七牛云空间不可用或无权访问，请确认 bucket 已创建: {}", props.getBucketName());
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
            String token = auth.uploadToken(props.getBucketName(), objectKey);
            Response response = uploadManager.put(stream, objectKey, token, null,
                resolveContentType(originalFilename, file.getContentType()));
            if (response == null || !response.isOK()) {
                log.error("七牛云上传失败, body:{}", response == null ? null : response.bodyString());
                return null;
            }
            return objectName;
        } catch (Exception e) {
            log.error("七牛云上传文件失败", e);
            return null;
        }
    }

    @Override
    public String upload(String path, String name, byte[] data, String contentType) {
        String objectName = buildObjectName(name);
        String objectKey = fullObjectKey(path, objectName);
        try {
            String token = auth.uploadToken(props.getBucketName(), objectKey);
            Response response = uploadManager.put(data, objectKey, token, null,
                resolveContentType(name, contentType), false);
            if (response == null || !response.isOK()) {
                log.error("七牛云上传失败, body:{}", response == null ? null : response.bodyString());
                return null;
            }
            return objectName;
        } catch (Exception e) {
            log.error("七牛云上传文件失败", e);
            return null;
        }
    }

    @Override
    public boolean remove(String objectKey) {
        try {
            bucketManager.delete(props.getBucketName(), objectKey);
            return true;
        } catch (QiniuException e) {
            if (e.response != null && e.response.statusCode == 612) {
                return true;
            }
            log.warn("七牛云删除文件失败, objectKey:{}", objectKey, e);
            return false;
        } catch (Exception e) {
            log.warn("七牛云删除文件失败, objectKey:{}", objectKey, e);
            return false;
        }
    }

    @Override
    public boolean isExist(String objectKey) {
        try {
            bucketManager.stat(props.getBucketName(), objectKey);
            return true;
        } catch (QiniuException e) {
            return false;
        }
    }

    @Override
    public byte[] getObject(String objectKey) {
        try {
            String url = joinDomain(objectKey);
            byte[] bytes = HttpUtil.downloadBytes(url);
            if (bytes == null || bytes.length == 0) {
                log.warn("七牛云下载文件为空, objectKey:{}", objectKey);
                return null;
            }
            return bytes;
        } catch (Exception e) {
            log.warn("七牛云下载文件失败, objectKey:{}", objectKey, e);
            return null;
        }
    }

    private String joinDomain(String objectKey) {
        String domain = props.getDomain();
        if (domain.endsWith("/")) {
            return domain + objectKey;
        }
        return domain + "/" + objectKey;
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

package com.bx.implatform.thirdparty.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件对象存储
 */
public interface FileStorageAPI {

    /**
     * 启动时确保桶可用
     */
    void ensureBucket();

    /**
     * 上传文件，返回 objectName（不含 path 前缀，如 20251009/xxx.jpg）
     */
    String upload(String path, MultipartFile file);

    /**
     * 上传字节，返回 objectName
     */
    String upload(String path, String name, byte[] data, String contentType);

    /**
     * 按完整 objectKey 删除（如 image/20251009/xxx.jpg）
     */
    boolean remove(String objectKey);

    /**
     * 判断文件是否存在
     */
    boolean isExist(String objectKey);

    /**
     * 下载文件内容
     */
    byte[] getObject(String objectKey);

    /**
     * 从访问 URL 解析 objectKey
     */
    String resolveObjectKey(String fileUrl);

    /**
     * 拼对外访问 URL
     *
     * @param path       目录前缀，如 image
     * @param objectName upload 返回的相对名
     */
    String getFileUrl(String path, String objectName);

}

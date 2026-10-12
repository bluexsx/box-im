package com.bx.implatform.thirdparty.storage;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.bx.implatform.config.props.StorageProperties;
import com.bx.implatform.util.DateTimeUtils;
import com.bx.implatform.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 对象命名与 URL 拼装公共逻辑
 */
public abstract class AbstractFileStorageAPI implements FileStorageAPI {

    protected final StorageProperties props;

    protected AbstractFileStorageAPI(StorageProperties props) {
        this.props = props;
    }

    protected String buildObjectName(String originalFilename) {
        String fileName = IdUtil.getSnowflakeNextIdStr() + "." + FileUtil.getFileExtension(originalFilename);
        return DateTimeUtils.getFormatDate(new Date(), DateTimeUtils.PARTDATEFORMAT) + "/" + fileName;
    }

    protected String fullObjectKey(String path, String objectName) {
        return path + "/" + objectName;
    }

    /**
     * MinIO path-style：domain/bucket/path/file
     */
    protected String buildPathStyleUrl(String path, String objectName) {
        return StrUtil.join("/", props.getDomain(), props.getBucketName(), path, objectName);
    }

    /**
     * 虚拟主机 / CDN：domain/path/file
     */
    protected String buildVirtualHostUrl(String path, String objectName) {
        return StrUtil.join("/", props.getDomain(), path, objectName);
    }

    /**
     * 从 URL 中截取 domain 之后的路径
     */
    protected String stripDomain(String fileUrl) {
        if (StringUtils.isBlank(fileUrl) || StringUtils.isBlank(props.getDomain())) {
            return null;
        }
        String domain = props.getDomain();
        int idx = fileUrl.indexOf(domain);
        if (idx < 0) {
            return null;
        }
        String rest = fileUrl.substring(idx + domain.length());
        if (rest.startsWith("/")) {
            rest = rest.substring(1);
        }
        return StringUtils.isBlank(rest) ? null : rest;
    }

}

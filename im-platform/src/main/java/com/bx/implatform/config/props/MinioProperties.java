package com.bx.implatform.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: Blue
 * @date: 2024-09-28
 * @version: 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String domain;

    private String bucketName;

    private String imagePath;

    private String filePath;

    private String videoPath;
}

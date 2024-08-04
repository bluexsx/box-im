package com.bx.implatform.service.thirdparty;

import com.bx.implatform.contant.Constant;
import com.bx.implatform.enums.FileType;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.util.FileUtil;
import com.bx.implatform.util.ImageUtil;
import com.bx.implatform.util.MinioUtil;
import com.bx.implatform.vo.UploadImageVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

/**
 * 通过校验文件MD5实现重复文件秒传
 * 文件上传服务
 *
 * @author Blue
 * @date 2022/10/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioUtil minioUtil;
    @Value("${minio.public}")
    private String minIoServer;
    @Value("${minio.bucketName}")
    private String bucketName;
    @Value("${minio.imagePath}")
    private String imagePath;
    @Value("${minio.filePath}")
    private String filePath;
    @Value("${minio.videoPath}")
    private String videoPath;


    @PostConstruct
    public void init() {
        if (!minioUtil.bucketExists(bucketName)) {
            // 创建bucket
            minioUtil.makeBucket(bucketName);
            // 公开bucket
            minioUtil.setBucketPublic(bucketName);
        }
    }


    public String uploadFile(MultipartFile file) {
        Long userId = SessionContext.getSession().getUserId();
        // 大小校验
        if (file.getSize() > Constant.MAX_FILE_SIZE) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件大小不能超过20M");
        }
        // 上传
        String fileName = minioUtil.upload(bucketName, filePath, file);
        if (StringUtils.isEmpty(fileName)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件上传失败");
        }
        String url = generUrl(FileType.FILE, fileName);
        log.info("文件文件成功，用户id:{},url:{}", userId, url);
        return url;
    }

    public UploadImageVO uploadImage(MultipartFile file) {
        try {
            Long userId = SessionContext.getSession().getUserId();
            // 大小校验
            if (file.getSize() > Constant.MAX_IMAGE_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片大小不能超过20M");
            }
            // 图片格式校验
            if (!FileUtil.isImage(file.getOriginalFilename())) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片格式不合法");
            }
            // 上传原图
            UploadImageVO vo = new UploadImageVO();
            String fileName = minioUtil.upload(bucketName, imagePath, file);
            if (StringUtils.isEmpty(fileName)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
            }
            vo.setOriginUrl(generUrl(FileType.IMAGE, fileName));
            // 大于30K的文件需上传缩略图
            if (file.getSize() > 30 * 1024) {
                byte[] imageByte = ImageUtil.compressForScale(file.getBytes(), 30);
                fileName = minioUtil.upload(bucketName, imagePath, Objects.requireNonNull(file.getOriginalFilename()), imageByte, file.getContentType());
                if (StringUtils.isEmpty(fileName)) {
                    throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
                }
            }
            vo.setThumbUrl(generUrl(FileType.IMAGE, fileName));
            log.info("文件图片成功，用户id:{},url:{}", userId, vo.getOriginUrl());
            return vo;
        } catch (IOException e) {
            log.error("上传图片失败，{}", e.getMessage(), e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
        }
    }


    public String generUrl(FileType fileTypeEnum, String fileName) {
        String url = minIoServer + "/" + bucketName;
        switch (fileTypeEnum) {
            case FILE:
                url += "/" + filePath + "/";
                break;
            case IMAGE:
                url += "/" + imagePath + "/";
                break;
            case VIDEO:
                url += "/" + videoPath + "/";
                break;
            default:
                break;
        }
        url += fileName;
        return url;
    }

}

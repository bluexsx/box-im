package com.lx.implatform.service.thirdparty;

import com.lx.common.contant.Contant;
import com.lx.common.enums.FileTypeEnum;
import com.lx.common.enums.ResultCode;
import com.lx.implatform.exception.GlobalException;
import com.lx.implatform.util.FileUtil;
import com.lx.implatform.util.ImageUtil;
import com.lx.implatform.util.MinioUtil;
import com.lx.implatform.vo.UploadImageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;

/*
 * 文件上传服务
 * @Author Blue
 * @Date 2022/10/28
 */
@Slf4j
@Service
public class FileService {

    @Autowired
    private MinioUtil  minioUtil;

    @Value("${minio.public}")
    private String  minIOServer;
    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.imagePath}")
    private String imagePath;

    @Value("${minio.filePath}")
    private String filePath;

    @PostConstruct
    public void init(){
        if(!minioUtil.bucketExists(bucketName)){
            minioUtil.makeBucket(bucketName);
        }
    }


    public String uploadFile(MultipartFile file){
        // 大小校验
        if(file.getSize() > Contant.MAX_FILE_SIZE){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"文件大小不能超过10M");
        }
        // 上传
        String fileName = minioUtil.upload(bucketName,filePath,file);
        if(StringUtils.isEmpty(fileName)){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"文件上传失败");
        }
        return generUrl(FileTypeEnum.FILE,fileName);
    }

    public UploadImageVO uploadImage(MultipartFile file){
        try {
            // 大小校验
            if(file.getSize() > Contant.MAX_IMAGE_SIZE){
                throw new GlobalException(ResultCode.PROGRAM_ERROR,"图片大小不能超过5M");
            }
            // 图片格式校验
            if(!FileUtil.isImage(file.getOriginalFilename())){
                throw new GlobalException(ResultCode.PROGRAM_ERROR,"图片格式不合法");
            }
            // 上传原图
            UploadImageVO vo = new UploadImageVO();
            String fileName = minioUtil.upload(bucketName,imagePath,file);
            if(StringUtils.isEmpty(fileName)){
                throw new GlobalException(ResultCode.PROGRAM_ERROR,"图片上传失败");
            }
            vo.setOriginUrl(generUrl(FileTypeEnum.IMAGE,fileName));
            // 上传缩略图
            byte[] imageByte = ImageUtil.compressForScale(file.getBytes(),100);
            fileName = minioUtil.upload(bucketName,imagePath,file.getOriginalFilename(),imageByte,file.getContentType());
            if(StringUtils.isEmpty(fileName)){
                throw new GlobalException(ResultCode.PROGRAM_ERROR,"图片上传失败");
            }
            vo.setThumbUrl(generUrl(FileTypeEnum.IMAGE,fileName));
            return vo;
        } catch (IOException e) {
            log.error("上传图片失败，{}",e.getMessage(),e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"图片上传失败");
        }
    }


    public String generUrl(FileTypeEnum fileTypeEnum, String fileName){
        String url = minIOServer+"/"+bucketName;
        switch (fileTypeEnum){
            case FILE:
                url += "/file/";
                break;
            case IMAGE:
                url += "/image/";
                break;
            case VIDEO:
                url += "/video/";
                break;
        }
        url += fileName;
        return url;
    }

}

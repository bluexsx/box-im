package com.bx.implatform.util;


import com.bx.common.util.DateTimeUtils;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

@Slf4j
@Component
public class MinioUtil {


    @Autowired
    private MinioClient minioClient;

    /**
     * 查看存储bucket是否存在
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        Boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("查询bucket失败",e);
            return false;
        }
        return found;
    }

    /**
     * 创建存储bucket
     * @return Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("创建bucket失败,",e);
            return false;
        }
        return true;
    }

    /**
     * 删除存储bucket
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("删除bucket失败,",e);
            return false;
        }
        return true;
    }


    /**
     * 文件上传
     * @bucketName bucket名称
     * @path 路径
     * @param file 文件
     * @return Boolean
     */
    public String upload(String bucketName,String path,MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)){
            throw new RuntimeException();
        }
        String fileName = System.currentTimeMillis() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = DateTimeUtils.getFormatDate(new Date(),DateTimeUtils.PARTDATEFORMAT)+ "/" + fileName;
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(path+"/" +objectName)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("上传图片失败,",e);
            return null;
        }
        return objectName;
    }

    /**
     * 文件上传
     * @param bucketName bucket名称
     * @param path 路径
     * @param name 文件名
     * @param fileByte 文件内容
     * @param contentType
     * @return Boolean
     */
    public String upload(String bucketName,String path,String name,byte[] fileByte,String contentType) {

        String fileName = System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
        String objectName = DateTimeUtils.getFormatDate(new Date(),DateTimeUtils.PARTDATEFORMAT)+ "/" + fileName;
        try {
            InputStream stream = new ByteArrayInputStream(fileByte);
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(path+"/" +objectName)
                    .stream(stream, fileByte.length, -1).contentType(contentType).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("上传图片失败,",e);
            return null;
        }
        return objectName;
    }


    /**
     * 删除
     * @param bucketName bucket名称
     * @path path
     * @param fileName
     * @return
     * @throws Exception
     */
    public boolean remove(String bucketName,String path,String fileName){
        try {
            minioClient.removeObject( RemoveObjectArgs.builder().bucket(bucketName).object(path+fileName).build());
        }catch (Exception e){
            log.error("删除图片失败,",e);
            return false;
        }
        return true;
    }
}

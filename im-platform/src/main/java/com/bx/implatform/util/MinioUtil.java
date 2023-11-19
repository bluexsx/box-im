package com.bx.implatform.util;


import io.minio.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

@Slf4j
@Component
@AllArgsConstructor
public class MinioUtil {

    private final  MinioClient minioClient;

    /**
     * 查看存储bucket是否存在
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        try {
            return  minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("查询bucket失败",e);
            return false;
        }
    }

    /**
     * 创建存储bucket
     */
    public void makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("创建bucket失败,",e);
        }
    }

    /**
     * 设置bucket权限为public
     */
    public void setBucketPublic(String bucketName) {
        try {
            // 设置公开
            String sb = "{\"Version\":\"2012-10-17\"," +
                    "\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":" +
                    "{\"AWS\":[\"*\"]},\"Action\":[\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"," +
                    "\"s3:GetBucketLocation\"],\"Resource\":[\"arn:aws:s3:::" + bucketName +
                    "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\"],\"Resource\":[\"arn:aws:s3:::" +
                    bucketName +
                    "/*\"]}]}";
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(sb)
                            .build());
        } catch (Exception e) {
            log.error("创建bucket失败,",e);
        }
    }

    /**
     * 文件上传
     * @param bucketName bucket名称
     * @param path 路径
     * @param file 文件
     * @return Boolean
     */
    public String upload(String bucketName,String path,MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)){
            throw new RuntimeException();
        }
        String fileName = System.currentTimeMillis()+"";
        if(originalFilename.lastIndexOf(".") >= 0){
            fileName +=originalFilename.substring(originalFilename.lastIndexOf("."));
        }
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
     * @param contentType  contentType
     * @return  objectName
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
            log.error("上传文件失败,",e);
            return null;
        }
        return objectName;
    }


    /**
     * 删除
     * @param bucketName bucket名称
     * @param path  路径
     * @param fileName 文件名
     * @return true/false
     */
    public boolean remove(String bucketName,String path,String fileName){
        try {
            minioClient.removeObject( RemoveObjectArgs.builder().bucket(bucketName).object(path+fileName).build());
        }catch (Exception e){
            log.error("删除文件失败,",e);
            return false;
        }
        return true;
    }
}

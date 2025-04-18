package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author Blue
 * @version 1.0
 */
@Data
@TableName("im_file_info")
public class FileInfo {

    /**
     * 文件ID
     */
    @TableId
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件存储路径
     */
    private String filePath;

    /**
     * 压缩文件存储路径
     */
    private String compressedPath;

    /**
     * 封面文件路径
     */
    private String coverPath;

    /**
     * 原始文件大小(字节)
     */
    private Long fileSize;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 文件类型，枚举: FileType
     */
    private Integer fileType;

    /**
     * 是否永久存储
     */
    private Boolean isPermanent;

    /**
     * 文件MD5哈希值
     */
    private String md5;
}

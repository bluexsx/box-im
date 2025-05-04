package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.FileInfo;
import com.bx.implatform.vo.UploadImageVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<FileInfo> {

    String uploadFile(MultipartFile file);

    UploadImageVO uploadImage(MultipartFile file,Boolean isPermanent);


}

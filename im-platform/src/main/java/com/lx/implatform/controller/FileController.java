package com.lx.implatform.controller;

import com.lx.common.result.Result;
import com.lx.common.result.ResultUtils;
import com.lx.implatform.service.thirdparty.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/*
 * 文件上传
 * @Author Blue
 * @Date 2022/10/28
 */
@Slf4j
@RestController
@Api(tags = "文件上传")
public class FileController {


    @Autowired
    private FileService fileService;

    @ApiOperation(value = "上传图片",notes="上传图片")
    @PostMapping("/image/upload")
    public Result upload(MultipartFile file) {
        return ResultUtils.success(fileService.uploadImage(file));
    }


    @GetMapping("/online")
    @ApiOperation(value = "查找用户",notes="根据昵称查找用户")
    public Result checkOnline(){

        return ResultUtils.success("");
    }
}
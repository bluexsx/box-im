package com.bx.implatform.controller;


import com.bx.common.result.Result;
import com.bx.common.result.ResultUtils;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.service.IGroupMessageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping("/message/group")
public class GroupMessageController {

    @Autowired
    private IGroupMessageService groupMessageService;


    @PostMapping("/send")
    @ApiOperation(value = "发送群聊消息",notes="发送群聊消息")
    public Result register(@Valid @RequestBody GroupMessageVO vo){
        groupMessageService.sendMessage(vo);
        return ResultUtils.success();
    }


    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息",notes="拉取未读消息")
    public Result pullUnreadMessage(){
        groupMessageService.pullUnreadMessage();
        return ResultUtils.success();
    }

}


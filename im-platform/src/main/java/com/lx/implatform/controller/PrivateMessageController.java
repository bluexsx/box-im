package com.lx.implatform.controller;


import com.lx.common.result.Result;
import com.lx.common.result.ResultUtils;
import com.lx.implatform.service.IPrivateMessageService;
import com.lx.implatform.vo.PrivateMessageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "私聊消息")
@RestController
@RequestMapping("/message/private")
public class PrivateMessageController {

    @Autowired
    private IPrivateMessageService privateMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送消息",notes="发送单人消息")
    public Result register(@Valid @RequestBody PrivateMessageVO vo){
        privateMessageService.sendMessage(vo);
        return ResultUtils.success();
    }

    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息",notes="拉取未读消息")
    public Result pullUnreadMessage(){
        privateMessageService.pullUnreadMessage();
        return ResultUtils.success();
    }
}


package com.lx.implatform.controller;


import com.lx.common.result.Result;
import com.lx.common.result.ResultUtils;
import com.lx.implatform.service.IGroupMemberService;
import com.lx.implatform.service.IGroupMessageService;
import com.lx.implatform.vo.GroupMessageVO;
import com.lx.implatform.vo.PrivateMessageVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping("/group/message")
public class GroupMessageController {

    @Autowired
    private IGroupMessageService groupMessageService;


    @PostMapping("/send")
    @ApiOperation(value = "发送群聊消息",notes="发送群聊消息")
    public Result register(@Valid @RequestBody GroupMessageVO vo){
        groupMessageService.sendMessage(vo);
        return ResultUtils.success();
    }

}


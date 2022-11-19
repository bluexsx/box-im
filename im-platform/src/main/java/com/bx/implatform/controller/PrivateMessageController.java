package com.bx.implatform.controller;


import com.bx.imcommon.model.PrivateMessageInfo;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IPrivateMessageService;
import com.bx.implatform.vo.PrivateMessageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "私聊消息")
@RestController
@RequestMapping("/message/private")
public class PrivateMessageController {

    @Autowired
    private IPrivateMessageService privateMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送消息",notes="发送私聊消息")
    public Result<Long> sendMessage(@Valid @RequestBody PrivateMessageVO vo){
        return ResultUtils.success(privateMessageService.sendMessage(vo));
    }


    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息",notes="撤回私聊消息")
    public Result<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id){
        privateMessageService.recallMessage(id);
        return ResultUtils.success();
    }


    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息",notes="拉取未读消息")
    public Result pullUnreadMessage(){
        privateMessageService.pullUnreadMessage();
        return ResultUtils.success();
    }


    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录",notes="查询聊天记录")
    public Result<List<PrivateMessageInfo>> recallMessage(@NotNull(message = "好友id不能为空") @RequestParam Long friendId,
                                                          @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                          @NotNull(message = "size不能为空") @RequestParam Long size){
        return ResultUtils.success( privateMessageService.findHistoryMessage(friendId,page,size));
    }

}


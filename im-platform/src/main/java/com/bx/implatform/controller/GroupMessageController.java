package com.bx.implatform.controller;


import com.bx.common.model.im.GroupMessageInfo;
import com.bx.common.result.Result;
import com.bx.common.result.ResultUtils;
import com.bx.implatform.service.IGroupMessageService;
import com.bx.implatform.vo.GroupMessageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


@Api(tags = "群聊消息")
@RestController
@RequestMapping("/message/group")
public class GroupMessageController {

    @Autowired
    private IGroupMessageService groupMessageService;


    @PostMapping("/send")
    @ApiOperation(value = "发送群聊消息",notes="发送群聊消息")
    public Result<Long> sendMessage(@Valid @RequestBody GroupMessageVO vo){
        return ResultUtils.success(groupMessageService.sendMessage(vo));
    }

    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息",notes="撤回群聊消息")
    public Result<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id){
        groupMessageService.recallMessage(id);
        return ResultUtils.success();
    }

    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息",notes="拉取未读消息")
    public Result pullUnreadMessage(){
        groupMessageService.pullUnreadMessage();
        return ResultUtils.success();
    }

    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录",notes="查询聊天记录")
    public Result<List<GroupMessageInfo>> recallMessage(@NotNull(message = "群聊id不能为空") @RequestParam Long groupId,
                                                        @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                        @NotNull(message = "size不能为空") @RequestParam Long size){
        return ResultUtils.success( groupMessageService.findHistoryMessage(groupId,page,size));
    }
}


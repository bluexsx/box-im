package com.bx.implatform.controller;

import com.bx.implatform.dto.GroupMessageDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.GroupMessageService;
import com.bx.implatform.vo.GroupMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "群聊消息")
@RestController
@RequestMapping("/message/group")
@RequiredArgsConstructor
public class GroupMessageController {

    private final GroupMessageService groupMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送群聊消息", description = "发送群聊消息")
    public Result<GroupMessageVO> sendMessage(@Valid @RequestBody GroupMessageDTO vo) {
        return ResultUtils.success(groupMessageService.sendMessage(vo));
    }

    @DeleteMapping("/recall/{id}")
    @Operation(summary = "撤回消息", description = "撤回群聊消息")
    public Result<GroupMessageVO> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id) {
        return ResultUtils.success(groupMessageService.recallMessage(id));
    }

    @GetMapping("/pullOfflineMessage")
    @Operation(summary = "拉取离线消息(已废弃)", description = "拉取离线消息,消息将通过webscoket异步推送")
    public Result pullOfflineMessage(@RequestParam Long minId) {
        groupMessageService.pullOfflineMessage(minId);
        return ResultUtils.success();
    }

    @GetMapping(value = "/loadOfflineMessage")
    @Operation(summary = "拉取离线消息", description = "拉取离线消息")
    public Result<List<GroupMessageVO>> loadOfflineMessage(@RequestParam Long minId) {
        return ResultUtils.success(groupMessageService.loadOffineMessage(minId));
    }


    @PutMapping("/readed")
    @Operation(summary = "消息已读", description = "将群聊中的消息状态置为已读")
    public Result readedMessage(@RequestParam Long groupId) {
        groupMessageService.readedMessage(groupId);
        return ResultUtils.success();
    }

    @GetMapping("/findReadedUsers")
    @Operation(summary = "获取已读用户id", description = "获取消息已读用户列表")
    public Result<List<Long>> findReadedUsers(@RequestParam Long groupId,
        @RequestParam Long messageId) {
        return ResultUtils.success(groupMessageService.findReadedUsers(groupId, messageId));
    }

    @GetMapping("/history")
    @Operation(summary = "查询聊天记录", description = "查询聊天记录")
    public Result<List<GroupMessageVO>> recallMessage(@NotNull(message = "群聊id不能为空") @RequestParam Long groupId,
        @NotNull(message = "页码不能为空") @RequestParam Long page,
        @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(groupMessageService.findHistoryMessage(groupId, page, size));
    }
}


package com.bx.implatform.controller;

import com.bx.implatform.dto.PrivateMessageDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.PrivateMessageService;
import com.bx.implatform.vo.PrivateMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "私聊消息")
@RestController
@RequestMapping("/message/private")
@RequiredArgsConstructor
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "发送私聊消息")
    public Result<PrivateMessageVO> sendMessage(@Valid @RequestBody PrivateMessageDTO vo) {
        return ResultUtils.success(privateMessageService.sendMessage(vo));
    }

    @DeleteMapping("/recall/{id}")
    @Operation(summary = "撤回消息", description = "撤回私聊消息")
    public Result<PrivateMessageVO> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id) {
        return ResultUtils.success( privateMessageService.recallMessage(id));
    }

    @GetMapping("/pullOfflineMessage")
    @Operation(summary = "拉取离线消息", description = "拉取离线消息,消息将通过webscoket异步推送")
    public Result pullOfflineMessage(@RequestParam Long minId) {
        privateMessageService.pullOfflineMessage(minId);
        return ResultUtils.success();
    }

    @PutMapping("/readed")
    @Operation(summary = "消息已读", description = "将会话中接收的消息状态置为已读")
    public Result readedMessage(@RequestParam Long friendId) {
        privateMessageService.readedMessage(friendId);
        return ResultUtils.success();
    }

    @GetMapping("/maxReadedId")
    @Operation(summary = "获取最大已读消息的id", description = "获取某个会话中已读消息的最大id")
    public Result<Long> getMaxReadedId(@RequestParam Long friendId) {
        return ResultUtils.success(privateMessageService.getMaxReadedId(friendId));
    }

    @GetMapping("/history")
    @Operation(summary = "查询聊天记录", description = "查询聊天记录")
    public Result<List<PrivateMessageVO>> recallMessage(
        @NotNull(message = "好友id不能为空") @RequestParam Long friendId,
        @NotNull(message = "页码不能为空") @RequestParam Long page,
        @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(privateMessageService.findHistoryMessage(friendId, page, size));
    }

}


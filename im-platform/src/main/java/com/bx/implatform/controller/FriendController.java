package com.bx.implatform.controller;

import com.bx.implatform.annotation.RepeatSubmit;
import com.bx.implatform.dto.FriendDndDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.vo.FriendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "好友")
@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/list")
    @Operation(summary = "好友列表", description = "获取好友列表")
    public Result<List<FriendVO>> findFriends() {
        return ResultUtils.success(friendService.findFriends());
    }


    @RepeatSubmit
    @PostMapping("/add")
    @Operation(summary = "添加好友", description = "双方建立好友关系")
    public Result addFriend(@NotNull(message = "好友id不可为空") @RequestParam Long friendId) {
        friendService.addFriend(friendId);
        return ResultUtils.success();
    }

    @GetMapping("/find/{friendId}")
    @Operation(summary = "查找好友信息", description = "查找好友信息")
    public Result<FriendVO> findFriend(@NotNull(message = "好友id不可为空") @PathVariable Long friendId) {
        return ResultUtils.success(friendService.findFriend(friendId));
    }


    @DeleteMapping("/delete/{friendId}")
    @Operation(summary = "删除好友", description = "解除好友关系")
    public Result delFriend(@NotNull(message = "好友id不可为空") @PathVariable Long friendId) {
        friendService.delFriend(friendId);
        return ResultUtils.success();
    }

    @PutMapping("/dnd")
    @Operation(summary = "开启/关闭免打扰状态", description = "开启/关闭免打扰状态")
    public Result setFriendDnd(@Valid @RequestBody FriendDndDTO dto) {
        friendService.setDnd(dto);
        return ResultUtils.success();
    }

}


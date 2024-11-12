package com.bx.implatform.controller;

import com.bx.implatform.annotation.OnlineCheck;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.WebrtcPrivateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "单人通话")
@RestController
@RequestMapping("/webrtc/private")
@RequiredArgsConstructor
public class WebrtcPrivateController {

    private final WebrtcPrivateService webrtcPrivateService;


    @Operation(summary = "呼叫视频通话")
    @PostMapping("/call")
    public Result call(@RequestParam Long uid, @RequestParam(defaultValue = "video") String mode,
        @RequestBody String offer) {
        webrtcPrivateService.call(uid, mode, offer);
        return ResultUtils.success();
    }

    @Operation(summary = "接受视频通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long uid, @RequestBody String answer) {
        webrtcPrivateService.accept(uid, answer);
        return ResultUtils.success();
    }

    @Operation(summary = "拒绝视频通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long uid) {
        webrtcPrivateService.reject(uid);
        return ResultUtils.success();
    }

    @Operation(summary = "取消呼叫")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long uid) {
        webrtcPrivateService.cancel(uid);
        return ResultUtils.success();
    }

    @Operation(summary = "呼叫失败")
    @PostMapping("/failed")
    public Result failed(@RequestParam Long uid, @RequestParam String reason) {
        webrtcPrivateService.failed(uid, reason);
        return ResultUtils.success();
    }

    @Operation(summary = "挂断")
    @PostMapping("/handup")
    public Result handup(@RequestParam Long uid) {
        webrtcPrivateService.handup(uid);
        return ResultUtils.success();
    }

    @PostMapping("/candidate")
    @Operation(summary = "同步candidate")
    public Result candidate(@RequestParam Long uid, @RequestBody String candidate) {
        webrtcPrivateService.candidate(uid, candidate);
        return ResultUtils.success();
    }

    @Operation(summary = "心跳")
    @PostMapping("/heartbeat")
    public Result heartbeat(@RequestParam Long uid) {
        webrtcPrivateService.heartbeat(uid);
        return ResultUtils.success();
    }
}

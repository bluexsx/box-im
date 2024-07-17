package com.bx.implatform.controller;

import com.bx.implatform.annotation.OnlineCheck;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IWebrtcPrivateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "webrtc视频单人通话")
@RestController
@RequestMapping("/webrtc/private")
@RequiredArgsConstructor
public class WebrtcPrivateController {

    private final IWebrtcPrivateService webrtcPrivateService;

    @OnlineCheck
    @Operation(summary = "呼叫视频通话")
    @PostMapping("/call")
    public Result call(@RequestParam("uid") Long uid, @RequestParam(name = "mode", defaultValue = "video") String mode,
        @RequestBody String offer) {
        webrtcPrivateService.call(uid, mode, offer);
        return ResultUtils.success();
    }

    @Operation(summary = "接受视频通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam("uid") Long uid, @RequestBody String answer) {
        webrtcPrivateService.accept(uid, answer);
        return ResultUtils.success();
    }

    @Operation(summary = "拒绝视频通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam("uid") Long uid) {
        webrtcPrivateService.reject(uid);
        return ResultUtils.success();
    }

    @Operation(summary = "取消呼叫")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam("uid") Long uid) {
        webrtcPrivateService.cancel(uid);
        return ResultUtils.success();
    }

    @Operation(summary = "呼叫失败")
    @PostMapping("/failed")
    public Result failed(@RequestParam("uid") Long uid, @RequestParam String reason) {
        webrtcPrivateService.failed(uid, reason);
        return ResultUtils.success();
    }

    @Operation(summary = "挂断")
    @PostMapping("/handup")
    public Result handup(@RequestParam("uid") Long uid) {
        webrtcPrivateService.handup(uid);
        return ResultUtils.success();
    }

    @PostMapping("/candidate")
    @Operation(summary = "同步candidate")
    public Result candidate(@RequestParam("uid") Long uid, @RequestBody String candidate) {
        webrtcPrivateService.candidate(uid, candidate);
        return ResultUtils.success();
    }

    @Operation(summary = "获取通话信息")
    @PostMapping("/heartbeat")
    public Result heartbeat(@RequestParam("uid") Long uid) {
        webrtcPrivateService.heartbeat(uid);
        return ResultUtils.success();
    }
}

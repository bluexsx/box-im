package com.bx.implatform.controller;

import com.bx.implatform.annotation.OnlineCheck;
import com.bx.implatform.config.ICEServer;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IWebrtcPrivateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "webrtc视频单人通话")
@RestController
@RequestMapping("/webrtc/private")
@RequiredArgsConstructor
public class WebrtcPrivateController {

    private final IWebrtcPrivateService webrtcPrivateService;

    @OnlineCheck
    @ApiOperation(httpMethod = "POST", value = "呼叫视频通话")
    @PostMapping("/call")
    public Result call(@RequestParam Long uid, @RequestParam(defaultValue = "video") String mode, @RequestBody String offer) {
        webrtcPrivateService.call(uid, mode, offer);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long uid, @RequestBody String answer) {
        webrtcPrivateService.accept(uid, answer);
        return ResultUtils.success();
    }


    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long uid) {
        webrtcPrivateService.reject(uid);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long uid) {
        webrtcPrivateService.cancel(uid);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
    @PostMapping("/failed")
    public Result failed(@RequestParam Long uid, @RequestParam String reason) {
        webrtcPrivateService.failed(uid, reason);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "挂断")
    @PostMapping("/handup")
    public Result handup(@RequestParam Long uid) {
        webrtcPrivateService.handup(uid);
        return ResultUtils.success();
    }


    @PostMapping("/candidate")
    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    public Result candidate(@RequestParam Long uid, @RequestBody String candidate) {
        webrtcPrivateService.candidate(uid, candidate);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "获取通话信息")
    @PostMapping("/heartbeat")
    public Result heartbeat(@RequestParam Long uid) {
        webrtcPrivateService.heartbeat(uid);
        return ResultUtils.success();
    }
}

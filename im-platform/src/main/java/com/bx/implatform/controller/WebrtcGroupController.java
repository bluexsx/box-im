package com.bx.implatform.controller;

import com.bx.implatform.dto.*;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.IWebrtcGroupService;
import com.bx.implatform.vo.WebrtcGroupInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Blue
 * @date: 2024-06-01
 * @version: 1.0
 */
@Tag(name = "多人通话")
@RestController
@RequestMapping("/webrtc/group")
@RequiredArgsConstructor
public class WebrtcGroupController {

    private final IWebrtcGroupService webrtcGroupService;

    @Operation(summary = "发起群视频通话")
    @PostMapping("/setup")
    public Result setup(@Valid @RequestBody WebrtcGroupSetupDTO dto) {
        webrtcGroupService.setup(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "接受通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam("groupId") Long groupId) {
        webrtcGroupService.accept(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "拒绝通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam("groupId") Long groupId) {
        webrtcGroupService.reject(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "通话失败")
    @PostMapping("/failed")
    public Result failed(@Valid @RequestBody WebrtcGroupFailedDTO dto) {
        webrtcGroupService.failed(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "进入视频通话")
    @PostMapping("/join")
    public Result join(@RequestParam("groupId") Long groupId) {
        webrtcGroupService.join(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "取消通话")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam("groupId") Long groupId) {
        webrtcGroupService.cancel(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "离开视频通话")
    @PostMapping("/quit")
    public Result quit(@RequestParam("groupId") Long groupId) {
        webrtcGroupService.quit(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "推送offer信息")
    @PostMapping("/offer")
    public Result offer(@Valid @RequestBody WebrtcGroupOfferDTO dto) {
        webrtcGroupService.offer(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "推送answer信息")
    @PostMapping("/answer")
    public Result answer(@Valid @RequestBody WebrtcGroupAnswerDTO dto) {
        webrtcGroupService.answer(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "邀请用户进入视频通话")
    @PostMapping("/invite")
    public Result invite(@Valid @RequestBody WebrtcGroupInviteDTO dto) {
        webrtcGroupService.invite(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "同步candidate")
    @PostMapping("/candidate")
    public Result candidate(@Valid @RequestBody WebrtcGroupCandidateDTO dto) {
        webrtcGroupService.candidate(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "设备操作")
    @PostMapping("/device")
    public Result device(@Valid @RequestBody WebrtcGroupDeviceDTO dto) {
        webrtcGroupService.device(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "获取通话信息")
    @GetMapping("/info")
    public Result<WebrtcGroupInfoVO> info(@RequestParam("groupId") Long groupId) {
        return ResultUtils.success(webrtcGroupService.info(groupId));
    }

    @Operation(summary = "获取通话信息")
    @PostMapping("/heartbeat")
    public Result heartbeat(@RequestParam("groupId") Long groupId) {
        webrtcGroupService.heartbeat(groupId);
        return ResultUtils.success();
    }

}

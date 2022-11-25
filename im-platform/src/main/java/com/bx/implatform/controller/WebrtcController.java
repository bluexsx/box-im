package com.bx.implatform.controller;


import com.bx.imclient.IMClient;
import com.bx.imcommon.model.PrivateMessageInfo;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.session.SessionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "webrtc视频单人通话")
@RestController
@RequestMapping("/webrtc/private")
public class WebrtcController {

    @Autowired
    private IMClient imClient;


    @ApiOperation(httpMethod = "POST", value = "呼叫视频通话")
    @PostMapping("/call")
    public Result call(@RequestParam Long uid, @RequestBody String offer) {
        Long userId = SessionContext.getSession().getId();

        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_CALL.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        message.setContent(offer);
        imClient.sendPrivateMessage(uid,message);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long uid,@RequestBody String answer) {
        Long userId = SessionContext.getSession().getId();

        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_ACCEPT.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        message.setContent(answer);
        imClient.sendPrivateMessage(uid,message);
        return ResultUtils.success();
    }


    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long uid) {
        Long userId = SessionContext.getSession().getId();
        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_REJECT.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        imClient.sendPrivateMessage(uid,message);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long uid) {
        Long userId = SessionContext.getSession().getId();
        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_CANCEL.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        imClient.sendPrivateMessage(uid,message);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
    @PostMapping("/failed")
    public Result failed(@RequestParam Long uid,@RequestParam String reason) {
        Long userId = SessionContext.getSession().getId();

        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_FAILED.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        message.setContent(reason);
        imClient.sendPrivateMessage(uid,message);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "挂断")
    @PostMapping("/handup")
    public Result leave(@RequestParam Long uid) {
        Long userId = SessionContext.getSession().getId();

        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_HANDUP.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        imClient.sendPrivateMessage(uid,message);
        return ResultUtils.success();
    }


    @PostMapping("/candidate")
    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    public Result candidate(@RequestParam Long uid,@RequestBody String candidate ) {
        Long userId = SessionContext.getSession().getId();
        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_CANDIDATE.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        message.setContent(candidate);
        imClient.sendPrivateMessage(uid,message);
        return ResultUtils.success();
    }
}

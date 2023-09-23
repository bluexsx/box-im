package com.bx.implatform.controller;


import cn.hutool.core.util.ArrayUtil;
import com.bx.imclient.IMClient;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.PrivateMessageInfo;
import com.bx.implatform.config.ICEServerConfig;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.Arrays;

@Api(tags = "webrtc视频单人通话")
@RestController
@RequestMapping("/webrtc/private")
public class WebrtcController {

    @Autowired
    private IMClient imClient;

    @Autowired
    private ICEServerConfig iceServerConfig;

    @ApiOperation(httpMethod = "POST", value = "呼叫视频通话")
    @PostMapping("/call")
    public Result call(@RequestParam Long uid, @RequestBody String offer) {
        if(!imClient.isOnline(uid)){
            throw new GlobalException("对方目前不在线");
        }
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_CALL,uid,offer));
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long uid,@RequestBody String answer) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_ACCEPT,uid,answer));
        return ResultUtils.success();
    }


    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long uid) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_REJECT,uid,null));
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long uid) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_CANCEL,uid,null));
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
    @PostMapping("/failed")
    public Result failed(@RequestParam Long uid,@RequestParam String reason) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_FAILED,uid,reason));
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "挂断")
    @PostMapping("/handup")
    public Result leave(@RequestParam Long uid) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_HANDUP,uid,null));
        return ResultUtils.success();
    }


    @PostMapping("/candidate")
    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    public Result candidate(@RequestParam Long uid,@RequestBody String candidate ) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_CANDIDATE,uid,candidate));
        return ResultUtils.success();
    }

    @GetMapping("/iceservers")
    @ApiOperation(httpMethod = "GET", value = "获取iceservers")
    public Result iceservers() {
        return ResultUtils.success(iceServerConfig.getIceServers());
    }

    private IMPrivateMessage buildSendMessage(MessageType messageType,Long uid,String content){
        UserSession session  = SessionContext.getSession();
        PrivateMessageInfo messageInfo = new PrivateMessageInfo();
        messageInfo.setType(messageType.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(content);

        IMPrivateMessage sendMessage = new IMPrivateMessage();
        sendMessage.setSendId(session.getUserId());
        sendMessage.setSendTerminal(session.getTerminal());
        sendMessage.setSendToSelf(false);
        sendMessage.setRecvId(uid);
        sendMessage.setDatas(Arrays.asList(messageInfo));
        return sendMessage;
    }
}

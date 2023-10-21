package com.bx.imserver.netty.processor;

import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.model.IMSendResult;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GroupMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Async
    @Override
    public void process(IMRecvInfo recvInfo) {
        IMUserInfo sender = recvInfo.getSender();
        List<IMUserInfo> receivers = recvInfo.getReceivers();
        log.info("接收到群消息，发送者:{},接收用户数量:{}，内容:{}",sender.getId(),receivers.size(),recvInfo.getData());
        for(IMUserInfo receiver:receivers){
            try {
                ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getId(),receiver.getTerminal());
                if(channelCtx != null){
                    // 推送消息到用户
                    IMSendInfo sendInfo = new IMSendInfo();
                    sendInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    sendInfo.setData(recvInfo.getData());
                    channelCtx.channel().writeAndFlush(sendInfo);
                    // 消息发送成功确认
                    sendResult(recvInfo,receiver,IMSendCode.SUCCESS);

                }else {
                    // 消息发送成功确认
                    sendResult(recvInfo,receiver,IMSendCode.NOT_FIND_CHANNEL);
                    log.error("未找到channel,发送者:{},接收id:{}，内容:{}",sender.getId(),receiver.getId(),recvInfo.getData());
                }
            }catch (Exception e){
                // 消息发送失败确认
                sendResult(recvInfo,receiver,IMSendCode.UNKONW_ERROR);
                log.error("发送消息异常,发送者:{},接收id:{}，内容:{}",sender.getId(),receiver.getId(),recvInfo.getData());
            }
        }
    }


    private void sendResult(IMRecvInfo recvInfo,IMUserInfo receiver,IMSendCode sendCode){
        if(recvInfo.getSendResult()) {
            IMSendResult result = new IMSendResult();
            result.setSender(recvInfo.getSender());
            result.setReceiver(receiver);
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            // 推送到结果队列
            String key = IMRedisKey.IM_RESULT_GROUP_QUEUE;
            redisTemplate.opsForList().rightPush(key, result);
        }
    }
}

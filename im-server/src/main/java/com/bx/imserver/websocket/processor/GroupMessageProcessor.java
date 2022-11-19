package com.bx.imserver.websocket.processor;

import com.bx.common.contant.RedisKey;
import com.bx.common.enums.IMCmdType;
import com.bx.common.enums.SendResultType;
import com.bx.common.model.im.GroupMessageInfo;
import com.bx.common.model.im.IMRecvInfo;
import com.bx.common.model.im.SendInfo;
import com.bx.common.model.im.SendResult;
import com.bx.imserver.websocket.WebsocketChannelCtxHolder;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GroupMessageProcessor extends  MessageProcessor<IMRecvInfo<GroupMessageInfo>> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Async
    @Override
    public void process(IMRecvInfo<GroupMessageInfo> recvInfo) {
        GroupMessageInfo messageInfo = recvInfo.getData();
        List<Long> recvIds = recvInfo.getRecvIds();
        log.info("接收到群消息，发送者:{},群id:{},接收id:{}，内容:{}",messageInfo.getSendId(),messageInfo.getGroupId(),recvIds,messageInfo.getContent());
        for(Long recvId:recvIds){
            try {
                ChannelHandlerContext channelCtx = WebsocketChannelCtxHolder.getChannelCtx(recvId);
                if(channelCtx != null){
                    // 自己发的消息不用推送
                    if(recvId != messageInfo.getSendId()){
                        // 推送消息到用户
                        SendInfo sendInfo = new SendInfo();
                        sendInfo.setCmd(IMCmdType.GROUP_MESSAGE.getCode());
                        sendInfo.setData(messageInfo);
                        channelCtx.channel().writeAndFlush(sendInfo);
                        // 消息发送成功确认
                        String key = RedisKey.IM_RESULT_GROUP_QUEUE;
                        SendResult sendResult = new SendResult();
                        sendResult.setRecvId(recvId);
                        sendResult.setResult(SendResultType.SUCCESS);
                        sendResult.setMessageInfo(messageInfo);
                        redisTemplate.opsForList().rightPush(key,sendResult);
                    }
                }else {
                    // 消息发送失败确认
                    String key = RedisKey.IM_RESULT_GROUP_QUEUE;
                    SendResult sendResult = new SendResult();
                    sendResult.setRecvId(recvId);
                    sendResult.setResult(SendResultType.FAIL);
                    sendResult.setFailReason("未找到WS连接");
                    sendResult.setMessageInfo(messageInfo);
                    redisTemplate.opsForList().rightPush(key,sendResult);
                    log.error("未找到WS连接,发送者:{},群id:{},接收id:{}，内容:{}",messageInfo.getSendId(),messageInfo.getGroupId(),recvIds,messageInfo.getContent());
                }
            }catch (Exception e){
                // 消息发送失败确认
                String key = RedisKey.IM_RESULT_GROUP_QUEUE;
                SendResult sendResult = new SendResult();
                sendResult.setRecvId(recvId);
                sendResult.setResult(SendResultType.FAIL);
                sendResult.setFailReason("未知异常");
                sendResult.setMessageInfo(messageInfo);
                redisTemplate.opsForList().rightPush(key,sendResult);
                log.error("发送消息异常,发送者:{},群id:{},接收id:{}，内容:{}",messageInfo.getSendId(),messageInfo.getGroupId(),recvIds,messageInfo.getContent());
            }
        }
    }

}

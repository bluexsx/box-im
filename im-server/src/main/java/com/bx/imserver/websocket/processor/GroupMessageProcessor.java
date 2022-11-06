package com.bx.imserver.websocket.processor;

import com.bx.common.contant.RedisKey;
import com.bx.common.enums.WSCmdEnum;
import com.bx.common.model.im.GroupMessageInfo;
import com.bx.common.model.im.SendInfo;
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
public class GroupMessageProcessor extends  MessageProcessor<GroupMessageInfo> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Async
    @Override
    public void process(GroupMessageInfo data) {
        log.info("接收到群消息，发送者:{},群id:{},接收id:{}，内容:{}",data.getSendId(),data.getGroupId(),data.getRecvIds(),data.getContent());
        List<Long> recvIds = data.getRecvIds();
        // 接收者id列表不需要传输，节省带宽
        data.setRecvIds(null);
        for(Long recvId:recvIds){
            ChannelHandlerContext channelCtx = WebsocketChannelCtxHolder.getChannelCtx(recvId);
            if(channelCtx != null){
                // 自己发的消息不用推送
                if(recvId != data.getSendId()){
                    // 推送消息到用户
                    SendInfo sendInfo = new SendInfo();
                    sendInfo.setCmd(WSCmdEnum.GROUP_MESSAGE.getCode());
                    sendInfo.setData(data);
                    channelCtx.channel().writeAndFlush(sendInfo);
                }
                // 设置已读最大id
                String key = RedisKey.IM_GROUP_READED_POSITION + data.getGroupId()+":"+recvId;
                redisTemplate.opsForValue().set(key,data.getId());
            }else {
                log.error("未找到WS连接,发送者:{},群id:{},接收id:{}，内容:{}",data.getSendId(),data.getGroupId(),data.getRecvIds());
            }

        }
    }

}

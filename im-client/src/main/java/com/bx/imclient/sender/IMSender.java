package com.bx.imclient.sender;

import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IMSender {

    @Autowired
    @Qualifier("IMRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MessageListenerMulticaster listenerMulticaster;

    public void sendPrivateMessage(IMPrivateMessage<?> message) {
        for (Integer terminal : message.getRecvTerminals()) {
            // 获取对方连接的channelId
            String key = String.join(":",RedisKey.IM_USER_SERVER_ID, message.getRecvId().toString(), terminal.toString());
            Integer serverId = (Integer) redisTemplate.opsForValue().get(key);
            // 如果对方在线，将数据存储至redis，等待拉取推送
            if (serverId != null) {
                IMRecvInfo[] recvInfos = new IMRecvInfo[message.getDatas().size()];
                String sendKey = String.join(":",RedisKey.IM_UNREAD_PRIVATE_QUEUE,serverId.toString());
                for (int i = 0; i < message.getDatas().size(); i++) {
                    IMRecvInfo recvInfo = new IMRecvInfo();
                    recvInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                    recvInfo.setSendResult(message.getSendResult());
                    recvInfo.setSender(message.getSender());
                    recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getRecvId(), terminal)));
                    recvInfo.setData(message.getDatas().get(i));
                    recvInfos[i]=recvInfo;
                }
                redisTemplate.opsForList().rightPushAll(sendKey, recvInfos);
            } else if(message.getSendResult()){
                // 回复消息状态
                for (int i = 0; i < message.getDatas().size(); i++) {
                    SendResult result = new SendResult();
                    result.setSender(message.getSender());
                    result.setReceiver(new IMUserInfo(message.getRecvId(), terminal));
                    result.setCode(IMSendCode.NOT_ONLINE.code());
                    result.setData(message.getDatas().get(i));
                    listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, result);
                }

            }
            // 推送给自己的其他终端
            if (message.getSendToSelf() && !message.getSender().getTerminal().equals(terminal)) {
                    // 获取终端连接的channelId
                    key = String.join(":",RedisKey.IM_USER_SERVER_ID, message.getSender().getId().toString(), terminal.toString());
                    serverId = (Integer) redisTemplate.opsForValue().get(key);
                    // 如果终端在线，将数据存储至redis，等待拉取推送
                    if (serverId != null) {
                        String sendKey = String.join(":",RedisKey.IM_UNREAD_PRIVATE_QUEUE, serverId.toString());
                        IMRecvInfo[] recvInfos = new IMRecvInfo[message.getDatas().size()];
                        for (int i = 0; i < message.getDatas().size(); i++) {
                            IMRecvInfo recvInfo = new IMRecvInfo();
                            // 自己的消息不需要回推消息结果
                            recvInfo.setSendResult(false);
                            recvInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                            recvInfo.setSender(message.getSender());
                            recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getSender().getId(),terminal)));
                            recvInfo.setData(message.getDatas().get(i));
                            recvInfos[i]=recvInfo;
                        }
                        redisTemplate.opsForList().rightPushAll(sendKey, recvInfos);
                    }
                }
        }
    }

    public void sendGroupMessage(IMGroupMessage<?> message) {
        // 根据群聊每个成员所连的IM-server，进行分组
        List<IMUserInfo> offLineUsers = Collections.synchronizedList(new LinkedList<>());
        // 格式:map<服务器id,list<接收方>>
        Map<Integer, List<IMUserInfo>> serverMap = new ConcurrentHashMap<>();
        for (Integer terminal : message.getRecvTerminals()) {
            message.getRecvIds().parallelStream().forEach(id -> {
                String key = String.join(":",RedisKey.IM_USER_SERVER_ID, id.toString(),terminal.toString());
                Integer serverId = (Integer)redisTemplate.opsForValue().get(key);
                if (serverId != null) {
                    List<IMUserInfo> list = serverMap.computeIfAbsent(serverId,o->Collections.synchronizedList(new LinkedList<>()));
                    list.add(new IMUserInfo(id,terminal));
                } else {
                    // 加入离线列表
                    offLineUsers.add(new IMUserInfo(id,terminal));
                }
            });
        }
        // 逐个server发送
        for (Map.Entry<Integer, List<IMUserInfo>> entry : serverMap.entrySet()) {
            IMRecvInfo recvInfo = new IMRecvInfo();
            recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
            recvInfo.setReceivers(new LinkedList<>(entry.getValue()));
            recvInfo.setSender(message.getSender());
            recvInfo.setSendResult(message.getSendResult());
            recvInfo.setData(message.getData());
            // 推送至队列
            String key = String.join(":",RedisKey.IM_UNREAD_GROUP_QUEUE,entry.getKey().toString());
            redisTemplate.opsForList().rightPush(key, recvInfo);
        }
        // 对离线用户回复消息状态
        if(message.getSendResult()){
            for (IMUserInfo offLineUser : offLineUsers) {
                SendResult result = new SendResult();
                result.setSender(message.getSender());
                result.setReceiver(offLineUser);
                result.setCode(IMSendCode.NOT_ONLINE.code());
                result.setData(message.getData());
                listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, result);
            }
        }

        // 推送给自己的其他终端
        if (message.getSendToSelf()) {
            for (Integer terminal : message.getRecvTerminals()) {
                if(terminal.equals(message.getSender().getTerminal())){
                    continue;
                }
                // 获取终端连接的channelId
                String key = String.join(":",RedisKey.IM_USER_SERVER_ID, message.getSender().getId().toString(), terminal.toString());
                Integer serverId = (Integer) redisTemplate.opsForValue().get(key);
                // 如果终端在线，将数据存储至redis，等待拉取推送
                if (serverId != null) {
                    IMRecvInfo recvInfo = new IMRecvInfo();
                    recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    recvInfo.setSender(message.getSender());
                    recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getSender().getId(),terminal)));
                    // 自己的消息不需要回推消息结果
                    recvInfo.setSendResult(false);
                    recvInfo.setData(message.getData());
                    String sendKey = String.join(":",RedisKey.IM_UNREAD_GROUP_QUEUE,serverId.toString());
                    redisTemplate.opsForList().rightPush(sendKey, recvInfo);
                }
            }
        }
    }

    public Boolean isOnline(Long userId) {
        String key = String.join(":",RedisKey.IM_USER_SERVER_ID,userId.toString(),"*");
        return !redisTemplate.keys(key).isEmpty();
    }
}

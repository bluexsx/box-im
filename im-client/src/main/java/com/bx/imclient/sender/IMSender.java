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
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageListenerMulticaster listenerMulticaster;

    public void sendPrivateMessage(IMPrivateMessage<?> message) {
        List<Integer> terminals = message.getRecvTerminals();
        for (Integer terminal : terminals) {
            // 获取对方连接的channelId
            String key = String.join(":",RedisKey.IM_USER_SERVER_ID, message.getRecvId().toString(), terminal.toString());
            Integer serverId = (Integer) redisTemplate.opsForValue().get(key);
            // 如果对方在线，将数据存储至redis，等待拉取推送
            if (serverId != null) {
                IMRecvInfo[] recvInfos = new IMRecvInfo[message.getDatas().size()];
                String sendKey = RedisKey.IM_UNREAD_PRIVATE_QUEUE + serverId;
                for (int i = 0; i < message.getDatas().size(); i++) {
                    IMRecvInfo recvInfo = new IMRecvInfo();
                    recvInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                    recvInfo.setRecvTerminal(terminal);
                    recvInfo.setSendResult(message.getSendResult());
                    List<Long> recvIds = new LinkedList<>();
                    recvIds.add(message.getRecvId());
                    recvInfo.setRecvIds(recvIds);
                    recvInfo.setData(message.getDatas().get(i));
                    recvInfos[i]=recvInfo;
                }
                redisTemplate.opsForList().rightPushAll(sendKey, recvInfos);
            } else if(message.getSendResult()){
                // 回复消息状态
                for (int i = 0; i < message.getDatas().size(); i++) {
                    SendResult result = new SendResult();
                    result.setRecvId(message.getRecvId());
                    result.setRecvTerminal(terminal);
                    result.setCode(IMSendCode.NOT_ONLINE.code());
                    result.setData(message.getDatas().get(i));
                    listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, result);
                }

            }
            // 推送给自己的其他终端
            if (message.getSendToSelf() && !message.getSendTerminal().equals(terminal)) {
                // 获取终端连接的channelId
                key = String.join(":",RedisKey.IM_USER_SERVER_ID, message.getSendId().toString(), terminal.toString());
                serverId = (Integer) redisTemplate.opsForValue().get(key);
                // 如果终端在线，将数据存储至redis，等待拉取推送
                if (serverId != null) {
                    String sendKey = RedisKey.IM_UNREAD_PRIVATE_QUEUE + serverId;
                    IMRecvInfo[] recvInfos = new IMRecvInfo[message.getDatas().size()];
                    for (int i = 0; i < message.getDatas().size(); i++) {
                        IMRecvInfo recvInfo = new IMRecvInfo();
                        recvInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                        recvInfo.setRecvTerminal(terminal);
                        // 自己的消息不需要回推消息结果
                        recvInfo.setSendResult(false);
                        LinkedList<Long> recvIds = new LinkedList<>();
                        recvIds.add(message.getSendId());
                        recvInfo.setRecvIds(recvIds);
                        recvInfo.setData(message.getDatas().get(i));
                        recvInfos[i]=recvInfo;
                    }
                    redisTemplate.opsForList().rightPushAll(sendKey, recvInfos);
                }
            }
        }
    }

    public void sendGroupMessage(List<Long> recvIds, GroupMessageInfo... messageInfos) {
        // 根据群聊每个成员所连的IM-server，进行分组
        List<Long> offLineIds = Collections.synchronizedList(new LinkedList<Long>());
        Map<Integer, List<Long>> serverMap = new ConcurrentHashMap<>();
        recvIds.parallelStream().forEach(id -> {
            String key = RedisKey.IM_USER_SERVER_ID + id;
            Integer serverId = (Integer) redisTemplate.opsForValue().get(key);
            if (serverId != null) {
                // 此处需要加锁，否则list可以会被覆盖
                synchronized (serverMap) {
                    if (serverMap.containsKey(serverId)) {
                        serverMap.get(serverId).add(id);
                    } else {
                        List<Long> list = Collections.synchronizedList(new LinkedList<Long>());
                        list.add(id);
                        serverMap.put(serverId, list);
                    }
                }
            } else {
                offLineIds.add(id);
            }
        });
        // 逐个server发送
//        for (Map.Entry<Integer, List<Long>> entry : serverMap.entrySet()) {
//            IMRecvInfo[] recvInfos = new IMRecvInfo[messageInfos.length];
//            for (int i = 0; i < messageInfos.length; i++) {
//                IMRecvInfo<GroupMessageInfo> recvInfo = new IMRecvInfo<>();
//                recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
//                recvInfo.setRecvIds(new LinkedList<>(entry.getValue()));
//                recvInfo.setData(messageInfos[i]);
//                recvInfos[i] = recvInfo;
//            }
//            String key = RedisKey.IM_UNREAD_GROUP_QUEUE + entry.getKey();
//            redisTemplate.opsForList().rightPushAll(key, recvInfos);
//        }
//        // 不在线的用户，回复消息状态
//        for (GroupMessageInfo messageInfo : messageInfos) {
//            for (Long id : offLineIds) {
//                // 回复消息状态
//                SendResult result = new SendResult();
//                result.setMessageInfo(messageInfo);
//                result.setRecvId(id);
//                result.setCode(IMSendCode.NOT_ONLINE);
//                listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, result);
//            }
//        }
    }

    public Boolean isOnline(Long userId) {
        String key = String.join(":",RedisKey.IM_USER_SERVER_ID,userId.toString(),"*");
        return !redisTemplate.keys(key).isEmpty();
    }
}

package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.common.contant.RedisKey;
import com.bx.common.enums.MessageStatusEnum;
import com.bx.common.enums.ResultCode;
import com.bx.common.model.im.PrivateMessageInfo;
import com.bx.common.util.BeanUtils;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.PrivateMessageMapper;
import com.bx.implatform.service.IFriendService;
import com.bx.implatform.service.IPrivateMessageService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.vo.PrivateMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements IPrivateMessageService {

    @Autowired
    private IFriendService friendService;
    @Autowired
    private  RedisTemplate<String, Object> redisTemplate;

    /**
     * 发送私聊消息
     *
     * @param vo 私聊消息vo
     * @return
     */
    @Override
    public void sendMessage(PrivateMessageVO vo) {
        Long userId = SessionContext.getSession().getId();
        Boolean isFriends = friendService.isFriend(userId,vo.getRecvId());
        if(!isFriends){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"您已不是对方好友，无法发送消息");
        }
        // 保存消息
        PrivateMessage msg = BeanUtils.copyProperties(vo, PrivateMessage.class);
        msg.setSendId(userId);
        msg.setStatus(MessageStatusEnum.UNREAD.getCode());
        msg.setSendTime(new Date());
        this.save(msg);
        // 获取对方连接的channelId
        String key = RedisKey.IM_USER_SERVER_ID+msg.getRecvId();
        Integer serverId = (Integer)redisTemplate.opsForValue().get(key);
        // 如果对方在线，将数据存储至redis，等待拉取推送
        if(serverId != null){
            String sendKey =  RedisKey.IM_UNREAD_PRIVATE_MESSAGE + serverId;
            PrivateMessageInfo msgInfo = BeanUtils.copyProperties(msg, PrivateMessageInfo.class);
            redisTemplate.opsForList().rightPush(sendKey,msgInfo);
        }
    }

    /**
     * 异步拉取私聊消息，通过websocket异步推送
     *
     * @return
     */
    @Override
    public void pullUnreadMessage() {
        // 获取当前连接的channelId
        Long userId = SessionContext.getSession().getId();
        String key = RedisKey.IM_USER_SERVER_ID+userId;
        Integer serverId = (Integer)redisTemplate.opsForValue().get(key);
        if(serverId == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"用户未建立连接");
        }
        // 获取当前用户所有未读消息
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PrivateMessage::getRecvId,userId)
                .eq(PrivateMessage::getStatus,MessageStatusEnum.UNREAD);
        List<PrivateMessage> messages = this.list(queryWrapper);
        // 上传至redis，等待推送
        if(!messages.isEmpty()){
            List<PrivateMessageInfo> infos = messages.stream().map(m->{
                PrivateMessageInfo msgInfo = BeanUtils.copyProperties(m, PrivateMessageInfo.class);
                return  msgInfo;
            }).collect(Collectors.toList());
            String sendKey =  RedisKey.IM_UNREAD_PRIVATE_MESSAGE + serverId;
            redisTemplate.opsForList().rightPushAll(sendKey,infos.toArray());
        }
    }
}

package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lx.common.contant.RedisKey;
import com.lx.common.enums.MessageStatusEnum;
import com.lx.common.enums.ResultCode;
import com.lx.common.model.im.PrivateMessageInfo;
import com.lx.common.util.BeanUtils;
import com.lx.implatform.entity.PrivateMessage;
import com.lx.implatform.exception.GlobalException;
import com.lx.implatform.mapper.PrivateMessageMapper;
import com.lx.implatform.service.IFriendService;
import com.lx.implatform.service.IPrivateMessageService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.vo.PrivateMessageVO;
import org.apache.commons.lang3.StringUtils;
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
        String serverId = (String)redisTemplate.opsForValue().get(key);
        // 如果对方在线，将数据存储至redis，等待拉取推送
        if(!StringUtils.isEmpty(serverId)){
            String sendKey =  RedisKey.IM_UNREAD_MESSAGE + serverId;
            PrivateMessageInfo msgInfo = BeanUtils.copyProperties(msg, PrivateMessageInfo.class);
            redisTemplate.opsForList().rightPush(sendKey,msgInfo);
        }
    }

    @Override
    public void pullUnreadMessage() {
        // 获取当前连接的channelId
        Long userId = SessionContext.getSession().getId();
        String key = RedisKey.IM_USER_SERVER_ID+userId;
        String serverId = (String)redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(serverId)){
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
            String sendKey =  RedisKey.IM_UNREAD_MESSAGE + serverId;
            redisTemplate.opsForList().rightPushAll(sendKey,infos.toArray());
        }
    }
}

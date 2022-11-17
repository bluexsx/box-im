package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.common.contant.Constant;
import com.bx.common.contant.RedisKey;
import com.bx.common.enums.MessageStatusEnum;
import com.bx.common.enums.MessageTypeEnum;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
     * @return 消息id
     */
    @Override
    public Long sendMessage(PrivateMessageVO vo) {
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
        log.info("发送私聊消息，发送id:{},接收id:{}，内容:{}",userId,vo.getRecvId(),vo.getContent());
        return msg.getId();
    }

    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    @Override
    public void recallMessage(Long id) {
        Long userId = SessionContext.getSession().getId();
        PrivateMessage msg = this.getById(id);
        if(msg == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"消息不存在");
        }
        if(msg.getSendId() != userId){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"这条消息不是由您发送,无法撤回");
        }
        if(System.currentTimeMillis() - msg.getSendTime().getTime() > Constant.ALLOW_RECALL_SECOND * 1000){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"消息已发送超过5分钟，无法撤回");
        }
        // 直接物理删除
        this.removeById(id);
        // 获取对方连接的channelId
        String key = RedisKey.IM_USER_SERVER_ID+msg.getRecvId();
        Integer serverId = (Integer)redisTemplate.opsForValue().get(key);
        // 如果对方在线，将数据存储至redis，等待拉取推送
        if(serverId != null){
            String sendKey =  RedisKey.IM_UNREAD_PRIVATE_MESSAGE + serverId;
            PrivateMessageInfo msgInfo = BeanUtils.copyProperties(msg, PrivateMessageInfo.class);
            msgInfo.setType(MessageTypeEnum.TIP.getCode());
            msgInfo.setContent("对方撤回了一条消息");
            redisTemplate.opsForList().rightPush(sendKey,msgInfo);
        }
        log.info("删除私聊消息，发送id:{},接收id:{}，内容:{}",msg.getSendId(),msg.getRecvId(),msg.getContent());
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
            log.info("拉取未读私聊消息，用户id:{},数量:{}",userId,infos.size());
        }
    }
}

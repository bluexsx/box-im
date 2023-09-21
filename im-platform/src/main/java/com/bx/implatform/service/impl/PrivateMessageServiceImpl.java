package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.Constant;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.model.PrivateMessageInfo;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.PrivateMessageMapper;
import com.bx.implatform.service.IFriendService;
import com.bx.implatform.service.IPrivateMessageService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.util.BeanUtils;
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
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private IMClient imClient;
    /**
     * 发送私聊消息
     *
     * @param vo 私聊消息vo
     * @return 消息id
     */
    @Override
    public Long sendMessage(PrivateMessageVO vo) {
        Long userId = SessionContext.getSession().getId();
        Boolean isFriends = friendService.isFriend(userId, vo.getRecvId());
        if (!isFriends) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不是对方好友，无法发送消息");
        }
        // 保存消息
        PrivateMessage msg = BeanUtils.copyProperties(vo, PrivateMessage.class);
        msg.setSendId(userId);
        msg.setStatus(MessageStatus.UNREAD.code());
        msg.setSendTime(new Date());
        this.save(msg);
        // 推送消息
        PrivateMessageInfo msgInfo = BeanUtils.copyProperties(msg, PrivateMessageInfo.class);
        imClient.sendPrivateMessage(vo.getRecvId(),msgInfo);
        log.info("发送私聊消息，发送id:{},接收id:{}，内容:{}", userId, vo.getRecvId(), vo.getContent());
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
        if (msg == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        if (!msg.getSendId().equals(userId)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > Constant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }
        // 修改消息状态
        msg.setStatus(MessageStatus.RECALL.code());
        this.updateById(msg);
        // 推送消息
        PrivateMessageInfo msgInfo = BeanUtils.copyProperties(msg, PrivateMessageInfo.class);
        msgInfo.setType(MessageType.TIP.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setContent("对方撤回了一条消息");
        imClient.sendPrivateMessage(msgInfo.getRecvId(),msgInfo);
        log.info("撤回私聊消息，发送id:{},接收id:{}，内容:{}", msg.getSendId(), msg.getRecvId(), msg.getContent());
    }


    /**
     * 拉取历史聊天记录
     *
     * @param friendId 好友id
     * @param page     页码
     * @param size     页码大小
     * @return 聊天记录列表
     */
    @Override
    public List<PrivateMessageInfo> findHistoryMessage(Long friendId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getId();
        Long stIdx = (page - 1) * size;
        QueryWrapper<PrivateMessage> wrapper = new QueryWrapper<>();
        wrapper.lambda().and(wrap -> wrap.and(
                wp -> wp.eq(PrivateMessage::getSendId, userId)
                        .eq(PrivateMessage::getRecvId, friendId))
                .or(wp -> wp.eq(PrivateMessage::getRecvId, userId)
                        .eq(PrivateMessage::getSendId, friendId)))
                .ne(PrivateMessage::getStatus, MessageStatus.RECALL.code())
                .orderByDesc(PrivateMessage::getId)
                .last("limit " + stIdx + "," + size);

        List<PrivateMessage> messages = this.list(wrapper);
        List<PrivateMessageInfo> messageInfos = messages.stream().map(m -> {
            PrivateMessageInfo info = BeanUtils.copyProperties(m, PrivateMessageInfo.class);
            return info;
        }).collect(Collectors.toList());
        log.info("拉取聊天记录，用户id:{},好友id:{}，数量:{}", userId, friendId, messageInfos.size());
        return messageInfos;
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
        if (!imClient.isOnline(userId)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户未建立连接");
        }
        // 获取当前用户所有未读消息
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PrivateMessage::getRecvId, userId)
                .eq(PrivateMessage::getStatus, MessageStatus.UNREAD);
        List<PrivateMessage> messages = this.list(queryWrapper);
        // 上传至redis，等待推送
        if (!messages.isEmpty()) {
            List<PrivateMessageInfo> messageInfos = messages.stream().map(m -> {
                PrivateMessageInfo msgInfo = BeanUtils.copyProperties(m, PrivateMessageInfo.class);
                return msgInfo;
            }).collect(Collectors.toList());
            // 推送消息
            PrivateMessageInfo[] infoArr = messageInfos.toArray(new PrivateMessageInfo[messageInfos.size()]);
            imClient.sendPrivateMessage(userId,infoArr);
            log.info("拉取未读私聊消息，用户id:{},数量:{}", userId, infoArr.length);
        }
    }
}

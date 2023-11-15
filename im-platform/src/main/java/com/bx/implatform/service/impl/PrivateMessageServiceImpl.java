package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.dto.PrivateMessageDTO;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.PrivateMessageMapper;
import com.bx.implatform.service.IFriendService;
import com.bx.implatform.service.IPrivateMessageService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.DateTimeUtils;
import com.bx.implatform.vo.PrivateMessageVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements IPrivateMessageService {

    private final IFriendService friendService;
    private final IMClient imClient;

    @Override
    public Long sendMessage(PrivateMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Boolean isFriends = friendService.isFriend(session.getUserId(), dto.getRecvId());
        if (!isFriends) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不是对方好友，无法发送消息");
        }
        // 保存消息
        PrivateMessage msg = BeanUtils.copyProperties(dto, PrivateMessage.class);
        msg.setSendId(session.getUserId());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setSendTime(new Date());
        this.save(msg);
        // 推送消息
        PrivateMessageVO msgInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(msgInfo.getRecvId());
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        imClient.sendPrivateMessage(sendMessage);
        log.info("发送私聊消息，发送id:{},接收id:{}，内容:{}", session.getUserId(), dto.getRecvId(), dto.getContent());
        return msg.getId();
    }

    @Override
    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        PrivateMessage msg = this.getById(id);
        if (msg == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }
        // 修改消息状态
        msg.setStatus(MessageStatus.RECALL.code());
        this.updateById(msg);
        // 推送消息
        PrivateMessageVO msgInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        msgInfo.setType(MessageType.RECALL.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setContent("对方撤回了一条消息");

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(msgInfo.getRecvId());
        sendMessage.setSendToSelf(false);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);

        // 推给自己其他终端
        msgInfo.setContent("你撤回了一条消息");
        sendMessage.setSendToSelf(true);
        sendMessage.setRecvTerminals(Collections.emptyList());
        imClient.sendPrivateMessage(sendMessage);
        log.info("撤回私聊消息，发送id:{},接收id:{}，内容:{}", msg.getSendId(), msg.getRecvId(), msg.getContent());
    }



    @Override
    public List<PrivateMessageVO> findHistoryMessage(Long friendId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
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
        List<PrivateMessageVO> messageInfos = messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageVO.class)).collect(Collectors.toList());
        log.info("拉取聊天记录，用户id:{},好友id:{}，数量:{}", userId, friendId, messageInfos.size());
        return messageInfos;
    }


    @Override
    public void pullUnreadMessage() {
        UserSession session = SessionContext.getSession();
        // 获取当前连接的channelId
        if (!imClient.isOnline(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户未建立连接");
        }

        List<Friend> friends = friendService.findFriendByUserId(session.getUserId());
        if (friends.isEmpty()) {
            return;
        }
        List<Long> friendIds = friends.stream().map(Friend::getFriendId).collect(Collectors.toList());
        // 获取当前用户所有未读消息
        LambdaQueryWrapper<PrivateMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PrivateMessage::getRecvId, session.getUserId())
                .eq(PrivateMessage::getStatus, MessageStatus.UNSEND)
                .in(PrivateMessage::getSendId, friendIds);
        List<PrivateMessage> messages = this.list(queryWrapper);
        // 上传至redis，等待推送
        for (PrivateMessage message : messages) {
            PrivateMessageVO msgInfo = BeanUtils.copyProperties(message, PrivateMessageVO.class);
            // 推送消息
            IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
            sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
            sendMessage.setRecvId(session.getUserId());
            sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
            sendMessage.setSendToSelf(false);
            sendMessage.setData(msgInfo);
            imClient.sendPrivateMessage(sendMessage);
        }
        log.info("拉取未读私聊消息，用户id:{},数量:{}", session.getUserId(), messages.size());

    }



    @Override
    public List<PrivateMessageVO> loadMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        List<Friend> friends = friendService.findFriendByUserId(session.getUserId());
        if (friends.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> friendIds = friends.stream().map(Friend::getFriendId).collect(Collectors.toList());
        // 获取当前用户的消息
        LambdaQueryWrapper<PrivateMessage> queryWrapper = Wrappers.lambdaQuery();
        // 只能拉取最近1个月的
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);
        queryWrapper.gt(PrivateMessage::getId, minId)
                .ge(PrivateMessage::getSendTime, minDate)
                .ne(PrivateMessage::getStatus, MessageStatus.RECALL.code())
                .and(wrap -> wrap.and(
                        wp -> wp.eq(PrivateMessage::getSendId, session.getUserId())
                                .in(PrivateMessage::getRecvId, friendIds))
                        .or(wp -> wp.eq(PrivateMessage::getRecvId, session.getUserId())
                                .in(PrivateMessage::getSendId, friendIds)))
                .orderByAsc(PrivateMessage::getId)
                .last("limit 100");

        List<PrivateMessage> messages = this.list(queryWrapper);
        // 更新发送状态
        List<Long> ids = messages.stream()
                .filter(m -> !m.getSendId().equals(session.getUserId()) && m.getStatus().equals(MessageStatus.UNSEND.code()))
                .map(PrivateMessage::getId)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            LambdaUpdateWrapper<PrivateMessage> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.in(PrivateMessage::getId, ids)
                    .set(PrivateMessage::getStatus, MessageStatus.SENDED.code());
            this.update(updateWrapper);
        }
        log.info("拉取消息，用户id:{},数量:{}", session.getUserId(), messages.size());
        return messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageVO.class)).collect(Collectors.toList());
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void readedMessage(Long friendId) {
        UserSession session = SessionContext.getSession();
        // 推送消息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setRecvId(friendId);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(friendId);
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);
        // 修改消息状态为已读
        LambdaUpdateWrapper<PrivateMessage> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(PrivateMessage::getSendId, friendId)
                .eq(PrivateMessage::getRecvId, session.getUserId())
                .eq(PrivateMessage::getStatus, MessageStatus.SENDED.code())
                .set(PrivateMessage::getStatus, MessageStatus.READED.code());
        this.update(updateWrapper);
        log.info("消息已读，接收方id:{},发送方id:{}", session.getUserId(), friendId);
    }
}

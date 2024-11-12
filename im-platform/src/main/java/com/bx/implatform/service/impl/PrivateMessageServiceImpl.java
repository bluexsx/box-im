package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.dto.PrivateMessageDTO;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.PrivateMessageMapper;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.service.PrivateMessageService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.SensitiveFilterUtil;
import com.bx.implatform.vo.PrivateMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
    implements PrivateMessageService {

    private final FriendService friendService;
    private final IMClient imClient;
    private final SensitiveFilterUtil sensitiveFilterUtil;

    @Override
    public PrivateMessageVO sendMessage(PrivateMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Boolean isFriends = friendService.isFriend(session.getUserId(), dto.getRecvId());
        if (Boolean.FALSE.equals(isFriends)) {
            throw new GlobalException("您已不是对方好友，无法发送消息");
        }
        // 保存消息
        PrivateMessage msg = BeanUtils.copyProperties(dto, PrivateMessage.class);
        msg.setSendId(session.getUserId());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setSendTime(new Date());
        // 过滤内容中的敏感词
        if (MessageType.TEXT.code().equals(dto.getType())) {
            msg.setContent(sensitiveFilterUtil.filter(dto.getContent()));
        }
        this.save(msg);
        // 推送消息
        PrivateMessageVO msgInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(msgInfo.getRecvId());
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendPrivateMessage(sendMessage);
        log.info("发送私聊消息，发送id:{},接收id:{}，内容:{}", session.getUserId(), dto.getRecvId(), dto.getContent());
        return msgInfo;
    }

    @Override
    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        PrivateMessage msg = this.getById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException("消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException("这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException("消息已发送超过5分钟，无法撤回");
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
        wrapper.lambda().and(
                wrap -> wrap.and(wp -> wp.eq(PrivateMessage::getSendId, userId).eq(PrivateMessage::getRecvId, friendId))
                    .or(wp -> wp.eq(PrivateMessage::getRecvId, userId).eq(PrivateMessage::getSendId, friendId)))
            .ne(PrivateMessage::getStatus, MessageStatus.RECALL.code()).orderByDesc(PrivateMessage::getId)
            .last("limit " + stIdx + "," + size);

        List<PrivateMessage> messages = this.list(wrapper);
        List<PrivateMessageVO> messageInfos =
            messages.stream().map(m -> BeanUtils.copyProperties(m, PrivateMessageVO.class))
                .collect(Collectors.toList());
        log.info("拉取聊天记录，用户id:{},好友id:{}，数量:{}", userId, friendId, messageInfos.size());
        return messageInfos;
    }

    @Override
    public void pullOfflineMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        if (!imClient.isOnline(session.getUserId())) {
            throw new GlobalException("网络连接失败，无法拉取离线消息");
        }
        // 查询用户好友列表
        List<Friend> friends = friendService.findFriendByUserId(session.getUserId());
        if (friends.isEmpty()) {
            // 关闭加载中标志
            this.sendLoadingMessage(false);
            return;
        }
        // 开启加载中标志
        this.sendLoadingMessage(true);
        List<Long> friendIds = friends.stream().map(Friend::getFriendId).collect(Collectors.toList());
        // 获取当前用户的消息
        LambdaQueryWrapper<PrivateMessage> queryWrapper = Wrappers.lambdaQuery();
        // 只能拉取最近3个月的消息,移动端只拉取一个月消息
        int months = session.getTerminal().equals(IMTerminalType.APP.code()) ? 1 : 3;
        Date minDate = DateUtils.addMonths(new Date(), -months);
        queryWrapper.gt(PrivateMessage::getId, minId).ge(PrivateMessage::getSendTime, minDate)
            .ne(PrivateMessage::getStatus, MessageStatus.RECALL.code()).and(wrap -> wrap.and(
                    wp -> wp.eq(PrivateMessage::getSendId, session.getUserId()).in(PrivateMessage::getRecvId, friendIds))
                .or(wp -> wp.eq(PrivateMessage::getRecvId, session.getUserId()).in(PrivateMessage::getSendId, friendIds)))
            .orderByAsc(PrivateMessage::getId);
        List<PrivateMessage> messages = this.list(queryWrapper);
        // 推送消息
        for (PrivateMessage m : messages) {
            PrivateMessageVO vo = BeanUtils.copyProperties(m, PrivateMessageVO.class);
            IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
            sendMessage.setSender(new IMUserInfo(m.getSendId(), IMTerminalType.WEB.code()));
            sendMessage.setRecvId(session.getUserId());
            sendMessage.setRecvTerminals(List.of(session.getTerminal()));
            sendMessage.setSendToSelf(false);
            sendMessage.setData(vo);
            sendMessage.setSendResult(true);
            imClient.sendPrivateMessage(sendMessage);
        }
        // 关闭加载中标志
        this.sendLoadingMessage(false);
        log.info("拉取私聊消息，用户id:{},数量:{}", session.getUserId(), messages.size());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void readedMessage(Long friendId) {
        UserSession session = SessionContext.getSession();
        // 推送消息给自己，清空会话列表上的已读数量
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setRecvId(friendId);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setData(msgInfo);
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);
        // 推送回执消息给对方，更新已读状态
        msgInfo = new PrivateMessageVO();
        msgInfo.setType(MessageType.RECEIPT.code());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setRecvId(friendId);
        sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(friendId);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(msgInfo);
        imClient.sendPrivateMessage(sendMessage);
        // 修改消息状态为已读
        LambdaUpdateWrapper<PrivateMessage> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(PrivateMessage::getSendId, friendId).eq(PrivateMessage::getRecvId, session.getUserId())
            .eq(PrivateMessage::getStatus, MessageStatus.SENDED.code())
            .set(PrivateMessage::getStatus, MessageStatus.READED.code());
        this.update(updateWrapper);
        log.info("消息已读，接收方id:{},发送方id:{}", session.getUserId(), friendId);
    }

    @Override
    public Long getMaxReadedId(Long friendId) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<PrivateMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PrivateMessage::getSendId, session.getUserId()).eq(PrivateMessage::getRecvId, friendId)
            .eq(PrivateMessage::getStatus, MessageStatus.READED.code()).orderByDesc(PrivateMessage::getId)
            .select(PrivateMessage::getId).last("limit 1");
        PrivateMessage message = this.getOne(wrapper);
        if (Objects.isNull(message)) {
            return -1L;
        }
        return message.getId();
    }

    private void sendLoadingMessage(Boolean isLoadding) {
        UserSession session = SessionContext.getSession();
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setType(MessageType.LOADING.code());
        msgInfo.setContent(isLoadding.toString());
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(session.getUserId());
        sendMessage.setRecvTerminals(List.of(session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);
    }
}

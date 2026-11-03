package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.annotation.RedisLock;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.ChatDeleteDTO;
import com.bx.implatform.dto.MessageDeleteDTO;
import com.bx.implatform.dto.PrivateMessageDTO;
import com.bx.implatform.dto.PrivateMessageHistoryDTO;
import com.bx.implatform.entity.MessageDeletion;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.ChatType;
import com.bx.implatform.enums.DeleteType;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.PrivateMessageMapper;
import com.bx.implatform.mapper.UserMapper;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.service.MessageDeletionService;
import com.bx.implatform.service.PrivateMessageService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.ConvUtil;
import com.bx.implatform.util.SensitiveFilterUtil;
import com.bx.implatform.vo.PrivateMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
        implements PrivateMessageService {

    private final FriendService friendService;
    private final IMClient imClient;
    private final MessageDeletionService messageDeletionService;
    private final SensitiveFilterUtil sensitiveFilterUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final UserMapper userMapper;

    @Override
    public PrivateMessageVO sendMessage(PrivateMessageDTO dto) {
        validMessage(dto);
        UserSession session = SessionContext.getSession();
        Boolean isFriends = friendService.isFriend(session.getUserId(), dto.getRecvId());
        if (Boolean.FALSE.equals(isFriends)) {
            throw new GlobalException("您已不是对方好友，无法发送消息");
        }
        // 保存消息
        PrivateMessage message = BeanUtils.copyProperties(dto, PrivateMessage.class);
        message.setConvKey(ConvUtil.buildConvKey(session.getUserId(), dto.getRecvId()));
        message.setSendId(session.getUserId());
        message.setStatus(MessageStatus.PENDING.code());
        message.setSendTime(new Date());
        // 过滤内容中的敏感词
        if (MessageType.TEXT.code().equals(dto.getType())) {
            message.setContent(sensitiveFilterUtil.filter(dto.getContent()));
        }
        // 保存消息(走代理触发分布式锁)
        PrivateMessageServiceImpl proxy = (PrivateMessageServiceImpl) AopContext.currentProxy();
        proxy.saveMessage(message);
        // 推送消息
        PrivateMessageVO vo = BeanUtils.copyProperties(message, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(vo.getRecvId());
        sendMessage.setSendToSelf(true);
        sendMessage.setData(vo);
        sendMessage.setSendResult(true);
        imClient.sendPrivateMessage(sendMessage);
        log.info("发送私聊消息,发送id:{},接收id:{},内容:{}", session.getUserId(), dto.getRecvId(), dto.getContent());
        return vo;
    }

    @Transactional
    @Override
    public PrivateMessageVO recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        PrivateMessage recallMessage = this.getById(id);
        if (Objects.isNull(recallMessage)) {
            throw new GlobalException("消息不存在");
        }
        if (!recallMessage.getSendId().equals(session.getUserId())) {
            throw new GlobalException("这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - recallMessage.getSendTime()
                .getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException("消息已发送超过5分钟，无法撤回");
        }
        // 记录撤回提示语到扩展字段(用于前端展示)
        User user = userMapper.selectById(session.getUserId());
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        String recallTip = String.format("%s 撤回了一条消息", user.getNickName());
        recallMessage.setStatus(MessageStatus.RECALL.code());
        this.updateById(recallMessage);
        // 生成一条撤回消息
        PrivateMessage message = new PrivateMessage();
        message.setLocalId(IdWorker.getIdStr());
        message.setConvKey(recallMessage.getConvKey());
        message.setSendId(session.getUserId());
        message.setStatus(MessageStatus.PENDING.code());
        message.setSendTime(new Date());
        message.setRecvId(recallMessage.getRecvId());
        message.setType(MessageType.RECALL.code());
        HashMap contentMap = new HashMap();
        contentMap.put("id", id);
        contentMap.put("tip", recallTip);
        message.setContent(JSON.toJSONString(contentMap));
        // 走代理触发分布式锁
        PrivateMessageServiceImpl proxy = (PrivateMessageServiceImpl) AopContext.currentProxy();
        proxy.saveMessage(message);
        // 推送消息
        PrivateMessageVO vo = BeanUtils.copyProperties(message, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(vo.getRecvId());
        sendMessage.setData(vo);
        imClient.sendPrivateMessage(sendMessage);
        log.info("撤回私聊消息，发送id:{},接收id:{}，内容:{}", message.getSendId(), message.getRecvId(),
                message.getContent());
        return vo;
    }


    @Override
    public List<PrivateMessageVO> loadOfflineMessage(Long minId) {
        long time = System.currentTimeMillis();
        UserSession session = SessionContext.getSession();
        // 获取当前用户的消息
        LambdaQueryWrapper<PrivateMessage> wrapper = Wrappers.lambdaQuery();
        // 只能拉取最近1个月的消息
        Date minDate = DateUtils.addDays(new Date(), Math.toIntExact(-Constant.MAX_OFFLINE_MESSAGE_DAYS));
        wrapper.gt(PrivateMessage::getId, minId);
        wrapper.ge(PrivateMessage::getSendTime, minDate);
        wrapper.and(wp -> wp.eq(PrivateMessage::getSendId, session.getUserId()).or()
                .eq(PrivateMessage::getRecvId, session.getUserId()));
        wrapper.orderByDesc(PrivateMessage::getId);
        wrapper.last("limit " + Constant.MAX_OFFLINE_MESSAGE_SIZE);
        List<PrivateMessage> messages = this.list(wrapper);
        // 保证每个会话至少会拉到一条消息
        if (messages.size() >= Constant.MAX_OFFLINE_MESSAGE_SIZE) {
            messages = appendLastMessageInConversation(messages, minId);
        }
        List<MessageDeletion> deletions =
                messageDeletionService.findByChatType(session.getUserId(), ChatType.PRIVATE.getCode());
        // 整个会话删除的消息不在重复推送
        messages = messages.stream().filter(m -> !isDeleteChat(m, deletions)).collect(Collectors.toList());
        // 更新消息为送达状态
        List<Long> messageIds = messages.stream().filter(m -> m.getRecvId().equals(session.getUserId()))
                .filter(m -> m.getStatus().equals(MessageStatus.PENDING.code())).map(PrivateMessage::getId)
                .collect(Collectors.toList());
        if (!messageIds.isEmpty()) {
            LambdaUpdateWrapper<PrivateMessage> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.in(PrivateMessage::getId, messageIds);
            updateWrapper.set(PrivateMessage::getStatus, MessageStatus.DELIVERED.code());
            update(updateWrapper);
        }
        // 转换vo
        List<PrivateMessageVO> vos = messages.stream().map(m -> {
            PrivateMessageVO vo = BeanUtils.copyProperties(m, PrivateMessageVO.class);
            vo.setDeleted(isDeleteMessage(m, deletions));
            return vo;
        }).toList();
        log.info("拉取离线私聊消息,用户id:{},数量:{},耗时:{},minId:{}", session.getUserId(), vos.size(),
                System.currentTimeMillis() - time, minId);
        return vos.stream().sorted(Comparator.comparing(PrivateMessageVO::getId)).collect(Collectors.toList());
    }

    @Override
    public void readedMessage(Long friendId, Long messageId) {
        UserSession session = SessionContext.getSession();
        // 如果前端没有传消息id,取出最后一条消息id
        String convKey = ConvUtil.buildConvKey(session.getUserId(), friendId);
        if (Objects.isNull(messageId)) {
            messageId = findMaxMessageId(convKey);
            if (messageId < 0) {
                return;
            }
        }
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
        HashMap contentMap = new HashMap();
        contentMap.put("id", messageId);
        msgInfo.setContent(JSON.toJSONString(contentMap));
        sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(friendId);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(msgInfo);
        imClient.sendPrivateMessage(sendMessage);
        // 会话最大的消息id
        Long lastMaxReadedId = getMaxReadedId(friendId, session.getUserId());
        if (lastMaxReadedId < messageId) {
            // 修改消息状态为已读
            LambdaUpdateWrapper<PrivateMessage> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(PrivateMessage::getSendId, friendId);
            updateWrapper.eq(PrivateMessage::getRecvId, session.getUserId());
            updateWrapper.gt(PrivateMessage::getId, lastMaxReadedId);
            updateWrapper.ne(PrivateMessage::getStatus, MessageStatus.RECALL.code());
            updateWrapper.set(PrivateMessage::getStatus, MessageStatus.READED.code());
            this.update(updateWrapper);
            // 记录新的最大已读消息id
            String key = StrUtil.join(":", RedisKey.IM_PRIVATE_READED_POSITION, friendId, session.getUserId());
            redisTemplate.opsForValue().set(key, messageId, Constant.MAX_OFFLINE_MESSAGE_DAYS, TimeUnit.DAYS);
        }
        log.info("消息已读，接收方id:{},发送方id:{}", session.getUserId(), friendId);
    }

    @Override
    public Long getMaxReadedId(Long sendId, Long recvId) {
        String key = StrUtil.join(":", RedisKey.IM_PRIVATE_READED_POSITION, sendId, recvId);
        Object id = redisTemplate.opsForValue().get(key);
        if (!Objects.isNull(id)) {
            return Long.parseLong(id.toString());
        }
        LambdaQueryWrapper<PrivateMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PrivateMessage::getSendId, sendId);
        wrapper.eq(PrivateMessage::getRecvId, recvId);
        wrapper.eq(PrivateMessage::getStatus, MessageStatus.READED.code());
        wrapper.orderByDesc(PrivateMessage::getId);
        wrapper.select(PrivateMessage::getId).last("limit 1");
        PrivateMessage message = this.getOne(wrapper);
        if (!Objects.isNull(message)) {
            return message.getId();
        }
        return -1L;
    }

    /**
     * 保存消息 加分布式锁是为了让数据库自增id和seq_no保持同序
     */
    @Override
    @RedisLock(prefixKey = RedisKey.IM_LOCK_PRIVATE_MESSAGE_SAVE, key = "#message.convKey")
    public void saveMessage(PrivateMessage message) {
        if (StrUtil.isEmpty(message.getLocalId())) {
            message.setLocalId(IdWorker.getIdStr());
        }
        message.setSeqNo(getNextSeqNo(message.getConvKey()));
        save(message);
        // 记录消息最大id
        String key = StrUtil.join(":", RedisKey.IM_PRIVATE_MESSAGE_MAX_ID, message.getConvKey());
        redisTemplate.opsForValue().set(key, message.getId(), Constant.MAX_OFFLINE_MESSAGE_DAYS, TimeUnit.DAYS);
    }

    @Override
    public List<PrivateMessageVO> loadHistoryMessage(PrivateMessageHistoryDTO dto) {
        UserSession session = SessionContext.getSession();
        String convKey = ConvUtil.buildConvKey(session.getUserId(), dto.getFriendId());
        LambdaQueryWrapper<PrivateMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PrivateMessage::getConvKey, convKey);
        if (CollectionUtil.isNotEmpty(dto.getLocalIds())) {
            wrapper.in(PrivateMessage::getLocalId, dto.getLocalIds());
        } else if (CollectionUtil.isNotEmpty(dto.getSeqNos())) {
            wrapper.in(PrivateMessage::getSeqNo, dto.getSeqNos());
        } else if (!Objects.isNull(dto.getMaxSeqNo()) && !Objects.isNull(dto.getMinSeqNo())) {
            wrapper.between(PrivateMessage::getSeqNo, dto.getMinSeqNo(), dto.getMaxSeqNo());
        }
        Date minDate = DateUtils.addDays(new Date(), Math.toIntExact(-Constant.MAX_OFFLINE_MESSAGE_DAYS));
        wrapper.ge(PrivateMessage::getSendTime, minDate);
        wrapper.orderByAsc(PrivateMessage::getSeqNo);
        wrapper.last("limit 100");
        List<PrivateMessage> messages = this.list(wrapper);
        if (CollectionUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }
        // 已经删除的消息
        List<MessageDeletion> deletions =
                messageDeletionService.findByChatIdAndType(session.getUserId(), ChatType.PRIVATE.getCode(),
                        dto.getFriendId());
        // 整个会话删掉的消息就不再推送了
        messages = messages.stream().filter(m -> !isDeleteChat(m, deletions)).collect(Collectors.toList());
        // 转换vo
        return messages.stream().map(m -> {
            PrivateMessageVO vo = BeanUtils.copyProperties(m, PrivateMessageVO.class);
            vo.setDeleted(isDeleteMessage(m, deletions));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteMessage(MessageDeleteDTO dto) {
        UserSession session = SessionContext.getSession();
        messageDeletionService.deleteByMessage(session.getUserId(), ChatType.PRIVATE.getCode(), dto.getChatId(),
                dto.getMessageIds());
    }

    @Override
    public void deleteChat(ChatDeleteDTO dto) {
        Long userId = SessionContext.getSession().getUserId();
        // 查询会话最大消息id
        String convKey = ConvUtil.buildConvKey(userId, dto.getChatId());
        Long maxMessageId = this.findMaxMessageId(convKey);
        if (maxMessageId < 0) {
            return;
        }
        messageDeletionService.deleteByChat(userId, ChatType.PRIVATE.getCode(), dto.getChatId(), maxMessageId);
    }


    private Long getNextSeqNo(String convKey) {
        String key = StrUtil.join(":", RedisKey.IM_PRIVATE_MESSAGE_MAX_SEQ, convKey);
        Long seqNo = redisTemplate.opsForValue().increment(key);
        if (seqNo > 1L) {
            return seqNo;
        }
        redisTemplate.delete(key);
        RLock lock = redissonClient.getLock(RedisKey.IM_LOCK_PRIVATE_MESSAGE_MAX_SEQ);
        lock.lock();
        try {
            //Double check
            if (redisTemplate.hasKey(key)) {
                return redisTemplate.opsForValue().increment(key);
            }
            LambdaQueryWrapper<PrivateMessage> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(PrivateMessage::getConvKey, convKey);
            wrapper.orderByDesc(PrivateMessage::getSeqNo);
            wrapper.last("limit 1");
            PrivateMessage lastMessage = this.getOne(wrapper);
            seqNo = 1L;
            if (!Objects.isNull(lastMessage) && !Objects.isNull(lastMessage.getSeqNo())) {
                seqNo += lastMessage.getSeqNo();
            }
            redisTemplate.opsForValue().set(key, seqNo);
            return seqNo;
        } finally {
            lock.unlock();
        }
    }

    Long findMaxMessageId(String convKey) {
        String key = StrUtil.join(":", RedisKey.IM_PRIVATE_MESSAGE_MAX_ID, convKey);
        Object id = redisTemplate.opsForValue().get(key);
        if (!Objects.isNull(id)) {
            return Long.parseLong(id.toString());
        }
        LambdaQueryWrapper<PrivateMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PrivateMessage::getConvKey, convKey);
        wrapper.orderByDesc(PrivateMessage::getId);
        wrapper.last("limit 1");
        PrivateMessage message = this.getOne(wrapper);
        if (!Objects.isNull(message)) {
            redisTemplate.opsForValue().set(key, message.getId());
            return message.getId();
        }
        return -1L;
    }

    private void validMessage(PrivateMessageDTO dto) {
        // 文字消息-长度校验
        if (MessageType.TEXT.code().equals(dto.getType()) && dto.getContent().length() > Constant.MAX_MESSAGE_LENGTH) {
            throw new GlobalException(String.format("消息长度不能大于%s个字符", Constant.MAX_MESSAGE_LENGTH));
        }
        try {
            // 非文字消息-保证数据格式是json,防止前端报错
            if (!MessageType.TEXT.code().equals(dto.getType())) {
                JSON.parse(dto.getContent());
            }
        } catch (Exception e) {
            throw new GlobalException("消息格式异常");
        }
    }

    List<PrivateMessage> appendLastMessageInConversation(List<PrivateMessage> messages, Long minId) {
        UserSession session = SessionContext.getSession();
        List<Long> fIds = friendService.findFriendIds();
        Set<Long> existIds = messages.stream().map(m -> getFriendId(session, m)).collect(Collectors.toSet());
        // 移除已经拉到消息的会话
        fIds.removeAll(existIds);
        if (fIds.isEmpty()) {
            return messages;
        }
        List<String> keys =
                fIds.stream().map(id -> buildMaxMessageIdKey(session.getUserId(), id)).collect(Collectors.toList());
        List<Object> maxMessageIds = redisTemplate.opsForValue().multiGet(keys);
        maxMessageIds =
                maxMessageIds.stream().filter(id -> !Objects.isNull(id) && Long.parseLong(id.toString()) > minId)
                        .collect(Collectors.toList());
        if (maxMessageIds.isEmpty()) {
            return messages;
        }
        LambdaQueryWrapper<PrivateMessage> wrapper = Wrappers.lambdaQuery();
        Date minDate = DateUtils.addDays(new Date(), Math.toIntExact(-Constant.MAX_OFFLINE_MESSAGE_DAYS));
        wrapper.ge(PrivateMessage::getSendTime, minDate);
        wrapper.in(PrivateMessage::getId, maxMessageIds);
        List<PrivateMessage> lastMessages = this.list(wrapper);
        messages.addAll(lastMessages);
        return messages;
    }

    private Boolean isDeleteMessage(PrivateMessage message, List<MessageDeletion> deletions) {
        return deletions.stream().anyMatch(deletion -> {
            if (!message.getSendId().equals(deletion.getChatId()) && !message.getRecvId()
                    .equals(deletion.getChatId())) {
                return false;
            }
            if (DeleteType.BY_MESSAGE.getCode().equals(deletion.getDeleteType())) {
                return deletion.getMessageId().equals(message.getId());
            }
            return false;
        });
    }

    private Boolean isDeleteChat(PrivateMessage message, List<MessageDeletion> deletions) {
        return deletions.stream().anyMatch(deletion -> {
            if (!message.getSendId().equals(deletion.getChatId()) && !message.getRecvId()
                    .equals(deletion.getChatId())) {
                return false;
            }
            if (DeleteType.BY_CHAT.getCode().equals(deletion.getDeleteType())) {
                return deletion.getMessageId() >= message.getId();
            }
            return false;
        });
    }

    private Long getFriendId(UserSession session, PrivateMessage message) {
        return session.getUserId().equals(message.getSendId()) ? message.getRecvId() : message.getSendId();
    }

    private String buildMaxMessageIdKey(Long userId1, Long userId2) {
        return StrUtil.join(":", RedisKey.IM_PRIVATE_MESSAGE_MAX_ID, ConvUtil.buildConvKey(userId1, userId2));
    }
}

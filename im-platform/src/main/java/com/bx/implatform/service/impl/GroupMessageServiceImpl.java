package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.GroupMessageDTO;
import com.bx.implatform.entity.Group;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.GroupMessageMapper;
import com.bx.implatform.service.IGroupMemberService;
import com.bx.implatform.service.IGroupMessageService;
import com.bx.implatform.service.IGroupService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.SensitiveFilterUtil;
import com.bx.implatform.vo.GroupMessageVO;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage> implements IGroupMessageService {
    private final IGroupService groupService;
    private final IGroupMemberService groupMemberService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IMClient imClient;
    private final SensitiveFilterUtil sensitiveFilterUtil;

    @Override
    public Long sendMessage(GroupMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = groupService.getById(dto.getGroupId());
        if (Objects.isNull(group)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (Boolean.TRUE.equals(group.getDeleted())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊已解散");
        }
        // 是否在群聊里面
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || Boolean.TRUE.equals(member.getQuit())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法发送消息");
        }
        // 群聊成员列表
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(dto, GroupMessage.class);
        msg.setSendId(session.getUserId());
        msg.setSendTime(new Date());
        msg.setSendNickName(member.getAliasName());
        if (CollectionUtil.isNotEmpty(dto.getAtUserIds())) {
            msg.setAtUserIds(StrUtil.join(",", dto.getAtUserIds()));
        }
        this.save(msg);
        // 过滤消息内容
        String content = sensitiveFilterUtil.filter(dto.getContent());
        msg.setContent(content);
        // 群发
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        msgInfo.setAtUserIds(dto.getAtUserIds());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setSendResult(false);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), dto.getGroupId(), dto.getContent());
        return msg.getId();
    }

    @Override
    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupMessage msg = this.getById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }
        // 判断是否在群里
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || Boolean.TRUE.equals(member.getQuit())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法撤回消息");
        }
        // 修改数据库
        msg.setStatus(MessageStatus.RECALL.code());
        this.updateById(msg);
        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        // 不用发给自己
        userIds = userIds.stream().filter(uid -> !session.getUserId().equals(uid)).collect(Collectors.toList());
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        msgInfo.setType(MessageType.RECALL.code());
        String content = String.format("'%s'撤回了一条消息", member.getAliasName());
        msgInfo.setContent(content);
        msgInfo.setSendTime(new Date());

        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);

        // 推给自己其他终端
        msgInfo.setContent("你撤回了一条消息");
        sendMessage.setSendToSelf(true);
        sendMessage.setRecvIds(Collections.emptyList());
        sendMessage.setRecvTerminals(Collections.emptyList());
        imClient.sendGroupMessage(sendMessage);
        log.info("撤回群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), msg.getGroupId(), msg.getContent());
    }


    @Override
    public List<GroupMessageVO> loadMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
        if (CollectionUtil.isEmpty(members)) {
            return new ArrayList<>();
        }
        Map<Long, GroupMember> groupMemberMap = CollStreamUtil.toIdentityMap(members, GroupMember::getGroupId);
        Set<Long> groupIds = groupMemberMap.keySet();
        // 只能拉取最近1个月的
        Date minDate = DateUtils.addMonths(new Date(), -1);
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.gt(GroupMessage::getId, minId).gt(GroupMessage::getSendTime, minDate).in(GroupMessage::getGroupId, groupIds)
                .ne(GroupMessage::getStatus, MessageStatus.RECALL.code()).orderByAsc(GroupMessage::getId).last("limit 100");

        List<GroupMessage> messages = this.list(wrapper);
        // 转成vo
        List<GroupMessageVO> vos = messages.stream()
                .filter(m -> {
                    //排除加群之前的消息
                    GroupMember member = groupMemberMap.get(m.getGroupId());
                    return Objects.nonNull(member) && DateUtil.compare(member.getCreatedTime(), m.getSendTime()) <= 0;
                })
                .map(m -> {
                    GroupMessageVO vo = BeanUtils.copyProperties(m, GroupMessageVO.class);
                    // 被@用户列表
                    if (StringUtils.isNotBlank(m.getAtUserIds()) && Objects.nonNull(vo)) {
                        List<String> atIds = Splitter.on(",").trimResults().splitToList(m.getAtUserIds());
                        vo.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
                    }
                    return vo;
                }).collect(Collectors.toList());
        // 通过群聊对消息进行分组
        Map<Long, List<GroupMessageVO>> messageGroupMap = vos.stream().collect(Collectors.groupingBy(GroupMessageVO::getGroupId));
        messageGroupMap.forEach((groupId, messageVos) -> {
            // 填充消息状态
            String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
            Object o = redisTemplate.opsForHash().get(key, session.getUserId().toString());
            long readedMaxId = Objects.isNull(o) ? -1 : Long.parseLong(o.toString());
            messageVos.forEach(messageVo -> messageVo.setStatus(readedMaxId >= messageVo.getId() ? MessageStatus.READED.code() : MessageStatus.UNSEND.code()));
            // 针对回执消息填充已读人数
            List<GroupMessageVO> receiptMessageVos = messageVos.stream().filter(GroupMessageVO::getReceipt).collect(Collectors.toList());
            if (!receiptMessageVos.isEmpty()) {
                Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
                receiptMessageVos.forEach(receiptMessageVo -> {
                    int count = getReadedUserIds(maxIdMap, receiptMessageVo.getId(),receiptMessageVo.getSendId()).size();
                    receiptMessageVo.setReadedCount(count);
                });
            }
        });
        return vos;
    }

    @Override
    public void readedMessage(Long groupId) {
        UserSession session = SessionContext.getSession();
        // 取出最后的消息id
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GroupMessage::getGroupId, groupId)
                .orderByDesc(GroupMessage::getId)
                .last("limit 1")
                .select(GroupMessage::getId);
        GroupMessage message = this.getOne(wrapper);
        if (Objects.isNull(message)) {
            return;
        }
        // 推送消息给自己的其他终端,同步清空会话列表中的未读数量
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setGroupId(groupId);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendGroupMessage(sendMessage);
        // 已读消息key
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        // 原来的已读消息位置
        Object maxReadedId = redisTemplate.opsForHash().get(key, session.getUserId().toString());
        // 记录已读消息位置
        redisTemplate.opsForHash().put(key, session.getUserId().toString(), message.getId());
        // 推送消息回执，刷新已读人数显示
        wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GroupMessage::getGroupId, groupId);
        wrapper.gt(!Objects.isNull(maxReadedId), GroupMessage::getId, maxReadedId);
        wrapper.le(!Objects.isNull(maxReadedId), GroupMessage::getId, message.getId());
        wrapper.eq(GroupMessage::getReceipt, true);
        List<GroupMessage> receiptMessages = this.list(wrapper);
        if (CollectionUtil.isNotEmpty(receiptMessages)) {
            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
            Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
            for (GroupMessage receiptMessage : receiptMessages) {
                Integer readedCount = getReadedUserIds(maxIdMap, receiptMessage.getId(),receiptMessage.getSendId()).size();
                msgInfo = new GroupMessageVO();
                msgInfo.setId(receiptMessage.getId());
                msgInfo.setGroupId(groupId);
                msgInfo.setReadedCount(readedCount);
                msgInfo.setType(MessageType.RECEIPT.code());;
                sendMessage = new IMGroupMessage<>();
                sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                sendMessage.setRecvIds(userIds);
                sendMessage.setData(msgInfo);
                sendMessage.setSendToSelf(false);
                sendMessage.setSendResult(false);
                imClient.sendGroupMessage(sendMessage);
            }
        }
    }

    @Override
    public List<Long> findReadedUsers(Long groupId, Long messageId) {
        GroupMessage message = this.getById(messageId);
        if (Objects.isNull(message)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        // 已读位置key
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        // 一次获取所有用户的已读位置
        Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
        // 返回已读用户的id集合
        return getReadedUserIds(maxIdMap, message.getId(),message.getSendId());
    }

    @Override
    public List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
        // 群聊成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, userId);
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊中");
        }
        // 查询聊天记录，只查询加入群聊时间之后的消息
        QueryWrapper<GroupMessage> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMessage::getGroupId, groupId).gt(GroupMessage::getSendTime, member.getCreatedTime())
                .ne(GroupMessage::getStatus, MessageStatus.RECALL.code()).orderByDesc(GroupMessage::getId).last("limit " + stIdx + "," + size);

        List<GroupMessage> messages = this.list(wrapper);
        List<GroupMessageVO> messageInfos =
                messages.stream().map(m -> BeanUtils.copyProperties(m, GroupMessageVO.class)).collect(Collectors.toList());
        log.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, messageInfos.size());
        return messageInfos;
    }

    private List<Long> getReadedUserIds(Map<Object, Object> maxIdMap, Long messageId, Long sendId) {
        List<Long> userIds = new LinkedList<>();
        maxIdMap.forEach((k, v) -> {
            Long userId = Long.valueOf(k.toString());
            Long maxId = Long.valueOf(v.toString());
            // 发送者不计入已读人数
            if (!sendId.equals(userId) && maxId >= messageId) {
                userIds.add(userId);
            }
        });
        return userIds;
    }


}

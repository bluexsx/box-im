package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
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
import com.bx.implatform.util.DateTimeUtils;
import com.bx.implatform.vo.GroupMessageVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage> implements IGroupMessageService {
    private IGroupService groupService;
    private IGroupMemberService groupMemberService;
    private RedisTemplate<String, Object> redisTemplate;
    private IMClient imClient;


    @Override
    public Long sendMessage(GroupMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = groupService.getById(dto.getGroupId());
        if (Objects.isNull(group)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊已解散");
        }
        // 是否在群聊里面
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        if (Objects.isNull(member)||member.getQuit()) {
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
        if(CollectionUtil.isNotEmpty(dto.getAtUserIds())){
            msg.setAtUserIds(StrUtil.join(",",dto.getAtUserIds()));
        }
        this.save(msg);
        // 群发
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        msgInfo.setAtUserIds(dto.getAtUserIds());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
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
        if (Objects.isNull(member)||member.getQuit()) {
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
    public void pullUnreadMessage() {
        UserSession session = SessionContext.getSession();
        List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
        for (GroupMember member : members) {
            // 获取群聊已读的最大消息id，只推送未读消息
            String key = String.join(":", RedisKey.IM_GROUP_READED_POSITION, member.getGroupId().toString(), session.getUserId().toString());
            Integer maxReadedId = (Integer) redisTemplate.opsForValue().get(key);
            LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(GroupMessage::getGroupId, member.getGroupId())
                    .gt(GroupMessage::getSendTime, member.getCreatedTime())
                    .ne(GroupMessage::getSendId, session.getUserId())
                    .ne(GroupMessage::getStatus, MessageStatus.RECALL.code());
            if (maxReadedId != null) {
                wrapper.gt(GroupMessage::getId, maxReadedId);
            }
            wrapper.last("limit 100");
            List<GroupMessage> messages = this.list(wrapper);
            if (messages.isEmpty()) {
                continue;
            }
            // 推送
            for (GroupMessage message : messages) {
                GroupMessageVO msgInfo = BeanUtils.copyProperties(message, GroupMessageVO.class);
                IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
                sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                // 只推给自己当前终端
                sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
                sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
                sendMessage.setData(msgInfo);
                imClient.sendGroupMessage(sendMessage);
            }
            // 发送消息
            log.info("拉取未读群聊消息，用户id:{},群聊id:{},数量:{}", session.getUserId(), member.getGroupId(), messages.size());
        }

    }


    @Override
    public List<GroupMessageVO> loadMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
        List<Long> ids = members.stream().map(GroupMember::getGroupId).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(ids)){
            return new ArrayList<>();
        }
        // 只能拉取最近1个月的
        Date minDate = DateTimeUtils.addMonths(new Date(), -1);
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.gt(GroupMessage::getId, minId)
                .gt(GroupMessage::getSendTime, minDate)
                .in(GroupMessage::getGroupId, ids)
                .ne(GroupMessage::getStatus, MessageStatus.RECALL.code())
                .orderByAsc(GroupMessage::getId)
                .last("limit 100");

        List<GroupMessage> messages = this.list(wrapper);
        // 转成vo
        List<GroupMessageVO> vos = messages.stream().map(m -> {
            GroupMessageVO vo = BeanUtils.copyProperties(m, GroupMessageVO.class);
            // 被@用户列表
            List<String> atIds = Arrays.asList(StrUtil.split(m.getAtUserIds(),","));
            vo.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
        // 消息状态,数据库没有存群聊的消息状态，需要从redis取
        List<String> keys = ids.stream()
                .map(id -> String.join(":", RedisKey.IM_GROUP_READED_POSITION, id.toString(), session.getUserId().toString()))
                .collect(Collectors.toList());
        List<Object> sendPos = redisTemplate.opsForValue().multiGet(keys);
        int idx = 0;
        for (Long id : ids) {
            Object o = sendPos.get(idx);
            Integer sendMaxId = Objects.isNull(o) ? -1 : (Integer) o;
            vos.stream().filter(vo -> vo.getGroupId().equals(id)).forEach(vo -> {
                if (vo.getId() <= sendMaxId) {
                    // 已读
                    vo.setStatus(MessageStatus.READED.code());
                } else {
                    // 未推送
                    vo.setStatus(MessageStatus.UNSEND.code());
                }
            });
            idx++;
        }
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
        if(Objects.isNull(message)){
            return;
        }
        // 推送消息给自己的其他终端
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setGroupId(groupId);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
        // 记录已读消息位置
        String key = StrUtil.join(":",RedisKey.IM_GROUP_READED_POSITION,groupId,session.getUserId());
        redisTemplate.opsForValue().set(key, message.getId());

    }


    @Override
    public List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
        // 群聊成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, userId);
        if (member == null || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊中");
        }
        // 查询聊天记录，只查询加入群聊时间之后的消息
        QueryWrapper<GroupMessage> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMessage::getGroupId, groupId)
                .gt(GroupMessage::getSendTime, member.getCreatedTime())
                .ne(GroupMessage::getStatus, MessageStatus.RECALL.code())
                .orderByDesc(GroupMessage::getId)
                .last("limit " + stIdx + "," + size);

        List<GroupMessage> messages = this.list(wrapper);
        List<GroupMessageVO> messageInfos = messages.stream().map(m -> BeanUtils.copyProperties(m, GroupMessageVO.class)).collect(Collectors.toList());
        log.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, messageInfos.size());
        return messageInfos;
    }


}

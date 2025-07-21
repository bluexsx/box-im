package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.util.CommaTextUtils;
import com.bx.implatform.annotation.RedisLock;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.GroupDndDTO;
import com.bx.implatform.dto.GroupInviteDTO;
import com.bx.implatform.dto.GroupMemberRemoveDTO;
import com.bx.implatform.entity.*;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.GroupMapper;
import com.bx.implatform.mapper.GroupMessageMapper;
import com.bx.implatform.service.FriendService;
import com.bx.implatform.service.GroupMemberService;
import com.bx.implatform.service.GroupService;
import com.bx.implatform.service.UserService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.vo.GroupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP)
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
    private final UserService userService;
    private final GroupMemberService groupMemberService;
    private final GroupMessageMapper groupMessageMapper;
    private final FriendService friendsService;
    private final IMClient imClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public GroupVO createGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        // 保存群组数据
        Group group = BeanUtils.copyProperties(vo, Group.class);
        group.setOwnerId(user.getId());
        this.save(group);
        // 把群主加入群
        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(user.getId());
        member.setHeadImage(user.getHeadImageThumb());
        member.setUserNickName(user.getNickName());
        member.setRemarkNickName(vo.getRemarkNickName());
        member.setRemarkGroupName(vo.getRemarkGroupName());
        groupMemberService.save(member);
        GroupVO groupVo = findById(group.getId());
        // 推送同步消息给自己的其他终端
        sendAddGroupMessage(groupVo, Lists.newArrayList(), true);
        // 返回
        log.info("创建群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());
        return groupVo;
    }

    @CacheEvict(key = "#vo.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO modifyGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        // 校验是不是群主，只有群主能改信息
        Group group = this.getAndCheckById(vo.getId());
        // 更新成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(vo.getId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您不是群聊的成员");
        }
        member.setRemarkNickName(vo.getRemarkNickName());
        member.setRemarkGroupName(vo.getRemarkGroupName());
        groupMemberService.updateById(member);
        // 群主有权修改群基本信息
        if (group.getOwnerId().equals(session.getUserId())) {
            group = BeanUtils.copyProperties(vo, Group.class);
            this.updateById(group);
        }
        log.info("修改群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());
        return convert(group, member);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#groupId")
    @Override
    public void deleteGroup(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if (!group.getOwnerId().equals(session.getUserId())) {
            throw new GlobalException("只有群主才有权限解除群聊");
        }
        // 群聊用户id
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        // 逻辑删除群数据
        group.setDissolve(true);
        this.updateById(group);
        // 删除成员数据
        groupMemberService.removeByGroupId(groupId);
        // 清理已读缓存
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        redisTemplate.delete(key);
        // 推送解散群聊提示
        String content = String.format("'%s'解散了群聊", session.getNickName());
        this.sendTipMessage(groupId, userIds, content, true);
        // 推送同步消息
        this.sendDelGroupMessage(groupId, userIds);
        log.info("删除群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());
    }

    @Override
    public void quitGroup(Long groupId) {
        Long userId = SessionContext.getSession().getUserId();
        Group group = this.getById(groupId);
        if (group.getOwnerId().equals(userId)) {
            throw new GlobalException("您是群主，不可退出群聊");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId, userId);
        // 清理已读缓存
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        redisTemplate.opsForHash().delete(key, userId.toString());
        // 推送退出群聊提示
        this.sendTipMessage(groupId, List.of(userId), "您已退出群聊", false);
        // 推送同步消息
        this.sendDelGroupMessage(groupId, List.of(userId));
        log.info("退出群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), userId);
    }

    @Override
    public void kickGroup(Long groupId, Long userId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getAndCheckById(groupId);
        if (!group.getOwnerId().equals(session.getUserId())) {
            throw new GlobalException("您不是群主，没有权限踢人");
        }
        if (userId.equals(session.getUserId())) {
            throw new GlobalException("亲，不能移除自己哟");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId, userId);
        // 清理已读缓存
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        redisTemplate.opsForHash().delete(key, userId.toString());
        // 推送踢出群聊提示
        this.sendTipMessage(groupId, List.of(userId), "您已被移出群聊", false);
        // 推送同步消息
        this.sendDelGroupMessage(groupId, List.of(userId));
        log.info("踢出群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), userId);
    }

    @Override
    public void removeGroupMembers(GroupMemberRemoveDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = this.getAndCheckById(dto.getGroupId());
        if (!group.getOwnerId().equals(session.getUserId())) {
            throw new GlobalException("您没有权限");
        }
        if (dto.getUserIds().contains(group.getOwnerId())) {
            throw new GlobalException("不允许移除群主");
        }
        if (dto.getUserIds().contains(session.getUserId())) {
            throw new GlobalException("不允许移除自己");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserIds(dto.getGroupId(), dto.getUserIds());
        // 清理已读缓存
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, dto.getGroupId());
        dto.getUserIds().forEach(id -> redisTemplate.opsForHash().delete(key, id.toString()));
        // 推送踢出群聊提示
        this.sendTipMessage(dto.getGroupId(), dto.getUserIds(), "您已被移出群聊", false);
        // 推送同步消息
        this.sendDelGroupMessage(dto.getGroupId(), dto.getUserIds());
        log.info("踢出群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), dto.getUserIds());
    }

    @Override
    public GroupVO findById(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = super.getById(groupId);
        if (Objects.isNull(group)) {
            throw new GlobalException("群组不存在");
        }
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (Objects.isNull(member)) {
            throw new GlobalException("您未加入群聊");
        }
        return convert(group, member);
    }

    @Cacheable(key = "#groupId")
    @Override
    public Group getAndCheckById(Long groupId) {
        Group group = super.getById(groupId);
        if (Objects.isNull(group)) {
            throw new GlobalException("群组不存在");
        }
        if (group.getDissolve()) {
            throw new GlobalException("群组'" + group.getName() + "'已解散");
        }
        if (group.getIsBanned()) {
            throw new GlobalException("群组'" + group.getName() + "'已被封禁,原因:" + group.getReason());
        }
        return group;
    }

    @Override
    public List<GroupVO> findGroups() {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群id列表
        List<GroupMember> groupMembers = groupMemberService.findByUserId(session.getUserId());
        // 一个月内退的群可能存在退群前的离线消息,一并返回作为前端缓存
        groupMembers.addAll(groupMemberService.findQuitInMonth(session.getUserId()));
        if (groupMembers.isEmpty()) {
            return new LinkedList<>();
        }
        // 拉取群列表
        List<Long> ids = groupMembers.stream().map((GroupMember::getGroupId)).collect(Collectors.toList());
        LambdaQueryWrapper<Group> groupWrapper = Wrappers.lambdaQuery();
        groupWrapper.in(Group::getId, ids);
        List<Group> groups = this.list(groupWrapper);
        // 转vo
        return groups.stream().map(group -> {
            GroupMember member =
                groupMembers.stream().filter(m -> group.getId().equals(m.getGroupId())).findFirst().get();
            return convert(group, member);
        }).collect(Collectors.toList());
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_GROUP_ENTER,key = "#dto.getGroupId()")
    @Override
    public void invite(GroupInviteDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = this.getAndCheckById(dto.getGroupId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        if (Objects.isNull(group) || member.getQuit()) {
            throw new GlobalException("您不在群聊中,邀请失败");
        }
        // 群聊人数校验
        List<GroupMember> members = groupMemberService.findByGroupId(dto.getGroupId());
        long size = members.stream().filter(m -> !m.getQuit()).count();
        if (dto.getFriendIds().size() + size > Constant.MAX_LARGE_GROUP_MEMBER) {
            throw new GlobalException("群聊人数不能大于" + Constant.MAX_LARGE_GROUP_MEMBER + "人");
        }
        // 找出好友信息
        List<Friend> friends = friendsService.findByFriendIds(dto.getFriendIds());
        if (dto.getFriendIds().size() != friends.size()) {
            throw new GlobalException("部分用户不是您的好友，邀请失败");
        }
        // 批量保存成员数据
        List<GroupMember> groupMembers = friends.stream().map(f -> {
            Optional<GroupMember> optional =
                members.stream().filter(m -> m.getUserId().equals(f.getFriendId())).findFirst();
            GroupMember groupMember = optional.orElseGet(GroupMember::new);
            groupMember.setGroupId(dto.getGroupId());
            groupMember.setUserId(f.getFriendId());
            groupMember.setUserNickName(f.getFriendNickName());
            groupMember.setHeadImage(f.getFriendHeadImage());
            groupMember.setCreatedTime(new Date());
            groupMember.setQuit(false);
            return groupMember;
        }).collect(Collectors.toList());
        if (!groupMembers.isEmpty()) {
            groupMemberService.saveOrUpdateBatch(group.getId(), groupMembers);
        }
        // 推送同步消息给被邀请人
        for (GroupMember m : groupMembers) {
            GroupVO groupVo = convert(group, m);
            sendAddGroupMessage(groupVo, List.of(m.getUserId()), false);
        }
        // 推送进入群聊消息
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(dto.getGroupId());
        String memberNames = groupMembers.stream().map(GroupMember::getShowNickName).collect(Collectors.joining(","));
        String content = String.format("'%s'邀请'%s'加入了群聊", session.getNickName(), memberNames);
        this.sendTipMessage(dto.getGroupId(), userIds, content, true);
        log.info("邀请进入群聊，群聊id:{},群聊名称:{},被邀请用户id:{}", group.getId(), group.getName(),
            dto.getFriendIds());
    }

    @Override
    public List<GroupMemberVO> findGroupMembers(Long groupId) {
        Group group = getAndCheckById(groupId);
        List<GroupMember> members = groupMemberService.findByGroupId(groupId);
        List<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        return members.stream().map(m -> {
            GroupMemberVO vo = BeanUtils.copyProperties(m, GroupMemberVO.class);
            vo.setShowNickName(m.getShowNickName());
            vo.setShowGroupName(StrUtil.blankToDefault(m.getRemarkGroupName(), group.getName()));
            vo.setOnline(onlineUserIds.contains(m.getUserId()));
            return vo;
        }).sorted((m1, m2) -> m2.getOnline().compareTo(m1.getOnline())).collect(Collectors.toList());
    }

    @Override
    public void setDnd(GroupDndDTO dto) {
        UserSession session = SessionContext.getSession();
        groupMemberService.setDnd(dto.getGroupId(), session.getUserId(), dto.getIsDnd());
        // 推送同步消息
        sendSyncDndMessage(dto.getGroupId(), dto.getIsDnd());
    }


    private void sendTipMessage(Long groupId, List<Long> recvIds, String content, Boolean sendToAll) {
        UserSession session = SessionContext.getSession();
        // 消息入库
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setType(MessageType.TIP_TEXT.code());
        message.setStatus(MessageStatus.PENDING.code());
        message.setSendTime(new Date());
        message.setSendNickName(session.getNickName());
        message.setGroupId(groupId);
        message.setSendId(session.getUserId());
        message.setRecvIds(sendToAll ? "" : CommaTextUtils.asText(recvIds));
        groupMessageMapper.insert(message);
        // 推送
        GroupMessageVO msgInfo = BeanUtils.copyProperties(message, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        if (CollUtil.isEmpty(recvIds)) {
            // 为空表示向全体发送
            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
            sendMessage.setRecvIds(userIds);
        } else {
            sendMessage.setRecvIds(recvIds);
        }
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private GroupVO convert(Group group, GroupMember member) {
        GroupVO vo = BeanUtils.copyProperties(group, GroupVO.class);
        vo.setRemarkGroupName(member.getRemarkGroupName());
        vo.setRemarkNickName(member.getRemarkNickName());
        vo.setShowNickName(member.getShowNickName());
        vo.setShowGroupName(StrUtil.blankToDefault(member.getRemarkGroupName(), group.getName()));
        vo.setQuit(member.getQuit());
        vo.setIsDnd(member.getIsDnd());
        return vo;
    }

    private void sendAddGroupMessage(GroupVO group, List<Long> recvIds, Boolean sendToSelf) {
        UserSession session = SessionContext.getSession();
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setContent(JSON.toJSONString(group));
        msgInfo.setType(MessageType.GROUP_NEW.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setGroupId(group.getId());
        msgInfo.setSendId(session.getUserId());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(recvIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(sendToSelf);
        imClient.sendGroupMessage(sendMessage);
    }

    private void sendDelGroupMessage(Long groupId, List<Long> recvIds) {
        UserSession session = SessionContext.getSession();
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.GROUP_DEL.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setGroupId(groupId);
        msgInfo.setSendId(session.getUserId());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(recvIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private void sendSyncDndMessage(Long groupId, Boolean isDnd) {
        UserSession session = SessionContext.getSession();
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.GROUP_DND.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setGroupId(groupId);
        msgInfo.setSendId(session.getUserId());
        msgInfo.setContent(isDnd.toString());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
    }

}

package com.bx.implatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.bx.imclient.IMClient;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.annotation.RedisLock;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.*;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.service.IGroupMemberService;
import com.bx.implatform.service.IWebrtcGroupService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.session.WebrtcGroupSession;
import com.bx.implatform.session.WebrtcUserInfo;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.vo.WebrtcGroupFailedVO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 群语音通话服务类,所有涉及修改webtcSession的方法都要挂分布式锁
 *
 * @author: blue
 * @date: 2024-06-01
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebrtcGroupServiceImpl implements IWebrtcGroupService {

    private final IGroupMemberService groupMemberService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IMClient imClient;
    /**
     * 最多支持8路视频
     */
    private final int maxChannel = 9;

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#dto.groupId")
    @Override
    public void setup(WebrtcGroupSetupDTO dto) {
        UserSession userSession = SessionContext.getSession();
        List<Long> userIds = getRecvIds(dto.getUserInfos());
        String key = buildWebrtcSessionKey(dto.getGroupId());
        if (redisTemplate.hasKey(key)) {
            throw new GlobalException("该群聊已存在一个通话");
        }
        if (!groupMemberService.isInGroup(dto.getGroupId(), userIds)) {
            throw new GlobalException("存在不在群聊中的用户");
        }
        // 离线用户处理
        List<WebrtcUserInfo> userInfos = new LinkedList<>();
        List<Long> offlineUserIds = new LinkedList<>();
        for (WebrtcUserInfo userInfo : dto.getUserInfos()) {
            if (imClient.isOnline(userInfo.getId())) {
                userInfos.add(userInfo);
            } else {
                offlineUserIds.add(userInfo.getId());
            }
        }
        // 创建通话session
        WebrtcGroupSession webrtcSession = new WebrtcGroupSession();
        IMUserInfo userInfo = new IMUserInfo(userSession.getUserId(), userSession.getTerminal());
        webrtcSession.setHost(userInfo);
        webrtcSession.setUserInfos(userInfos);
        webrtcSession.getInChatUsers().add(userInfo);
        saveWebrtcSession(dto.getGroupId(), webrtcSession);
        // 向发起邀请者推送邀请失败消息
        if(!offlineUserIds.isEmpty()){
            WebrtcGroupFailedVO vo = new WebrtcGroupFailedVO();
            vo.setUserIds(offlineUserIds);
            vo.setReason("用户不在线");
            sendMessage2(MessageType.RTC_GROUP_FAILED, dto.getGroupId(), userInfo, JSON.toJSONString(vo));
        }
        // 向被邀请的用户广播消息，发起呼叫
        List<Long> recvIds = getRecvIds(dto.getUserInfos());
        sendMessage1(MessageType.RTC_GROUP_SETUP, dto.getGroupId(), recvIds, JSON.toJSONString(userInfos));
        log.info("发起群通话,userId:{},groupId:{}", userSession.getUserId(), dto.getGroupId());
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#groupId")
    @Override
    public void accept(Long groupId) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(groupId);
        // 校验
        if (!isExist(webrtcSession, userSession.getUserId())) {
            throw new GlobalException("您未被邀请通话");
        }
        // 防止重复进入
        if (isInchat(webrtcSession, userSession.getUserId())) {
            throw new GlobalException("您已在通话中");
        }
        // 将当前用户加入通话用户列表中
        webrtcSession.getInChatUsers().add(new IMUserInfo(userSession.getUserId(), userSession.getTerminal()));
        saveWebrtcSession(groupId, webrtcSession);
        // 广播信令
        List<Long> recvIds = getRecvIds(webrtcSession.getUserInfos());
        sendMessage1(MessageType.RTC_GROUP_ACCEPT, groupId, recvIds, "");
        log.info("加入群通话,userId:{},groupId:{}", userSession.getUserId(), groupId);
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#groupId")
    @Override
    public void reject(Long groupId) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(groupId);
        // 校验
        if (!isExist(webrtcSession, userSession.getUserId())) {
            throw new GlobalException("您未被邀请通话");
        }
        // 防止重复进入
        if (isInchat(webrtcSession, userSession.getUserId())) {
            throw new GlobalException("您已在通话中");
        }
        // 将用户从列表中移除
        List<WebrtcUserInfo> userInfos =
            webrtcSession.getUserInfos().stream().filter(user -> !user.getId().equals(userSession.getUserId()))
                .collect(Collectors.toList());
        webrtcSession.setUserInfos(userInfos);
        saveWebrtcSession(groupId, webrtcSession);
        // 广播消息给的所有用户
        List<Long> recvIds = getRecvIds(userInfos);
        sendMessage1(MessageType.RTC_GROUP_REJECT, groupId, recvIds, "");
        log.info("拒绝群通话,userId:{},groupId:{}", userSession.getUserId(), groupId);
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#dto.groupId")
    @Override
    public void failed(WebrtcGroupFailedDTO dto) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(dto.getGroupId());
        // 校验
        if (!isExist(webrtcSession, userSession.getUserId())) {
            return;
        }
        if (isInchat(webrtcSession, userSession.getUserId())) {
            return;
        }
        // 将用户从列表中移除
        List<WebrtcUserInfo> userInfos =
            webrtcSession.getUserInfos().stream().filter(user -> !user.getId().equals(userSession.getUserId()))
                .collect(Collectors.toList());
        webrtcSession.setUserInfos(userInfos);
        saveWebrtcSession(dto.getGroupId(), webrtcSession);
        // 广播信令
        WebrtcGroupFailedVO vo = new WebrtcGroupFailedVO();
        vo.setUserIds(Arrays.asList(userSession.getUserId()));
        vo.setReason(dto.getReason());
        List<Long> recvIds = getRecvIds(userInfos);
        sendMessage1(MessageType.RTC_GROUP_FAILED, dto.getGroupId(), recvIds, JSON.toJSONString(vo));
        log.info("群通话失败,userId:{},groupId:{},原因:{}", userSession.getUserId(), dto.getReason());
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#groupId")
    @Override
    public void join(Long groupId) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(groupId);
        // 校验
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, userSession.getUserId());
        if (Objects.isNull(member)) {
            throw new GlobalException("您不在群里中");
        }
        // 防止重复进入
        if (isInchat(webrtcSession, userSession.getUserId())) {
            throw new GlobalException("您已在通话中");
        }
        WebrtcUserInfo userInfo = new WebrtcUserInfo();
        userInfo.setId(userSession.getUserId());
        userInfo.setNickName(member.getAliasName());
        userInfo.setHeadImage(member.getHeadImage());
        userInfo.setIsCamera(false);
        // 将当前用户加入通话用户列表中
        if (!isExist(webrtcSession, userSession.getUserId())) {
            webrtcSession.getUserInfos().add(userInfo);
        }
        webrtcSession.getInChatUsers().add(new IMUserInfo(userSession.getUserId(), userSession.getTerminal()));
        saveWebrtcSession(groupId, webrtcSession);
        // 广播信令
        List<Long> recvIds = getRecvIds(webrtcSession.getUserInfos());
        sendMessage1(MessageType.RTC_GROUP_JOIN, groupId, recvIds, JSON.toJSONString(userInfo));
        log.info("加入群通话,userId:{},groupId:{}", userSession.getUserId(), groupId);
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#dto.groupId")
    @Override
    public void invite(WebrtcGroupInviteDTO dto) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(dto.getGroupId());
        // 过滤掉已经在通话中的用户
        List<WebrtcUserInfo> userInfos = webrtcSession.getUserInfos();
        // 原用户id
        List<Long> userIds = getRecvIds(userInfos);
        // 离线用户id
        List<Long> offlineUserIds = new LinkedList<>();
        // 新加入的用户
        List<WebrtcUserInfo> newUserInfos = new LinkedList<>();
        for (WebrtcUserInfo userInfo : dto.getUserInfos()) {
            if (isExist(webrtcSession, userInfo.getId())) {
                // 防止重复进入
                continue;
            }
            if (imClient.isOnline(userInfo.getId())) {
                newUserInfos.add(userInfo);
            } else {
                offlineUserIds.add(userInfo.getId());
            }
        }
        // 更新会话信息
        userInfos.addAll(newUserInfos);
        saveWebrtcSession(dto.getGroupId(), webrtcSession);
        // 向发起邀请者推送邀请失败消息
        if(!offlineUserIds.isEmpty()){
            WebrtcGroupFailedVO vo = new WebrtcGroupFailedVO();
            vo.setUserIds(offlineUserIds);
            vo.setReason("用户不在线");
            IMUserInfo reciver = new IMUserInfo(userSession.getUserId(), userSession.getTerminal());
            sendMessage2(MessageType.RTC_GROUP_FAILED, dto.getGroupId(), reciver, JSON.toJSONString(vo));
        }
        // 向被邀请的发起呼叫
        List<Long> newUserIds = getRecvIds(newUserInfos);
        sendMessage1(MessageType.RTC_GROUP_SETUP, dto.getGroupId(), newUserIds, JSON.toJSONString(userInfos));
        // 向已在通话中的用户同步新邀请的用户信息
        sendMessage1(MessageType.RTC_GROUP_INVITE, dto.getGroupId(), userIds, JSON.toJSONString(newUserInfos));
        log.info("邀请加入群通话,userId:{},groupId:{},邀请用户:{}", userSession.getUserId(), dto.getGroupId(),
            newUserIds);
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#groupId")
    @Override
    public void cancel(Long groupId) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(groupId);
        if (!userSession.getUserId().equals(webrtcSession.getHost().getId())) {
            throw new GlobalException("只有发起人可以取消通话");
        }
        // 移除rtc session
        String key = buildWebrtcSessionKey(groupId);
        redisTemplate.delete(key);
        // 广播消息给的所有用户
        List<Long> recvIds = getRecvIds(webrtcSession.getUserInfos());
        sendMessage1(MessageType.RTC_GROUP_CANCEL, groupId, recvIds, "");
        log.info("发起人取消群通话,userId:{},groupId:{}", userSession.getUserId(), groupId);
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#groupId")
    @Override
    public void quit(Long groupId) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(groupId);
        // 将用户从列表中移除
        List<IMUserInfo> inChatUsers =
            webrtcSession.getInChatUsers().stream().filter(user -> !user.getId().equals(userSession.getUserId()))
                .collect(Collectors.toList());
        List<WebrtcUserInfo> userInfos =
            webrtcSession.getUserInfos().stream().filter(user -> !user.getId().equals(userSession.getUserId()))
                .collect(Collectors.toList());
        // 如果群聊中没有人已经接受了通话，则直接取消整个通话
        if (inChatUsers.isEmpty() || userInfos.isEmpty()) {
            // 移除rtc session
            String key = buildWebrtcSessionKey(groupId);
            redisTemplate.delete(key);
            // 广播给还在呼叫中的用户，取消通话
            List<Long> recvIds = getRecvIds(webrtcSession.getUserInfos());
            sendMessage1(MessageType.RTC_GROUP_CANCEL, groupId, recvIds, "");
            log.info("群通话结束,groupId:{}", groupId);
        } else {
            // 更新会话信息
            webrtcSession.setInChatUsers(inChatUsers);
            webrtcSession.setUserInfos(userInfos);
            saveWebrtcSession(groupId, webrtcSession);
            // 广播信令
            List<Long> recvIds = getRecvIds(userInfos);
            sendMessage1(MessageType.RTC_GROUP_QUIT, groupId, recvIds, "");
            log.info("用户退出群通话,userId:{},groupId:{}", userSession.getUserId(), groupId);
        }
    }

    @Override
    public void offer(WebrtcGroupOfferDTO dto) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(dto.getGroupId());
        IMUserInfo userInfo = findInChatUser(webrtcSession, dto.getUserId());
        if (Objects.isNull(userInfo)) {
            log.info("对方未加入群通话,userId:{},对方id:{},groupId:{}", userSession.getUserId(), dto.getUserId(),
                dto.getGroupId());
            return;
        }
        // 推送offer给对方
        sendMessage2(MessageType.RTC_GROUP_OFFER, dto.getGroupId(), userInfo, dto.getOffer());
        log.info("推送offer信息,userId:{},对方id:{},groupId:{}", userSession.getUserId(), dto.getUserId(),
            dto.getGroupId());
    }

    @Override
    public void answer(WebrtcGroupAnswerDTO dto) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(dto.getGroupId());
        IMUserInfo userInfo = findInChatUser(webrtcSession, dto.getUserId());
        if (Objects.isNull(userInfo)) {
            // 对方未加入群通话
            log.info("对方未加入群通话,userId:{},对方id:{},groupId:{}", userSession.getUserId(), dto.getUserId(),
                dto.getGroupId());
            return;
        }
        // 推送answer信息给对方
        sendMessage2(MessageType.RTC_GROUP_ANSWER, dto.getGroupId(), userInfo, dto.getAnswer());
        log.info("回复answer信息,userId:{},对方id:{},groupId:{}", userSession.getUserId(), dto.getUserId(),
            dto.getGroupId());
    }

    @Override
    public void candidate(WebrtcGroupCandidateDTO dto) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(dto.getGroupId());
        IMUserInfo userInfo = findInChatUser(webrtcSession, dto.getUserId());
        if (Objects.isNull(userInfo)) {
            // 对方未加入群通话
            log.info("对方未加入群通话,无法同步candidate,userId:{},remoteUserId:{},groupId:{}", userSession.getUserId(),
                dto.getUserId(), dto.getGroupId());
            return;
        }
        // 推送candidate信息给对方
        sendMessage2(MessageType.RTC_GROUP_CANDIDATE, dto.getGroupId(), userInfo, dto.getCandidate());
        log.info("同步candidate信息,userId:{},groupId:{}", userSession.getUserId(), dto.getGroupId());
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#dto.groupId")
    @Override
    public void device(WebrtcGroupDeviceDTO dto) {
        UserSession userSession = SessionContext.getSession();
        // 查询会话信息
        WebrtcGroupSession webrtcSession = getWebrtcSession(dto.getGroupId());
        WebrtcUserInfo userInfo = findUserInfo(webrtcSession, userSession.getUserId());
        if (Objects.isNull(userInfo)) {
            throw new GlobalException("您已不在通话中");
        }
        // 更新设备状态
        userInfo.setIsCamera(dto.getIsCamera());
        saveWebrtcSession(dto.getGroupId(), webrtcSession);
        // 广播信令
        List<Long> recvIds = getRecvIds(webrtcSession.getUserInfos());
        sendMessage1(MessageType.RTC_GROUP_DEVICE, dto.getGroupId(), recvIds, JSON.toJSONString(dto));
        log.info("设备操作,userId:{},groupId:{},摄像头:{}", userSession.getUserId(), dto.getGroupId(),
            dto.getIsCamera());
    }

    private WebrtcGroupSession getWebrtcSession(Long groupId) {
        String key = buildWebrtcSessionKey(groupId);
        WebrtcGroupSession webrtcSession = (WebrtcGroupSession)redisTemplate.opsForValue().get(key);
        if (Objects.isNull(webrtcSession)) {
            throw new GlobalException("通话已结束");
        }
        return webrtcSession;
    }

    private void saveWebrtcSession(Long groupId, WebrtcGroupSession webrtcSession) {
        String key = buildWebrtcSessionKey(groupId);
        redisTemplate.opsForValue().set(key, webrtcSession, 2, TimeUnit.HOURS);
    }

    private String buildWebrtcSessionKey(Long groupId) {
        return StrUtil.join(":", RedisKey.IM_WEBRTC_GROUP_SESSION, groupId);
    }

    private IMUserInfo findInChatUser(WebrtcGroupSession webrtcSession, Long userId) {
        for (IMUserInfo userInfo : webrtcSession.getInChatUsers()) {
            if (userInfo.getId().equals(userId)) {
                return userInfo;
            }
        }
        return null;
    }

    private WebrtcUserInfo findUserInfo(WebrtcGroupSession webrtcSession, Long userId) {
        for (WebrtcUserInfo userInfo : webrtcSession.getUserInfos()) {
            if (userInfo.getId().equals(userId)) {
                return userInfo;
            }
        }
        return null;
    }

    private List<Long> getRecvIds(List<WebrtcUserInfo> userInfos) {
        UserSession userSession = SessionContext.getSession();
        return userInfos.stream().map(WebrtcUserInfo::getId).filter(id -> !id.equals(userSession.getUserId()))
            .collect(Collectors.toList());
    }

    private Boolean isInchat(WebrtcGroupSession webrtcSession, Long userId) {
        return webrtcSession.getInChatUsers().stream().anyMatch(user -> user.getId().equals(userId));
    }

    private Boolean isExist(WebrtcGroupSession webrtcSession, Long userId) {
        return webrtcSession.getUserInfos().stream().anyMatch(user -> user.getId().equals(userId));

    }

    private void sendMessage1(MessageType messageType, Long groupId, List<Long> recvIds, String content) {
        UserSession userSession = SessionContext.getSession();
        GroupMessageVO messageInfo = new GroupMessageVO();
        messageInfo.setType(messageType.code());
        messageInfo.setGroupId(groupId);
        messageInfo.setSendId(userSession.getUserId());
        messageInfo.setContent(content);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(userSession.getUserId(), userSession.getTerminal()));
        sendMessage.setRecvIds(recvIds);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        imClient.sendGroupMessage(sendMessage);
    }

    private void sendMessage2(MessageType messageType, Long groupId, IMUserInfo receiver, String content) {
        UserSession userSession = SessionContext.getSession();
        GroupMessageVO messageInfo = new GroupMessageVO();
        messageInfo.setType(messageType.code());
        messageInfo.setGroupId(groupId);
        messageInfo.setSendId(userSession.getUserId());
        messageInfo.setContent(content);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(userSession.getUserId(), userSession.getTerminal()));
        sendMessage.setRecvIds(Arrays.asList(receiver.getId()));
        sendMessage.setRecvTerminals(Arrays.asList(receiver.getTerminal()));
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        imClient.sendGroupMessage(sendMessage);
    }
}

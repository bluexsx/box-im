package com.bx.implatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.bx.imclient.IMClient;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.annotation.RedisLock;
import com.bx.implatform.config.WebrtcConfig;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.*;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.service.IGroupMemberService;
import com.bx.implatform.service.IGroupMessageService;
import com.bx.implatform.service.IWebrtcGroupService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.session.WebrtcGroupSession;
import com.bx.implatform.session.WebrtcUserInfo;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.UserStateUtils;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.vo.WebrtcGroupFailedVO;
import com.bx.implatform.vo.WebrtcGroupInfoVO;
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
    private final IGroupMessageService groupMessageService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IMClient imClient;
    private final UserStateUtils userStateUtils;
    private final WebrtcConfig webrtcConfig;


    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#dto.groupId")
    @Override
    public void setup(WebrtcGroupSetupDTO dto) {
        UserSession userSession = SessionContext.getSession();
        if (dto.getUserInfos().size() > webrtcConfig.getMaxChannel()) {
            throw new GlobalException("最多支持" + webrtcConfig.getMaxChannel() + "人进行通话");
        }
        List<Long> userIds = getRecvIds(dto.getUserInfos());
        if (!groupMemberService.isInGroup(dto.getGroupId(), userIds)) {
            throw new GlobalException("部分用户不在群聊中");
        }
        String key = buildWebrtcSessionKey(dto.getGroupId());
        if (redisTemplate.hasKey(key)) {
            throw new GlobalException("该群聊已存在一个通话");
        }
        // 有效用户
        List<WebrtcUserInfo> userInfos = new LinkedList<>();
        // 离线用户
        List<Long> offlineUserIds = new LinkedList<>();
        // 忙线用户
        List<Long> busyUserIds = new LinkedList<>();
        for (WebrtcUserInfo userInfo : dto.getUserInfos()) {
            if (!imClient.isOnline(userInfo.getId())) {
                //userInfos.add(userInfo);
                offlineUserIds.add(userInfo.getId());
            } else if (userStateUtils.isBusy(userInfo.getId())) {
                busyUserIds.add(userInfo.getId());
            } else {
                userInfos.add(userInfo);
                // 设置用户忙线状态
                userStateUtils.setBusy(userInfo.getId());
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
        if (!offlineUserIds.isEmpty()) {
            WebrtcGroupFailedVO vo = new WebrtcGroupFailedVO();
            vo.setUserIds(offlineUserIds);
            vo.setReason("用户不在线");
            sendRtcMessage2(MessageType.RTC_GROUP_FAILED, dto.getGroupId(), userInfo, JSON.toJSONString(vo));
        }
        if (!busyUserIds.isEmpty()) {
            WebrtcGroupFailedVO vo = new WebrtcGroupFailedVO();
            vo.setUserIds(busyUserIds);
            vo.setReason("用户正忙");
            IMUserInfo reciver = new IMUserInfo(userSession.getUserId(), userSession.getTerminal());
            sendRtcMessage2(MessageType.RTC_GROUP_FAILED, dto.getGroupId(), reciver, JSON.toJSONString(vo));
        }
        // 向被邀请的用户广播消息，发起呼叫
        List<Long> recvIds = getRecvIds(userInfos);
        sendRtcMessage1(MessageType.RTC_GROUP_SETUP, dto.getGroupId(), recvIds, JSON.toJSONString(userInfos));
        // 发送文字提示信息
        WebrtcUserInfo mineInfo = findUserInfo(webrtcSession,userSession.getUserId());
        String content = mineInfo.getNickName() + " 发起了语音通话";
        sendTipMessage(dto.getGroupId(),content);
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
        sendRtcMessage1(MessageType.RTC_GROUP_ACCEPT, groupId, recvIds, "");
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
        // 进入空闲状态
        userStateUtils.setFree(userSession.getUserId());
        // 广播消息给的所有用户
        List<Long> recvIds = getRecvIds(userInfos);
        sendRtcMessage1(MessageType.RTC_GROUP_REJECT, groupId, recvIds, "");
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
        // 进入空闲状态
        userStateUtils.setFree(userSession.getUserId());
        // 广播信令
        WebrtcGroupFailedVO vo = new WebrtcGroupFailedVO();
        vo.setUserIds(Arrays.asList(userSession.getUserId()));
        vo.setReason(dto.getReason());
        List<Long> recvIds = getRecvIds(userInfos);
        sendRtcMessage1(MessageType.RTC_GROUP_FAILED, dto.getGroupId(), recvIds, JSON.toJSONString(vo));
        log.info("群通话失败,userId:{},groupId:{},原因:{}", userSession.getUserId(), dto.getReason());
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#groupId")
    @Override
    public void join(Long groupId) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(groupId);
        if (webrtcSession.getUserInfos().size() >= webrtcConfig.getMaxChannel()) {
            throw new GlobalException("人员已满，无法进入通话");
        }
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, userSession.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
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
        // 进入忙线状态
        userStateUtils.setBusy(userSession.getUserId());
        // 广播信令
        List<Long> recvIds = getRecvIds(webrtcSession.getUserInfos());
        sendRtcMessage1(MessageType.RTC_GROUP_JOIN, groupId, recvIds, JSON.toJSONString(userInfo));
        log.info("加入群通话,userId:{},groupId:{}", userSession.getUserId(), groupId);
    }

    @RedisLock(prefixKey = RedisKey.IM_LOCK_RTC_GROUP, key = "#dto.groupId")
    @Override
    public void invite(WebrtcGroupInviteDTO dto) {
        UserSession userSession = SessionContext.getSession();
        WebrtcGroupSession webrtcSession = getWebrtcSession(dto.getGroupId());
        if (dto.getUserInfos().size() + dto.getUserInfos().size() > webrtcConfig.getMaxChannel()) {
            throw new GlobalException("最多支持" + webrtcConfig.getMaxChannel() + "人进行通话");
        }
        if (!groupMemberService.isInGroup(dto.getGroupId(), getRecvIds(dto.getUserInfos()))) {
            throw new GlobalException("部分用户不在群聊中");
        }
        // 保存开启通话提示消息

        // 过滤掉已经在通话中的用户
        List<WebrtcUserInfo> userInfos = webrtcSession.getUserInfos();
        // 原用户id
        List<Long> userIds = getRecvIds(userInfos);
        // 离线用户id
        List<Long> offlineUserIds = new LinkedList<>();
        // 忙线用户
        List<Long> busyUserIds = new LinkedList<>();
        // 新加入的用户
        List<WebrtcUserInfo> newUserInfos = new LinkedList<>();
        for (WebrtcUserInfo userInfo : dto.getUserInfos()) {
            if (isExist(webrtcSession, userInfo.getId())) {
                // 防止重复进入
                continue;
            }
            if (!imClient.isOnline(userInfo.getId())) {
                offlineUserIds.add(userInfo.getId());
            } else if (userStateUtils.isBusy(userInfo.getId())) {
                busyUserIds.add(userInfo.getId());
            } else {
                // 进入忙线状态
                userStateUtils.setBusy(userInfo.getId());
                newUserInfos.add(userInfo);
            }
        }
        // 更新会话信息
        userInfos.addAll(newUserInfos);
        saveWebrtcSession(dto.getGroupId(), webrtcSession);
        // 向发起邀请者推送邀请失败消息
        if (!offlineUserIds.isEmpty()) {
            WebrtcGroupFailedVO vo = new WebrtcGroupFailedVO();
            vo.setUserIds(offlineUserIds);
            vo.setReason("用户不在线");
            IMUserInfo reciver = new IMUserInfo(userSession.getUserId(), userSession.getTerminal());
            sendRtcMessage2(MessageType.RTC_GROUP_FAILED, dto.getGroupId(), reciver, JSON.toJSONString(vo));
        }
        if (!busyUserIds.isEmpty()) {
            WebrtcGroupFailedVO vo = new WebrtcGroupFailedVO();
            vo.setUserIds(busyUserIds);
            vo.setReason("用户正在忙");
            IMUserInfo reciver = new IMUserInfo(userSession.getUserId(), userSession.getTerminal());
            sendRtcMessage2(MessageType.RTC_GROUP_FAILED, dto.getGroupId(), reciver, JSON.toJSONString(vo));
        }
        // 向被邀请的发起呼叫
        List<Long> newUserIds = getRecvIds(newUserInfos);
        sendRtcMessage1(MessageType.RTC_GROUP_SETUP, dto.getGroupId(), newUserIds, JSON.toJSONString(userInfos));
        // 向已在通话中的用户同步新邀请的用户信息
        sendRtcMessage1(MessageType.RTC_GROUP_INVITE, dto.getGroupId(), userIds, JSON.toJSONString(newUserInfos));
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
        // 进入空闲状态
        webrtcSession.getUserInfos().forEach(user -> userStateUtils.setFree(user.getId()));
        // 广播消息给的所有用户
        List<Long> recvIds = getRecvIds(webrtcSession.getUserInfos());
        sendRtcMessage1(MessageType.RTC_GROUP_CANCEL, groupId, recvIds, "");
        // 发送文字提示信息
        sendTipMessage(groupId,"通话结束");
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
            // 进入空闲状态
            webrtcSession.getUserInfos().forEach(user -> userStateUtils.setFree(user.getId()));
            // 广播给还在呼叫中的用户，取消通话
            List<Long> recvIds = getRecvIds(webrtcSession.getUserInfos());
            sendRtcMessage1(MessageType.RTC_GROUP_CANCEL, groupId, recvIds, "");
            // 发送文字提示信息
            sendTipMessage(groupId,"通话结束");
            log.info("群通话结束,groupId:{}", groupId);
        } else {
            // 更新会话信息
            webrtcSession.setInChatUsers(inChatUsers);
            webrtcSession.setUserInfos(userInfos);
            saveWebrtcSession(groupId, webrtcSession);
            // 进入空闲状态
            userStateUtils.setFree(userSession.getUserId());
            // 广播信令
            List<Long> recvIds = getRecvIds(userInfos);
            sendRtcMessage1(MessageType.RTC_GROUP_QUIT, groupId, recvIds, "");
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
        sendRtcMessage2(MessageType.RTC_GROUP_OFFER, dto.getGroupId(), userInfo, dto.getOffer());
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
        sendRtcMessage2(MessageType.RTC_GROUP_ANSWER, dto.getGroupId(), userInfo, dto.getAnswer());
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
        sendRtcMessage2(MessageType.RTC_GROUP_CANDIDATE, dto.getGroupId(), userInfo, dto.getCandidate());
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
        sendRtcMessage1(MessageType.RTC_GROUP_DEVICE, dto.getGroupId(), recvIds, JSON.toJSONString(dto));
        log.info("设备操作,userId:{},groupId:{},摄像头:{}", userSession.getUserId(), dto.getGroupId(),
            dto.getIsCamera());
    }

    @Override
    public WebrtcGroupInfoVO info(Long groupId) {
        WebrtcGroupInfoVO vo = new WebrtcGroupInfoVO();
        String key = buildWebrtcSessionKey(groupId);
        WebrtcGroupSession webrtcSession = (WebrtcGroupSession)redisTemplate.opsForValue().get(key);
        if (Objects.isNull(webrtcSession)) {
            // 群聊当前没有通话
            vo.setIsChating(false);
        } else {
            // 群聊正在通话中
            vo.setIsChating(true);
            vo.setUserInfos(webrtcSession.getUserInfos());
            Long hostId = webrtcSession.getHost().getId();
            WebrtcUserInfo host = findUserInfo(webrtcSession, hostId);
            if (Objects.isNull(host)) {
                // 如果发起人已经退出了通话，则从数据库查询发起人数据
                GroupMember member = groupMemberService.findByGroupAndUserId(groupId, hostId);
                host = new WebrtcUserInfo();
                host.setId(hostId);
                host.setNickName(member.getAliasName());
                host.setHeadImage(member.getHeadImage());
                host.setIsCamera(false);
            }
            vo.setHost(host);
        }
        return vo;
    }

    @Override
    public void heartbeat(Long groupId) {
        UserSession userSession = SessionContext.getSession();
        // 给通话session续命
        String key = buildWebrtcSessionKey(groupId);
        redisTemplate.expire(key, 30, TimeUnit.SECONDS);
        // 用户忙线状态续命
        userStateUtils.expire(userSession.getUserId());
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
        redisTemplate.opsForValue().set(key, webrtcSession, 30, TimeUnit.SECONDS);
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

    private void sendRtcMessage1(MessageType messageType, Long groupId, List<Long> recvIds, String content) {
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

    private void sendRtcMessage2(MessageType messageType, Long groupId, IMUserInfo receiver, String content) {
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

    private void sendTipMessage(Long groupId,String content){
        UserSession userSession = SessionContext.getSession();
        // 群聊成员列表
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        // 保存消息
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setContent(content);
        msg.setSendId(userSession.getUserId());
        msg.setSendTime(new Date());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setSendNickName(userSession.getNickName());
        msg.setType(MessageType.TIP_TEXT.code());
        groupMessageService.save(msg);
        // 群发罅隙
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(userSession.getUserId(), userSession.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setSendResult(false);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
    };
}

package com.lx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lx.common.contant.RedisKey;
import com.lx.common.enums.ResultCode;
import com.lx.common.model.im.GroupMessageInfo;
import com.lx.common.util.BeanUtils;
import com.lx.implatform.entity.Group;
import com.lx.implatform.entity.GroupMember;
import com.lx.implatform.entity.GroupMessage;
import com.lx.implatform.exception.GlobalException;
import com.lx.implatform.mapper.GroupMessageMapper;
import com.lx.implatform.service.IGroupMemberService;
import com.lx.implatform.service.IGroupMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lx.implatform.service.IGroupService;
import com.lx.implatform.session.SessionContext;
import com.lx.implatform.vo.GroupMessageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage> implements IGroupMessageService {


    @Autowired
    private IGroupService groupService;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 发送群聊消息(与mysql所有交换都要进行缓存)
     *
     * @param vo
     * @return
     */
    @Override
    public void sendMessage(GroupMessageVO vo) {
        Long userId = SessionContext.getSession().getId();
        Group group = groupService.getById(vo.getGroupId());
        if(group == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"群聊不存在或已解散");
        }
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(vo, GroupMessage.class);
        msg.setSendId(userId);
        msg.setSendTime(new Date());
        this.save(msg);
        // 根据群聊每个成员所连的IM-server，进行分组
        Map<Integer,List<Long>> serverMap = new ConcurrentHashMap<>();
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
        if(!userIds.contains(userId)){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您已不在群聊里面，无法发送消息");
        }

        userIds.parallelStream().forEach(id->{

            String key = RedisKey.IM_USER_SERVER_ID + id;
            Integer serverId = (Integer)redisTemplate.opsForValue().get(key);
            if(serverId != null){
                if(serverMap.containsKey(serverId)){
                    serverMap.get(serverId).add(id);
                }else {
                    List<Long> list = Collections.synchronizedList(new LinkedList<Long>());
                    list.add(id);
                    serverMap.put(serverId,list);
                }
            }
        });
        // 逐个server发送
        for (Map.Entry<Integer,List<Long>> entry : serverMap.entrySet()) {
            GroupMessageInfo  msgInfo = BeanUtils.copyProperties(msg, GroupMessageInfo.class);
            msgInfo.setRecvIds(new LinkedList<>(entry.getValue()));
            String key = RedisKey.IM_UNREAD_GROUP_MESSAGE +entry.getKey();
            redisTemplate.opsForList().rightPush(key,msgInfo);
        }
    }

    /**
     * 异步拉取群聊消息，通过websocket异步推送
     *
     * @return
     */
    @Override
    public void pullUnreadMessage() {
        Long userId = SessionContext.getSession().getId();
        String key = RedisKey.IM_USER_SERVER_ID+userId;
        Integer serverId = (Integer)redisTemplate.opsForValue().get(key);
        if(serverId == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"用户未建立连接");
        }
        List<Long> recvIds = new LinkedList();
        recvIds.add(userId);
        List<GroupMember> members = groupMemberService.findByUserId(userId);
        for(GroupMember member:members){
            // 获取群聊已读的最大消息id，只推送未读消息
            key = RedisKey.IM_GROUP_READED_POSITION + member.getGroupId()+":"+userId;
            Integer maxReadedId = (Integer)redisTemplate.opsForValue().get(key);
            QueryWrapper<GroupMessage> wrapper = new QueryWrapper();
            wrapper.lambda().eq(GroupMessage::getGroupId,member.getGroupId());
            if(maxReadedId!=null){
                wrapper.lambda().gt(GroupMessage::getId,maxReadedId);
            }
            wrapper.last("limit 100");
            List<GroupMessage> messages = this.list(wrapper);
            if(messages.isEmpty()){
                continue;
            }
            // 组装消息，准备推送
            List<GroupMessageInfo> messageInfos = messages.stream().map(m->{
                GroupMessageInfo  msgInfo = BeanUtils.copyProperties(m, GroupMessageInfo.class);
                msgInfo.setRecvIds(recvIds);
                return  msgInfo;
            }).collect(Collectors.toList());
            key = RedisKey.IM_UNREAD_GROUP_MESSAGE + serverId;
            redisTemplate.opsForList().rightPushAll(key,messageInfos.toArray());
        }
    }
}

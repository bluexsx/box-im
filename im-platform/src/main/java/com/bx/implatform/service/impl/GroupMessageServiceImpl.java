package com.bx.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.common.contant.Constant;
import com.bx.common.contant.RedisKey;
import com.bx.common.enums.MessageTypeEnum;
import com.bx.common.enums.ResultCode;
import com.bx.common.model.im.GroupMessageInfo;
import com.bx.common.util.BeanUtils;
import com.bx.implatform.entity.Group;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.GroupMessageMapper;
import com.bx.implatform.service.IGroupMemberService;
import com.bx.implatform.service.IGroupMessageService;
import com.bx.implatform.service.IGroupService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.vo.GroupMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
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
    public Long sendMessage(GroupMessageVO vo) {
        Long userId = SessionContext.getSession().getId();
        Group group = groupService.getById(vo.getGroupId());
        if(group == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"群聊不存在");
        }
        if(group.getDeleted()){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"群聊已解散");
        }
        // 判断是否在群里
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
        if(!userIds.contains(userId)){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您已不在群聊里面，无法发送消息");
        }
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(vo, GroupMessage.class);
        msg.setSendId(userId);
        msg.setSendTime(new Date());
        this.save(msg);
        // 群发
        GroupMessageInfo  msgInfo = BeanUtils.copyProperties(msg, GroupMessageInfo.class);
        this.sendMessage(userIds,msgInfo);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}",userId,vo.getGroupId(),vo.getContent());
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
        GroupMessage msg = this.getById(id);
        if(msg == null){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"消息不存在");
        }
        if(msg.getSendId() != userId){
            throw new GlobalException(ResultCode.PROGRAM_ERROR,"这条消息不是由您发送,无法撤回");
        }
        if(System.currentTimeMillis() - msg.getSendTime().getTime() > Constant.ALLOW_RECALL_SECOND * 1000){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"消息已发送超过5分钟，无法撤回");
        }
        // 判断是否在群里
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(),userId);
        if(member == null){
            throw  new GlobalException(ResultCode.PROGRAM_ERROR,"您已不在群聊里面，无法撤回消息");
        }
        // 直接物理删除
        this.removeById(id);
        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        GroupMessageInfo  msgInfo = BeanUtils.copyProperties(msg, GroupMessageInfo.class);
        msgInfo.setType(MessageTypeEnum.TIP.getCode());
        String content = String.format("'%s'撤回了一条消息",member.getAliasName());
        msgInfo.setContent(content);
        this.sendMessage(userIds,msgInfo);
        log.info("撤回群聊消息，发送id:{},群聊id:{},内容:{}",userId,msg.getGroupId(),msg.getContent());
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
            wrapper.lambda().eq(GroupMessage::getGroupId,member.getGroupId())
                    .gt(GroupMessage::getSendTime,member.getCreatedTime());
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
            log.info("拉取未读群聊消息，用户id:{},群聊id:{},数量:{}",userId,member.getGroupId(),messageInfos.size());
        }

    }

    private void  sendMessage(List<Long> userIds, GroupMessageInfo  msgInfo){
        // 根据群聊每个成员所连的IM-server，进行分组
        Map<Integer,List<Long>> serverMap = new ConcurrentHashMap<>();
        userIds.parallelStream().forEach(id->{
            String key = RedisKey.IM_USER_SERVER_ID + id;
            Integer serverId = (Integer)redisTemplate.opsForValue().get(key);
            if(serverId != null){
                if(serverMap.containsKey(serverId)){
                    serverMap.get(serverId).add(id);
                }else {
                    // 此处需要加锁，否则list可以会被覆盖
                    synchronized(serverMap){
                        List<Long> list = Collections.synchronizedList(new LinkedList<Long>());
                        list.add(id);
                        serverMap.put(serverId,list);
                    }
                }
            }
        });
        // 逐个server发送
        for (Map.Entry<Integer,List<Long>> entry : serverMap.entrySet()) {
            msgInfo.setRecvIds(new LinkedList<>(entry.getValue()));
            String key = RedisKey.IM_UNREAD_GROUP_MESSAGE +entry.getKey();
            redisTemplate.opsForList().rightPush(key,msgInfo);
        }
    }
}

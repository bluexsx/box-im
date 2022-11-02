package com.lx.implatform.service.impl;

import com.lx.common.contant.RedisKey;
import com.lx.common.enums.ResultCode;
import com.lx.common.model.im.GroupMessageInfo;
import com.lx.common.util.BeanUtils;
import com.lx.implatform.entity.Group;
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
        Group group = groupService.findById(vo.getGroupId());
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
            msgInfo.setRecvIds(entry.getValue());
            String key = RedisKey.IM_UNREAD_PRIVATE_MESSAGE +entry.getKey();
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

    }
}

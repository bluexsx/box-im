package com.bx.implatform.task;

import cn.hutool.core.util.StrUtil;
import com.bx.imclient.IMClient;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMSystemMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.mq.RedisMQConsumer;
import com.bx.imcommon.mq.RedisMQListener;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.GroupBanDTO;
import com.bx.implatform.dto.UserBanDTO;
import com.bx.implatform.entity.Group;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.service.IGroupMemberService;
import com.bx.implatform.service.IGroupMessageService;
import com.bx.implatform.service.IGroupService;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.vo.SystemMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.properties.source.ConfigurationPropertyState;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author: Blue
 * @date: 2024-07-15
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = RedisKey.IM_QUEUE_GROUP_BANNED)
public class GroupBannedConsumerTask extends RedisMQConsumer<GroupBanDTO> {

    private final IMClient imClient;

    private final IGroupMessageService groupMessageService;

    private final IGroupMemberService groupMemberService;

    @Override
    public void onMessage(GroupBanDTO dto) {
        log.info("群聊被封禁处理,群id:{},原因:{}", dto.getId(), dto.getReason());
        // 群聊成员列表
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(dto.getId());
        // 保存消息
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(dto.getId());
        String tip = "本群聊已被管理员封禁,原因:" + dto.getReason();
        msg.setContent(tip);
        msg.setSendId(Constant.SYS_USER_ID);
        msg.setSendTime(new Date());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setSendNickName("系统管理员");
        msg.setType(MessageType.TIP_TEXT.code());
        groupMessageService.save(msg);
        // 推送提示语到群聊中
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(Constant.SYS_USER_ID, IMTerminalType.PC.code()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setSendResult(true);
        sendMessage.setSendToSelf(false);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
    }
}

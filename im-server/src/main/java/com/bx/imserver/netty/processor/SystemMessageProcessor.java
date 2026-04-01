package com.bx.imserver.netty.processor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.*;
import com.bx.imcommon.mq.RedisMQTemplate;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisMQTemplate redisMQTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        log.info("接收到系统消息,接收用户数量:{}，内容:{}",  recvInfo.getReceivers().size(), recvInfo.getData());
        List<IMUserInfo> sucessReceivers = new LinkedList<>();
        List<IMUserInfo> noChannelReceivers = new LinkedList<>();
        List<IMUserInfo> errorReceivers = new LinkedList<>();
        for (IMUserInfo receiver : recvInfo.getReceivers()) {
            try {
                ChannelHandlerContext channelCtx =
                    UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
                if (!Objects.isNull(channelCtx)) {
                    // 推送消息到用户
                    IMSendInfo<Object> sendInfo = new IMSendInfo<>();
                    sendInfo.setCmd(IMCmdType.SYSTEM_MESSAGE.code());
                    sendInfo.setData(recvInfo.getData());
                    channelCtx.channel().writeAndFlush(sendInfo);
                    sucessReceivers.add(receiver);
                } else {
                    noChannelReceivers.add(receiver);
                    log.error("未找到channel，接收者:{}，内容:{}", receiver.getId(), recvInfo.getData());
                }
            } catch (Exception e) {
                errorReceivers.add(receiver);
                log.error("发送异常，,接收者:{}，内容:{}", receiver.getId(), recvInfo.getData(), e);
            }
        }
        // 批量回复推送结果
        sendResult(recvInfo, sucessReceivers, IMSendCode.SUCCESS);
        sendResult(recvInfo, noChannelReceivers, IMSendCode.NOT_FIND_CHANNEL);
        sendResult(recvInfo, errorReceivers, IMSendCode.UNKONW_ERROR);
    }

    private void sendResult(IMRecvInfo recvInfo, List<IMUserInfo> receivers, IMSendCode sendCode) {
        if (recvInfo.getSendResult() && CollectionUtil.isNotEmpty(receivers)) {
            IMBatchSendResult<Object> result = new IMBatchSendResult<>();
            result.setSender(recvInfo.getSender());
            result.setReceivers(receivers);
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            // 推送到结果队列
            String key = StrUtil.join(":", IMRedisKey.IM_RESULT_SYSTEM_QUEUE, recvInfo.getServiceName());
            redisMQTemplate.opsForList().rightPush(key, result);
        }
    }
}

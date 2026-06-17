package com.bx.imserver.netty.processor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMBatchSendResult;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.mq.RedisMQTemplate;
import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisMQTemplate redisMQTemplate;
    private final ScheduledThreadPoolExecutor EXECUTOR = ThreadPoolExecutorFactory.getThreadPoolExecutor();

    @Override
    public void process(IMRecvInfo recvInfo) {
        IMUserInfo sender = recvInfo.getSender();
        List<IMUserInfo> noChannelReceivers = new ArrayList<>();
        List<IMUserInfo> errorReceivers = new ArrayList<>();
        List<PushTask> pushTasks = new ArrayList<>(recvInfo.getReceivers().size());
        for (IMUserInfo receiver : recvInfo.getReceivers()) {
            log.info("接收到私聊消息，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(),
                recvInfo.getData());
            try {
                ChannelHandlerContext channelCtx =
                    UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
                if (!Objects.isNull(channelCtx)) {
                    IMSendInfo<Object> sendInfo = new IMSendInfo<>();
                    sendInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                    sendInfo.setData(recvInfo.getData());
                    pushTasks.add(new PushTask(receiver, channelCtx.channel().writeAndFlush(sendInfo)));
                } else {
                    noChannelReceivers.add(receiver);
                    log.error("未找到channel，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(),
                        recvInfo.getData());
                }
            } catch (Exception e) {
                errorReceivers.add(receiver);
                log.error("发送异常，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(), recvInfo.getData(),
                    e);
            }
        }
        sendResult(recvInfo, noChannelReceivers, IMSendCode.NOT_FIND_CHANNEL);
        sendResult(recvInfo, errorReceivers, IMSendCode.UNKONW_ERROR);
        listenPushResults(recvInfo, sender, pushTasks);
    }

    private void listenPushResults(IMRecvInfo recvInfo, IMUserInfo sender, List<PushTask> pushTasks) {
        if (!Boolean.TRUE.equals(recvInfo.getSendResult()) || CollectionUtil.isEmpty(pushTasks)) {
            return;
        }
        List<IMUserInfo> successReceivers = Collections.synchronizedList(new ArrayList<>(pushTasks.size()));
        List<IMUserInfo> asyncErrorReceivers = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pendingPush = new AtomicInteger(pushTasks.size());
        AtomicBoolean loopFinished = new AtomicBoolean(false);
        Runnable tryComplete = () -> {
            if (loopFinished.get() && pendingPush.get() == 0) {
                sendResult(recvInfo, successReceivers, IMSendCode.SUCCESS);
                sendResult(recvInfo, asyncErrorReceivers, IMSendCode.UNKONW_ERROR);
            }
        };
        for (PushTask task : pushTasks) {
            IMUserInfo receiver = task.getReceiver();
            task.getFuture().addListener(f -> {
                if (f.isSuccess()) {
                    successReceivers.add(receiver);
                } else {
                    asyncErrorReceivers.add(receiver);
                    log.error("消息推送失败，发送者:{},接收者:{}，内容:{}", sender.getId(), receiver.getId(),
                        recvInfo.getData(), f.cause());
                }
                pendingPush.decrementAndGet();
                tryComplete.run();
            });
        }
        loopFinished.set(true);
        tryComplete.run();
    }

    private void sendResult(IMRecvInfo recvInfo, List<IMUserInfo> receivers, IMSendCode sendCode) {
        if (Boolean.TRUE.equals(recvInfo.getSendResult()) && CollectionUtil.isNotEmpty(receivers)) {
            EXECUTOR.execute(() -> {
                IMBatchSendResult<Object> result = new IMBatchSendResult<>();
                result.setSender(recvInfo.getSender());
                result.setReceivers(receivers);
                result.setCode(sendCode.code());
                result.setData(recvInfo.getData());
                String key = StrUtil.join(":", IMRedisKey.IM_RESULT_PRIVATE_QUEUE, recvInfo.getServiceName());
                redisMQTemplate.opsForList().rightPush(key, result);
            });
        }
    }
}

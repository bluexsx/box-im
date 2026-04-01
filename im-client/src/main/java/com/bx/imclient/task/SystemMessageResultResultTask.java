package com.bx.imclient.task;

import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.model.IMBatchSendResult;
import com.bx.imcommon.model.IMSendResult;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.mq.RedisMQListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_RESULT_SYSTEM_QUEUE, batchSize = 100)
public class SystemMessageResultResultTask extends AbstractMessageResultTask<IMBatchSendResult> {

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void onMessage(List<IMBatchSendResult> batchResults) {
        // 批量的结果转成单个返回给应用层
        List<IMSendResult> results = new LinkedList<>();
        for (IMBatchSendResult batchResult : batchResults) {
            List<IMUserInfo> receivers = batchResult.getReceivers();
            for (IMUserInfo receiver : receivers) {
                IMSendResult result = new IMSendResult();
                result.setSender(batchResult.getSender());
                result.setCode(batchResult.getCode());
                result.setReceiver(receiver);
                result.setData(batchResult.getData());
                results.add(result);
            }
        }
        listenerMulticaster.multicast(IMListenerType.SYSTEM_MESSAGE, results);
    }

}

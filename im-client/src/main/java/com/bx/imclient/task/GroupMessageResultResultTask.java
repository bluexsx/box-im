package com.bx.imclient.task;

import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.model.IMSendResult;
import com.bx.imcommon.mq.RedisMQListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_RESULT_GROUP_QUEUE, batchSize = 100)
public class GroupMessageResultResultTask extends AbstractMessageResultTask<IMSendResult> {

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void onMessage(List<IMSendResult> results) {
        listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, results);
    }


}

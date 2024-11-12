package com.bx.implatform.listener;

import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.MessageListener;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMSendResult;
import com.bx.implatform.vo.SystemMessageVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@IMListener(type = IMListenerType.SYSTEM_MESSAGE)
public class SystemMessageListener implements MessageListener<SystemMessageVO> {

    @Override
    public void process(List<IMSendResult<SystemMessageVO>> results) {
        for(IMSendResult<SystemMessageVO> result : results){
            SystemMessageVO messageInfo = result.getData();
            if (result.getCode().equals(IMSendCode.SUCCESS.code())) {
                log.info("消息送达，消息id:{},接收者:{},终端:{}", messageInfo.getId(), result.getReceiver().getId(), result.getReceiver().getTerminal());
            }
        }
    }
}

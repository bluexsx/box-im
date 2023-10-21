package com.bx.imclient.listener;


import com.bx.imcommon.model.IMSendResult;

public interface MessageListener<T> {

     void process(IMSendResult<T> result);

}

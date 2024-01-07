package com.bx.imclient.listener;


import com.bx.imcommon.model.IMSendResult;

import java.util.List;

public interface MessageListener<T> {

     void process(List<IMSendResult<T>> result);

}

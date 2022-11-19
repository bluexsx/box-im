package com.bx.imclient.listener;


import com.bx.imcommon.model.im.SendResult;

public interface MessageListener {

     void process(SendResult result);

}

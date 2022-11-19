package com.bx.imclient.listener;


import com.bx.common.model.im.SendResult;

public interface MessageListener {

     void process(SendResult result);

}

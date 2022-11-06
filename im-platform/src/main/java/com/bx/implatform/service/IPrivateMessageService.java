package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.vo.PrivateMessageVO;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    void sendMessage(PrivateMessageVO vo);

    void pullUnreadMessage();

}

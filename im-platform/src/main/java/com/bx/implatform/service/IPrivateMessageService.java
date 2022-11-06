package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.vo.PrivateMessageVO;
import com.bx.implatform.entity.PrivateMessage;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    void sendMessage(PrivateMessageVO vo);

    void pullUnreadMessage();

}

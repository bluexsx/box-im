package com.lx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.implatform.entity.PrivateMessage;
import com.lx.implatform.vo.PrivateMessageVO;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    void sendMessage(PrivateMessageVO vo);

    void pullUnreadMessage();

}

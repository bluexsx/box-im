package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.vo.PrivateMessageVO;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    Long sendMessage(PrivateMessageVO vo);

    void recallMessage(Long id);

    void pullUnreadMessage();

}

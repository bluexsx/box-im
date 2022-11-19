package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.imcommon.model.PrivateMessageInfo;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.vo.PrivateMessageVO;

import java.util.List;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    Long sendMessage(PrivateMessageVO vo);

    void recallMessage(Long id);

    List<PrivateMessageInfo> findHistoryMessage(Long friendId, Long page,Long size);

    void pullUnreadMessage();

}

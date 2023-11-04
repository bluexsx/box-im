package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.vo.PrivateMessageVO;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.dto.PrivateMessageDTO;

import java.util.List;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    Long sendMessage(PrivateMessageDTO vo);

    void recallMessage(Long id);

    List<PrivateMessageVO> findHistoryMessage(Long friendId, Long page,Long size);

    void pullUnreadMessage();

    List<PrivateMessageVO> loadMessage(Long minId);

    void readedMessage(Long friendId);
}

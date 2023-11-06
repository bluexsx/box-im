package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.dto.GroupMessageDTO;

import java.util.List;


public interface IGroupMessageService extends IService<GroupMessage> {


    Long sendMessage(GroupMessageDTO vo);

    void recallMessage(Long id);

    void pullUnreadMessage();

    List<GroupMessageVO> loadMessage(Long minId);

    void readedMessage(Long groupId);

    List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size);
}

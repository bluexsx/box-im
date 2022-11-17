package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.vo.GroupMessageVO;


public interface IGroupMessageService extends IService<GroupMessage> {


    Long sendMessage(GroupMessageVO vo);

    void recallMessage(Long id);

    void pullUnreadMessage();
}

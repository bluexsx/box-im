package com.bx.implatform.service;

import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.entity.GroupMessage;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IGroupMessageService extends IService<GroupMessage> {


    void sendMessage(GroupMessageVO vo);

    void pullUnreadMessage();
}

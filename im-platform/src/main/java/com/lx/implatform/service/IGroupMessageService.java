package com.lx.implatform.service;

import com.lx.implatform.entity.GroupMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.implatform.vo.GroupMessageVO;
import com.lx.implatform.vo.PrivateMessageVO;


public interface IGroupMessageService extends IService<GroupMessage> {


    void sendMessage(GroupMessageVO vo);

    void pullUnreadMessage();
}

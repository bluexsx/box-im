package com.lx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.implatform.entity.SingleMessage;
import com.lx.implatform.vo.SingleMessageVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author blue
 * @since 2022-10-01
 */
public interface ISingleMessageService extends IService<SingleMessage> {

    void sendMessage(SingleMessageVO vo);

    void pullUnreadMessage();

}

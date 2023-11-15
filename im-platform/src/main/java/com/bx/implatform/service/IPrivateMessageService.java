package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.vo.PrivateMessageVO;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.dto.PrivateMessageDTO;

import java.util.List;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    /**
     * 发送私聊消息(高并发接口，查询mysql接口都要进行缓存)
     *
     * @param dto 私聊消息
     * @return 消息id
     */
    Long sendMessage(PrivateMessageDTO dto);


    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    void recallMessage(Long id);

    /**
     * 拉取历史聊天记录
     *
     * @param friendId 好友id
     * @param page     页码
     * @param size     页码大小
     * @return 聊天记录列表
     */
    List<PrivateMessageVO> findHistoryMessage(Long friendId, Long page,Long size);

    /**
     * 异步拉取私聊消息，通过websocket异步推送
     */
    void pullUnreadMessage();

    /**
     * 拉取消息，只能拉取最近1个月的消息，一次拉取100条
     *
     * @param minId 消息起始id
     * @return 聊天消息列表
     */
    List<PrivateMessageVO> loadMessage(Long minId);

    /**
     * 消息已读,将整个会话的消息都置为已读状态
     *
     * @param friendId 好友id
     */
    void readedMessage(Long friendId);
}

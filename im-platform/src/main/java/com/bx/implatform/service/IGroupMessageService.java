package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.dto.GroupMessageDTO;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.vo.GroupMessageVO;

import java.util.List;

public interface IGroupMessageService extends IService<GroupMessage> {

    /**
     * 发送群聊消息(高并发接口，查询mysql接口都要进行缓存)
     *
     * @param dto 群聊消息
     * @return 群聊id
     */
    Long sendMessage(GroupMessageDTO dto);

    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    void recallMessage(Long id);

    /**
     * 异步拉取群聊消息，通过websocket异步推送
     */
    void pullUnreadMessage();

    /**
     * 拉取消息，只能拉取最近1个月的消息，一次拉取100条
     *
     * @param minId 消息起始id
     * @return 聊天消息列表
     */
    List<GroupMessageVO> loadMessage(Long minId);

    /**
     * 消息已读,同步其他终端，清空未读数量
     *
     * @param groupId 群聊
     */
    void readedMessage(Long groupId);

    /**
     * 拉取历史聊天记录
     *
     * @param groupId 群聊id
     * @param page    页码
     * @param size    页码大小
     * @return 聊天记录列表
     */
    List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size);
}

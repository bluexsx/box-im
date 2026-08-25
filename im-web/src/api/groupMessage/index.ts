import request from '@/utils/request';
import type { ChatDeleteDTO, GroupMessageDTO, GroupMessageHistoryDTO, GroupMessageVO, MessageDeleteDTO } from './types';

/** 发送群聊消息 */
export const sendGroupMessage = (data: GroupMessageDTO) => {
  return request<GroupMessageVO>({ url: '/message/group/send', method: 'post', data });
};

/** 撤回群聊消息 */
export const recallGroupMessage = (id: number) => {
  return request<GroupMessageVO>({ url: `/message/group/recall/${id}`, method: 'delete' });
};

/** 拉取离线消息 */
export const loadGroupOfflineMessage = (minId: number) => {
  return request<GroupMessageVO[]>({
    url: '/message/group/loadOfflineMessage',
    method: 'get',
    params: { minId }
  });
};

/** 消息已读 */
export const readedGroupMessage = (groupId: number, messageId?: number) => {
  return request<void>({
    url: '/message/group/readed',
    method: 'put',
    params: { groupId, messageId }
  });
};

/** 获取已读用户 id */
export const findGroupReadedUsers = (groupId: number, messageId: number) => {
  return request<number[]>({
    url: '/message/group/findReadedUsers',
    method: 'get',
    params: { groupId, messageId }
  });
};

/** 按 id 列表删除消息 */
export const deleteGroupMessage = (data: MessageDeleteDTO) => {
  return request<void>({ url: '/message/group/deleteMessage', method: 'delete', data });
};

/** 删除会话及全部消息 */
export const deleteGroupChat = (data: ChatDeleteDTO) => {
  return request<void>({ url: '/message/group/deleteChat', method: 'delete', data });
};

/** 查询历史消息 */
export const historyGroupMessage = (data: GroupMessageHistoryDTO) => {
  return request<GroupMessageVO[]>({ url: '/message/group/history', method: 'post', data });
};

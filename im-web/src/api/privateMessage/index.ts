import request from '@/utils/request';
import type { ChatDeleteDTO, MessageDeleteDTO, PrivateMessageDTO, PrivateMessageHistoryDTO, PrivateMessageVO } from './types';

/** 发送私聊消息 */
export const sendPrivateMessage = (data: PrivateMessageDTO) => {
  return request<PrivateMessageVO>({ url: '/message/private/send', method: 'post', data });
};

/** 撤回私聊消息 */
export const recallPrivateMessage = (id: number) => {
  return request<PrivateMessageVO>({ url: `/message/private/recall/${id}`, method: 'delete' });
};

/** 拉取离线消息 */
export const loadPrivateOfflineMessage = (minId: number) => {
  return request<PrivateMessageVO[]>({
    url: '/message/private/loadOfflineMessage',
    method: 'get',
    params: { minId }
  });
};

/** 消息已读 */
export const readedPrivateMessage = (friendId: number, messageId?: number) => {
  return request<void>({
    url: '/message/private/readed',
    method: 'put',
    params: { friendId, messageId }
  });
};

/** 获取最大已读消息 id */
export const getPrivateMaxReadedId = (friendId: number) => {
  return request<number>({
    url: '/message/private/maxReadedId',
    method: 'get',
    params: { friendId }
  });
};

/** 按 id 列表删除消息 */
export const deletePrivateMessage = (data: MessageDeleteDTO) => {
  return request<void>({ url: '/message/private/deleteMessage', method: 'delete', data });
};

/** 删除会话及全部消息 */
export const deletePrivateChat = (data: ChatDeleteDTO) => {
  return request<void>({ url: '/message/private/deleteChat', method: 'delete', data });
};

/** 查询历史消息 */
export const historyPrivateMessage = (data: PrivateMessageHistoryDTO) => {
  return request<PrivateMessageVO[]>({
    url: '/message/private/history',
    method: 'post',
    data
  });
};

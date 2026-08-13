import request from '@/utils/request';
import type { FriendDndDTO, FriendVO, UserOnlineVO } from './types';

/** 好友列表 */
export const listFriend = (version = 0) => {
  return request<FriendVO[]>({ url: '/friend/list', method: 'get', params: { version } });
};

/** 好友在线情况 */
export const listFriendOnline = () => {
  return request<UserOnlineVO[]>({ url: '/friend/online', method: 'get' });
};

/** 添加好友（即时成为好友） */
export const addFriend = (friendId: number) => {
  return request<void>({ url: '/friend/add', method: 'post', params: { friendId } });
};

/** 查找好友信息 */
export const findFriend = (friendId: number) => {
  return request<FriendVO>({ url: `/friend/find/${friendId}`, method: 'get' });
};

/** 删除好友 */
export const deleteFriend = (friendId: number) => {
  return request<void>({ url: `/friend/delete/${friendId}`, method: 'delete' });
};

/** 开启/关闭免打扰 */
export const setFriendDnd = (data: FriendDndDTO) => {
  return request<void>({ url: '/friend/dnd', method: 'put', data });
};

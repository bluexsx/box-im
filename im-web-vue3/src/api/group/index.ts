import request from '@/utils/request';
import type {
  GroupDndDTO,
  GroupInviteDTO,
  GroupMemberRemoveDTO,
  GroupMemberVO,
  GroupNewDTO,
  GroupVO
} from './types';

/** 选择好友创建群聊 */
export const newGroup = (data: GroupNewDTO) => {
  return request<GroupVO>({ url: '/group/new', method: 'post', data });
};

/** 修改群聊信息 */
export const modifyGroup = (data: GroupVO) => {
  return request<GroupVO>({ url: '/group/modify', method: 'put', data });
};

/** 解散群聊 */
export const deleteGroup = (groupId: number) => {
  return request<void>({ url: `/group/delete/${groupId}`, method: 'delete' });
};

/** 查询单个群聊 */
export const findGroup = (groupId: number) => {
  return request<GroupVO>({ url: `/group/find/${groupId}`, method: 'get' });
};

/** 查询群聊列表 */
export const listGroup = (version = 0) => {
  return request<GroupVO[]>({ url: '/group/list', method: 'get', params: { version } });
};

/** 邀请好友进群 */
export const inviteGroup = (data: GroupInviteDTO) => {
  return request<void>({ url: '/group/invite', method: 'post', data });
};

/** 查询群聊成员 */
export const listGroupMembers = (groupId: number, version = 0) => {
  return request<GroupMemberVO[]>({
    url: `/group/members/${groupId}`,
    method: 'get',
    params: { version }
  });
};

/** 查询在线成员 id */
export const listGroupOnlineMembers = (groupId: number) => {
  return request<number[]>({ url: `/group/members/online/${groupId}`, method: 'get' });
};

/** 将成员移出群聊 */
export const removeGroupMembers = (data: GroupMemberRemoveDTO) => {
  return request<void>({ url: '/group/members/remove', method: 'delete', data });
};

/** 退出群聊 */
export const quitGroup = (groupId: number) => {
  return request<void>({ url: `/group/quit/${groupId}`, method: 'delete' });
};

/** 开启/关闭免打扰 */
export const setGroupDnd = (data: GroupDndDTO) => {
  return request<void>({ url: '/group/dnd', method: 'put', data });
};

import request from '@/utils/request';
import type { UserVO } from './types';

/** 获取当前用户信息 */
export const getSelf = () => {
  return request<UserVO>({ url: '/user/self', method: 'get' });
};

/** 根据 id 查找用户 */
export const findUser = (id: number) => {
  return request<UserVO>({ url: `/user/find/${id}`, method: 'get' });
};

/** 修改用户信息 */
export const updateUser = (data: UserVO) => {
  return request<void>({ url: '/user/update', method: 'put', data });
};

/** 搜索用户 */
export const searchUser = (name: string) => {
  return request<UserVO[]>({ url: '/user/search', method: 'get', params: { name } });
};

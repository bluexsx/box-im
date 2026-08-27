import request from '@/utils/request';
import type { LoginDTO, LoginVO, ModifyPwdDTO, RegisterDTO } from './types';

/** 用户登录 */
export const login = (data: LoginDTO) => {
  return request<LoginVO>({
    url: '/login',
    method: 'post',
    data
  });
};

/** 刷新 token */
export const refreshToken = (token: string) => {
  return request<LoginVO>({
    url: '/refreshToken',
    method: 'put',
    headers: {
      refreshToken: token
    }
  });
};

/** 用户注册 */
export const register = (data: RegisterDTO) => {
  return request<void>({
    url: '/register',
    method: 'post',
    data
  });
};

/** 修改密码 */
export const modifyPwd = (data: ModifyPwdDTO) => {
  return request<void>({
    url: '/modifyPwd',
    method: 'put',
    data
  });
};

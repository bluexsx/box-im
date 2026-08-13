import request from '@/utils/request';
import type { LoginVO } from '@/api/login/types';

const AUTO_LOGIN_KEY = 'isAutoLogin';
const USERNAME_KEY = 'username';
const REFRESH_TOKEN_KEY = 'refreshToken';

export interface SaveLoginOptions {
  autoLogin: boolean;
  userName?: string;
}

export const getSavedUsername = (): string => {
  return localStorage.getItem(USERNAME_KEY) || '';
};

export const isAutoLoginEnabled = (): boolean => {
  const value = localStorage.getItem(AUTO_LOGIN_KEY);
  if (value == null) {
    return true;
  }
  return JSON.parse(value) as boolean;
};

export const getPersistedRefreshToken = (): string => {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || '';
};

export const isLoggedIn = (): boolean => {
  return !!sessionStorage.getItem('accessToken');
};

export const getRefreshToken = (): string => {
  return sessionStorage.getItem('refreshToken') || getPersistedRefreshToken();
};

/**
 * 登录成功后保存会话
 * @param data
 * @param options
 */
export const saveLoginSession = (data: LoginVO, { autoLogin, userName }: SaveLoginOptions): void => {
  sessionStorage.setItem('accessToken', data.accessToken);
  sessionStorage.setItem('refreshToken', data.refreshToken);
  localStorage.setItem(AUTO_LOGIN_KEY, JSON.stringify(autoLogin));
  if (userName) {
    localStorage.setItem(USERNAME_KEY, userName);
  }
  if (autoLogin) {
    localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
};

/** 刷新 token 后同步更新存储 */
export const saveTokens = (data: LoginVO): void => {
  sessionStorage.setItem('accessToken', data.accessToken);
  sessionStorage.setItem('refreshToken', data.refreshToken);
  if (isAutoLoginEnabled()) {
    localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
  }
};

/**
 * 退出登录
 * @param clearAutoLogin 是否清除自动登录
 */
export const clearLoginSession = (clearAutoLogin = true): void => {
  sessionStorage.removeItem('accessToken');
  sessionStorage.removeItem('refreshToken');
  if (clearAutoLogin) {
    localStorage.setItem(AUTO_LOGIN_KEY, 'false');
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
};

/** 使用本地 refreshToken 自动登录（直接调 request，避免 api/login ↔ auth 循环依赖） */
export const refreshLogin = (): Promise<LoginVO> => {
  const token = getPersistedRefreshToken();
  if (!token) {
    return Promise.reject(new Error('no refresh token'));
  }
  return request<LoginVO>({
    url: '/refreshToken',
    method: 'put',
    headers: { refreshToken: token }
  }).then((data) => {
    saveTokens(data);
    return data;
  });
};

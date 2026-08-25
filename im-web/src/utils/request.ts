import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import type { LoginVO } from '@/api/login/types';
import * as auth from '@/utils/auth';

export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 30_000
});

/**
 * 请求拦截
 */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const accessToken = sessionStorage.getItem('accessToken');
    if (accessToken) {
      config.headers.accessToken = encodeURIComponent(accessToken);
    }
    return config;
  },
  (error: unknown) => Promise.reject(error)
);

/**
 * 响应拦截
 */
service.interceptors.response.use(
  async (response: AxiosResponse<ApiResponse>) => {
    const { code, message, data } = response.data;
    if (code === 200) {
      return data as never;
    }
    if (code === 400) {
      return exit();
    }
    if (code === 401) {
      const token = auth.getRefreshToken();
      if (!token) {
        return exit();
      }
      // 发送请求, 进行刷新token操作, 获取新的token
      try {
        const tokens = (await service.put('/refreshToken', null, {
          headers: { refreshToken: token }
        })) as LoginVO;
        auth.saveTokens(tokens);
        response.config.headers.accessToken = encodeURIComponent(tokens.accessToken);
        // 重新发送刚才的请求
        return service(response.config);
      } catch {
        exit();
        return Promise.reject(response.data);
      }
    }
    ElMessage({
      message,
      type: 'error',
      duration: 1500,
      customClass: 'element-error-message-zindex'
    });
    return Promise.reject(response.data);
  },
  (error: { response?: { status?: number; data?: string } }) => {
    const status = error.response?.status;
    switch (status) {
      case 400:
        ElMessage({
          message: error.response?.data || '服务器出了点小差，请稍后再试',
          type: 'error',
          duration: 1500,
          customClass: 'element-error-message-zindex'
        });
        break;
      case 401:
        exit();
        break;
      case 405:
        ElMessage({
          message: 'http请求方式有误',
          type: 'error',
          duration: 1500,
          customClass: 'element-error-message-zindex'
        });
        break;
      case 404:
      case 500:
        ElMessage({
          message: '服务器出了点小差，请稍后再试',
          type: 'error',
          duration: 1500,
          customClass: 'element-error-message-zindex'
        });
        break;
      case 501:
        ElMessage({
          message: '服务器不支持当前请求所需要的某个功能',
          type: 'error',
          duration: 1500,
          customClass: 'element-error-message-zindex'
        });
        break;
      default:
        break;
    }
    return Promise.reject(error);
  }
);

const exit = (): Promise<never> => {
  auth.clearLoginSession(false);
  if (router.currentRoute.value.path !== '/login') {
    void router.push('/login');
  }
  return Promise.reject(new Error('unauthorized'));
};

/** 统一请求：拦截器已解包 Result.data，泛型 T 即业务数据类型 */
const request = <T = unknown>(config: AxiosRequestConfig): Promise<T> => {
  return service(config) as Promise<T>;
};

export default request;

import request from '@/utils/request';
import type { SystemConfigVO } from './types';

/** 加载系统配置 */
export const loadConfig = () => {
  return request<SystemConfigVO>({
    url: '/system/config',
    method: 'get'
  });
};

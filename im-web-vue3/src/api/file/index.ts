import request from '@/utils/request';

/** 上传文件，返回 url */
export const uploadFile = (file: File) => {
  const data = new FormData();
  data.append('file', file);
  return request<string>({
    url: '/file/upload',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

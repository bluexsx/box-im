import request from '@/utils/request';

/** 呼叫视频通话（SDP offer 在 body） */
export const callPrivate = (uid: number, mode: string, offer: RTCSessionDescriptionInit) => {
  return request<void>({
    url: '/webrtc/private/call',
    method: 'post',
    params: { uid, mode },
    data: JSON.stringify(offer),
    headers: { 'Content-Type': 'application/json; charset=utf-8' }
  });
};

/** 接受视频通话（SDP answer 在 body） */
export const acceptPrivate = (uid: number, answer: RTCSessionDescriptionInit) => {
  return request<void>({
    url: '/webrtc/private/accept',
    method: 'post',
    params: { uid },
    data: JSON.stringify(answer),
    headers: { 'Content-Type': 'application/json; charset=utf-8' }
  });
};

/** 拒绝视频通话 */
export const rejectPrivate = (uid: number) => {
  return request<void>({ url: '/webrtc/private/reject', method: 'post', params: { uid } });
};

/** 取消呼叫 */
export const cancelPrivate = (uid: number) => {
  return request<void>({ url: '/webrtc/private/cancel', method: 'post', params: { uid } });
};

/** 呼叫失败 */
export const failedPrivate = (uid: number, reason: string) => {
  return request<void>({
    url: '/webrtc/private/failed',
    method: 'post',
    params: { uid, reason }
  });
};

/** 挂断 */
export const handupPrivate = (uid: number) => {
  return request<void>({ url: '/webrtc/private/handup', method: 'post', params: { uid } });
};

/** 同步 candidate */
export const candidatePrivate = (uid: number, candidate: RTCIceCandidateInit) => {
  return request<void>({
    url: '/webrtc/private/candidate',
    method: 'post',
    params: { uid },
    data: JSON.stringify(candidate),
    headers: { 'Content-Type': 'application/json; charset=utf-8' }
  });
};

/** 心跳 */
export const heartbeatPrivate = (uid: number) => {
  return request<void>({
    url: '/webrtc/private/heartbeat',
    method: 'post',
    params: { uid }
  });
};

/** WS 报文信封（对应后端 IMSendInfo） */
export interface WsSendInfo<T = unknown> {
  cmd: number;
  data: T;
}

/** 强制下线推送数据（对应后端 IMForceLogoutData） */
export interface ForceLogoutData {
  type: number;
  reason?: string;
}

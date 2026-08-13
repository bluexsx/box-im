/** 消息类型（对齐 OSS enums） */
export const MESSAGE_TYPE = {
  TEXT: 0,
  IMAGE: 1,
  FILE: 2,
  AUDIO: 3,
  VIDEO: 4,
  RECALL: 10,
  READED: 11,
  RECEIPT: 12,
  TIP_TIME: 20,
  TIP_TEXT: 21,
  LOADING: 30,
  ACT_RT_VOICE: 40,
  ACT_RT_VIDEO: 41,
  USER_BANNED: 50,
  FRIEND_NEW: 80,
  FRIEND_DEL: 81,
  FRIEND_ONLINE: 82,
  FRIEND_DND: 83,
  GROUP_NEW: 90,
  GROUP_DEL: 91,
  GROUP_DND: 92,
  RTC_CALL_VOICE: 100,
  RTC_CALL_VIDEO: 101,
  RTC_ACCEPT: 102,
  RTC_REJECT: 103,
  RTC_CANCEL: 104,
  RTC_FAILED: 105,
  RTC_HANDUP: 106,
  RTC_CANDIDATE: 107
} as const;

/** RTC 状态 */
export const RTC_STATE = {
  FREE: 0,
  WAIT_CALL: 1,
  WAIT_ACCEPT: 2,
  ACCEPTED: 3,
  CHATING: 4
} as const;

/** 终端类型 */
export const TERMINAL_TYPE = {
  WEB: 0,
  APP: 1
} as const;

/** 消息状态 */
export const MESSAGE_STATUS = {
  FAILED: -2,
  SENDING: -1,
  PENDING: 0,
  DELIVERED: 1,
  RECALL: 2,
  READED: 3
} as const;

/** 会话类型 */
export const CONVERSATION_TYPE = {
  PRIVATE: 1,
  GROUP: 2,
  SYSTEM: 3
} as const;

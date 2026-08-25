/** 会话 */
export interface Conversation {
  key: string;
  targetId: number;
  type: number;
  showName: string;
  headImage?: string;
  isDnd?: boolean;
  isTop?: boolean;
  lastContent?: string;
  lastSendTime?: number;
  optTime: number;
  unreadCount: number;
  atMe?: boolean;
  atAll?: boolean;
  lastAtMessageId: number;
  lastTimeTip: number;
  maxMessageId: number;
  minSeqNo: number;
  maxSeqNo: number;
  maxReadedId: number;
  sendNickName?: string;
}

/** 打开会话用的基础信息 */
export interface ChatInfo {
  type: number;
  targetId: number;
  showName: string;
  headImage?: string;
  isDnd?: boolean;
  isTop?: boolean;
}

/** 统一聊天消息（私聊 / 群聊 / 系统通知） */
export interface ChatMessage {
  id: number;
  localId: string;
  content: string;
  type: number;
  seqNo?: number;
  sendId?: number;
  recvId?: number;
  groupId?: number;
  sendNickName?: string;
  status?: number;
  sendTime?: number | string;
  deleted?: boolean;
  atUserIds?: number[];
  receipt?: boolean;
  receiptOk?: boolean;
  readedCount?: number;
  convKey?: string;
  selfSend?: boolean;
  /** 系统消息等 */
  title?: string;
}

/** 待发送消息（recvId/groupId 补全后再调对应 API） */
export interface SendMessageDTO {
  localId: string;
  content: string;
  type: number;
  receipt?: boolean;
  atUserIds?: number[];
  recvId?: number;
  groupId?: number;
}

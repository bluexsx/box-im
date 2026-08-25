/** 私聊消息相关类型（对齐 PrivateMessageController） */

export interface PrivateMessageDTO {
  /** 本地消息id,前端通过雪花算法生成 */
  localId: string;
  /** 接收用户id */
  recvId: number;
  /** 发送内容 */
  content: string;
  /** 消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 */
  type: number;
}

export interface PrivateMessageVO {
  /**  消息id */
  id: number;
  /** 本地消息id */
  localId: string;
  /** 消息序列号，会话内连续递增 */
  seqNo?: number;
  /**  发送者id */
  sendId?: number;
  /**  接收者id */
  recvId?: number;
  /**  发送内容 */
  content: string;
  /** 消息内容类型 MessageType */
  type: number;
  /**  状态 */
  status?: number;
  /**  发送时间 */
  sendTime?: number | string;
  /** 是否已删除 */
  deleted?: boolean;
  /** 前端：会话key */
  convKey?: string;
  /** 前端：是否自己发送 */
  selfSend?: boolean;
  /** 前端：卡片标题等 */
  title?: string;
  /** 发送者昵称（统一聊天列表） */
  sendNickName?: string;
  /** @用户列表 */
  atUserIds?: number[];
  /** 群聊id（统一聊天列表） */
  groupId?: number;
}

export interface PrivateMessageHistoryDTO {
  /** 好友id */
  friendId: number;
  /** 条件1:本地消息列表 */
  localIds?: string[];
  /** 条件2:消息序号列表 */
  seqNos?: number[];
  /** 条件3:最小消息序号 */
  minSeqNo?: number;
  /** 条件3:最大消息序号,0或负值表示不限制 */
  maxSeqNo?: number;
}

export interface MessageDeleteDTO {
  /** 会话id,即好友id/群id */
  chatId: number;
  /** 消息id */
  messageIds: number[];
}

export interface ChatDeleteDTO {
  /** 会话id,即好友id/群id */
  chatId: number;
}

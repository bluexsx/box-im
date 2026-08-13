/** 好友相关类型（对齐 FriendController） */

export interface FriendVO {
  /** 好友id */
  id: number;
  /** 好友昵称 */
  nickName: string;
  /** 好友头像 */
  headImage?: string;
  /** 是否开启免打扰 */
  isDnd?: boolean;
  /** 会话置顶（仅本地） */
  isTop?: boolean;
  /** 是否已删除 */
  deleted?: boolean;
  /** 版本号 */
  version?: number;
  /** 前端：是否在线 */
  online?: boolean;
  onlineWeb?: boolean;
  onlineApp?: boolean;
}

export interface FriendDndDTO {
  /** 好友用户id */
  friendId: number;
  /** 消息免打扰状态 */
  isDnd: boolean;
}

export interface UserOnlineVO {
  /** 用户id */
  userId?: number;
  /** 终端类型 */
  terminal?: number;
  /** 是否在线 */
  online?: boolean;
}

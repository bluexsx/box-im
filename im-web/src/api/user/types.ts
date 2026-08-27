/** 用户相关类型（对齐 OSS UserController） */

export interface UserVO {
  id: number;
  userName?: string;
  nickName: string;
  sex?: number;
  type?: number;
  signature?: string;
  headImage?: string;
  headImageThumb?: string;
  isBanned?: boolean;
  online?: boolean;
  reason?: string;
}

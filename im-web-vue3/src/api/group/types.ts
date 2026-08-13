/** 群聊相关类型（对齐 OSS GroupController） */

export interface GroupVO {
  id: number;
  name: string;
  ownerId?: number;
  headImage?: string;
  headImageThumb?: string;
  notice?: string;
  remarkNickName?: string;
  showNickName?: string;
  showGroupName: string;
  remarkGroupName?: string;
  dissolve?: boolean;
  quit?: boolean;
  isDnd?: boolean;
  /** 会话置顶（仅本地） */
  isTop?: boolean;
  isBanned?: boolean;
  reason?: string;
  version?: number;
  members?: GroupMemberVO[];
}

export interface GroupNewDTO {
  userIds: number[];
}

export interface GroupInviteDTO {
  groupId: number;
  friendIds: number[];
}

export interface GroupMemberVO {
  userId: number;
  showNickName: string;
  remarkNickName?: string;
  headImage?: string;
  quit?: boolean;
  version?: number;
  online?: boolean;
}

export interface GroupMemberRemoveDTO {
  groupId: number;
  userIds: number[];
}

export interface GroupDndDTO {
  groupId: number;
  isDnd: boolean;
}

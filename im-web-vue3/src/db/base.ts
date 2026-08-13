export const RECENT_EMOJI_MAX = 10;

/** 本地库基类（会话 key / 用户态） */
export default class BaseDB {
  userId: number | string | null = null;

  open(userId: number | string) {
    this.userId = userId;
  }

  close() {
    this.userId = null;
  }

  buildConversationKey(type: number, targetId: number | string) {
    return type + '-' + targetId;
  }

  async findRecentEmojis(_limit?: number): Promise<string[]> {
    return [];
  }

  async addRecentEmoji(_text: string): Promise<void> {}
}

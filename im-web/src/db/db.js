
const RECENT_EMOJI_MAX = 10;

class BaseDB {

  constructor() {
    this.userId = null;
  }

  open(userId) {
    this.userId = userId
  }

  close() {

  }

  buildConversationKey(type, targetId) {
    return type + '-' + targetId;
  }

  async findRecentEmojis() {
    return [];
  }

  async addRecentEmoji(text) {
  }
}

export { RECENT_EMOJI_MAX };
export default BaseDB;

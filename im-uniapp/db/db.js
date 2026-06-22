
const RECENT_EMOJI_MAX = 7;

class BaseDB {

  constructor() {
    this.userId = null;
  }

  async open(userId) {
    this.userId = userId
  }

  async close() {

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

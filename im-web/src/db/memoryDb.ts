import type { FriendVO } from '@/api/friend/types';
import type { GroupVO } from '@/api/group/types';
import type { ChatMessage, Conversation } from '@/types';
import BaseDB, { RECENT_EMOJI_MAX } from './base';

/** 内存版会话与消息存储，API 与 Dexie 版 indexDb 一致 */
class ImMemoryDB extends BaseDB {
  conversationMap = new Map<string, Conversation>();
  messageMap = new Map<string, ChatMessage>();
  convMessageMap = new Map<string, Map<string, ChatMessage>>();
  recentEmojiList: string[] = [];

  open(userId: number | string) {
    super.open(userId);
    this.conversationMap = new Map();
    this.messageMap = new Map();
    this.convMessageMap = new Map();
    this.recentEmojiList = [];
  }

  close() {
    this.conversationMap = new Map();
    this.messageMap = new Map();
    this.convMessageMap = new Map();
    this.recentEmojiList = [];
    super.close();
  }

  async loadAllConversations() {
    return Array.from(this.conversationMap.values());
  }

  async deleteConversationByKey(convKey: string) {
    this.conversationMap.delete(convKey);
    const convMessages = this.convMessageMap.get(convKey);
    if (!convMessages) {
      return;
    }
    for (const localId of convMessages.keys()) {
      this.messageMap.delete(localId);
    }
    this.convMessageMap.delete(convKey);
  }

  async findConversationByKey(key: string) {
    return this.conversationMap.get(key);
  }

  async saveConversation(conversation: Conversation) {
    this.conversationMap.set(conversation.key, conversation);
  }

  async saveConversationAndMessage(conversations: Conversation[], messages: ChatMessage[]) {
    for (const c of conversations) {
      this.conversationMap.set(c.key, c);
    }
    for (const m of messages) {
      this.messageMap.set(m.localId, m);
      this._convMessageMap(String(m.convKey)).set(m.localId, m);
    }
  }

  async deleteMessageByLocalId(localId: string | number) {
    const message = this.messageMap.get(String(localId));
    if (!message) {
      return;
    }
    message.deleted = true;
  }

  async deleteMessageByConvKey(convKey: string) {
    const convMessages = this.convMessageMap.get(convKey);
    if (!convMessages) {
      return;
    }
    for (const localId of convMessages.keys()) {
      this.messageMap.delete(localId);
    }
    this.convMessageMap.delete(convKey);
  }

  async saveMessage(message: ChatMessage) {
    this.messageMap.set(message.localId, message);
    this._convMessageMap(String(message.convKey)).set(message.localId, message);
  }

  async findMessageById(messageId: number | string, convKey: string) {
    return this._convMessages(convKey).find((m) => m.id == messageId);
  }

  async findMessageByLocalId(localId: string | number) {
    return this.messageMap.get(String(localId));
  }

  async findMessageByConvKey(convKey: string) {
    return this._convMessages(convKey);
  }

  async findRecentMessagesByConvKey(convKey: string, limit: number) {
    return this._convMessages(convKey)
      .sort((a, b) => {
        if (a.seqNo !== b.seqNo) {
          return Number(a.seqNo) - Number(b.seqNo);
        }
        return Number(a.sendTime) - Number(b.sendTime);
      })
      .slice(-limit);
  }

  async findPageMessage(convKey: string, minSeqNo: number, maxSeqNo: number) {
    return this._convMessages(convKey)
      .filter((m) => Number(m.seqNo) >= minSeqNo && Number(m.seqNo) <= maxSeqNo)
      .sort((a, b) => {
        if (a.seqNo !== b.seqNo) {
          return Number(a.seqNo) - Number(b.seqNo);
        }
        return Number(a.sendTime) - Number(b.sendTime);
      });
  }

  async findAllFriends() {
    return [] as FriendVO[];
  }

  async saveFriends(_friends: FriendVO[]) {}
  async saveFriend(_friend: FriendVO) {}
  async syncAllFriends(_friends: FriendVO[]) {}

  async findLastSyncFriendsTime() {
    return 0;
  }

  async findAllGroups() {
    return [] as GroupVO[];
  }

  async saveGroups(_groups: GroupVO[]) {}
  async saveGroup(_group: GroupVO) {}
  async syncAllGroups(_groups: GroupVO[]) {}

  async findLastSyncGroupsTime() {
    return 0;
  }

  async findRecentEmojis(limit = RECENT_EMOJI_MAX) {
    return this.recentEmojiList.slice(0, limit);
  }

  async addRecentEmoji(text: string) {
    this.recentEmojiList = [text, ...this.recentEmojiList.filter((item) => item !== text)].slice(0, RECENT_EMOJI_MAX);
  }

  _convMessageMap(convKey: string) {
    let convMessages = this.convMessageMap.get(convKey);
    if (!convMessages) {
      convMessages = new Map();
      this.convMessageMap.set(convKey, convMessages);
    }
    return convMessages;
  }

  _convMessages(convKey: string) {
    const convMessages = this.convMessageMap.get(convKey);
    return convMessages ? Array.from(convMessages.values()) : [];
  }
}

export default ImMemoryDB;

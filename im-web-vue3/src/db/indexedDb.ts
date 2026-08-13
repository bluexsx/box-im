import Dexie, { type Table } from 'dexie';
import { toRaw } from 'vue';
import type { FriendVO } from '@/api/friend/types';
import type { GroupVO } from '@/api/group/types';
import type { ChatMessage, Conversation } from '@/types';
import BaseDB, { RECENT_EMOJI_MAX } from './base';

const DB_NAME_PREFIX = 'im-web-';

interface DbConfig {
  key: string;
  val: number | string;
}

interface RecentEmoji {
  text: string;
  usedAt: number;
}

/** IndexedDB structured clone 无法处理 Vue Proxy，所有写入统一在此转为纯对象 */
function cloneForIdb<T>(data: T): T {
  return JSON.parse(JSON.stringify(toRaw(data as object))) as T;
}

class ImIndexedDB extends BaseDB {
  db: Dexie | null = null;

  open(userId: number | string) {
    super.open(userId);
    this.db = new Dexie(DB_NAME_PREFIX + userId);
    this.db.version(5).stores({
      // 配置表
      config: 'key',
      // 会话信息
      conversations: 'key',
      // 消息表
      messages: 'localId,id,[convKey+seqNo+sendTime]',
      // 好友表
      friends: 'id',
      // 群表
      groups: 'id',
      // 最近使用的默认表情
      recentEmojis: 'text,usedAt'
    });
  }

  close() {
    this.db?.close();
    this.db = null;
    super.close();
  }

  private table<T>(name: string): Table<T> {
    if (!this.db) {
      throw new Error('DB 未打开');
    }
    return this.db.table(name) as Table<T>;
  }

  private putPlain<T>(name: string, data: T) {
    return this.table<T>(name).put(cloneForIdb(data));
  }

  private bulkPutPlain<T>(name: string, list: T[]) {
    return this.table<T>(name).bulkPut(list.map((item) => cloneForIdb(item)));
  }

  private bulkAddPlain<T>(name: string, list: T[]) {
    return this.table<T>(name).bulkAdd(list.map((item) => cloneForIdb(item)));
  }

  async loadAllConversations() {
    return this.table<Conversation>('conversations').toArray();
  }

  async deleteConversationByKey(convKey: string) {
    return this.db!.transaction('rw', this.table('conversations'), this.table('messages'), async () => {
      await this.table<Conversation>('conversations').delete(convKey);
      await this.table<ChatMessage>('messages').where('convKey').equals(convKey).delete();
    });
  }

  async findConversationByKey(key: string) {
    return this.table<Conversation>('conversations').get(key);
  }

  async saveConversation(conversation: Conversation) {
    return this.putPlain('conversations', conversation);
  }

  async saveConversationAndMessage(conversations: Conversation[], messages: ChatMessage[]) {
    return this.db!.transaction('rw', this.table('conversations'), this.table('messages'), async () => {
      await this.bulkPutPlain('conversations', conversations);
      await this.bulkPutPlain('messages', messages);
    });
  }

  /**
   * 向上加载某个会话的历史消息
   * @param {string} convKey   会话key
   * @param {number} minSeqNo    最小消息序号
   * @param {object} maxSeqNo  最大消息序号
   */

  async findPageMessage(convKey: string, minSeqNo: number, maxSeqNo: number) {
    // 构建查询范围
    const lower = [convKey, minSeqNo, 0];
    const upper = [convKey, maxSeqNo, Infinity];
    return this.table<ChatMessage>('messages')
      .where('[convKey+seqNo+sendTime]')
      .between(lower, upper, true, true) // 后面两个true是包含边界值
      .toArray();
  }

  async deleteMessageByLocalId(localId: string | number) {
    return this.table<ChatMessage>('messages').update(localId, { deleted: true });
  }

  async deleteMessageByConvKey(convKey: string) {
    return this.table<ChatMessage>('messages').where('convKey').equals(convKey).delete();
  }

  async saveMessage(message: ChatMessage) {
    return this.putPlain('messages', message);
  }

  async findMessageById(messageId: number | string, convKey: string) {
    return this.table<ChatMessage>('messages')
      .where('id')
      .equals(messageId)
      .filter((m) => m.convKey === convKey)
      .first();
  }

  async findMessageByLocalId(localId: string | number) {
    return this.table<ChatMessage>('messages').where('localId').equals(localId).first();
  }

  async findMessageByConvKey(convKey: string) {
    return this.table<ChatMessage>('messages').where('convKey').equals(convKey).toArray();
  }

  async findAllFriends() {
    return this.table<FriendVO>('friends').toArray();
  }

  async saveFriends(friends: FriendVO[]) {
    await this.bulkPutPlain('friends', friends);
  }

  async saveFriend(friend: FriendVO) {
    return this.putPlain('friends', friend);
  }

  async syncAllFriends(friends: FriendVO[]) {
    return this.db!.transaction('rw', this.table('config'), this.table('friends'), async () => {
      await this.putPlain('config', {
        key: 'lastSyncFriendTime',
        val: new Date().getTime()
      } as DbConfig);
      await this.table<FriendVO>('friends').clear();
      await this.bulkAddPlain('friends', friends);
    });
  }

  async findLastSyncFriendsTime() {
    const config = await this.table<DbConfig>('config').where('key').equals('lastSyncFriendTime').first();
    return config ? Number(config.val) : 0;
  }

  async findAllGroups() {
    return this.table<GroupVO>('groups').toArray();
  }

  async saveGroups(groups: GroupVO[]) {
    await this.bulkPutPlain('groups', groups);
  }

  async saveGroup(group: GroupVO) {
    return this.putPlain('groups', group);
  }

  async syncAllGroups(groups: GroupVO[]) {
    return this.db!.transaction('rw', this.table('config'), this.table('groups'), async () => {
      await this.putPlain('config', {
        key: 'lastSyncGroupTime',
        val: new Date().getTime()
      } as DbConfig);
      await this.table<GroupVO>('groups').clear();
      await this.bulkAddPlain('groups', groups);
    });
  }

  async findLastSyncGroupsTime() {
    const config = await this.table<DbConfig>('config').where('key').equals('lastSyncGroupTime').first();
    return config ? Number(config.val) : 0;
  }

  async findRecentEmojis(limit = RECENT_EMOJI_MAX) {
    const list = await this.table<RecentEmoji>('recentEmojis').orderBy('usedAt').reverse().limit(limit).toArray();
    return list.map((item) => item.text);
  }

  async addRecentEmoji(text: string) {
    await this.putPlain('recentEmojis', { text, usedAt: Date.now() });
    const count = await this.table<RecentEmoji>('recentEmojis').count();
    if (count > RECENT_EMOJI_MAX) {
      const oldItems = await this.table<RecentEmoji>('recentEmojis')
        .orderBy('usedAt')
        .limit(count - RECENT_EMOJI_MAX)
        .toArray();
      await this.table<RecentEmoji>('recentEmojis').bulkDelete(oldItems.map((item) => item.text));
    }
  }
}

export default ImIndexedDB;

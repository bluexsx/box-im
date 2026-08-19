import Dexie from 'dexie';
import DB, { RECENT_EMOJI_MAX } from "./db.js";

const DB_NAME_PREFIX = 'im-app-';

class ImIndexedDB extends DB {
	constructor() {
		super();
		this.db = null;
	}

	open(userId) {
		super.open(userId);
		this.db = new Dexie(DB_NAME_PREFIX + userId);
		this.db.version(4).stores({
			config: 'key',
			conversations: 'key',
			messages: 'localId,id,[convKey+seqNo+sendTime]',
			friends: 'id',
			groups: 'id'
		});
		this.db.version(5).stores({
			config: 'key',
			conversations: 'key',
			messages: 'localId,id,[convKey+seqNo+sendTime]',
			friends: 'id',
			groups: 'id',
			recentEmojis: 'text,usedAt'
		});
	}

	close() {
		this.db && this.db.close();
	}


	async loadAllConversations() {
		return this.db.conversations.toArray();
	}

	async deleteConversationByKey(convKey) {
		return this.db.transaction('rw', this.db.conversations, this.db.messages, async () => {
			await this.db.conversations.delete(convKey);
			await this.db.messages.where("convKey").equals(convKey).delete();
		});
	}

	async findConversationByKey(key) {
		return this.db.conversations.get(key);
	}

	async saveConversation(conversation) {
		return this.db.conversations.put(conversation);
	}

	async saveConversationAndMessage(conversations, messages) {
		return this.db.transaction('rw', this.db.conversations, this.db.messages, async () => {
			await this.db.conversations.bulkPut(conversations);
			await this.db.messages.bulkPut(messages);
		})
	}

	/**
	 * 向上加载某个会话的历史消息
	 * @param {string} convKey   会话key
	 * @param {number} minSeqNo    最小消息序号
	 * @param {object} maxSeqNo  最大消息序号
	 */
	async findPageMessage(convKey, minSeqNo, maxSeqNo) {
		// 构建查询范围
		const lower = [convKey, minSeqNo, 0];
		const upper = [convKey, maxSeqNo, Infinity];
		const messages = await this.db.messages
			.where('[convKey+seqNo+sendTime]')
			.between(lower, upper, true, true) // 后面两个true是包含边界值
			.toArray();
		return messages;
	}

	async deleteMessageByLocalId(localId) {
		return await this.db.messages.update(localId, { deleted: true });
	}

	async deleteMessageByConvKey(convKey) {
		return await this.db.messages.where("convKey").equals(convKey).delete();
	}

	async saveMessage(message) {
		return await this.db.messages.put(message);
	}

	async findMessageById(convKey, messageId) {
		return await this.db.messages
			.where('convKey')
			.equals(convKey)
			.filter(m => m.id == messageId)
			.first();
	}

	async findMessageByLocalId(localId) {
		return await this.db.messages.where('localId').equals(localId).first();
	}

	async findMessageByConvKey(convKey) {
		return this.db.messages.where("convKey").equals(convKey).toArray();
	}

	async findRecentMessagesByConvKey(convKey, limit) {
		const messages = await this.db.messages
			.where('[convKey+seqNo+sendTime]')
			.between([convKey, Dexie.minKey, 0], [convKey, Dexie.maxKey, Infinity], true, true)
			.reverse()
			.limit(limit)
			.toArray();
		return messages.reverse();
	}

	async findAllFriends() {
		return await this.db.friends.toArray();
	}

	async saveFriends(friends) {
		await this.db.friends.bulkPut(friends);
	}

	async saveFriend(friend) {
		return await this.db.friends.put(friend);
	}

	async syncAllFriends(friends) {
		return this.db.transaction('rw', this.db.config, this.db.friends, async () => {
			const config = {
				key: 'lastSyncFriendTime',
				val: new Date().getTime()
			}
			await this.db.config.put(config);
			await this.db.friends.clear();
			await this.db.friends.bulkAdd(friends);
		});
	}

	async findLastSyncFriendsTime(friends) {
		const config = await this.db.config.where('key').equals('lastSyncFriendTime').first();
		return config ? config.val : 0;
	}

	async findAllGroups() {
		return await this.db.groups.toArray();
	}

	async saveGroups(groups) {
		await this.db.groups.bulkPut(groups);
	}

	async saveGroup(group) {
		return await this.db.groups.put(group);
	}

	async syncAllGroups(groups) {
		return this.db.transaction('rw', this.db.config, this.db.groups, async () => {
			const config = {
				key: 'lastSyncGroupTime',
				val: new Date().getTime()
			}
			await this.db.config.put(config);
			await this.db.groups.clear();
			await this.db.groups.bulkAdd(groups);
		});
	}

	async findLastSyncGroupsTime(groups) {
		const config = await this.db.config.where('key').equals('lastSyncGroupTime').first();
		return config ? config.val : 0;
	}

	async findRecentEmojis(limit = RECENT_EMOJI_MAX) {
		const list = await this.db.recentEmojis
			.orderBy('usedAt')
			.reverse()
			.limit(limit)
			.toArray();
		return list.map(item => item.text);
	}

	async addRecentEmoji(text) {
		await this.db.recentEmojis.put({ text, usedAt: Date.now() });
		const count = await this.db.recentEmojis.count();
		if (count > RECENT_EMOJI_MAX) {
			const oldItems = await this.db.recentEmojis
				.orderBy('usedAt')
				.limit(count - RECENT_EMOJI_MAX)
				.toArray();
			await this.db.recentEmojis.bulkDelete(oldItems.map(item => item.text));
		}
	}
}


export default ImIndexedDB;
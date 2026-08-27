import DB, { RECENT_EMOJI_MAX } from "./db.js";

const DB_NAME_PREFIX = 'im-app-';
const MAX_MESSAGES_PER_CONV = 50;

/**
 * storage版会话与消息存储，API 与 Dexie 版 indexDb 一致
 * 每个会话消息独立存储，key = dbName + '-' + convKey，最多保留50条
 */
class ImStorageDB extends DB {
	constructor() {
		super();
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
		this.recentEmojiList = [];
		this.dbName = '';
	}

	async open(userId) {
		this.userId = userId;
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
		this.recentEmojiList = [];
		this.dbName = DB_NAME_PREFIX + userId;
		this._loadFromStorage();
		this._loadRecentEmojisFromStorage();
	}

	async close() {
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
		this.recentEmojiList = [];
		this.userId = null;
		this.dbName = '';
	}

	async loadAllConversations() {
		return Array.from(this.conversationMap.values());
	}

	async deleteConversationByKey(convKey) {
		this.conversationMap.delete(convKey);
		this._removeConvMessages(convKey);
		this._saveConversations();
	}

	async findConversationByKey(key) {
		return this.conversationMap.get(key);
	}

	async saveConversation(conversation) {
		this.conversationMap.set(conversation.key, conversation);
		this._saveConversations();
	}

	async saveConversationAndMessage(conversations, messages) {
		for (const m of messages) {
			const convMessages = this._convMessageMap(m.convKey);
			convMessages.set(m.localId, m)
		}
		for (const c of conversations) {
			this.conversationMap.set(c.key, c);
			this._saveConvMessages(c.key);
		}
		this._saveConversations();
	}

	async deleteMessageByLocalId(localId) {
		const message = this.messageMap.get(localId);
		if (!message) {
			return;
		}
		this.messageMap.delete(localId);
		const convMessages = this.convMessageMap.get(message.convKey);
		if (!convMessages) {
			return;
		}
		convMessages.delete(localId);
		if (!convMessages.size) {
			this.convMessageMap.delete(message.convKey);
			uni.removeStorageSync(this._convStorageKey(message.convKey));
		} else {
			this._saveConvMessages(message.convKey);
		}
	}

	async deleteMessageByConvKey(convKey) {
		this._removeConvMessages(convKey);
	}

	async saveMessage(message) {
		const convMessages = this._convMessageMap(message.convKey);
		convMessages.set(message.localId, message);
		this.messageMap.set(message.localId, message);
		this._saveConvMessages(message.convKey);
	}

	async findMessageById(messageId, convKey) {
		return this._convMessages(convKey).find(m => m.id == messageId);
	}

	async findMessageByLocalId(localId) {
		return this.messageMap.get(localId);
	}

	async findMessageByConvKey(convKey) {
		return this._convMessages(convKey);
	}

	async findRecentMessagesByConvKey(convKey, limit) {
		return this._convMessages(convKey)
			.sort((a, b) => {
				if (a.seqNo !== b.seqNo) {
					return a.seqNo - b.seqNo;
				}
				return a.sendTime - b.sendTime;
			})
			.slice(-limit);
	}

	async findPageMessage(convKey, minSeqNo, maxSeqNo) {
		return this._convMessages(convKey)
			.filter((m) => m.seqNo >= minSeqNo && m.seqNo <= maxSeqNo)
			.sort((a, b) => {
				if (a.seqNo !== b.seqNo) {
					return a.seqNo - b.seqNo;
				}
				return a.sendTime - b.sendTime;
			});
	}

	async findAllFriends() { return []; }

	async saveFriends(friends) {}

	async saveFriend(friend) {}

	async syncAllFriends(friends) {}

	async findLastSyncFriendsTime(friends) { return 0; }

	async findAllGroups() { return []; }

	async saveGroups(groups) {}

	async saveGroup(group) {}

	async syncAllGroups(friends) {}

	async findLastSyncGroupsTime(friends) { return 0; }

	async findRecentEmojis(limit = RECENT_EMOJI_MAX) {
		if (!this.userId) {
			return [];
		}
		return this.recentEmojiList.slice(0, limit);
	}

	async addRecentEmoji(text) {
		if (!this.userId) {
			return;
		}
		this.recentEmojiList = [
			text,
			...this.recentEmojiList.filter(item => item !== text)
		].slice(0, RECENT_EMOJI_MAX);
		this._saveRecentEmojis();
	}

	_recentEmojiStorageKey() {
		return this.dbName + '-recentEmojis';
	}

	_loadRecentEmojisFromStorage() {
		this.recentEmojiList = uni.getStorageSync(this._recentEmojiStorageKey()) || [];
	}

	_saveRecentEmojis() {
		uni.setStorageSync(this._recentEmojiStorageKey(), this.recentEmojiList);
	}

	_convStorageKey(convKey) {
		return this.dbName + '-' + convKey;
	}

	_loadFromStorage() {
		const conversations = uni.getStorageSync(this.dbName) || [];
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
		conversations.forEach(conv => {
			this.conversationMap.set(conv.key, conv);
			const messages = uni.getStorageSync(this._convStorageKey(conv.key)) || [];
			if (!messages.length) {
				return;
			}
			const convMessages = new Map();
			messages.forEach(m => {
				convMessages.set(m.localId, m);
				this.messageMap.set(m.localId, m);
			});
			this.convMessageMap.set(conv.key, convMessages);
		});
	}

	_saveConversations() {
		uni.setStorageSync(this.dbName, Array.from(this.conversationMap.values()));
	}

	_sortMessages(messages) {
		return messages.sort((a, b) => {
			if (a.seqNo !== b.seqNo) {
				return a.seqNo - b.seqNo;
			}
			return a.sendTime - b.sendTime;
		});
	}

	_trimToLast(messages) {
		const sorted = this._sortMessages([...messages]);
		if (sorted.length <= MAX_MESSAGES_PER_CONV) {
			return sorted;
		}
		return sorted.slice(-MAX_MESSAGES_PER_CONV);
	}

	_saveConvMessages(convKey) {
		const messages = this._convMessages(convKey)
		const trimMessages= this._trimToLast(messages);
		const storageKey = this._convStorageKey(convKey);
		if (trimMessages.length) {
			uni.setStorageSync(storageKey, trimMessages);
		} else {
			uni.removeStorageSync(storageKey);
		}
	}


	_removeConvMessages(convKey) {
		const convMessages = this.convMessageMap.get(convKey);
		if (!convMessages) {
			return;
		}
		for (const localId of convMessages.keys()) {
			this.messageMap.delete(localId);
		}
		this.convMessageMap.delete(convKey);
		uni.removeStorageSync(this._convStorageKey(convKey));
	}

	_convMessageMap(convKey) {
		let convMessages = this.convMessageMap.get(convKey);
		if (!convMessages) {
			convMessages = new Map();
			this.convMessageMap.set(convKey, convMessages);
		}
		return convMessages;
	}

	_convMessages(convKey) {
		const convMessages = this.convMessageMap.get(convKey);
		return convMessages ? Array.from(convMessages.values()) : [];
	}

}

export default ImStorageDB;
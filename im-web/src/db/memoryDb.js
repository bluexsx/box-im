import DB, { RECENT_EMOJI_MAX } from "./db.js";

/**
 * 内存版会话与消息存储，API 与 Dexie 版 indexDb 一致
 */
class ImMemoryDB extends DB {
	constructor() {
		super();
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
		this.recentEmojiList = [];
	}

	open(userId) {
		this.userId = userId;
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
		this.userId = null;
	}

	async loadAllConversations() {
		return Array.from(this.conversationMap.values());
	}

	async deleteConversationByKey(convKey) {
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

	async findConversationByKey(key) {
		return this.conversationMap.get(key);
	}

	async saveConversation(conversation) {
		this.conversationMap.set(conversation.key, conversation);
	}

	async saveConversationAndMessage(conversations, messages) {
		for (const c of conversations) {
			this.conversationMap.set(c.key, c);
		}
		for (const m of messages) {
			this.messageMap.set(m.localId, m);
			this._convMessageMap(m.convKey).set(m.localId, m);
		}
	}

	async deleteMessageByLocalId(localId) {
		const message = this.messageMap.get(localId);
		if (!message) {
			return;
		}
		message.deleted = true;
	}

	async deleteMessageByConvKey(convKey) {
		const convMessages = this.convMessageMap.get(convKey);
		if (!convMessages) {
			return;
		}
		for (const localId of convMessages.keys()) {
			this.messageMap.delete(localId);
		}
		this.convMessageMap.delete(convKey);
	}

	async saveMessage(message) {
		this.messageMap.set(message.localId, message);
		this._convMessageMap(message.convKey).set(message.localId, message);
	}

	async findMessageById(convKey, messageId) {
		return this._convMessages(convKey).find(m => m.id == messageId);
	}

	async findMessageByLocalId(localId) {
		return this.messageMap.get(localId);
	}

	async findMessageByConvKey(convKey) {
		return this._convMessages(convKey);
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

	async saveFriends(friends) { }

	async saveFriend(friend) { }

	async syncAllFriends(friends) { }

	async findLastSyncFriendsTime(friends) { return 0; }

	async findAllGroups() { return []; }

	async saveGroups(groups) { }

	async saveGroup(group) { }

	async syncAllGroups(groups) { }

	async findLastSyncGroupsTime(groups) { return 0; }

	async findRecentEmojis(limit = RECENT_EMOJI_MAX) {
		return this.recentEmojiList.slice(0, limit);
	}

	async addRecentEmoji(text) {
		this.recentEmojiList = [
			text,
			...this.recentEmojiList.filter(item => item !== text)
		].slice(0, RECENT_EMOJI_MAX);
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

export default ImMemoryDB;

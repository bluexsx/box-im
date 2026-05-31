import DB from "./db.js";

const DB_NAME_PREFIX = 'im-app-';
/**
 * storage版会话与消息存储，API 与 Dexie 版 indexDb 一致
 */
class ImStorageDB extends DB {
	constructor() {
		super();
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
		this.dbName = '';
	}

	async open(userId) {
		this.userId = userId;
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
		this.dbName = DB_NAME_PREFIX + userId;
		this._loadFromStorage();
	}

	async close() {
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
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
		this._saveToStorage();
	}

	async findConversationByKey(key) {
		return this.conversationMap.get(key);
	}

	async saveConversation(conversation) {
		this.conversationMap.set(conversation.key, conversation);
		this._saveToStorage();
	}

	async saveConversationAndMessage(conversations, messages) {
		for (const c of conversations) {
			this.conversationMap.set(c.key, c);
		}
		for (const m of messages) {
			this.messageMap.set(m.localId, m);
			this._convMessageMap(m.convKey).set(m.localId, m);
		}
		this._saveToStorage();
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
		}
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

	async saveFriends(friends) {}

	async saveFriend(friend) {}

	async syncAllFriends(friends) {}

	async findLastSyncFriendsTime(friends) { return 0; }

	async findAllGroups() { return []; }

	async saveGroups(groups) {}

	async saveGroup(group) {}

	async syncAllGroups(friends) {}

	async findLastSyncGroupsTime(friends) { return 0; }

	_loadFromStorage() {
		const conversations = uni.getStorageSync(this.dbName) || [];
		this.conversationMap = new Map();
		this.messageMap = new Map();
		this.convMessageMap = new Map();
		conversations.forEach(conv => this.conversationMap.set(conv.key, conv))
	}


	_saveToStorage() {
		uni.setStorageSync(this.dbName, Array.from(this.conversationMap.values()));
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
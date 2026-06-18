import DB, { RECENT_EMOJI_MAX } from "./db.js";

const DB_NAME_PREFIX = 'im-app-';

class ImSqliteDB extends DB {
	constructor() {
		super();
		this.dbName = '';
		this.dbPath = '';
	}

	async open(userId) {
		this.userId = userId;
		this.dbName = DB_NAME_PREFIX + userId;
		this.dbPath = `_doc/${this.dbName}.db`;
		if (!this._isOpenDatabase()) {
			await this._openDatabase();
		}
		await this._executeSql(`
			CREATE TABLE IF NOT EXISTS conversations (
				"key" TEXT PRIMARY KEY,
				"data" TEXT NOT NULL
			)
		`);
		await this._executeSql(`
			CREATE TABLE IF NOT EXISTS messages (
				"localId" TEXT PRIMARY KEY,
				"id" INTEGER,
				"convKey" TEXT NOT NULL,
				"sendTime" INTEGER NOT NULL,
				"seqNo" INTEGER,
				"data" TEXT NOT NULL
			)
		`);
		await this._executeSql(`
			CREATE TABLE IF NOT EXISTS config (
				"key" TEXT PRIMARY KEY,
				"val" INTEGER
			)
		`);
		await this._executeSql(`
			CREATE TABLE IF NOT EXISTS friends (
				"id" INTEGER PRIMARY KEY,
				"data" TEXT NOT NULL
			)
		`);
		await this._executeSql(`
			CREATE TABLE IF NOT EXISTS groups (
				"id" INTEGER PRIMARY KEY,
				"data" TEXT NOT NULL
			)
		`);
		await this._executeSql(`
			CREATE TABLE IF NOT EXISTS recent_emojis (
				"text" TEXT PRIMARY KEY,
				"usedAt" INTEGER NOT NULL
			)
		`);
		await this._executeSql('CREATE INDEX IF NOT EXISTS idx_messages_id ON messages(id)');
		await this._executeSql('CREATE INDEX IF NOT EXISTS idx_messages_conv_key ON messages(convKey)');
		await this._executeSql(
			'CREATE INDEX IF NOT EXISTS idx_messages_conv_seq_time ON messages(convKey, seqNo, sendTime)');
	}

	async close() {
		if (!this._isOpenDatabase()) {
			return;
		}
		await new Promise((resolve) => {
			plus.sqlite.closeDatabase({
				name: this.dbName,
				success: () => resolve(),
				fail: () => resolve()
			});
		});
	}

	async loadAllConversations() {
		const rows = await this._selectSql('SELECT data FROM conversations');
		return rows.map((row) => JSON.parse(row.data));
	}

	async deleteConversationByKey(convKey) {
		await this._begin();
		try {
			await this._executeSql(`DELETE FROM conversations WHERE "key" = ${this._text(convKey)}`);
			await this._executeSql(`DELETE FROM messages WHERE "convKey" = ${this._text(convKey)}`);
			await this._commit();
		} catch (e) {
			await this._rollback();
			throw e;
		}
	}

	async findConversationByKey(key) {
		const rows = await this._selectSql(
			`SELECT data FROM conversations WHERE "key" = ${this._text(key)} LIMIT 1`);
		return rows.length ? JSON.parse(rows[0].data) : undefined;
	}

	async saveConversation(conversation) {
		await this._executeSql(`
			INSERT OR REPLACE INTO conversations("key", "data")
			VALUES(${this._text(conversation.key)}, ${this._text(JSON.stringify(conversation))})
		`);
	}

	async saveConversationAndMessage(conversations, messages) {
		await this._begin();
		try {
			if (conversations.length) {
				const convValues = conversations
					.map((c) => `(${this._text(c.key)}, ${this._text(JSON.stringify(c))})`)
					.join(',');
				await this._executeSql(`
					INSERT OR REPLACE INTO conversations("key", "data")
					VALUES ${convValues}
				`);
			}
			if (messages.length) {
				const msgValues = messages.map((message) => {
					return `(${this._text(message.localId)}, ${this._number(message.id)}, 
							${this._text(message.convKey)}, ${message.sendTime}, 
							${this._number(message.seqNo)}, ${this._text(JSON.stringify(message))})`;
				}).join(',');
				await this._executeSql(`
					INSERT OR REPLACE INTO messages("localId", "id", "convKey", "sendTime", "seqNo", "data")
					VALUES ${msgValues}
				`);
			}
			await this._commit();
		} catch (e) {
			await this._rollback();
			throw e;
		}
	}

	/**
	 * 加载某个会话的历史消息（与 indexedDb 复合索引 [convKey+seqNo+sendTime] 语义一致）
	 * @param {string} convKey   会话key
	 * @param {number} minSeqNo    最小消息序号（含）
	 * @param {number} maxSeqNo    最大消息序号（含）
	 */
	async findPageMessage(convKey, minSeqNo, maxSeqNo) {
		const rows = await this._selectSql(`
			SELECT data FROM messages
			WHERE "convKey" = ${this._text(convKey)}
			  AND "seqNo" >= ${this._number(minSeqNo)}
			  AND "seqNo" <= ${this._number(maxSeqNo)}
			ORDER BY "seqNo" ASC, "sendTime" ASC
		`);
		return rows.map((row) => JSON.parse(row.data));
	}

	async deleteMessageByLocalId(localId) {
		await this._executeSql(`DELETE FROM messages WHERE "localId" = ${this._text(localId)}`);
	}

	async deleteMessageByConvKey(convKey) {
		await this._executeSql(`DELETE FROM messages WHERE "convKey" = ${this._text(convKey)}`);
	}

	async saveMessage(message) {
		await this._executeSql(`
			INSERT OR REPLACE INTO messages("localId", "id", "convKey", "sendTime", "seqNo", "data")
			VALUES(
				${this._text(message.localId)},
				${this._number(message.id)},
				${this._text(message.convKey)},
				${message.sendTime},
				${this._number(message.seqNo)},
				${this._text(JSON.stringify(message))}
			)
		`);
	}

	async findMessageById(convKey, messageId) {
		const rows = await this._selectSql(
			`SELECT data FROM messages WHERE "convKey" = ${this._text(convKey)} AND "id" = ${this._number(messageId)} LIMIT 1`);
		return rows.length ? JSON.parse(rows[0].data) : undefined;
	}

	async findMessageByLocalId(localId) {
		const rows = await this._selectSql(
			`SELECT data FROM messages WHERE "localId" = ${this._text(localId)} LIMIT 1`);
		return rows.length ? JSON.parse(rows[0].data) : undefined;
	}

	async findMessageByConvKey(convKey) {
		const rows = await this._selectSql(`
			SELECT data FROM messages
			WHERE "convKey" = ${this._text(convKey)}
			ORDER BY "sendTime" ASC, "localId" ASC
		`);
		return rows.map((row) => JSON.parse(row.data));
	}

	async findAllFriends() {
		const rows = await this._selectSql('SELECT data FROM friends');
		return rows.map((row) => JSON.parse(row.data));
	}

	async saveFriends(friends) {
		if (!friends.length) {
			return;
		}
		const values = friends
			.map((friend) => `(${this._number(friend.id)}, ${this._text(JSON.stringify(friend))})`)
			.join(',');
		await this._executeSql(`
			INSERT OR REPLACE INTO friends("id", "data")
			VALUES ${values}
		`);
	}

	async saveFriend(friend) {
		await this._executeSql(`
			INSERT OR REPLACE INTO friends("id", "data")
			VALUES(${this._number(friend.id)}, ${this._text(JSON.stringify(friend))})
		`);
	}

	async syncAllFriends(friends) {
		await this._executeSql(`DELETE FROM config WHERE "key" = 'lastSyncFriendTime'`);
		await this._executeSql('DELETE FROM friends');
		if (friends.length) {
			const values = friends
				.map((friend) => `(${this._number(friend.id)}, ${this._text(JSON.stringify(friend))})`)
				.join(',');
			await this._executeSql(`
				INSERT INTO friends("id", "data")
				VALUES ${values}
			`);
		}
		await this._executeSql(`
			INSERT OR REPLACE INTO config("key", "val")
			VALUES('lastSyncFriendTime', ${Date.now()})
		`);
	}

	async findLastSyncFriendsTime() {
		const rows = await this._selectSql(
			`SELECT val FROM config WHERE "key" = 'lastSyncFriendTime' LIMIT 1`);
		return rows.length ? Number(rows[0].val) : 0;
	}

	async findAllGroups() {
		const rows = await this._selectSql('SELECT data FROM groups');
		return rows.map((row) => JSON.parse(row.data));
	}

	async saveGroups(groups) {
		if (!groups.length) {
			return;
		}
		const values = groups
			.map((group) => `(${this._number(group.id)}, ${this._text(JSON.stringify(group))})`)
			.join(',');
		await this._executeSql(`
			INSERT OR REPLACE INTO groups("id", "data")
			VALUES ${values}
		`);
	}

	async saveGroup(group) {
		await this._executeSql(`
			INSERT OR REPLACE INTO groups("id", "data")
			VALUES(${this._number(group.id)}, ${this._text(JSON.stringify(group))})
		`);
	}

	async syncAllGroups(groups) {
		await this._executeSql(`DELETE FROM config WHERE "key" = 'lastSyncGroupTime'`);
		await this._executeSql('DELETE FROM groups');
		if (groups.length) {
			const values = groups
				.map((group) => `(${this._number(group.id)}, ${this._text(JSON.stringify(group))})`)
				.join(',');
			await this._executeSql(`
				INSERT INTO groups("id", "data")
				VALUES ${values}
			`);
		}
		await this._executeSql(`
			INSERT OR REPLACE INTO config("key", "val")
			VALUES('lastSyncGroupTime', ${Date.now()})
		`);
	}

	async findLastSyncGroupsTime() {
		const rows = await this._selectSql(
			`SELECT val FROM config WHERE "key" = 'lastSyncGroupTime' LIMIT 1`);
		return rows.length ? Number(rows[0].val) : 0;
	}

	async findRecentEmojis(limit = RECENT_EMOJI_MAX) {
		const rows = await this._selectSql(`
			SELECT "text" FROM recent_emojis
			ORDER BY "usedAt" DESC
			LIMIT ${limit}
		`);
		return rows.map((row) => row.text);
	}

	async addRecentEmoji(text) {
		await this._executeSql(`
			INSERT OR REPLACE INTO recent_emojis("text", "usedAt")
			VALUES(${this._text(text)}, ${Date.now()})
		`);
		const countRows = await this._selectSql('SELECT COUNT(*) AS cnt FROM recent_emojis');
		const count = Number(countRows[0]?.cnt || 0);
		if (count > RECENT_EMOJI_MAX) {
			const oldRows = await this._selectSql(`
				SELECT "text" FROM recent_emojis
				ORDER BY "usedAt" ASC
				LIMIT ${count - RECENT_EMOJI_MAX}
			`);
			for (const row of oldRows) {
				await this._executeSql(`DELETE FROM recent_emojis WHERE "text" = ${this._text(row.text)}`);
			}
		}
	}

	_openDatabase() {
		return new Promise((resolve, reject) => {
			plus.sqlite.openDatabase({
				name: this.dbName,
				path: this.dbPath,
				success: () => resolve(),
				fail: (error) => reject(error)
			});
		});
	}

	_isOpenDatabase() {
		return plus.sqlite.isOpenDatabase({
			name: this.dbName,
			path: this.dbPath
		});
	}

	_selectSql(sql) {
		return new Promise((resolve, reject) => {
			plus.sqlite.selectSql({
				name: this.dbName,
				sql: sql,
				success: (rows) => resolve(rows || []),
				fail: (error) => reject(error)
			});
		});
	}

	_executeSql(sql) {
		return new Promise((resolve, reject) => {
			plus.sqlite.executeSql({
				name: this.dbName,
				sql: sql,
				success: () => resolve(),
				fail: (error) => reject(error)
			});
		});
	}

	_begin() {
		return this._executeSql('BEGIN');
	}

	_commit() {
		return this._executeSql('COMMIT');
	}

	_rollback() {
		return this._executeSql('ROLLBACK');
	}

	_text(value) {
		if (value === null || value === undefined) {
			return 'NULL';
		}
		return `'${String(value).replace(/'/g, "''")}'`;
	}

	_number(value) {
		if (value === null || value === undefined || value === '') {
			return 'NULL';
		}
		return Number(value);
	}
}

export default ImSqliteDB;
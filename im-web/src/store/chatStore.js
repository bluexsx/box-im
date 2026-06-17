import { defineStore } from 'pinia';
import { MESSAGE_TYPE, MESSAGE_STATUS, CONVERSATION_TYPE } from "../api/enums.js"
import useUserStore from './userStore.js';
import useFriendStore from './friendStore.js';
import useGroupStore from './groupStore.js';
import * as messageUtil from '../api/messageUtil.js';
import { getDB } from '../db/index.js';
import nextSnowflakeId from '@/api/snowflake.js';
import http from '../api/httpRequest.js'

export default defineStore('chatStore', {
	state: () => {
		return {
			activeConversation: null, // 当前选中的会话
			conversations: [], 	// 全部会话列表
			conversationMap: new Map(), // 全部会话map
			loading: true,	// 是否正在加载离线消息
			loadingMessage: false,	// 是否正在从本地数据库拉取消息
			messages: [],	// 当前会话展示的消息列表
			hasMoreLastMessage: true, // 当前会话是否还有更多上翻消息
			hasMoreNextMessage: false, // 当前会话是否还有更多下翻消息,
			isInBottom: true,	// 滚动条是否在消息底部
			newMessageSize: 0,	// 新消息数量,
			minSeqNo: 0,	// 最小消息序号
			maxSeqNo: 0,	// 最大消息序号
		}
	},
	actions: {
		init(conversations) {
			conversations.forEach(conv => {
				conv.draftText = ''
				// 兼容历史数据缺失的字段
				if (!conv.minSeqNo) {
					conv.minSeqNo = 0;
				}
			});
			this.conversations = conversations;
			this.conversationMap.clear();
			this.conversations.forEach(conv => this.conversationMap.set(conv.key, conv));
			this.refreshDnd();
			this.startSortTimer();
		},
		append(conversations) {
			conversations.forEach(conv => {
				conv.draftText = '';
				if (this.conversationMap.has(conv.key)) {
					// 更新
					let conversation = this.conversationMap.get(conv.key);
					Object.assign(conversation, conv);
				} else {
					// 新增
					this.conversations.push(conv);
					this.conversationMap.set(conv.key, conv);
				}
				// 当前会话拉到了新消息
				if (this.isActive(conv.key)) {
					this.hasMoreNextMessage = true;
				}
			})
			this.refreshDnd();
		},
		setActive(convKey) {
			const conv = this.conversationMap.get(convKey);
			if (!this.activeConversation || convKey != this.activeConversation.key) {
				this.activeConversation = conv;
				this.messages = [];
				this.hasMoreNextMessage = false;
				this.hasMoreLastMessage = true;
				this.loadingMessage = false;
				this.minSeqNo = 0;
				this.maxSeqNo = 0;
			}
		},
		setIsInBottom(isInBottom) {
			this.isInBottom = isInBottom;
			// 既然在底部，新消息已经显示了
			if (isInBottom) {
				this.newMessageSize = 0;
			}
		},
		async openChat(chatInfo) {
			const key = getDB().buildConversationKey(chatInfo.type, chatInfo.targetId)
			let conv = this.conversationMap.get(key);
			// 创建会话
			if (conv == null) {
				conv = {
					key: key,	// 会话唯一key
					targetId: chatInfo.targetId,	// 会话对象id
					type: chatInfo.type,	// 会话类型 私聊|群聊|系统消息
					showName: chatInfo.showName,	// 昵称
					headImage: chatInfo.headImage,	// 头像
					isDnd: chatInfo.isDnd,	// 会话是否开启免打扰
					isTop: false,	// 会话是否置顶
					lastContent: "",	// 会话最后一条消息的内容
					lastSendTime: new Date().getTime(),	// 会话最后一条消息的发送时间
					optTime: new Date().getTime(), 	// 会话最后一次操作时间
					unreadCount: 0,	// 会话未读消息数量
					atMe: false,	// 会话是否有@我的消息
					atAll: false,	// 会话是否有@所有人的消息
					lastAtMessageId: -1, // 最后一条@我的消息id
					lastTimeTip: 0,	// 最后插入的时间提示
					maxMessageId: 0, // 会话最大消息id
					minSeqNo: 0, 	// 最小消息序号
					maxSeqNo: 0, // 最大消息序号
					maxReadedId: 0, // 已读最大消息id
				};
				this.conversations.push(conv);
				this.conversationMap.set(key, conv);
				await getDB().saveConversation(conv);
			}
		},
		async insertMessage(convKey, m) {
			const conv = this.conversationMap.get(convKey);
			conv.lastContent = messageUtil.previewContent(m);
			conv.lastSendTime = m.sendTime;
			conv.optTime = m.sendTime;
			// 其他成员发的消息显示发送昵称
			conv.sendNickName = m.selfSend ? '' : m.sendNickName;
			// 记录会话最大消息id
			if (m.id && m.seqNo) {
				conv.maxSeqNo = Math.max(conv.maxSeqNo, m.seqNo);
				conv.maxMessageId = Math.max(conv.maxMessageId, m.id);
			}
			// 会话未读加1
			if (!m.selfSend && m.status != MESSAGE_STATUS.READED &&
				m.status != MESSAGE_STATUS.RECALL && m.type != MESSAGE_TYPE.TIP_TEXT) {
				conv.unreadCount++;
			}
			// 是否有人@我
			if (!m.selfSend && m.atUserIds && m.status != MESSAGE_STATUS.READED) {
				const userId = useUserStore().userInfo.id;
				if (m.atUserIds.indexOf(userId) >= 0) {
					conv.atMe = true;
					conv.lastAtMessageId = m.id;
				}
				if (m.atUserIds.indexOf(-1) >= 0) {
					conv.atAll = true;
					conv.lastAtMessageId = m.id;
				}
			}
			// 间隔大于10分钟插入时间显示
			if (!conv.lastTimeTip || (conv.lastTimeTip < m.sendTime - 600 * 1000)) {
				conv.lastTimeTip = m.sendTime;
				if (this.isActive(convKey)) {
					const timeTipMessage = {
						convKey: conv.key,
						localId: nextSnowflakeId(),
						sendTime: m.sendTime,
						type: MESSAGE_TYPE.TIP_TIME,
					};
					this.messages.push(timeTipMessage);
				}
			}
			// 标记所属会话
			m.convKey = conv.key;
			// 如果是当前打开的会话窗口
			if (this.isActive(convKey)) {
				if (!this.hasMoreNextMessage) {
					// 消息插入底部
					this.messages.push(m);
					this.maxSeqNo = conv.maxSeqNo;
					// 积压数量过大时，主动释放掉一部分消息，避免消息堆积占影响渲染效率
					if (this.isInBottom && this.messages.length > 100) {
						await this.resetMessages(convKey)
					}
				}
				if (!this.isInBottom) {
					// 滚动条不在底部，需要展示新消息数量
					this.newMessageSize++;
				}
			}
			await getDB().saveConversationAndMessage([conv], [m])
		},
		async updateMessage(convKey, m) {
			const conv = this.conversationMap.get(convKey);
			// 查询原消息
			let message = this.messages.find(m1 => m.localId == m1.localId);
			if (!message) {
				message = await getDB().findMessageByLocalId(m.localId);
				if (!message) {
					return;
				}
			}
			// 通过属性拷贝的方式对字段更新
			Object.assign(message, m);
			await getDB().saveMessage(message);
			// 记录会话最大消息id
			if (m.id && m.seqNo) {
				conv.maxMessageId = Math.max(conv.maxMessageId, m.id)
				conv.maxSeqNo = Math.max(conv.maxSeqNo, m.seqNo)
				await getDB().saveConversation(conv);
			}
		},
		async deleteMessage(convKey, m) {
			// 删除旧消息
			await getDB().deleteMessageByLocalId(m.localId);
			if (this.isActive(convKey)) {
				for (let idx in this.messages) {
					// 已经发送成功的，根据id删除
					if (this.messages[idx].localId == m.localId) {
						this.messages.splice(idx, 1);
						break;
					}
				}
			}
			// 清空lastContent
			const conv = this.conversationMap.get(convKey)
			if (m.id && m.id == conv.maxMessageId) {
				conv.lastContent = '';
				conv.sendNickName = '';
				await getDB().saveConversation(conv)
			}
		},
		// 重置消息到底部30条
		async resetMessages(convKey) {
			if (!this.isActive(convKey)) {
				return;
			}
			this.setIsInBottom(true);
			const conv = this.conversationMap.get(convKey);
			this.minSeqNo = 0;
			this.maxSeqNo = conv.maxSeqNo;
			this.hasMoreLastMessage = true;
			const size = 30;
			if (this.isActive(convKey) && !this.hasMoreNextMessage && this.messages.length) {
				// 消息已在内存里，就不去db查询了，降低并发压力
				const minSeqNo = Math.max(1, conv.minSeqNo, conv.maxSeqNo - size + 1);
				const idx = this.messages.findIndex(m => m.seqNo == minSeqNo);
				if (idx >= 0) {
					this.messages.splice(0, idx);
					this.minSeqNo = minSeqNo;
					return;
				}
			}
			// 多加1是为了查询时修正边界值
			this.minSeqNo = conv.maxSeqNo + 1;
			this.hasMoreNextMessage = false;
			this.messages = [];
			await this.loadLastPageMessage(convKey, size)
		},
		// 拉取上一页消息
		async loadLastPageMessage(convKey, size) {
			if (!this.isActive(convKey)) {
				return;
			}
			// 防止滚动事件重复触发导致重复拉取
			if (this.loadingMessage || !this.hasMoreLastMessage) {
				return;
			}
			const conv = this.conversationMap.get(convKey);
			const minSeqNo = Math.max(1, conv.minSeqNo, this.minSeqNo - size);
			const maxSeqNo = this.minSeqNo - 1;
			if (maxSeqNo < minSeqNo) {
				this.hasMoreLastMessage = false;
				return;
			}
			this.loadingMessage = true;
			let messages = await getDB().findPageMessage(convKey, minSeqNo, maxSeqNo);
			messages = await this.reloadSendingMessage(convKey, messages);
			messages = await this.reloadMissMessage(convKey, minSeqNo, maxSeqNo, messages);
			this.refreshAtMessage(convKey, messages);
			this.hasMoreLastMessage = minSeqNo > 1 && messages.length > 0;
			messages = this.filterInvalidMessage(convKey, messages);
			messages = this.appendTimeTipMessage(convKey, messages);
			this.messages.unshift(...messages);
			this.minSeqNo = minSeqNo;
			this.loadingMessage = false;
			// 如果上方已无可拉取的消息，标记最小消息序号，避免下次重复拉取
			if (!this.hasMoreLastMessage) {
				await this.refreshMinSeqNo(convKey);
			}
			// 防止用户删除了过多消息导致滚动条不出来
			if (this.messages.length < 20) {
				await this.loadLastPageMessage(convKey, 30);
			}
		},
		// 拉取下一页消息
		async loadNextPageMessage(convKey, size) {
			if (!this.isActive(convKey)) {
				return;
			}
			// 防止滚动事件重复触发导致重复拉取
			if (this.loadingMessage || !this.hasMoreNextMessage) {
				return;
			}
			const conv = this.conversationMap.get(convKey);
			const minSeqNo = Math.min(conv.maxSeqNo, this.maxSeqNo + 1);
			const maxSeqNo = Math.min(conv.maxSeqNo, this.maxSeqNo + size);
			this.loadingMessage = true;
			let messages = await getDB().findPageMessage(convKey, minSeqNo, maxSeqNo);
			messages = await this.reloadSendingMessage(convKey, messages);
			messages = await this.reloadMissMessage(convKey, minSeqNo, maxSeqNo, messages);
			this.refreshAtMessage(convKey, messages);
			this.hasMoreNextMessage = maxSeqNo < conv.maxSeqNo && messages.length > 0;
			messages = this.filterInvalidMessage(convKey, messages);
			messages = this.appendTimeTipMessage(convKey, messages);
			this.messages.push(...messages);
			this.maxSeqNo = maxSeqNo;
			this.loadingMessage = false;
			// 防止用户删除了过多消息导致滚动条不出来
			if (this.messages.length < 20) {
				await this.loadNextPageMessage(convKey, 30);
			}
		},
		// 定位消息
		async locateToMessage(convKey, message) {
			if (!this.isActive(convKey)) {
				return;
			}
			const conv = this.conversationMap.get(convKey);
			// 向下取20条,向上取5条
			const maxSeqNo = Math.min(conv.maxSeqNo, message.seqNo + 20);
			const minSeqNo = Math.max(1, conv.minSeqNo, maxSeqNo - 25);
			this.loadingMessage = true;
			let messages = await getDB().findPageMessage(convKey, minSeqNo, maxSeqNo);
			messages = await this.reloadSendingMessage(convKey, messages);
			this.refreshAtMessage(convKey, this.messages);
			this.hasMoreLastMessage = minSeqNo > 1 && messages.length > 0;
			this.hasMoreNextMessage = maxSeqNo < conv.maxSeqNo && messages.length > 0;			
			messages = await this.reloadMissMessage(convKey, minSeqNo, maxSeqNo, messages);
			messages = this.filterInvalidMessage(convKey, messages);
			messages = this.appendTimeTipMessage(convKey, messages);
			this.messages = messages;
			this.maxSeqNo = maxSeqNo;
			this.minSeqNo = minSeqNo;
			this.loadingMessage = false;
			// 防止用户删除了过多消息导致滚动条不出来
			if (this.messages.length < 20) {
				await this.loadLastPageMessage(convKey, 20);
				await this.loadNextPageMessage(convKey, 20);
			}
		},
		// 拉取正在发送中的消息
		async reloadSendingMessage(convKey, localMessages) {
			const conv = this.conversationMap.get(convKey);
			// 用户如果在消息发送过程中退出，消息会永远处于发送中状态，这里从服务器重新拉取这部分消息
			const sendingMessages = localMessages.filter(m => m.status == MESSAGE_STATUS.SENDING);
			if (!sendingMessages.length) {
				return localMessages;
			}
			let messages = [];
			const ids = sendingMessages.map(m => m.localId);
			if (conv.type == CONVERSATION_TYPE.PRIVATE) {
				messages = await http({
					url: '/message/private/history',
					method: 'post',
					data: {
						friendId: conv.targetId,
						localIds: ids
					}
				});
			} else {
				messages = await http({
					url: '/message/group/history',
					method: 'post',
					data: {
						groupId: conv.targetId,
						localIds: ids
					}
				});
			}
			const userId = useUserStore().userInfo.id;
			messages.forEach(m => {
				m.convKey = convKey;
				m.selfSend = m.sendId == userId;
				conv.maxMessageId = Math.max(conv.maxMessageId, m.id);
				conv.maxSeqNo = Math.max(conv.maxSeqNo, m.seqNo);
			});
			// 逐条更新
			const saveMessages = []
			for (let idx in sendingMessages) {
				const localMessage = sendingMessages[idx];
				const message = messages.find(m => m.localId == localMessage.localId);
				if (message) {
					Object.assign(localMessage, message);
				} else {
					// 没拉取到这条消息，说明没发成功
					localMessage.status = MESSAGE_STATUS.FAILED;
				}
				saveMessages.push(localMessage);
			}
			await getDB().saveConversationAndMessage([conv], saveMessages);
			return localMessages;
		},
		// 从服务器拉取丢失的消息
		async reloadMissMessage(convKey, minSeqNo, maxSeqNo, localMessages) {
			const conv = this.conversationMap.get(convKey);
			// 如果没有缺失的数据直接返回	
			if (conv.maxSeqNo <= 0 || !this.existMissMessage(localMessages, minSeqNo, maxSeqNo)) {
				return localMessages;
			}
			const userId = useUserStore().userInfo.id;
			// 从服务器重新拉取这一页数据
			let messages = [];
			if (conv.type == CONVERSATION_TYPE.PRIVATE) {
				messages = await http({
					url: '/message/private/history',
					method: 'post',
					data: {
						friendId: conv.targetId,
						minSeqNo: minSeqNo,
						maxSeqNo: maxSeqNo
					}
				});
			} else {
				messages = await http({
					url: '/message/group/history',
					method: 'post',
					data: {
						groupId: conv.targetId,
						minSeqNo: minSeqNo,
						maxSeqNo: maxSeqNo
					}
				});
			}
			const saveMessages = [];
			messages.forEach(m => {
				m.convKey = convKey;
				m.selfSend = m.sendId == userId;
				conv.maxMessageId = Math.max(conv.maxMessageId, m.id);
				conv.maxSeqNo = Math.max(conv.maxSeqNo, m.seqNo);
				// 只保存新拉到的消息,不能直接覆盖旧消息,否则被撤回的消息提示语会被覆盖
				if (!localMessages.some(localMessage => localMessage.id == m.id)) {
					saveMessages.push(m);
				}
			});
			if (!saveMessages.length) {
				return localMessages;
			}
			await getDB().saveConversationAndMessage([conv], saveMessages);
			// 合并消息
			const messageMap = new Map();
			localMessages.forEach(m => messageMap.set(m.localId, m));
			saveMessages.forEach(m => messageMap.set(m.localId, m));
			return Array.from(messageMap.values()).sort((m1, m2) => {
				if (m1.seqNo != m2.seqNo) {
					return m1.seqNo - m2.seqNo;
				}
				return m1.sendTime - m2.sendTime;
			});
		},
		filterInvalidMessage(convKey, localMessages) {
			// 排除已经删除或撤回的消息
			return localMessages.filter(m => !m.deleted && m.status != MESSAGE_STATUS.RECALL
				&& m.type != MESSAGE_TYPE.RECALL);
		},
		appendTimeTipMessage(convKey, localMessages) {
			const messages = [];
			let lastTimeTip = 0;
			localMessages.forEach(m => {
				if (!m.deleted && m.sendTime - lastTimeTip > 600 * 1000) {
					const timeTipMessage = {
						localId: nextSnowflakeId(),
						convKey: convKey,
						sendTime: m.sendTime,
						type: MESSAGE_TYPE.TIP_TIME,
					};
					messages.push(timeTipMessage);
					lastTimeTip = m.sendTime;
				}
				messages.push(m);
			})
			return messages;
		},
		async recallMessage(convKey, message) {
			const batchMessages = []
			const conv = this.conversationMap.get(convKey);
			// 要撤回的消息id
			const recallMessageId = JSON.parse(message.content).id;
			const recallMessageTip = JSON.parse(message.content).tip;
			const recallMessage = await getDB().findMessageById(convKey, recallMessageId);
			if (!recallMessage) {
				return;
			}
			// 把原消息改造成一条提示消息
			recallMessage.status = MESSAGE_STATUS.PENDING;
			recallMessage.content = recallMessageTip;
			recallMessage.type = MESSAGE_TYPE.TIP_TEXT
			batchMessages.push(recallMessage);
			// 会话列表
			conv.lastContent = messageUtil.previewContent(recallMessage);
			conv.lastSendTime = message.sendTime;
			conv.sendNickName = '';
			conv.maxMessageId = Math.max(conv.maxMessageId, message.id);
			conv.maxSeqNo = Math.max(conv.maxSeqNo, message.seqNo);
			if (!message.selfSend && message.status != MESSAGE_STATUS.READED) {
				conv.unreadCount++;
			}
			// 同步内存中的引用消息
			if (this.isActive(convKey)) {
				this.messages.forEach(m1 => {
					if (m1.id == recallMessageId) {
						m1.status = MESSAGE_STATUS.RECALL;
						m1.content = recallMessageTip;
						m1.type = MESSAGE_TYPE.TIP_TEXT;
					}
				})
			}
			// 撤回指令也要入库，保证seqNo连续
			message.convKey = conv.key;
			batchMessages.push(message);
			await getDB().saveConversationAndMessage([conv], batchMessages);
		},
		async remove(convKey) {
			const idx = this.findIdx(convKey);
			this.conversations.splice(idx, 1);
			this.conversationMap.delete(convKey);
			if (this.isActive(convKey)) {
				this.activeConversation = null;
				this.messages = []
			}
			await getDB().deleteMessageByConvKey(convKey);
			await getDB().deleteConversationByKey(convKey);
		},
		async cleanMessage(convKey) {
			const conv = this.conversationMap.get(convKey);
			conv.lastContent = '';
			conv.unreadCount = 0;
			conv.atMe = false;
			conv.atAll = false;
			conv.lastAtMessageId = -1;
			conv.sendNickName = '';
			if (this.isActive(convKey)) {
				this.messages = []
			}
			await getDB().deleteMessageByConvKey(convKey);
			await getDB().saveConversation(conv);
		},
		async setDnd(convKey, isDnd) {
			const conv = this.conversationMap.get(convKey);
			if (!conv) return;
			conv.isDnd = isDnd;
			await getDB().saveConversation(conv);

		},
		async setTop(convKey, isTop) {
			const conv = this.conversationMap.get(convKey);
			if (!conv) return;
			conv.isTop = isTop;
			await this.moveTop(convKey);
			await getDB().saveConversation(conv);
		},
		async moveTop(convKey) {
			const idx = this.findIdx(convKey);
			const conv = this.conversationMap.get(convKey);
			const insertIdx = conv.isTop ? 0 : this.findTopSize();
			if (idx != insertIdx) {
				this.conversations.splice(idx, 1);
				this.conversations.splice(insertIdx, 0, conv);
				conv.optTime = new Date().getTime();
				await getDB().saveConversation(conv);
			}
		},
		async readedMessage(convKey, messageId) {
			const conv = this.conversationMap.get(convKey);
			// 没传messageId就是整个会话已读
			messageId = messageId || conv.maxMessageId;
			if (conv && conv.maxReadedId < messageId) {
				conv.maxReadedId = messageId;
				await getDB().saveConversation(conv);
			}
		},
		async resetUnreadCount(convKey) {
			const conv = this.conversationMap.get(convKey);
			if (conv) {
				conv.unreadCount = 0;
				await getDB().saveConversation(conv);
			}
		},
		async resetAtMessage(convKey) {
			const conv = this.conversationMap.get(convKey);
			if (conv) {
				conv.atAll = false;
				conv.atMe = false;
				conv.lastAtMessageId = -1;
				await getDB().saveConversation(conv);
			}
		},
		async refreshAtMessage(convKey, readedMessages) {
			const conv = this.conversationMap.get(convKey);
			if (!conv.atAll && !conv.atMe) {
				return;
			}
			if (readedMessages.some(m => m.id == conv.lastAtMessageId)) {
				await this.resetAtMessage(convKey);
			}
		},
		async updateFromFriend(friend) {
			const conv = this.findByFriend(friend.id);
			// 更新会话中的昵称和头像
			if (conv && (conv.headImage != friend.headImage ||
				conv.showName != friend.nickName)) {
				conv.headImage = friend.headImage;
				conv.showName = friend.nickName;
				await getDB().saveConversation(conv);
			}
		},
		async updateFromUser(user) {
			const conv = this.findByFriend(user.id);
			// 更新会话中的昵称和头像
			if (conv && (conv.headImage != user.headImageThumb ||
				conv.showName != user.nickName)) {
				conv.headImage = user.headImageThumb;
				conv.showName = user.nickName;
				await getDB().saveConversation(conv);
			}
		},
		async updateFromGroup(group) {
			const conv = this.findByGroup(group.id);
			if (conv && (conv.headImage != group.headImageThumb ||
				conv.showName != group.showGroupName)) {
				// 更新会话中的群名称和头像
				conv.headImage = group.headImageThumb;
				conv.showName = group.showGroupName;
				await getDB().saveConversation(conv);
			}
		},
		async refreshMinSeqNo(convKey) {
			const conv = this.conversationMap.get(convKey);
			for (const m of this.messages) {
				if (m.seqNo) {
					conv.minSeqNo = m.seqNo;
					await getDB().saveConversation(conv);
					break;
				}
			}
		},
		setDraftText(convKey, draftText) {
			const conv = this.conversationMap.get(convKey);
			conv.draftText = draftText;
		},
		setLoading(loading) {
			this.loading = loading;
		},
		refreshDnd() {
			// 更新会话免打扰状态
			const friendStore = useFriendStore();
			const groupStore = useGroupStore();
			this.conversations.forEach(conv => {
				if (conv.type == CONVERSATION_TYPE.PRIVATE) {
					const friend = friendStore.findFriend(conv.targetId);
					if (friend) {
						conv.isDnd = friend.isDnd;
					}
				} else if (conv.type == CONVERSATION_TYPE.GROUP) {
					const group = groupStore.findGroup(conv.targetId);
					if (group) {
						conv.isDnd = group.isDnd;
					}
				}
			})
		},
		startSortTimer() {
			this.sort();
			this.sortTimer && clearInterval(this.sortTimer);
			this.sortTimer = setInterval(() => this.sort(), 1000)
		},
		sort() {
			this.conversations.sort((conv1, conv2) => {
				if (conv1.isTop && !conv2.isTop) {
					return -1;
				} else if (conv2.isTop && !conv1.isTop) {
					return 1;
				} else {
					return conv2.optTime - conv1.optTime
				}
			});
		},
		existMissMessage(messages, minSeqNo, maxSeqNo) {
			for (let i = minSeqNo; i <= maxSeqNo; i++) {
				if (!messages.some(m => m.id && m.seqNo == i)) {
					return true;
				}
			}
			return false;
		},
		clear() {
			this.activeConversation = null;
			this.conversations = [];
			this.conversationMap.clear();
			this.messages = [];
			this.loading = true;
		},
		async loadConversations() {
			const conversations = await getDB().loadAllConversations();
			this.init(conversations || []);
		}
	},
	getters: {
		findIdx: (state) => (convKey) => {
			for (let idx in state.conversations) {
				if (state.conversations[idx].key == convKey) {
					return idx
				}
			}
			return -1;
		},
		findTopSize: (state) => () => {
			return state.conversations.filter(conv => conv.isTop).length;
		},
		findMaxMessageId: (state) => (type) => {
			let maxId = 0;
			state.conversations.forEach(conv => {
				if (conv.maxMessageId && conv.type == type) {
					maxId = Math.max(maxId, conv.maxMessageId)
				}
			});
			return maxId;
		},
		findMaxSeqNo: (state) => (type) => {
			let maxSeqNo = 0;
			state.conversations.forEach(conv => {
				if (conv.maxSeqNo && conv.type == type) {
					maxSeqNo = Math.max(maxSeqNo, conv.maxSeqNo)
				}
			});
			return maxSeqNo;
		},
		findByFriend: (state) => (friendId) => {
			const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, friendId);
			return state.conversationMap.get(convKey);
		},
		findByGroup: (state) => (groupId) => {
			const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, groupId);
			return state.conversationMap.get(convKey);
		},
		isActive: (state) => (convKey) => {
			return state.activeConversation && state.activeConversation.key == convKey;
		}
	}
});
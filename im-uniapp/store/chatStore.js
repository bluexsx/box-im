import { defineStore } from 'pinia';
import { MESSAGE_TYPE, MESSAGE_STATUS } from '@/common/enums.js';
import useFriendStore from './friendStore.js';
import useGroupStore from './groupStore.js';
import useUserStore from './userStore';

let cacheChats = [];
export default defineStore('chatStore', {
	state: () => {
		return {
			chats: [],
			privateMsgMaxId: 0,
			groupMsgMaxId: 0,
			loading: false
		}
	},
	actions: {
		initChats(chatsData) {
			cacheChats = [];
			this.chats = [];
			for (let chat of chatsData.chats) {
				chat.stored = false;
				// 暂存至缓冲区
				cacheChats.push(JSON.parse(JSON.stringify(chat)));
				// 加载期间显示只前15个会话做做样子,一切都为了加快初始化时间
				if (this.chats.length < 15) {
					this.chats.push(chat);
				}
			}
			this.privateMsgMaxId = chatsData.privateMsgMaxId || 0;
			this.groupMsgMaxId = chatsData.groupMsgMaxId || 0;
		},
		openChat(chatInfo) {
			let chats = this.curChats;
			let chat = null;
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId === chatInfo.targetId) {
					chat = chats[idx];
					// 放置头部
					this.moveTop(idx)
					break;
				}
			}
			// 创建会话
			if (chat == null) {
				chat = {
					targetId: chatInfo.targetId,
					type: chatInfo.type,
					showName: chatInfo.showName,
					headImage: chatInfo.headImage,
					isDnd: chatInfo.isDnd,
					lastContent: "",
					lastSendTime: new Date().getTime(),
					unreadCount: 0,
					hotMinIdx: 0,
					readedMessageIdx: 0,
					messages: [],
					atMe: false,
					atAll: false,
					stored: false
				};
				chats.unshift(chat);
				this.saveToStorage();
			}
		},
		activeChat(idx) {
			let chats = this.curChats;
			if (idx >= 0) {
				chats[idx].unreadCount = 0;
			}
		},
		resetUnreadCount(chatInfo) {
			let chats = this.curChats;
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId == chatInfo.targetId) {
					chats[idx].unreadCount = 0;
					chats[idx].atMe = false;
					chats[idx].atAll = false;
					chats[idx].stored = false;
					this.saveToStorage();
				}
			}

		},
		readedMessage(pos) {
			let chat = this.findChatByFriend(pos.friendId);
			if (!chat) return;
			for (let idx = chat.readedMessageIdx; idx < chat.messages.length; idx++) {
				let m = chat.messages[idx];
				if (m.id && m.selfSend && m.status < MESSAGE_STATUS.RECALL) {
					// pos.maxId为空表示整个会话已读
					if (!pos.maxId || m.id <= pos.maxId) {
						m.status = MESSAGE_STATUS.READED
						chat.readedMessageIdx = idx;
						chat.stored = false;
					}
				}
			}
			if (!chat.stored) {
				this.saveToStorage();
			}
		},
		cleanMessage(idx) {
			let chat = this.curChats[idx];
			chat.lastContent = '';
			chat.hotMinIdx = 0;
			chat.unreadCount = 0;
			chat.atMe = false;
			chat.atAll = false;
			chat.stored = false
			chat.messages = [];
			this.saveToStorage(true);
		},
		removeChat(idx) {
			let chats = this.curChats;
			chats[idx].delete = true;
			chats[idx].stored = false;
			this.saveToStorage();
		},
		removePrivateChat(userId) {
			let chats = this.curChats;
			for (let idx in chats) {
				if (chats[idx].type == 'PRIVATE' &&
					chats[idx].targetId == userId) {
					this.removeChat(idx);
				}
			}
		},
		removeGroupChat(groupId) {
			let chats = this.curChats;
			for (let idx in chats) {
				if (chats[idx].type == 'GROUP' &&
					chats[idx].targetId == groupId) {
					this.removeChat(idx);
				}
			}
		},
		moveTop(idx) {
			if (this.loading) {
				return;
			}
			let chats = this.curChats;
			if (idx > 0) {
				let chat = chats[idx];
				chats.splice(idx, 1);
				chats.unshift(chat);
				chat.lastSendTime = new Date().getTime();
				chat.stored = false;
				this.saveToStorage();
			}
		},
		insertMessage(msgInfo, chatInfo) {
			// 获取对方id或群id
			let type = chatInfo.type;
			// 记录消息的最大id
			if (msgInfo.id && type == "PRIVATE" && msgInfo.id > this.privateMsgMaxId) {
				this.privateMsgMaxId = msgInfo.id;
			}
			if (msgInfo.id && type == "GROUP" && msgInfo.id > this.groupMsgMaxId) {
				this.groupMsgMaxId = msgInfo.id;
			}
			// 如果是已存在消息，则覆盖旧的消息数据
			let chat = this.findChat(chatInfo);
			let message = this.findMessage(chat, msgInfo);
			if (message) {
				console.log("message:", message)
				Object.assign(message, msgInfo);
				chat.stored = false;
				this.saveToStorage();
				return;
			}
			// 会话列表内容
			if (msgInfo.type == MESSAGE_TYPE.IMAGE) {
				chat.lastContent = "[图片]";
			} else if (msgInfo.type == MESSAGE_TYPE.FILE) {
				chat.lastContent = "[文件]";
			} else if (msgInfo.type == MESSAGE_TYPE.AUDIO) {
				chat.lastContent = "[语音]";
			} else if (msgInfo.type == MESSAGE_TYPE.ACT_RT_VOICE) {
				chat.lastContent = "[语音通话]";
			} else if (msgInfo.type == MESSAGE_TYPE.ACT_RT_VIDEO) {
				chat.lastContent = "[视频通话]";
			} else if (msgInfo.type == MESSAGE_TYPE.TEXT ||
				msgInfo.type == MESSAGE_TYPE.RECALL ||
				msgInfo.type == MESSAGE_TYPE.TIP_TEXT) {
				chat.lastContent = msgInfo.content;
			}
			chat.lastSendTime = msgInfo.sendTime;
			chat.sendNickName = msgInfo.sendNickName;
			// 未读加1
			if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED &&
				msgInfo.status != MESSAGE_STATUS.RECALL && msgInfo.type != MESSAGE_TYPE.TIP_TEXT) {
				chat.unreadCount++;
			}
			// 是否有人@我
			if (!msgInfo.selfSend && chat.type == "GROUP" && msgInfo.atUserIds &&
				msgInfo.status != MESSAGE_STATUS.READED) {
				const userStore = useUserStore();
				let userId = userStore.userInfo.id;
				if (msgInfo.atUserIds.indexOf(userId) >= 0) {
					chat.atMe = true;
				}
				if (msgInfo.atUserIds.indexOf(-1) >= 0) {
					chat.atAll = true;
				}
			}
			// 间隔大于10分钟插入时间显示
			if (!chat.lastTimeTip || (chat.lastTimeTip < msgInfo.sendTime - 600 * 1000)) {
				chat.messages.push({
					sendTime: msgInfo.sendTime,
					type: MESSAGE_TYPE.TIP_TIME,
				});
				chat.lastTimeTip = msgInfo.sendTime;
			}
			// 插入消息
			chat.messages.push(msgInfo);
			chat.stored = false;
			this.saveToStorage();
		},
		updateMessage(msgInfo, chatInfo) {
			// 获取对方id或群id
			let chat = this.findChat(chatInfo);
			let message = this.findMessage(chat, msgInfo);
			if (message) {
				// 属性拷贝
				Object.assign(message, msgInfo);
				chat.stored = false;
				this.saveToStorage();
			}
		},
		deleteMessage(msgInfo, chatInfo) {
			let isColdMessage = false;
			let chat = this.findChat(chatInfo);
			let delIdx = -1;
			for (let idx in chat.messages) {
				// 已经发送成功的，根据id删除
				if (chat.messages[idx].id && chat.messages[idx].id == msgInfo.id) {
					delIdx = idx;
					break;
				}
				// 正在发送中的消息可能没有id，只有临时id
				if (chat.messages[idx].tmpId && chat.messages[idx].tmpId == msgInfo.tmpId) {
					delIdx = idx;
					break;
				}
			}
			if (delIdx >= 0) {
				chat.messages.splice(delIdx, 1);
				if (delIdx < chat.hotMinIdx) {
					isColdMessage = true;
					chat.hotMinIdx--;
				}
				if (delIdx < chat.readedMessageIdx) {
					chat.readedMessageIdx--;
				}
				chat.stored = false;
				this.saveToStorage(isColdMessage);
			}
		},
		recallMessage(msgInfo, chatInfo) {
			let chat = this.findChat(chatInfo);
			if (!chat) return;
			let isColdMessage = false;
			// 要撤回的消息id
			let id = msgInfo.content;
			let name = msgInfo.selfSend ? '你' : chat.type == 'PRIVATE' ? '对方' : msgInfo.sendNickName;
			for (let idx in chat.messages) {
				let m = chat.messages[idx];
				if (m.id && m.id == id) {
					// 改造成一条提示消息
					m.status = MESSAGE_STATUS.RECALL;
					m.content = name + "撤回了一条消息";
					m.type = MESSAGE_TYPE.TIP_TEXT
					// 会话列表
					chat.lastContent = m.content;
					chat.lastSendTime = msgInfo.sendTime;
					chat.sendNickName = '';
					if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED) {
						chat.unreadCount++;
					}
					isColdMessage = idx < chat.hotMinIdx;
				}
				// 被引用的消息也要撤回
				if (m.quoteMessage && m.quoteMessage.id == msgInfo.id) {
					m.quoteMessage.content = "引用内容已撤回";
					m.quoteMessage.status = MESSAGE_STATUS.RECALL;
					m.quoteMessage.type = MESSAGE_TYPE.TIP_TEXT
				}
			}
			chat.stored = false;
			this.saveToStorage(isColdMessage);
		},
		updateChatFromFriend(friend) {
			let chat = this.findChatByFriend(friend.id)
			if (chat && (chat.headImage != friend.headImage ||
					chat.showName != friend.nickName)) {
				// 更新会话中的群名和头像
				chat.headImage = friend.headImage;
				chat.showName = friend.nickName;
				chat.stored = false;
				this.saveToStorage();
			}
		},
		updateChatFromUser(user) {
			let chat = this.findChatByFriend(user.id);
			// 更新会话中的昵称和头像
			if (chat && (chat.headImage != user.headImageThumb ||
					chat.showName != user.nickName)) {
				chat.headImage = user.headImageThumb;
				chat.showName = user.nickName;
				chat.stored = false;
				this.saveToStorage();
			}
		},
		updateChatFromGroup(group) {
			let chat = this.findChatByGroup(group.id);
			if (chat && (chat.headImage != group.headImageThumb ||
					chat.showName != group.showGroupName)) {
				// 更新会话中的群名称和头像
				chat.headImage = group.headImageThumb;
				chat.showName = group.showGroupName;
				chat.stored = false;
				this.saveToStorage();
			}
		},
		setLoading(loading) {
			this.loading = loading;
		},
		setDnd(chatInfo, isDnd) {
			let chat = this.findChat(chatInfo);
			if (chat) {
				chat.isDnd = isDnd;
			}
		},
		refreshChats() {
			let chats = cacheChats || this.chats;
			// 更新会话免打扰状态
			const friendStore = useFriendStore();
			const groupStore = useGroupStore();
			chats.forEach(chat => {
				if (chat.type == 'PRIVATE') {
					let friend = friendStore.findFriend(chat.targetId);
					if (friend) {
						chat.isDnd = friend.isDnd
					}
				} else if (chat.type == 'GROUP') {
					let group = groupStore.findGroup(chat.targetId);
					if (group) {
						chat.isDnd = group.isDnd
					}
				}
			})
			// 排序
			chats.sort((chat1, chat2) => chat2.lastSendTime - chat1.lastSendTime);
			// #ifndef APP-PLUS
			// h5和小程序的stroge一般只有5m,大约只能存储1w条消息，所以可能需要清理部分历史消息
			const storageInfo = uni.getStorageInfoSync();
			console.log(`storage缓存: ${storageInfo.currentSize} KB`)
			// 空间不足(大于3mb)时，清理这个设备登录过其他账户的消息
			if (storageInfo && storageInfo.currentSize > 3000) {
				console.log("storage空间不足,清理其他用户缓存..")
				this.cleanOtherUserCache();
			}
			// 保证消息总数量不超过3000条，每个会话不超过500条
			this.fliterMessage(chats, 3000, 500);
			// #endif
			// 记录热数据索引位置
			chats.forEach(chat => {
				if (!chat.hotMinIdx || chat.hotMinIdx != chat.messages.length) {
					chat.hotMinIdx = chat.messages.length;
					chat.stored = false;
				}
			});
			// 将消息一次性装载回来
			this.chats = chats;
			// 清空缓存,不再使用
			cacheChats = null;
			// 消息持久化
			this.saveToStorage(true);
		},
		fliterMessage(chats, maxTotalSize, maxPerChatSize) {
			// 每个会话只保留maxPerChatSize条消息
			let remainTotalSize = 0;
			chats.forEach(chat => {
				if (chat.messages.length > maxPerChatSize) {
					let idx = chat.messages.length - maxPerChatSize;
					chat.messages = chat.messages.slice(idx);
				}
				remainTotalSize += chat.messages.length;
			})
			// 保证消息总数不超过maxTotalSize条，否则继续清理
			if (remainTotalSize > maxTotalSize) {
				this.fliterMessage(chats, maxTotalSize, maxPerChatSize / 2);
			}
			console.log("消息留存总数量:", remainTotalSize)
			console.log("单会话消息数量:", maxPerChatSize)
		},
		cleanOtherUserCache() {
			const userStore = useUserStore();
			const userId = userStore.userInfo.id;
			const prefix = "chats-app-" + userId;
			const res = uni.getStorageInfoSync();
			res.keys.forEach(key => {
				// 清理其他用户的消息
				if (key.startsWith("chats-app") && !key.startsWith(prefix)) {
					uni.removeStorageSync(key);
					console.log("清理key:", key)
				}
			})
		},
		saveToStorage(withColdMessage) {
			// 加载中不保存，防止卡顿
			if (this.loading) {
				return;
			}
			const userStore = useUserStore();
			let userId = userStore.userInfo.id;
			let key = "chats-app-" + userId;
			let chatKeys = [];
			// 按会话为单位存储，只存储有改动的会话
			this.chats.forEach((chat) => {
				let chatKey = `${key}-${chat.type}-${chat.targetId}`
				if (!chat.stored) {
					if (chat.delete) {
						uni.removeStorageSync(chatKey);
					} else {
						// 存储冷数据
						if (withColdMessage) {
							let coldChat = Object.assign({}, chat);
							coldChat.messages = chat.messages.slice(0, chat.hotMinIdx);
							uni.setStorageSync(chatKey, coldChat)
						}
						// 存储热消息
						let hotKey = chatKey + '-hot';
						let hotChat = Object.assign({}, chat);
						hotChat.messages = chat.messages.slice(chat.hotMinIdx)
						uni.setStorageSync(hotKey, hotChat);
					}
					chat.stored = true;
				}
				if (!chat.delete) {
					chatKeys.push(chatKey);
				}
			})
			// 会话核心信息
			let chatsData = {
				privateMsgMaxId: this.privateMsgMaxId,
				groupMsgMaxId: this.groupMsgMaxId,
				chatKeys: chatKeys
			}
			uni.setStorageSync(key, chatsData)
			// 清理已删除的会话
			this.chats = this.chats.filter(chat => !chat.delete)
		},
		clear(state) {
			cacheChats = [];
			this.chats = [];
			this.privateMsgMaxId = 0;
			this.groupMsgMaxId = 0;
			this.loadingPrivateMsg = false;
			this.loadingGroupMsg = false;
		},
		loadChat() {
			return new Promise((resolve, reject) => {
				let userStore = useUserStore();
				let userId = userStore.userInfo.id;
				let chatsData = uni.getStorageSync("chats-app-" + userId)
				if (chatsData) {
					if (chatsData.chatKeys) {
						chatsData.chats = [];
						chatsData.chatKeys.forEach(key => {
							let coldChat = uni.getStorageSync(key);
							let hotChat = uni.getStorageSync(key + '-hot');
							if (!coldChat && !hotChat) {
								return;
							}
							// 防止消息一直处在发送中状态
							hotChat && hotChat.messages.forEach(msg => {
								if (msg.status == MESSAGE_STATUS.SENDING) {
									msg.status = MESSAGE_STATUS.FAILED
								}
							})
							// 冷热消息合并
							let chat = Object.assign({}, coldChat, hotChat);
							if (hotChat && coldChat) {
								chat.messages = coldChat.messages.concat(hotChat.messages)
							}
							// 历史版本没有readedMessageIdx字段，做兼容一下
							chat.readedMessageIdx = chat.readedMessageIdx || 0;
							chatsData.chats.push(chat);
						})
					}
					this.initChats(chatsData);
				}
				resolve()
			})
		}
	},
	getters: {
		curChats: (state) => {
			if (cacheChats && state.loading) {
				return cacheChats;
			}
			return state.chats;
		},
		findChatIdx: (state) => (chat) => {
			let chats = state.curChats;
			for (let idx in chats) {
				if (chats[idx].type == chat.type &&
					chats[idx].targetId === chat.targetId) {
					chat = state.chats[idx];
					return idx;
				}
			}
		},
		findChat: (state) => (chat) => {
			let chats = state.curChats;
			let idx = state.findChatIdx(chat);
			return chats[idx];
		},
		findChatByFriend: (state) => (fid) => {
			return state.curChats.find(chat => chat.type == 'PRIVATE' &&
				chat.targetId == fid)
		},
		findChatByGroup: (state) => (gid) => {
			return state.curChats.find(chat => chat.type == 'GROUP' &&
				chat.targetId == gid)
		},
		findMessage: (state) => (chat, msgInfo) => {
			if (!chat) {
				return null;
			}
			// 通过id判断
			if (msgInfo.id) {
				for (let idx = chat.messages.length - 1; idx >= 0; idx--) {
					let m = chat.messages[idx];
					if (m.id && msgInfo.id == m.id) {
						return m;
					}
					// 如果id比要查询的消息小，说明没有这条消息
					if (m.id && m.id < msgInfo.id) {
						break;
					}
				}
			}
			// 正在发送中的临时消息可能没有id,只有tmpId
			if (msgInfo.selfSend && msgInfo.tmpId) {
				for (let idx = chat.messages.length - 1; idx >= 0; idx--) {
					let m = chat.messages[idx];
					if (!m.selfSend || !m.tmpId) {
						continue;
					}
					if (msgInfo.tmpId == m.tmpId) {
						return m;
					}
					// 如果id比要查询的消息小，说明没有这条消息
					if (m.tmpId && m.tmpId < msgInfo.tmpId) {
						break;
					}
				}
			}
			return null;
		}
	}
});
import { defineStore } from 'pinia';
import { MESSAGE_TYPE, MESSAGE_STATUS } from '@/common/enums.js';
import useUserStore from './userStore';

let cacheChats = [];
export default defineStore('chatStore', {
	state: () => {
		return {
			chats: [],
			privateMsgMaxId: 0,
			groupMsgMaxId: 0,
			loadingPrivateMsg: false,
			loadingGroupMsg: false
		}
	},
	actions: {
		initChats(chatsData) {
			cacheChats = [];
			this.chats = [];
			for (let chat of chatsData.chats) {
				// 已删除的会话直接丢弃
				if (chat.delete) {
					continue;
				}
				// 暂存至缓冲区
				cacheChats.push(JSON.parse(JSON.stringify(chat)));
				// 加载期间显示只前15个会话做做样子,一切都为了加快初始化时间
				if (this.chats.length < 15) {
					chat.messages = [];
					this.chats.push(chat);
				}
			}
			this.privateMsgMaxId = chatsData.privateMsgMaxId || 0;
			this.groupMsgMaxId = chatsData.groupMsgMaxId || 0;
			// 防止图片一直处在加载中状态
			cacheChats.forEach((chat) => {
				chat.messages.forEach((msg) => {
					if (msg.loadStatus == "loading") {
						msg.loadStatus = "fail"
					}
				})
			})
		},
		openChat(chatInfo) {
			let chats = this.curChats;
			let chat = null;
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId === chatInfo.targetId) {
					chat = chats[idx];
					chat.delete = false;
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
					lastContent: "",
					lastSendTime: new Date().getTime(),
					unreadCount: 0,
					messages: [],
					atMe: false,
					atAll: false,
					delete: false
				};
				chats.push(chat);
				this.moveTop(chats.length - 1)
			}
		},
		activeChat(idx) {
			let chats = this.curChats;
			if (idx >= 0) {
				chats[idx].unreadCount = 0;
			}
		},
		resetUnreadCount(chatInfo) {
			console.log("resetUnreadCount")
			let chats = this.curChats;
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId == chatInfo.targetId) {
					chats[idx].unreadCount = 0;
					chats[idx].atMe = false;
					chats[idx].atAll = false;
				}
			}
			this.saveToStorage();
		},
		readedMessage(pos) {
			let chats = this.curChats;
			for (let idx in chats) {
				if (chats[idx].type == 'PRIVATE' &&
					chats[idx].targetId == pos.friendId) {
					chats[idx].messages.forEach((m) => {
						if (m.selfSend && m.status != MESSAGE_STATUS.RECALL) {
							// pos.maxId为空表示整个会话已读
							if (!pos.maxId || m.id <= pos.maxId) {
								m.status = MESSAGE_STATUS.READED
							}
						}
					})
				}
			}
			this.saveToStorage();
		},
		removeChat(idx) {
			let chats = this.curChats;
			chats[idx].delete = true;
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
			console.log("moveTop")
			let chats = this.curChats;
			let chat = chats[idx];
			// 最新的时间会显示在顶部
			chat.lastSendTime = new Date().getTime();
			this.saveToStorage();
		},
		insertMessage(msgInfo) {
			// 获取对方id或群id
			let type = msgInfo.groupId ? 'GROUP' : 'PRIVATE';
			// 记录消息的最大id
			if (msgInfo.id && type == "PRIVATE" && msgInfo.id > this.privateMsgMaxId) {
				this.privateMsgMaxId = msgInfo.id;
			}
			if (msgInfo.id && type == "GROUP" && msgInfo.id > this.groupMsgMaxId) {
				this.groupMsgMaxId = msgInfo.id;
			}
			// 如果是已存在消息，则覆盖旧的消息数据
			let chat = this.findChat(msgInfo);
			let message = this.findMessage(chat, msgInfo);
			if (message) {
				Object.assign(message, msgInfo);
				// 撤回消息需要显示
				if (msgInfo.type == MESSAGE_TYPE.RECALL) {
					chat.lastContent = msgInfo.content;
				}
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
			} else if (msgInfo.type == MESSAGE_TYPE.TEXT || msgInfo.type == MESSAGE_TYPE.RECALL) {
				chat.lastContent = msgInfo.content;
			} else if (msgInfo.type == MESSAGE_TYPE.ACT_RT_VOICE) {
				chat.lastContent = "[语音通话]";
			} else if (msgInfo.type == MESSAGE_TYPE.ACT_RT_VIDEO) {
				chat.lastContent = "[视频通话]";
			}
			chat.lastSendTime = msgInfo.sendTime;
			chat.sendNickName = msgInfo.sendNickName;
			// 未读加1
			if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED &&
				msgInfo.type != MESSAGE_TYPE.TIP_TEXT) {
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
			// 根据id顺序插入，防止消息乱序
			let insertPos = chat.messages.length;
			// 防止 图片、文件 在发送方 显示 在顶端  因为还没存库，id=0
			if (msgInfo.id && msgInfo.id > 0) {
				for (let idx in chat.messages) {
					if (chat.messages[idx].id && msgInfo.id < chat.messages[idx].id) {
						insertPos = idx;
						console.log(`消息出现乱序,位置:${chat.messages.length},修正至:${insertPos}`);
						break;
					}
				}
			}
			if (insertPos == chat.messages.length) {
				// 这种赋值效率最高
				chat.messages[insertPos] = msgInfo;
			} else {
				chat.messages.splice(insertPos, 0, msgInfo);
			}
			this.saveToStorage();
		},
		updateMessage(msgInfo) {
			// 获取对方id或群id
			let chat = this.findChat(msgInfo);
			let message = this.findMessage(chat, msgInfo);
			if (message) {
				// 属性拷贝
				Object.assign(message, msgInfo);
				this.saveToStorage();
			}
		},
		deleteMessage(msgInfo) {
			// 获取对方id或群id
			let chat = this.findChat(msgInfo);
			for (let idx in chat.messages) {
				// 已经发送成功的，根据id删除
				if (chat.messages[idx].id && chat.messages[idx].id == msgInfo.id) {
					chat.messages[idx].delete = true;
					break;
				}
				// 正在发送中的消息可能没有id，根据发送时间删除
				if (msgInfo.selfSend && chat.messages[idx].selfSend &&
					chat.messages[idx].sendTime == msgInfo.sendTime) {
					chat.messages[idx].delete = true;
					break;
				}
			}
			this.saveToStorage();
		},
		updateChatFromFriend(friend) {
			let chat = this.findChatByFriend(friend.id)
			if (chat.headImage != friend.headImageThumb ||
				chat.showName != friend.nickName) {
				// 更新会话中的群名和头像
				chat.headImage = friend.headImageThumb;
				chat.showName = friend.nickName;
				this.saveToStorage();
			}
		},
		updateChatFromGroup(group) {
			let chat = this.findChatByGroup(group.id);
			if (chat.headImage != group.headImageThumb ||
				chat.showName != group.showGroupName) {
				// 更新会话中的群名称和头像
				chat.headImage = group.headImageThumb;
				chat.showName = group.showGroupName;
				this.saveToStorage();
			}
		},
		setLoadingPrivateMsg(loading) {
			this.loadingPrivateMsg = loading;
			if (!this.isLoading()) {
				this.refreshChats()
			}
		},
		setLoadingGroupMsg(loading) {
			this.loadingGroupMsg = loading;
			if (!this.isLoading()) {
				this.refreshChats()
			}
		},
		refreshChats(state) {
			console.log("refreshChats")
			// 排序
			cacheChats.sort((chat1, chat2) => {
				return chat2.lastSendTime - chat1.lastSendTime;
			});
			// 将消息一次性装载回来
			this.chats = cacheChats;
			// 断线重连后不能使用缓存模式，否则会导致聊天窗口的消息不刷新
			cacheChats = this.chats;
			this.saveToStorage();
		},
		saveToStorage(state) {
			console.log("saveToStorage")
			// 加载中不保存，防止卡顿
			if (this.isLoading()) {
				return;
			}
			const timeStamp = new Date().getTime();
			const userStore = useUserStore();
			let userId = userStore.userInfo.id;
			let key = "chats-app-" + userId;
			let chatsData = {
				privateMsgMaxId: this.privateMsgMaxId,
				groupMsgMaxId: this.groupMsgMaxId,
				chats: this.chats
			}
			uni.setStorageSync(key, chatsData)
			console.log("耗时:", new Date().getTime() - timeStamp);
		},
		clear(state) {
			cacheChats = [];
			this.chats = [];
			this.privateMsgMaxId = 0;
			this.groupMsgMaxId = 0;
			this.loadingPrivateMsg = false;
			this.loadingGroupMsg = false;
		},
		loadChat(context) {
			return new Promise((resolve, reject) => {
				const userStore = useUserStore();
				let userId = userStore.userInfo.id;
				uni.getStorage({
					key: "chats-app-" + userId,
					success: (res) => {
						this.initChats(res.data);
						resolve()
					},
					fail: (e) => {
						resolve()
					}
				});
			})
		}
	},
	getters: {
		isLoading: (state) => () => {
			return state.loadingPrivateMsg || state.loadingGroupMsg
		},
		curChats: (state) => {
			return state.isLoading() ? cacheChats : state.chats;
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
		findChat: (state) => (msgInfo) => {
			let chats = state.curChats;
			// 获取对方id或群id
			let type = msgInfo.groupId ? 'GROUP' : 'PRIVATE';
			let targetId = msgInfo.groupId ? msgInfo.groupId : msgInfo.selfSend ? msgInfo.recvId : msgInfo
				.sendId;
			let chat = null;
			for (let idx in chats) {
				if (chats[idx].type == type &&
					chats[idx].targetId === targetId) {
					chat = chats[idx];
					break;
				}
			}
			return chat;
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
			for (let idx in chat.messages) {
				// 通过id判断
				if (msgInfo.id && chat.messages[idx].id == msgInfo.id) {
					return chat.messages[idx];
				}
				// 正在发送中的消息可能没有id,只有tmpId
				if (msgInfo.tmpId && chat.messages[idx].tmpId &&
					chat.messages[idx].tmpId == msgInfo.tmpId) {
					return chat.messages[idx];
				}
			}
		}
	}
});
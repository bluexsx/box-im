import { MESSAGE_TYPE, MESSAGE_STATUS } from "../api/enums.js"
import userStore from './userStore';
import localForage from 'localforage';

/* 为了加速拉取离线消息效率，拉取时消息暂时存储到cacheChats,等
待所有离线消息拉取完成后，再统一放至vuex中进行渲染*/
let cacheChats = [];

export default {
	state: {
		activeChat: null,
		privateMsgMaxId: 0,
		groupMsgMaxId: 0,
		loadingPrivateMsg: false,
		loadingGroupMsg: false,
		chats: []
	},

	mutations: {
		initChats(state, chatsData) {
			state.chats = [];
			state.privateMsgMaxId = chatsData.privateMsgMaxId || 0;
			state.groupMsgMaxId = chatsData.groupMsgMaxId || 0;
			cacheChats = chatsData.chats || [];
			// 防止图片一直处在加载中状态
			cacheChats.forEach((chat) => {
				chat.messages.forEach((msg) => {
					if (msg.loadStatus == "loading") {
						msg.loadStatus = "fail"
					}
				})
			})
		},
		openChat(state, chatInfo) {
			let chats = this.getters.findChats()
			let chat = null;
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId === chatInfo.targetId) {
					chat = chats[idx];
					// 放置头部
					this.commit("moveTop", idx)
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
					stored: false,
					delete: false
				};
				chats.unshift(chat);
			}
		},
		activeChat(state, idx) {
			let chats = this.getters.findChats();
			state.activeChat = chats[idx];
		},
		resetUnreadCount(state, chatInfo) {
			let chats = this.getters.findChats();
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId == chatInfo.targetId) {
					chats[idx].unreadCount = 0;
					chats[idx].atMe = false;
					chats[idx].atAll = false;
					chats[idx].stored = false;
					this.commit("saveToStorage");
					break;
				}
			}
		},
		readedMessage(state, pos) {
			let chat = this.getters.findChatByFriend(pos.friendId);
			chat.messages.forEach((m) => {
				if (m.id && m.selfSend && m.status != MESSAGE_STATUS.RECALL) {
					// pos.maxId为空表示整个会话已读
					if (!pos.maxId || m.id <= pos.maxId) {
						m.status = MESSAGE_STATUS.READED
						chat.stored = false;
					}
				}
			})
			this.commit("saveToStorage");
		},
		removeChat(state, idx) {
			let chats = this.getters.findChats();
			if (chats[idx] == state.activeChat) {
				state.activeChat = null;
			}
			chats[idx].delete = true;
			chats[idx].stored = false;
			this.commit("saveToStorage");
		},
		removePrivateChat(state,friendId){
			let chats = this.getters.findChats();
			for (let idx in chats) {
				if (chats[idx].type == 'PRIVATE' &&
					chats[idx].targetId === friendId) {
					this.commit("removeChat",idx)
					break;
				}
			}
		},
		removeGroupChat(state,groupId){
			let chats = this.getters.findChats();
			for (let idx in chats) {
				if (chats[idx].type == 'GROUP' &&
					chats[idx].targetId === groupId) {
					this.commit("removeChat",idx)
					break;
				}
			}
		},
		moveTop(state, idx) {
			// 加载中不移动，很耗性能
			if (this.getters.isLoading()) {
				return;
			}
			if (idx > 0) {
				let chats = this.getters.findChats();
				let chat = chats[idx];
				chats.splice(idx, 1);
				chats.unshift(chat);
				chat.lastSendTime = new Date().getTime();
				chat.stored = false;
				this.commit("saveToStorage");
			}
		},
		insertMessage(state, msgInfo) {
			let type = msgInfo.groupId ? 'GROUP' : 'PRIVATE';
			// 记录消息的最大id
			if (msgInfo.id && type == "PRIVATE" && msgInfo.id > state.privateMsgMaxId) {
				state.privateMsgMaxId = msgInfo.id;
			}
			if (msgInfo.id && type == "GROUP" && msgInfo.id > state.groupMsgMaxId) {
				state.groupMsgMaxId = msgInfo.id;
			}
			// 如果是已存在消息，则覆盖旧的消息数据
			let chat = this.getters.findChat(msgInfo);
			let message = this.getters.findMessage(chat, msgInfo);
			if (message) {
				Object.assign(message, msgInfo);
				// 撤回消息需要显示
				if (msgInfo.type == MESSAGE_TYPE.RECALL) {
					chat.lastContent = msgInfo.content;
				}
				chat.stored = false;
				this.commit("saveToStorage");
				return;
			}
			// 插入新的数据
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
			if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED && msgInfo.type != MESSAGE_TYPE.TIP_TEXT) {
				chat.unreadCount++;
			}
			// 是否有人@我
			if (!msgInfo.selfSend && chat.type == "GROUP" && msgInfo.atUserIds &&
				msgInfo.status != MESSAGE_STATUS.READED) {
				let userId = userStore.state.userInfo.id;
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
			chat.messages.splice(insertPos, 0, msgInfo);
			chat.stored = false;
			this.commit("saveToStorage");
		},
		updateMessage(state, msgInfo) {
			// 获取对方id或群id
			let chat = this.getters.findChat(msgInfo);
			let message = this.getters.findMessage(chat, msgInfo);
			if (message) {
				// 属性拷贝
				Object.assign(message, msgInfo);
				chat.stored = false;
				this.commit("saveToStorage");
			}
		},
		deleteMessage(state, msgInfo) {
			let chat = this.getters.findChat(msgInfo);
			for (let idx in chat.messages) {
				// 已经发送成功的，根据id删除
				if (chat.messages[idx].id && chat.messages[idx].id == msgInfo.id) {
					chat.messages.splice(idx, 1);
					break;
				}
				// 正在发送中的消息可能没有id，根据发送时间删除
				if (msgInfo.selfSend && chat.messages[idx].selfSend &&
					chat.messages[idx].sendTime == msgInfo.sendTime) {
					chat.messages.splice(idx, 1);
					break;
				}
			}
			chat.stored = false;
			this.commit("saveToStorage");
		},
		updateChatFromFriend(state, friend) {
			let chat = this.getters.findChatByFriend(friend.id);
			// 更新会话中的群名和头像
			if (chat && (chat.headImage != friend.headImageThumb ||
					chat.showName != friend.nickName)) {
				chat.headImage = friend.headImageThumb;
				chat.showName = friend.nickName;
				chat.stored = false;
				this.commit("saveToStorage")
			}
		},
		updateChatFromGroup(state, group) {
			let chat = this.getters.findChatByGroup(group.id);
			if (chat && (chat.headImage != group.headImageThumb ||
					chat.showName != group.showGroupName)) {
				// 更新会话中的群名称和头像
				chat.headImage = group.headImageThumb;
				chat.showName = group.showGroupName;
				chat.stored = false;
				this.commit("saveToStorage")
			}
		},
		loadingPrivateMsg(state, loading) {
			state.loadingPrivateMsg = loading;
			if (!this.getters.isLoading()) {
				this.commit("refreshChats")
			}
		},
		loadingGroupMsg(state, loading) {
			state.loadingGroupMsg = loading;
			if (!this.getters.isLoading()) {
				this.commit("refreshChats")
			}
		},
		refreshChats(state) {
			// 排序
			cacheChats.sort((chat1, chat2) => {
				return chat2.lastSendTime - chat1.lastSendTime;
			});
			// 将消息一次性装载回来
			state.chats = cacheChats;
			// 断线重连后不能使用缓存模式，否则会导致聊天窗口的消息不刷新
			cacheChats = state.chats;
			this.commit("saveToStorage");
		},
		saveToStorage(state) {
			// 加载中不保存，防止卡顿
			if (this.getters.isLoading()) {
				return;
			}
			let userId = userStore.state.userInfo.id;
			let key = "chats-" + userId;
			let chatKeys = [];
			// 按会话为单位存储，
			state.chats.forEach((chat) => {
				// 只存储有改动的会话
				let chatKey = `${key}-${chat.type}-${chat.targetId}`
				if (!chat.stored) {
					if (chat.delete) {
						localForage.removeItem(chatKey);
					} else {
						localForage.setItem(chatKey, chat);
					}
					chat.stored = true;
				}
				if (!chat.delete) {
					chatKeys.push(chatKey);
				}
			})
			// 会话核心信息
			let chatsData = {
				privateMsgMaxId: state.privateMsgMaxId,
				groupMsgMaxId: state.groupMsgMaxId,
				chatKeys: chatKeys
			}
			localForage.setItem(key, chatsData)
		},
		clear(state) {
			cacheChats = []
			state.chats = [];
			state.activeChat = null;
		}
	},
	actions: {
		loadChat(context) {
			return new Promise((resolve, reject) => {
				let userId = userStore.state.userInfo.id;
				let key = "chats-" + userId;
				localForage.getItem(key).then((chatsData) => {
					if (!chatsData) {
						resolve();
					}
					else if(chatsData.chats){
						// 兼容旧版本
						context.commit("initChats", chatsData);
						resolve();
					}else if (chatsData.chatKeys) {
						const promises = [];
						chatsData.chatKeys.forEach(key => {
							promises.push(localForage.getItem(key))
						})
						Promise.all(promises).then(chats => {
							chatsData.chats = chats.filter(o => o);
							context.commit("initChats", chatsData);
							resolve();
						})
					}
				}).catch((e) => {
					console.log("加载消息失败")
					reject();
				})
			})
		}
	},
	getters: {
		isLoading: (state) => () => {
			return state.loadingPrivateMsg || state.loadingGroupMsg
		},
		findChats: (state, getters) => () => {
			return getters.isLoading() ? cacheChats : state.chats;
		},
		findChatIdx: (state, getters) => (chat) => {
			let chats = getters.findChats();
			for (let idx in chats) {
				if (chats[idx].type == chat.type &&
					chats[idx].targetId === chat.targetId) {
					chat = chats[idx];
					return idx
				}
			}
		},
		findChat: (state, getters) => (msgInfo) => {
			let chats = getters.findChats();
			// 获取对方id或群id
			let type = msgInfo.groupId ? 'GROUP' : 'PRIVATE';
			let targetId = msgInfo.groupId ? msgInfo.groupId : msgInfo.selfSend ? msgInfo.recvId : msgInfo.sendId;
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
		findChatByFriend: (state, getters) => (fid) => {
			let chats = getters.findChats();
			return chats.find(chat => chat.type == 'PRIVATE' &&
				chat.targetId == fid)
		},
		findChatByGroup: (state, getters) => (gid) => {
			let chats = getters.findChats();
			return chats.find(chat => chat.type == 'GROUP' &&
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
}
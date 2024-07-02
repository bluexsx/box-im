import {
	MESSAGE_TYPE,
	MESSAGE_STATUS
} from '@/common/enums.js';
import userStore from './userStore';
/*
	uniapp性能优化：
	1.由于uniapp渲染消息性能非常拉胯,所以先把离线消息存储到cacheChats,等
		待所有离线消息拉取完成后，再统一进行渲染
	2.在vuex中对数组进行unshift,splice特别卡，所以删除会话、会话置顶、删
		除消息等操作进行优化，不通过unshift,splice实现，改造方案如下：
		删除会话： 通过delete标志判断是否删除
		删除消息：通过delete标志判断是否删除
		会话置顶：通过lastSendTime排序确定会话顺序
*/

let cacheChats = [];
export default {
	state: {
		chats: [],
		privateMsgMaxId: 0,
		groupMsgMaxId: 0,
		loadingPrivateMsg: false,
		loadingGroupMsg: false,
	},

	mutations: {
		initChats(state, chatsData) {
			cacheChats = [];
			state.chats = [];
			for (let chat of chatsData.chats) {
				// 已删除的会话直接丢弃
				if (chat.delete) {
					continue;
				}
				// 暂存至缓冲区
				cacheChats.push(JSON.parse(JSON.stringify(chat)));
				// 加载期间显示只前15个会话做做样子,一切都为了加快初始化时间
				if (state.chats.length < 15) {
					chat.messages = [];
					state.chats.push(chat);
				}
			}
			state.privateMsgMaxId = chatsData.privateMsgMaxId || 0;
			state.groupMsgMaxId = chatsData.groupMsgMaxId || 0;
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
			let chats = this.getters.findChats();
			let chat = null;
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId === chatInfo.targetId) {
					chat = chats[idx];
					chat.delete = false;
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
					delete: false
				};
				chats.push(chat);
				this.commit("moveTop", chats.length - 1)
			}
		},
		activeChat(state, idx) {
			let chats = this.getters.findChats();
			if (idx >= 0) {
				chats[idx].unreadCount = 0;
			}
		},
		resetUnreadCount(state, chatInfo) {
			let chats = this.getters.findChats();
			for (let idx in chats) {
				if (chats[idx].type == chatInfo.type &&
					chats[idx].targetId == chatInfo.targetId) {
					chats[idx].unreadCount = 0;
					chats[idx].atMe = false;
					chats[idx].atAll = false;
				}
			}
			this.commit("saveToStorage");
		},
		readedMessage(state, pos) {
			let chats = this.getters.findChats();
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
			this.commit("saveToStorage");
		},
		removeChat(state, idx) {
			let chats = this.getters.findChats();
			chats[idx].delete = true;
			this.commit("saveToStorage");
		},
		removePrivateChat(state, userId) {
			let chats = this.getters.findChats();
			for (let idx in chats) {
				if (chats[idx].type == 'PRIVATE' &&
					chats[idx].targetId == userId) {
					this.commit("removeChat", idx);
				}
			}
		},
		removeGroupChat(state, groupId) {
			let chats = this.getters.findChats();
			for (let idx in chats) {
				if (chats[idx].type == 'GROUP' &&
					chats[idx].targetId == groupId) {
					this.commit("removeChat", idx);
				}
			}
		},
		moveTop(state, idx) {
			let chats = this.getters.findChats();
			let chat = chats[idx];
			// 最新的时间会显示在顶部
			chat.lastSendTime = new Date().getTime();
			this.commit("saveToStorage");
		},
		insertMessage(state, msgInfo) {
			// 获取对方id或群id
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
				this.commit("saveToStorage");
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
			if (insertPos == chat.messages.length) {
				// 这种赋值效率最高
				chat.messages[insertPos] = msgInfo;
			} else {
				chat.messages.splice(insertPos, 0, msgInfo);
			}
			this.commit("saveToStorage");
		},
		updateMessage(state, msgInfo) {
			// 获取对方id或群id
			let chat = this.getters.findChat(msgInfo);
			let message = this.getters.findMessage(chat, msgInfo);
			if (message) {
				// 属性拷贝
				Object.assign(message, msgInfo);
				this.commit("saveToStorage");
			}
		},
		deleteMessage(state, msgInfo) {
			// 获取对方id或群id
			let chat = this.getters.findChat(msgInfo);
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
			this.commit("saveToStorage");
		},
		updateChatFromFriend(state, friend) {
			let chats = this.getters.findChats();
			for (let i in chats) {
				let chat = chats[i];
				if (chat.type == 'PRIVATE' && chat.targetId == friend.id) {
					chat.headImage = friend.headImageThumb;
					chat.showName = friend.nickName;
					break;
				}
			}
			this.commit("saveToStorage");
		},
		updateChatFromGroup(state, group) {
			let chats = this.getters.findChats();
			for (let i in chats) {
				let chat = chats[i];
				if (chat.type == 'GROUP' && chat.targetId == group.id) {
					chat.headImage = group.headImageThumb;
					chat.showName = group.remark;
					break;
				}
			}
			this.commit("saveToStorage");
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
			let key = "chats-app-" + userId;
			let chatsData = {
				privateMsgMaxId: state.privateMsgMaxId,
				groupMsgMaxId: state.groupMsgMaxId,
				chats: state.chats
			}
			uni.setStorage({
				key: key,
				data: chatsData ,
			})
		},
		clear(state) {
			cacheChats = [];
			state.chats = [];
			state.privateMsgMaxId = 0;
			state.groupMsgMaxId = 0;
			state.loadingPrivateMsg = false;
			state.loadingGroupMsg = false;
		}
	},
	actions: {
		loadChat(context) {
			return new Promise((resolve, reject) => {
				let userId = userStore.state.userInfo.id;
				uni.getStorage({
					key: "chats-app-" + userId,
					success(res) {
						context.commit("initChats", res.data);
						resolve()
					},
					fail(e) {
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
		findChats: (state, getters) => () => {
			return getters.isLoading() ? cacheChats : state.chats;
		},
		findChatIdx: (state, getters) => (chat) => {
			let chats = getters.findChats();
			for (let idx in chats) {
				if (chats[idx].type == chat.type &&
					chats[idx].targetId === chat.targetId) {
					chat = state.chats[idx];
					return idx;
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
						console.log("chat.messages[idx].tmpId == msgInfo.tmpId")
					return chat.messages[idx];
				}
			}
		}
	}
}
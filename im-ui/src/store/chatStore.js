import {
	MESSAGE_TYPE,
	MESSAGE_STATUS
} from "../api/enums.js"
import userStore from './userStore';
export default {

	state: {
		activeIndex: -1,
		privateMsgMaxId: 0,
		groupMsgMaxId: 0,
		loadingPrivateMsg: false,
		loadingGroupMsg: false,
		chats: []
	},

	mutations: {
		initChats(state, chatsData) {
			state.chats = chatsData.chats || [];
			state.privateMsgMaxId = chatsData.privateMsgMaxId || 0;
			state.groupMsgMaxId = chatsData.groupMsgMaxId || 0;
			// 防止图片一直处在加载中状态
			state.chats.forEach((chat) => {
				chat.messages.forEach((msg) => {
					if (msg.loadStatus == "loading") {
						msg.loadStatus = "fail"
					}
				})
			})
		},
		openChat(state, chatInfo) {
			let chat = null;
			let activeChat = state.activeIndex >= 0 ? state.chats[state.activeIndex] : null;
			for (let i in state.chats) {
				if (state.chats[i].type == chatInfo.type &&
					state.chats[i].targetId === chatInfo.targetId) {
					chat = state.chats[i];
					// 放置头部
					state.chats.splice(i, 1);
					state.chats.unshift(chat);
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
				};
				state.chats.unshift(chat);
			}
			// 选中会话保持不变
			if (activeChat) {
				state.chats.forEach((chat, idx) => {
					if (activeChat.type == chat.type &&
						activeChat.targetId == chat.targetId) {
						state.activeIndex = idx;
					}
				})
			}
		},
		activeChat(state, idx) {
			state.activeIndex = idx;
		},
		resetUnreadCount(state, chatInfo) {
			for (let idx in state.chats) {
				if (state.chats[idx].type == chatInfo.type &&
					state.chats[idx].targetId == chatInfo.targetId) {
					state.chats[idx].unreadCount = 0;
					state.chats[idx].atMe = false;
					state.chats[idx].atAll = false; 
				}
			}
			this.commit("saveToStorage");
		},
		readedMessage(state, friendId) {
			for (let idx in state.chats) {
				if (state.chats[idx].type == 'PRIVATE' &&
					state.chats[idx].targetId == friendId) {
					state.chats[idx].messages.forEach((m) => {
						if (m.selfSend && m.status != MESSAGE_STATUS.RECALL) {
							m.status = MESSAGE_STATUS.READED
						}
					})
				}
			}
			this.commit("saveToStorage");
		},
		removeChat(state, idx) {
			state.chats.splice(idx, 1);
			if (state.activeIndex >= state.chats.length) {
				state.activeIndex = state.chats.length - 1;
			}
			this.commit("saveToStorage");
		},
		moveTop(state, idx) {
			let chat = state.chats[idx];
			// 放置头部
			state.chats.splice(idx, 1);
			state.chats.unshift(chat);
			this.commit("saveToStorage");
		},
		removeGroupChat(state, groupId) {
			for (let idx in state.chats) {
				if (state.chats[idx].type == 'GROUP' &&
					state.chats[idx].targetId == groupId) {
					this.commit("removeChat", idx);
				}
			}
		},
		removePrivateChat(state, friendId) {
			for (let idx in state.chats) {
				if (state.chats[idx].type == 'PRIVATE' &&
					state.chats[idx].targetId == friendId) {
					this.commit("removeChat", idx);
				}
			}
		},
		insertMessage(state, msgInfo) {

			// 获取对方id或群id
			let type = msgInfo.groupId ? 'GROUP' : 'PRIVATE';
			let targetId = msgInfo.groupId ? msgInfo.groupId : msgInfo.selfSend ? msgInfo.recvId : msgInfo.sendId;
			let chat = null;
			for (let idx in state.chats) {
				if (state.chats[idx].type == type &&
					state.chats[idx].targetId === targetId) {
					chat = state.chats[idx];
					break;
				}
			}
			// 插入新的数据
			if (msgInfo.type == MESSAGE_TYPE.IMAGE) {
				chat.lastContent = "[图片]";
			} else if (msgInfo.type == MESSAGE_TYPE.FILE) {
				chat.lastContent = "[文件]";
			} else if (msgInfo.type == MESSAGE_TYPE.AUDIO) {
				chat.lastContent = "[语音]";
			} else {
				chat.lastContent = msgInfo.content;
			}
			chat.lastSendTime = msgInfo.sendTime;
			chat.sendNickName = msgInfo.sendNickName;
			// 未读加1
			if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED) {
				chat.unreadCount++;
			}
			// 是否有人@我
			if(!msgInfo.selfSend && chat.type=="GROUP" && msgInfo.atUserIds){
				let userId = userStore.state.userInfo.id;
				if(msgInfo.atUserIds.indexOf(userId)>=0){
					chat.atMe = true;
				}
				if(msgInfo.atUserIds.indexOf(-1)>=0){
					chat.atAll = true;
				}
			}
			// 记录消息的最大id
			if (msgInfo.id && type == "PRIVATE" && msgInfo.id > state.privateMsgMaxId) {
				state.privateMsgMaxId = msgInfo.id;
			}
			if (msgInfo.id && type == "GROUP" && msgInfo.id > state.groupMsgMaxId) {
				state.groupMsgMaxId = msgInfo.id;
			}
			// 如果是已存在消息，则覆盖旧的消息数据
			for (let idx in chat.messages) {
				if (msgInfo.id && chat.messages[idx].id == msgInfo.id) {
					Object.assign(chat.messages[idx], msgInfo);
					this.commit("saveToStorage");
					return;
				}
				// 正在发送中的消息可能没有id,通过发送时间判断
				if (msgInfo.selfSend && chat.messages[idx].selfSend &&
					chat.messages[idx].sendTime == msgInfo.sendTime) {
					Object.assign(chat.messages[idx], msgInfo);
					this.commit("saveToStorage");
					return;
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
			// 新的消息
			chat.messages.push(msgInfo);
			this.commit("saveToStorage");
		},
		deleteMessage(state, msgInfo) {
			// 获取对方id或群id
			let type = msgInfo.groupId ? 'GROUP' : 'PRIVATE';
			let targetId = msgInfo.groupId ? msgInfo.groupId : msgInfo.selfSend ? msgInfo.recvId : msgInfo.sendId;
			let chat = null;
			for (let idx in state.chats) {
				if (state.chats[idx].type == type &&
					state.chats[idx].targetId === targetId) {
					chat = state.chats[idx];
					break;
				}
			}

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
			this.commit("saveToStorage");
		},
		updateChatFromFriend(state, friend) {
			for (let i in state.chats) {
				let chat = state.chats[i];
				if (chat.type == 'PRIVATE' && chat.targetId == friend.id) {
					chat.headImage = friend.headImageThumb;
					chat.showName = friend.nickName;
					break;
				}
			}
			this.commit("saveToStorage");
		},
		updateChatFromGroup(state, group) {
			for (let i in state.chats) {
				let chat = state.chats[i];
				if (chat.type == 'GROUP' && chat.targetId == group.id) {
					chat.headImage = group.headImageThumb;
					chat.showName = group.remark;
					break;
				}
			}
			this.commit("saveToStorage");
		},

		loadingPrivateMsg(state, loadding) {
			state.loadingPrivateMsg = loadding;
		},
		loadingGroupMsg(state, loadding) {
			state.loadingGroupMsg = loadding;
		},
		saveToStorage(state) {
			let userId = userStore.state.userInfo.id;
			let key = "chats-" + userId;
			let chatsData = {
				privateMsgMaxId: state.privateMsgMaxId,
				groupMsgMaxId: state.groupMsgMaxId,
				chats: state.chats
			}
			localStorage.setItem(key, JSON.stringify(chatsData));
		},
		clear(state) {
			state.activeIndex = -1;
			state.chats = [];
		}
	},
	actions: {
		loadChat(context) {
			return new Promise((resolve, reject) => {
				let userId = userStore.state.userInfo.id;
				let key = "chats-" + userId;
				let item = localStorage.getItem(key)
				if (item) {
					let chatsData = JSON.parse(item);
					context.commit("initChats", chatsData);
				}
				resolve();
			})
		}
	}
}
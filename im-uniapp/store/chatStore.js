import {
	MESSAGE_TYPE,
	MESSAGE_STATUS
} from '@/common/enums.js';
import userStore from './userStore';

export default {

	state: {
		activeIndex: -1,
		chats: [],
		privateMsgMaxId: 0,
		groupMsgMaxId: 0,
		loadingPrivateMsg: false,
		loadingGroupMsg: false,
	},

	mutations: {
		initChats(state, chatsData) {
			state.chats = chatsData.chats ||[];
			state.privateMsgMaxId = chatsData.privateMsgMaxId||0;
			state.groupMsgMaxId = chatsData.groupMsgMaxId||0;
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
			for (let i in state.chats) {
				if (state.chats[i].type == chatInfo.type &&
					state.chats[i].targetId === chatInfo.targetId) {
					chat = state.chats[i];
					// 放置头部（这个操作非常耗资源，正在加载消息时不执行）
					if(!state.loadingPrivateMsg && !state.loadingPrivateMsg){
						state.chats.splice(i, 1);
						state.chats.unshift(chat);
					}
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
			this.commit("saveToStorage");
		},
		activeChat(state, idx) {
			state.activeIndex = idx;
			if (idx >= 0) {
				state.chats[idx].unreadCount = 0;
			}
		},
		resetUnreadCount(state, chatInfo) {
			for (let idx in state.chats) {
				if (state.chats[idx].type == chatInfo.type &&
					state.chats[idx].targetId == chatInfo.targetId) {
					state.chats[idx].unreadCount = 0;
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
		removePrivateChat(state, userId) {
			for (let idx in state.chats) {
				if (state.chats[idx].type == 'PRIVATE' &&
					state.chats[idx].targetId == userId) {
					this.commit("removeChat", idx);
				}
			}
		},
		moveTop(state, idx) {
			let chat = state.chats[idx];
			// 放置头部
			state.chats.splice(idx, 1);
			state.chats.unshift(chat);
		},
		insertMessage(state, msgInfo) {
			// 获取对方id或群id
			let type = msgInfo.groupId ? 'GROUP' : 'PRIVATE';
			let targetId = msgInfo.groupId ? msgInfo.groupId : msgInfo.selfSend ? msgInfo.recvId : msgInfo.sendId;
			let chat = null;
			let chatIdx = -1;
			for (let idx in state.chats) {
				if (state.chats[idx].type == type &&
					state.chats[idx].targetId === targetId) {
					chat = state.chats[idx];
					chatIdx = idx;
					break;
				}
			}
			
			// 会话列表内容
			if(!state.loadingPrivateMsg && !state.loadingPrivateMsg){
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
			}
			// 未读加1
			if (!msgInfo.selfSend && msgInfo.status != MESSAGE_STATUS.READED) {
				chat.unreadCount++;
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
		refreshChats(state){
			state.chats.forEach((chat)=>{
				if(chat.messages.length>0){
					let msgInfo = chat.messages[chat.messages.length-1];
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
				}else{
					chat.lastContent = "";
					chat.lastSendTime  = new Date()
				}
			})
			state.chats.sort((chat1, chat2) => {
				return chat2.lastSendTime-chat1.lastSendTime;
			});
		},
		saveToStorage(state) {
			let userId = userStore.state.userInfo.id;
			let key = "chats-" + userId;
			let chatsData = {
				privateMsgMaxId: state.privateMsgMaxId,
				groupMsgMaxId: state.groupMsgMaxId,
				chats: state.chats
			}
			// uni.setStorage({
			// 	key: key,
			// 	data: chatsData
			// })
		},
		clear(state) {
			state.chats = [];
			state.activeIndex = -1;
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
					key: "chats-" + userId,
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

	}
}
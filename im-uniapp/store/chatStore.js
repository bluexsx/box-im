import {MESSAGE_TYPE} from '@/common/enums.js';
import userStore from './userStore';

export default {
	
	state: {
		chats: []
	},

	mutations: {
		setChats(state,chats){
			state.chats = chats;
		},
		openChat(state, chatInfo) {
			let chat = null;
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
			this.commit("saveToStorage");
		},
		activeChat(state, idx) {
			state.activeIndex = idx;
			if(idx>=0){
				state.chats[idx].unreadCount = 0;
			}
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
		moveTop(state,idx){
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
			// 插入新的数据
			if(msgInfo.type == MESSAGE_TYPE.IMAGE){
				chat.lastContent =  "[图片]";
			}else if(msgInfo.type == MESSAGE_TYPE.FILE){
				chat.lastContent = "[文件]";
			}else if(msgInfo.type == MESSAGE_TYPE.AUDIO){
				chat.lastContent = "[语音]";
			}else{
				chat.lastContent =  msgInfo.content;
			}
			chat.lastSendTime = msgInfo.sendTime;
			// 如果不是当前会话，未读加1
			if(chatIdx !=  state.activeIndex){
				chat.unreadCount++;
			}
			// 自己回复了消息，说明消息已读
			if(msgInfo.selfSend){
				chat.unreadCount=0;
			}
			// 如果是已存在消息，则覆盖旧的消息数据
			for (let idx in chat.messages) {
				if(msgInfo.id && chat.messages[idx].id == msgInfo.id){
					Object.assign(chat.messages[idx], msgInfo);
					this.commit("saveToStorage");
					return;
				}
				// 正在发送中的消息可能没有id,通过发送时间判断
				if(msgInfo.selfSend && chat.messages[idx].selfSend
				&& chat.messages[idx].sendTime == msgInfo.sendTime){
					Object.assign(chat.messages[idx], msgInfo);
					this.commit("saveToStorage");
					return;
				}
			}
			// 新的消息
			chat.messages.push(msgInfo);
			console.log(chat.unreadCount)
			this.commit("saveToStorage");
			
		},
		deleteMessage(state, msgInfo){
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
				if(chat.messages[idx].id && chat.messages[idx].id == msgInfo.id){
					chat.messages.splice(idx, 1);
					break;
				}
				// 正在发送中的消息可能没有id，根据发送时间删除
				if(msgInfo.selfSend && chat.messages[idx].selfSend 
				&&chat.messages[idx].sendTime == msgInfo.sendTime){
					chat.messages.splice(idx, 1);
					break;
				}
			}
			this.commit("saveToStorage");
		},
		updateChatFromFriend(state, friend) {
			for (let i in state.chats) {
				let chat = state.chats[i];
				if (chat.type=='PRIVATE' && chat.targetId == friend.id) {
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
				if (chat.type=='GROUP' && chat.targetId == group.id) {
					chat.headImage = group.headImageThumb;
					chat.showName = group.remark;
					break;
				}
			}
			this.commit("saveToStorage");
		},
		saveToStorage(state){
			let userId = userStore.state.userInfo.id;
			uni.setStorage({
				key:"chats-"+userId,
				data: state.chats 
			})
		}
	}, 
	actions:{
		loadChat(context) {
			return new Promise((resolve, reject) => {
				let userId = userStore.state.userInfo.id;
				uni.getStorage({
					key:"chats-"+userId,
					success(res) {
						context.commit("setChats",res.data);
						resolve()
					},
					fail(e) {
						// 不存在聊天记录,清空聊天列表
						context.commit("setChats",[]);
						resolve()
					}
				});
			})
			
			
		}
	}
}

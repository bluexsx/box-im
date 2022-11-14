export default {

	state: {
		activeIndex: -1,
		chats: []
	},

	mutations: {
		initChatStore(state) {
			state.activeIndex = -1;
		},
		openChat(state, chatInfo) {
			let chat = null;
			let activeChat = state.activeIndex>=0?state.chats[state.activeIndex]:null;
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
			if(activeChat){
				state.chats.forEach((chat,idx)=>{
					if(activeChat.type == chat.type
					&& activeChat.targetId == chat.targetId){
						state.activeIndex = idx;
					}
				})
			}
		},
		activeChat(state, idx) {
			state.activeIndex = idx;
			state.chats[idx].unreadCount = 0;
		},
		removeChat(state, idx) {
			state.chats.splice(idx, 1);
			if (state.activeIndex >= state.chats.length) {
				state.activeIndex = state.chats.length - 1;
			}
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
			console.log(msgInfo.type)
			if(msgInfo.type == 1){
				chat.lastContent =  "[图片]";
			}else if(msgInfo.type == 2){
				chat.lastContent = "[文件]";
			}else if(msgInfo.type == 3){
				chat.lastContent = "[语音]";
			}else{
				chat.lastContent =  msgInfo.content;
			}
			chat.lastSendTime = msgInfo.sendTime;
			chat.messages.push(msgInfo);
			// 如果不是当前会话，未读加1
			chat.unreadCount++;
			if(msgInfo.selfSend){
				chat.unreadCount=0;
			}
		},
		handleFileUpload(state, info) {
			// 文件上传后数据更新
			let chat = state.chats.find((c) => c.type==info.type && c.targetId === info.targetId);
			let msg = chat.messages.find((m) => info.fileId == m.fileId);
			msg.loadStatus = info.loadStatus;
			if (info.content) {
				msg.content = info.content;
			}
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
		},
		resetChatStore(state) {
			state.activeIndex = -1;
			state.chats = [];
		}
	},

}

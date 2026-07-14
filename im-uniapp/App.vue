<script>
import { chatStore, friendStore, groupStore, configStore, userStore } from '@/store/stores.js'
import http from './common/request';
import * as msgType from './common/messageType';
import * as enums from './common/enums';
import * as wsApi from './common/wssocket';
import UNI_APP from '@/.env.js'

export default {
	data() {
		return {
			isExit: false, // 是否已退出
			audioTip: null,
			lastPlayAudioTime: 0,
			reconnecting: false, // 正在重连标志
			privateMessagesBuffer: [],
			groupMessagesBuffer: [],
		}
	},
	methods: {
		init() {
			this.reconnecting = false;
			this.isExit = false;
			// 加载数据
			this.loadStore().then(() => {
				// 初始化websocket
				this.initWebSocket();
			}).catch(e => {
				console.log(e);
				this.exit();
			})
		},
		initWebSocket() {
			let loginInfo = uni.getStorageSync("loginInfo")
			wsApi.connect(UNI_APP.WS_URL, loginInfo.accessToken);
			wsApi.onConnect(() => {
				if (this.reconnecting) {
					// 重连成功
					this.onReconnectWs();
				} else {
					// 加载离线消息
					this.pullOfflineMessage();
				}
			});
			wsApi.onMessage((cmd, msgInfo) => {
				if (cmd == 2) {
					// 异地登录，强制下线
					uni.showModal({
						content: '您已在其他地方登录，将被强制下线',
						showCancel: false,
					})
					this.exit();
				} else if (cmd == 3) {
					if (chatStore.loading) {
						// 如果正在拉取离线消息，先存入缓存区，等待消息拉取完成再处理，防止消息乱序
						this.privateMessagesBuffer.push(msgInfo);
					} else {
						// 插入私聊消息
						this.handlePrivateMessage(msgInfo);
					}
				} else if (cmd == 4) {
					if (chatStore.loading) {
						// 如果正在拉取离线消息，先存入缓存区，等待消息拉取完成再处理，防止消息乱序
						this.privateMessagesBuffer.push(msgInfo);
					} else {
						// 插入群聊消息
						this.handleGroupMessage(msgInfo);
					}
				} else if (cmd == 5) {
					// 系统消息
					this.handleSystemMessage(msgInfo);
				}
			});
			wsApi.onClose((res) => {
				console.log("ws断开", res);
				// 重新连接
				if (!this.reconnecting) {
					this.reconnectWs();
				}
			})
		},
		async loadStore() {
			await userStore.loadUser()
			// 初始化数据库
			await this.$db.open(userStore.userInfo.id);
			const promises = [];
			promises.push(friendStore.loadFriend());
			promises.push(groupStore.loadGroup());
			promises.push(chatStore.loadConversations());
			promises.push(configStore.loadConfig());
			return Promise.all(promises);
		},
		unloadStore() {
			friendStore.clear();
			groupStore.clear();
			chatStore.clear();
			configStore.clear();
			userStore.clear();
		},
		pullOfflineMessage() {
			let t1 = new Date().getTime();
			chatStore.setLoading(true);
			const promises = [];
			const maxPrivateMessageId = chatStore.findMaxMessageId(this.$enums.CONVERSATION_TYPE.PRIVATE);
			const maxGroupMessageId = chatStore.findMaxMessageId(this.$enums.CONVERSATION_TYPE.GROUP);
			promises.push(this.pullPrivateOfflineMessage(maxPrivateMessageId));
			promises.push(this.pullGroupOfflineMessage(maxGroupMessageId));
			Promise.all(promises).then(async (messages) => {
				let t2 = new Date().getTime();
				// 处理离线消息
				await this.handlePrivateOfflineMessage(messages[0]);
				await this.handleGroupOfflineMessage(messages[1]);
				// 处理缓冲区收到的实时消息
				for (const m of this.privateMessagesBuffer) {
					await this.handlePrivateMessage(m);
				}
				for (const m of this.groupMessagesBuffer) {
					await this.handleGroupMessage(m);
				}
				// 清空缓冲区
				this.privateMessagesBuffer = [];
				this.groupMessagesBuffer = [];
				// 关闭加载离线标记
				chatStore.setLoading(false);
				// 打印耗时
				let t3 = new Date().getTime();
				let size = messages[0].length + messages[1].length;
				console.log(`加载离线消息耗时:${t2 - t1},处理耗时:${t3 - t2},消息数量:${size}`)
			}).catch((e) => {
				console.log(e)
				uni.showToast({
					title: "拉取离线消息失败",
					icon: "none"
				})
				this.exit();
			})
		},
		async handlePrivateOfflineMessage(messages) {
			if (!messages || !messages.length) {
				return;
			}
			// 会话信息
			const conversationMap = new Map();
			// 离线消息,map结构方便查询
			const messageMap = new Map(messages.map(m => [m.id, m]));
			// 处理过程中衍生的需要入库的事消息
			const tmpMessages = [];
			for (const m of messages) {
				// 标记这条消息是不是自己发的
				m.selfSend = m.sendId == this.mine.id;
				// 好友id
				const friendId = m.selfSend ? m.recvId : m.sendId;
				// 标记消息所属会话id
				const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.PRIVATE, friendId);
				m.convKey = convKey;
				// 查询会话
				let conversation = conversationMap.get(friendId);
				if (!conversation) {
					// 查db
					conversation = await this.$db.findConversationByKey(convKey);
					if (!conversation) {
						// 创建新会话
						const friend = this.loadFriendInfo(friendId);
						conversation = {
							key: convKey,
							type: this.$enums.CONVERSATION_TYPE.PRIVATE,
							targetId: friend.id,
							showName: friend.nickName,
							headImage: friend.headImage,
							isDnd: friend.isDnd,
							isTop: false,
							lastContent: "",
							lastSendTime: new Date().getTime(),
							optTime: new Date().getTime(),
							unreadCount: 0,
							lastTimeTip: 0,
							maxMessageId: 0,
							minSeqNo: 0,
							maxSeqNo: 0,
							maxReadedId: 0
						}
					}
					conversationMap.set(friendId, conversation);
				}
				// 会话时间
				conversation.lastSendTime = m.sendTime;
				conversation.optTime = m.sendTime;
				// 记录会话最大消息id
				conversation.maxMessageId = Math.max(conversation.maxMessageId, m.id);
				conversation.maxSeqNo = Math.max(conversation.maxSeqNo, m.seqNo);
				// 会话未读加1
				if (!m.selfSend && m.status != this.$enums.MESSAGE_STATUS.READED &&
					m.status != this.$enums.MESSAGE_STATUS.RECALL && m.type != this.$enums.MESSAGE_TYPE.TIP_TEXT) {
					conversation.unreadCount++;
				}
				// 撤回消息
				if (m.type == this.$enums.MESSAGE_TYPE.RECALL) {
					const recallMessageId = Number(JSON.parse(m.content).id);
					const recallMessageTip = JSON.parse(m.content).tip || '';
					let recallMessage = messageMap.get(recallMessageId);
					if (!recallMessage) {
						recallMessage = await this.$db.findMessageById(convKey, recallMessageId);
						if (!recallMessage) {
							continue;
						}
						tmpMessages.push(recallMessage);
					}
					// 改造成一条提示消息
					recallMessage.status = this.$enums.MESSAGE_STATUS.PENDING;
					recallMessage.content = recallMessageTip;
					recallMessage.type = this.$enums.MESSAGE_TYPE.TIP_TEXT
					// 会话提示语
					conversation.lastContent = this.$msgUtil.previewContent(recallMessage);
					conversation.sendNickName = "";
				} else {
					// 会话内容
					conversation.lastContent = this.$msgUtil.previewContent(m);
				}
			}
			// 批量保存会话和消息
			const conversations = Array.from(conversationMap.values());
			await this.$db.saveConversationAndMessage(conversations, messages.concat(tmpMessages));
			chatStore.append(conversations);
		},
		async handleGroupOfflineMessage(messages) {
			if (!messages || !messages.length) {
				return;
			}
			// 会话信息
			const conversationMap = new Map();
			// 离线消息,map结构方便查询
			const messageMap = new Map(messages.map(m => [m.id, m]));
			// 处理过程中衍生的需要入库的事消息
			const tmpMessages = [];
			for (const m of messages) {
				// 标记这条消息是不是自己发的
				m.selfSend = m.sendId == this.mine.id;
				// 好友id
				const groupId = m.groupId;
				// 标记消息所属会话id
				const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, groupId);
				m.convKey = convKey;
				// 查询会话
				let conversation = conversationMap.get(groupId);
				if (!conversation) {
					// 查db
					conversation = await this.$db.findConversationByKey(convKey);
					if (!conversation) {
						// 创建新会话
						const group = this.loadGroupInfo(groupId);
						conversation = {
							key: convKey,
							type: this.$enums.CONVERSATION_TYPE.GROUP,
							targetId: group.id,
							showName: group.showGroupName,
							headImage: group.headImageThumb,
							isDnd: group.isDnd,
							isTop: group.isTop,
							lastContent: "",
							lastSendTime: new Date().getTime(),
							optTime: new Date().getTime(),
							unreadCount: 0,
							atMe: false,
							atAll: false,
							lastAtMessageId: -1,
							lastTimeTip: 0,
							maxMessageId: 0,
							minSeqNo: 0,
							maxSeqNo: 0,
							maxReadedId: 0
						}
					}
					conversationMap.set(groupId, conversation);
				}
				// 会话时间
				conversation.lastSendTime = m.sendTime;
				conversation.optTime = m.sendTime;
				// 记录会话最大消息id
				conversation.maxMessageId = Math.max(conversation.maxMessageId, m.id);
				conversation.maxSeqNo = Math.max(conversation.maxSeqNo, m.seqNo);
				// 会话未读加1
				if (!m.selfSend && m.status != this.$enums.MESSAGE_STATUS.READED &&
					m.status != this.$enums.MESSAGE_STATUS.RECALL && m.type != this.$enums.MESSAGE_TYPE.TIP_TEXT) {
					conversation.unreadCount++;
				}
				// 是否有人@我
				if (!m.selfSend && m.atUserIds && m.status != this.$enums.MESSAGE_STATUS.READED) {
					const userId = this.mine.id;
					if (m.atUserIds.indexOf(userId) >= 0) {
						conversation.atMe = true;
						conversation.lastAtMessageId = m.id;
					}
					if (m.atUserIds.indexOf(-1) >= 0) {
						conversation.atAll = true;
						conversation.lastAtMessageId = m.id;
					}
				}
				// 撤回消息
				if (m.type == this.$enums.MESSAGE_TYPE.RECALL) {
					const recallMessageId = Number(JSON.parse(m.content).id);
					const recallMessageTip = JSON.parse(m.content).tip || '';
					let recallMessage = messageMap.get(recallMessageId);
					if (!recallMessage) {
						recallMessage = await this.$db.findMessageById(convKey, recallMessageId);
						if (!recallMessage) {
							continue;
						}
						tmpMessages.push(recallMessage);
					}
					// 改造成一条提示消息
					recallMessage.status = this.$enums.MESSAGE_STATUS.PENDING;
					recallMessage.content = recallMessageTip;
					recallMessage.type = this.$enums.MESSAGE_TYPE.TIP_TEXT
					// 会话提示语
					conversation.lastContent = this.$msgUtil.previewContent(recallMessage);
					conversation.sendNickName = "";
				} else {
					// 会话列表内容
					conversation.lastContent = this.$msgUtil.previewContent(m);
					// 其他成员发的消息显示发送昵称
					conversation.sendNickName = m.selfSend ? '' : m.sendNickName;
				}
			}
			const conversations = Array.from(conversationMap.values());
			await this.$db.saveConversationAndMessage(conversations, messages.concat(tmpMessages));
			chatStore.append(conversations);
		},
		pullPrivateOfflineMessage(minId) {
			return this.$http({
				url: "/message/private/loadOfflineMessage?minId=" + minId,
				method: 'GET',
				timeout: 60000
			})
		},
		pullGroupOfflineMessage(minId) {
			return this.$http({
				url: "/message/group/loadOfflineMessage?minId=" + minId,
				method: 'GET',
				timeout: 60000
			})
		},
		async handlePrivateMessage(m) {
			// 标记这条消息是不是自己发的
			m.selfSend = m.sendId == userStore.userInfo.id;
			// 好友id
			const friendId = m.selfSend ? m.recvId : m.sendId;
			// 会话信息
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.PRIVATE, friendId);
			// 消息已读处理，清空已读数量
			if (m.type == this.$enums.MESSAGE_TYPE.READED) {
				await chatStore.resetUnreadCount(convKey)
				return;
			}
			// 消息回执处理,更新对方已读位置
			if (m.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
				const messageId = JSON.parse(m.content).id;
				await chatStore.readedMessage(convKey, messageId)
				return;
			}
			// 消息撤回
			if (m.type == this.$enums.MESSAGE_TYPE.RECALL) {
				await chatStore.recallMessage(convKey, m)
				return;
			}
			// 新增好友
			if (m.type == enums.MESSAGE_TYPE.FRIEND_NEW) {
				friendStore.addFriend(JSON.parse(m.content));
				return;
			}
			// 删除好友
			if (m.type == enums.MESSAGE_TYPE.FRIEND_DEL) {
				friendStore.removeFriend(friendId);
				return;
			}
			// 好友在线状态更新
			if (m.type == enums.MESSAGE_TYPE.FRIEND_ONLINE) {
				friendStore.updateOnlineStatus(JSON.parse(m.content));
				return;
			}
			// 对好友设置免打扰
			if (m.type == this.$enums.MESSAGE_TYPE.FRIEND_DND) {
				friendStore.setDnd(friendId, JSON.parse(m.content));
				await chatStore.setDnd(convKey, JSON.parse(m.content));
				return;
			}
			// 消息插入
			if (this.$msgType.isNormal(m.type) || this.$msgType.isTip(m.type) || this.$msgType.isAction(m.type)) {
				const friend = this.loadFriendInfo(friendId);
				await this.insertPrivateMessage(friend, m);
			}
		},
		async insertPrivateMessage(friend, m) {
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.PRIVATE, friend.id);
			const chatInfo = {
				type: this.$enums.CONVERSATION_TYPE.PRIVATE,
				targetId: friend.id,
				showName: friend.showNickName,
				headImage: friend.headImage,
				companyName: friend.companyName,
				isDnd: friend.isDnd,
				isTop: friend.isTop
			};
			// 打开会话
			await chatStore.openChat(chatInfo);
			// 插入消息
			await chatStore.insertMessage(convKey, m);
			// 通知chat-box组件
			if (chatStore.isActive(convKey)) {
				uni.$emit("newMessage", m);
			}
			if (!friend.isDnd && !chatStore.loading &&
				!m.selfSend && msgType.isNormal(m.type) && m.status != enums.MESSAGE_STATUS.READED) {
				// 播放提示音
				this.playAudioTip();
			}
		},
		async handleGroupMessage(m) {
			// 标记这条消息是不是自己发的
			m.selfSend = m.sendId == this.mine.id;
			// 会话信息
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, m.groupId);
			// 发送用户昵称优先显示好友备注的名字
			if (m.sendId && m.sendNickName) {
				const f = friendStore.findFriend(m.sendId);
				if (f && !f.deleted && f.remarkNickName) {
					m.sendNickName = f.remarkNickName;
				}
			}
			// 消息已读处理
			if (m.type == this.$enums.MESSAGE_TYPE.READED) {
				// 我已读对方的消息，清空已读数量
				await chatStore.resetUnreadCount(convKey)
				await chatStore.resetAtMessage(convKey)
				return;
			}
			// 消息撤回
			if (m.type == this.$enums.MESSAGE_TYPE.RECALL) {
				await chatStore.recallMessage(convKey, m)
				return;
			}
			// 新增群
			if (m.type == this.$enums.MESSAGE_TYPE.GROUP_NEW) {
				groupStore.addGroup(JSON.parse(m.content));
				return;
			}
			// 删除群
			if (m.type == this.$enums.MESSAGE_TYPE.GROUP_DEL) {
				groupStore.removeGroup(m.groupId);
				return;
			}
			// 对群设置免打扰
			if (m.type == this.$enums.MESSAGE_TYPE.GROUP_DND) {
				groupStore.setDnd(m.groupId, JSON.parse(m.content));
				chatStore.setDnd(chatInfo, JSON.parse(m.content));
				return;
			}
			// 插入群聊消息
			if (this.$msgType.isNormal(m.type) || this.$msgType.isTip(m.type) || this.$msgType.isAction(m.type)) {
				const group = this.loadGroupInfo(m.groupId);
				await this.insertGroupMessage(group, m);
			}
		},
		async insertGroupMessage(group, m) {
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, group.id);
			const chatInfo = {
				type: this.$enums.CONVERSATION_TYPE.GROUP,
				targetId: group.id,
				showName: group.showGroupName,
				headImage: group.headImageThumb,
				isDnd: group.isDnd,
				isTop: group.isTop
			};
			// 打开会话
			await chatStore.openChat(chatInfo);
			// 插入消息
			await chatStore.insertMessage(convKey, m);
			// 通知chat-box组件
			if (chatStore.isActive(convKey)) {
				uni.$emit("newMessage", m);
			}
			// 提示音和消息提醒
			if (!group.isDnd && !chatStore.loading &&
				!m.selfSend && this.$msgType.isNormal(m.type) &&
				m.status != this.$enums.MESSAGE_STATUS.READED) {
				// 播放提示音
				this.playAudioTip();
			}
		},
		handleSystemMessage(msg) {
			if (msg.type == enums.MESSAGE_TYPE.USER_BANNED) {
				// 用户被封禁
				wsApi.close(3099);
				uni.showModal({
					content: '您的账号已被管理员封禁，原因:' + msg.content,
					showCancel: false,
				})
				this.exit();
			}
		},
		async insertGroupMessage(group, m) {
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, group.id);
			const chatInfo = {
				type: this.$enums.CONVERSATION_TYPE.GROUP,
				targetId: group.id,
				showName: group.showGroupName,
				headImage: group.headImageThumb,
				isDnd: group.isDnd
			};
			// 打开会话
			await chatStore.openChat(chatInfo);
			// 插入消息
			await chatStore.insertMessage(convKey, m);
			// 通知chat-box组件
			if (chatStore.isActive(convKey)) {
				uni.$emit("newMessage", m);
			}
			// 提示音和消息提醒
			if (!group.isDnd && !chatStore.loading &&
				!m.selfSend && this.$msgType.isNormal(m.type) &&
				m.status != this.$enums.MESSAGE_STATUS.READED) {
				// 播放提示音
				this.playAudioTip();
			}
		},
		loadFriendInfo(id, callback) {
			let friend = friendStore.findFriend(id);
			if (!friend) {
				console.log("未知用户:", id)
				friend = {
					id: id,
					showNickName: "未知用户",
					headImage: ""
				}
			}
			return friend;
		},
		loadGroupInfo(id) {
			let group = groupStore.findGroup(id);
			if (!group) {
				group = {
					id: id,
					showGroupName: "未知群聊",
					headImageThumb: ""
				}
			}
			return group;
		},
		exit() {
			console.log("exit");
			this.isExit = true;
			wsApi.close(3099);
			uni.removeStorageSync("loginInfo");
			uni.reLaunch({
				url: "/pages/login/login"
			})
			this.unloadStore();
		},
		playAudioTip() {
			// 播放间隔必须大于1s
			const interval = new Date().getTime() - this.lastPlayAudioTime;
			if (userStore.userInfo.isAudioTip && interval > 1000) {
				if (!this.audioTip) {
					this.audioTip = uni.createInnerAudioContext();
					this.audioTip.src = "/static/audio/tip.mp3";
				}
				this.lastPlayAudioTime = new Date().getTime();
				this.audioTip.play();
			}
		},
		refreshToken(loginInfo) {
			return new Promise((resolve, reject) => {
				if (!loginInfo || !loginInfo.refreshToken) {
					reject();
					return;
				}
				http({
					url: '/refreshToken',
					method: 'PUT',
					header: {
						refreshToken: loginInfo.refreshToken
					}
				}).then((newLoginInfo) => {
					uni.setStorageSync("loginInfo", newLoginInfo)
					resolve()
				}).catch((e) => {
					reject(e)
				})
			})
		},
		reconnectWs() {
			// 已退出则不再重连
			if (this.isExit) {
				return;
			}
			// 记录标志
			this.reconnecting = true;
			// 重新加载一次个人信息，目的是为了保证网络已经正常且token有效
			userStore.loadUser().then((userInfo) => {
				uni.showToast({
					title: '连接已断开，尝试重新连接...',
					icon: 'none'
				})
				// 重新连接
				let loginInfo = uni.getStorageSync("loginInfo")
				wsApi.reconnect(UNI_APP.WS_URL, loginInfo.accessToken);
			}).catch(() => {
				// 5s后重试
				setTimeout(() => {
					this.reconnectWs();
				}, 5000)
			})
		},
		onReconnectWs() {
			this.reconnecting = false;
			// 重新加载好友和群聊
			const promises = [];
			promises.push(friendStore.loadFriend());
			promises.push(groupStore.loadGroup());
			Promise.all(promises).then(() => {
				uni.showToast({
					title: "已重新连接",
					icon: 'none'
				})
				// 加载离线消息
				this.pullOfflineMessage();
			}).catch((e) => {
				console.log(e);
				this.exit();
			})
		},
		closeSplashscreen(delay) {
			// #ifdef APP-PLUS
			// 关闭开机动画
			setTimeout(() => {
				plus.navigator.closeSplashscreen()
			}, delay)
			// #endif
		}
	},
	computed: {
		mine() {
			return userStore.userInfo;
		}
	},
	async onLaunch() {
		await this.$mountDb();
		// 延迟1s，避免用户看到页面跳转
		this.closeSplashscreen(1000);
		// 登录状态校验
		let loginInfo = uni.getStorageSync("loginInfo")
		this.refreshToken(loginInfo).then(() => {
			// #ifdef H5
			// 跳转到聊天页
			uni.switchTab({
				url: "/pages/chat/chat"
			})
			// #endif			
			// 初始化
			this.init();
			this.closeSplashscreen(0);
		}).catch(() => {
			// 跳转到登录页
			uni.navigateTo({
				url: "/pages/login/login"
			})
		})
	}
}
</script>

<style lang="scss">
@import "@/uni_modules/uview-plus/index.scss";
@import "@/im.scss";
@import url('./static/icon/iconfont.css');

page {
	background: $im-bg-linear;
}

.tab-page {
	position: relative; 
	display: flex;
	flex-direction: column;
	// #ifdef H5
	height: calc(100vh - 50px - $im-nav-bar-height); // h5平台100vh是包含了底部高度，需要减去
	top: $im-nav-bar-height;
	// #endif
	// #ifdef APP
	height: calc(100vh - var(--status-bar-height) - $im-nav-bar-height); // app平台还要减去顶部手机状态栏高度
	top: calc($im-nav-bar-height + var(--status-bar-height));
	// #endif
	// #ifdef MP-WEIXIN
	height: calc(100vh - $im-nav-bar-height);
	top: $im-nav-bar-height;
	// #endif
	color: $im-text-color;
	background: $im-bg-linear;
	font-size: $im-font-size;
	font-family: $font-family;
}

.page {
	position: relative;
	// #ifdef H5
	height: calc(100vh - $im-nav-bar-height); // h5平台100vh是包含了底部高度，需要减去
	top: $im-nav-bar-height;
	// #endif
	// #ifdef APP
	height: calc(100vh - var(--status-bar-height) - $im-nav-bar-height); // app平台还要减去顶部手机状态栏高度
	top: calc($im-nav-bar-height + var(--status-bar-height));
	// #endif
	// #ifdef MP-WEIXIN
	height: calc(100vh - $im-nav-bar-height);
	top: $im-nav-bar-height;
	// #endif
	color: $im-text-color;
	background: $im-bg-linear;
	font-size: $im-font-size;
	font-family: $font-family;
}

</style>
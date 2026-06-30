<template>
	<div class="home-page" @click="closeUserInfo">
		<div class="app-container" :class="{ fullscreen: configStore.fullScreen }">
			<div class="navi-bar">
				<div class="navi-bar-box">
					<div class="top">
						<div class="user-head-image">
							<head-image :name="userStore.userInfo.nickName" :size="38"
								:url="userStore.userInfo.headImageThumb" @click.native="showSettingDialog = true">
							</head-image>
						</div>
						<div class="menu">
							<router-link class="link" v-bind:to="'/home/chat'">
								<div class="menu-item">
									<span class="icon iconfont icon-chat"></span>
									<div v-show="unreadCount > 0" class="unread-text">{{ unreadCount }}</div>
								</div>
							</router-link>
							<router-link class="link" v-bind:to="'/home/friend'">
								<div class="menu-item">
									<span class="icon iconfont icon-friend"></span>
								</div>
							</router-link>
							<router-link class="link" v-bind:to="'/home/group'">
								<div class="menu-item">
									<span class="icon iconfont icon-group" style="font-size: 28px"></span>
								</div>
							</router-link>
						</div>
					</div>

					<div class="botoom">
						<div class="bottom-item" @click="onSwtichFullScreen">
							<i class="el-icon-full-screen"></i>
						</div>
						<div class="bottom-item" @click="showSetting">
							<span class="icon iconfont icon-setting" style="font-size: 20px"></span>
						</div>
						<div class="bottom-item" @click="onExit()" title="退出">
							<span class="icon iconfont icon-exit"></span>
						</div>
					</div>
				</div>
			</div>
			<div class="content-box">
				<router-view></router-view>
			</div>
			<setting :visible="showSettingDialog" @close="closeSetting()"></setting>
			<user-info ref="userInfo"></user-info>
			<full-image ref="fullImage"></full-image>
			<rtc-private-video ref="rtcPrivateVideo"></rtc-private-video>
			<rtc-group-video ref="rtcGroupVideo"></rtc-group-video>
		</div>
	</div>
</template>

<script>
import HeadImage from '../components/common/HeadImage.vue';
import Setting from '../components/setting/Setting.vue';
import UserInfo from '../components/common/UserInfo.vue';
import FullImage from '../components/common/FullImage.vue';
import RtcPrivateVideo from '../components/rtc/RtcPrivateVideo.vue';
import RtcPrivateAcceptor from '../components/rtc/RtcPrivateAcceptor.vue';
import RtcGroupVideo from '../components/rtc/RtcGroupVideo.vue';
import { clearLoginSession } from '../api/auth.js';

export default {
	components: {
		HeadImage,
		Setting,
		UserInfo,
		FullImage,
		RtcPrivateVideo,
		RtcPrivateAcceptor,
		RtcGroupVideo
	},
	data() {
		return {
			showSettingDialog: false,
			lastPlayAudioTime: new Date().getTime() - 1000,
			reconnecting: false,
			privateMessagesBuffer: [],
			groupMessagesBuffer: []
		}
	},
	methods: {
		init() {
			this.$eventBus.$on('openPrivateVideo', (rctInfo) => {
				// 进入单人视频通话
				this.$refs.rtcPrivateVideo.open(rctInfo);
			});
			this.$eventBus.$on('openGroupVideo', (rctInfo) => {
				// 进入多人视频通话
				this.$refs.rtcGroupVideo.open(rctInfo);
			});
			this.$eventBus.$on('openUserInfo', (user, pos) => {
				// 打开用户卡片
				console.log("this.$refs.userInfo:",this.$refs.userInfo)
				this.$refs.userInfo.open(user, pos);
			});
			this.$eventBus.$on('openFullImage', url => {
				// 图片全屏
				this.$refs.fullImage.open(url);
			});
			this.loadStore().then(() => {
				// ws初始化
				this.$wsApi.connect(process.env.VUE_APP_WS_URL, sessionStorage.getItem("accessToken"));
				this.$wsApi.onConnect(() => {
					if (this.reconnecting) {
						this.onReconnectWs();
					} else {
						// 加载离线消息
						this.pullOfflineMessage();
					}
				});
				this.$wsApi.onMessage((cmd, message) => {
					if (cmd == 2) {
						// 关闭ws
						this.$wsApi.close(3000)
						// 异地登录，强制下线
						this.$alert("您已在其他地方登录，将被强制下线", "强制下线通知", {
							confirmButtonText: '确定',
							callback: action => {
								location.href = "/";
							}
						});
					} else if (cmd == 3) {
						if (this.chatStore.loading) {
							// 如果正在拉取离线消息，先放进缓存区，等待消息拉取完成再处理，防止消息乱序
							this.privateMessagesBuffer.push(message);
						} else {
							// 插入私聊消息
							this.handlePrivateMessage(message);
						}
					} else if (cmd == 4) {
						if (this.chatStore.loading) {
							// 如果正在拉取离线消息，先放进缓存区，等待消息拉取完成再处理，防止消息乱序
							this.groupMessagesBuffer.push(message);
						} else {
							// 插入群聊消息
							this.handleGroupMessage(message);
						}
					} else if (cmd == 5) {
						// 处理系统消息
						this.handleSystemMessage(message);
					}
				});
				this.$wsApi.onClose((e) => {
					if (e.code != 3000) {
						// 断线重连
						if (!this.reconnecting) {
							this.reconnectWs();
						}
					}
				});
			}).catch((e) => {
				console.log("初始化失败", e);
			})
		},
		reconnectWs() {
			// 记录标志
			this.reconnecting = true;
			// 重新加载一次个人信息，目的是为了保证网络已经正常且token有效
			this.userStore.loadUser().then(() => {
				// 断线重连
				this.$message.error("连接断开，正在尝试重新连接...");
				this.$wsApi.reconnect(process.env.VUE_APP_WS_URL, sessionStorage.getItem(
					"accessToken"));
			}).catch(() => {
				// 10s后重试
				setTimeout(() => this.reconnectWs(), 10000)
			})
		},
		onReconnectWs() {
			// 重连成功
			this.reconnecting = false;
			// 重新加载群和好友
			const promises = [];
			promises.push(this.friendStore.loadFriend());
			promises.push(this.groupStore.loadGroup());
			Promise.all(promises).then(() => {
				// 加载离线消息
				this.pullOfflineMessage();
				this.$message.success("重新连接成功");
			}).catch(() => {
				this.$message.error("初始化失败");
				this.onExit();
			})
		},
		async loadStore() {
			await this.userStore.loadUser();
			await this.$db.open(this.userStore.userInfo.id);
			// 加载好友要在加载会话前面，否则好友在线状态不显示
			await this.friendStore.loadFriend();
			const promises = [];
			promises.push(this.groupStore.loadGroup());
			promises.push(this.configStore.loadConfig());
			promises.push(this.chatStore.loadConversations());
			return Promise.all(promises);
		},
		unloadStore() {
			this.friendStore.clear();
			this.groupStore.clear();
			this.chatStore.clear();
			this.userStore.clear();
		},
		pullOfflineMessage() {
			let timeStamp = new Date().getTime();
			this.chatStore.setLoading(true);
			const promises = [];
			const maxPrivateMessageId = this.chatStore.findMaxMessageId(this.$enums.CONVERSATION_TYPE.PRIVATE);
			const maxGroupMessageId = this.chatStore.findMaxMessageId(this.$enums.CONVERSATION_TYPE.GROUP);
			promises.push(this.pullPrivateOfflineMessage(maxPrivateMessageId));
			promises.push(this.pullGroupOfflineMessage(maxGroupMessageId));
			Promise.all(promises).then(async (messages) => {
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
				this.chatStore.setLoading(false);
				// 打印耗时
				let size = messages[0].length + messages[1].length;
				let time = new Date().getTime() - timeStamp;
				console.log("加载离线消息耗时:", time, ",消息数量:", size)
			}).catch((e) => {
				console.log(e)
				this.$message.error("拉取离线消息失败");
				this.onExit();
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
				conversation.maxMessageId = Math.max(conversation.maxMessageId, m.id)
				conversation.maxSeqNo = Math.max(conversation.maxSeqNo, m.seqNo)
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
					// 把原消息改造成一条提示消息
					recallMessage.status = this.$enums.MESSAGE_STATUS.PENDING;
					recallMessage.content = recallMessageTip;
					recallMessage.type = this.$enums.MESSAGE_TYPE.TIP_TEXT
					// 会话提示语
					conversation.lastContent = this.$msgUtil.previewContent(recallMessage);
					conversation.sendNickName = "";
				} else {
					// 会话列表内容
					conversation.lastContent = this.$msgUtil.previewContent(m);
				}
			}
			// 批量保存会话和消息
			const conversations = Array.from(conversationMap.values());
			await this.$db.saveConversationAndMessage(conversations, messages.concat(tmpMessages));
			this.chatStore.append(conversations);
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
							isTop: false,
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
				conversation.maxSeqNo = Math.max(conversation.maxSeqNo, m.seqNo)
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
			this.chatStore.append(conversations);
		},
		pullPrivateOfflineMessage(minId) {
			return this.$http({
				url: "/message/private/loadOfflineMessage?minId=" + minId
			})
		},
		pullGroupOfflineMessage(minId) {
			return this.$http({
				url: "/message/group/loadOfflineMessage?minId=" + minId
			})
		},
		async handlePrivateMessage(m) {
			// 标记这条消息是不是自己发的
			m.selfSend = m.sendId == this.mine.id;
			// 好友id
			const friendId = m.selfSend ? m.recvId : m.sendId;
			// 会话信息
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.PRIVATE, friendId);
			// 消息已读处理，清空已读数量
			if (m.type == this.$enums.MESSAGE_TYPE.READED) {
				await this.chatStore.resetUnreadCount(convKey)
				return;
			}
			// 消息回执处理,更新对方已读位置
			if (m.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
				const messageId = JSON.parse(m.content).id;
				await this.chatStore.readedMessage(convKey, messageId)
				return;
			}
			// 消息撤回
			if (m.type == this.$enums.MESSAGE_TYPE.RECALL) {
				await this.chatStore.recallMessage(convKey, m)
				return;
			}
			if (m.type == this.$enums.MESSAGE_TYPE.FRIEND_NEW) {
				this.friendStore.addFriend(JSON.parse(m.content));
				return;
			}
			// 删除好友
			if (m.type == this.$enums.MESSAGE_TYPE.FRIEND_DEL) {
				this.friendStore.removeFriend(friendId);
				return;
			}
			// 好友在线状态
			if (m.type == this.$enums.MESSAGE_TYPE.FRIEND_ONLINE) {
				this.friendStore.updateOnlineStatus(JSON.parse(m.content));
				return;
			}
			// 对好友设置免打扰
			if (m.type == this.$enums.MESSAGE_TYPE.FRIEND_DND) {
				this.friendStore.setDnd(friendId, JSON.parse(m.content));
				await this.chatStore.setDnd(convKey, JSON.parse(m.content));
				return;
			}
			// 单人webrtc 信令
			if (this.$msgType.isRtcPrivate(m.type)) {
				this.$refs.rtcPrivateVideo.onRTCMessage(m)
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
				showName: friend.nickName,
				headImage: friend.headImage,
				isDnd: friend.isDnd
			};
			// 打开会话
			await this.chatStore.openChat(chatInfo);
			// 插入消息
			await this.chatStore.insertMessage(convKey, m);
			// 通知chat-box组件
			if (this.chatStore.isActive(convKey)) {
				this.$eventBus.$emit("newMessage", m);
			}
			// 播放提示音
			if (!friend.isDnd && !m.selfSend && this.$msgType.isNormal(m.type)
				&& m.status != this.$enums.MESSAGE_STATUS.READED) {
				this.playAudioTip();
			}
		},
		async handleGroupMessage(m) {
			console.log("handleGroupMessage:", m)
			// 标记这条消息是不是自己发的
			m.selfSend = m.sendId == this.mine.id;
			// 会话信息
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, m.groupId);
			// 发送用户昵称优先显示好友备注的名字
			if (m.sendId && m.sendNickName) {
				const f = this.friendStore.findFriend(m.sendId);
				if (f && !f.deleted && f.remarkNickName) {
					m.sendNickName = f.remarkNickName;
				}
			}
			// 消息已读处理
			if (m.type == this.$enums.MESSAGE_TYPE.READED) {
				// 我已读对方的消息，清空已读数量
				await this.chatStore.resetUnreadCount(convKey)
				await this.chatStore.resetAtMessage(convKey)
				return;
			}
			// 消息回执处理
			if (m.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
				// 更新消息已读人数
				const message = {
					localId: m.localId,
					readedCount: m.readedCount,
					receiptOk: m.receiptOk
				};
				await this.chatStore.updateMessage(convKey, message)
				return;
			}
			// 消息撤回
			if (m.type == this.$enums.MESSAGE_TYPE.RECALL) {
				await this.chatStore.recallMessage(convKey, m)
				return;
			}
			// 新增群
			if (m.type == this.$enums.MESSAGE_TYPE.GROUP_NEW) {
				this.groupStore.addGroup(JSON.parse(m.content));
				return;
			}
			// 删除群
			if (m.type == this.$enums.MESSAGE_TYPE.GROUP_DEL) {
				this.groupStore.removeGroup(m.groupId);
				return;
			}
			// 对群设置免打扰
			if (m.type == this.$enums.MESSAGE_TYPE.GROUP_DND) {
				this.groupStore.setDnd(m.groupId, JSON.parse(m.content));
				await this.chatStore.setDnd(convKey, JSON.parse(m.content));
				return;
			}
			// 群视频信令
			if (this.$msgType.isRtcGroup(m.type)) {
				this.$nextTick(() => {
					this.$refs.rtcGroupVideo.onRTCMessage(m);
				})
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
				isDnd: group.isDnd
			};
			// 打开会话
			await this.chatStore.openChat(chatInfo);
			// 插入消息
			await this.chatStore.insertMessage(convKey, m);
			// 通知chat-box组件
			if (this.chatStore.isActive(convKey)) {
				this.$eventBus.$emit("newMessage", m);
			}
			// 提示音和消息提醒
			if (!group.isDnd && !m.selfSend && !this.chatStore.loading && this.$msgType.isNormal(m.type)
				&& m.status != this.$enums.MESSAGE_STATUS.READED) {
				// 播放提示音
				this.playAudioTip();
			}
		},
		handleSystemMessage(msg) {
			// 用户被封禁
			if (msg.type == this.$enums.MESSAGE_TYPE.USER_BANNED) {
				this.$wsApi.close(3000);
				this.$alert("您的账号已被管理员封禁,原因:" + msg.content, "账号被封禁", {
					confirmButtonText: '确定',
					callback: () => {
						this.onExit();
					}
				});
				return;
			}
		},
		closeUserInfo() {
			this.$refs.userInfo.close();
		},
		onSwtichFullScreen() {
			this.configStore.setFullScreen(!this.configStore.fullScreen);
		},
		onExit() {
			console.log("onExit")
			this.unloadStore();
			this.$wsApi.close(3000);
			clearLoginSession(true);
			this.$elm.setTitleTip("");
			this.$db.close();
			location.href = "/";
		},
		playAudioTip() {
			// 防止过于密集播放
			if (new Date().getTime() - this.lastPlayAudioTime > 1000) {
				this.lastPlayAudioTime = new Date().getTime();
				let audio = new Audio();
				let url = require(`@/assets/audio/tip.mp3`);
				audio.src = url;
				audio.play();
			}

		},
		showSetting() {
			this.showSettingDialog = true;
		},
		closeSetting() {
			this.showSettingDialog = false;
		},
		loadFriendInfo(id) {
			let friend = this.friendStore.findFriend(id);
			if (!friend) {
				friend = {
					id: id,
					nickName: "未知用户",
					headImage: ""
				}
			}
			return friend;
		},
		loadGroupInfo(id) {
			let group = this.groupStore.findGroup(id);
			if (!group) {
				group = {
					id: id,
					showGroupName: "未知群聊",
					headImageThumb: ""
				}
			}
			return group;
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		unreadCount() {
			let unreadCount = 0;
			const conversations = this.chatStore.conversations;
			conversations.forEach((conv) => {
				if (!conv.isDnd) {
					unreadCount += conv.unreadCount
				}
			})
			return unreadCount;
		},
	},
	watch: {
		unreadCount: {
			handler(newCount, oldCount) {
				let tip = newCount > 0 ? `${newCount}条未读` : "";
				this.$elm.setTitleTip(tip);
			},
			immediate: true
		}
	},
	mounted() {
		this.init();
	},
	unmounted() {
		this.$wsApi.close();
	}
}
</script>

<style scoped lang="scss">
.home-page {
	height: 100vh;
	width: 100vw;
	display: flex;
	justify-content: center;
	align-items: center;
	border-radius: 4px;
	overflow: hidden;
	background: var(--im-color-primary-light-9);

	.app-container {
		width: 62vw;
		height: 80vh;
		display: flex;
		min-height: 600px;
		min-width: 970px;
		position: absolute;
		border-radius: 4px;
		overflow: hidden;
		box-shadow: var(--im-box-shadow-dark);
		transition: 0.2s;

		&.fullscreen {
			transition: 0.2s;
			width: 100vw;
			height: 100vh;
		}
	}

	.navi-bar {
		--icon-font-size: 22px;
		--width: 70px;
		width: var(--width);
		background: linear-gradient(180deg, var(--im-color-primary-light-1) 0%, var(--im-color-primary-light-2) 100%);
		padding-top: 25px;
		position: relative;
		box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);

		.navi-bar-box {
			height: 100%;
			display: flex;
			flex-direction: column;
			justify-content: space-between;

			.botoom {
				margin-bottom: 25px;
			}
		}

		.user-head-image {
			display: flex;
			justify-content: center;
			margin-bottom: 10px;

			// 头像容器样式，参考新版本
			:deep(.head-image) {
				border: 3px solid rgba(255, 255, 255, 0.2);
				border-radius: 50%;
				box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
				transition: all 0.3s ease;

				&:hover {
					border-color: rgba(255, 255, 255, 0.4);
					transform: scale(1.05);
					box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
				}
			}
		}

		.menu {
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-content: center;
			flex-wrap: wrap;
			margin-top: 25px;
			gap: 8px;

			.link {
				text-decoration: none;
				display: flex;
				justify-content: center;
			}

			.router-link-active .menu-item {
				color: white;
				background: linear-gradient(135deg,
						var(--im-color-primary-light-2) 0%,
						var(--im-color-primary) 100%);
				box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
				transform: translateX(2px);

				&::before {
					opacity: 1;
					transform: scale(1);
				}
			}

			.link:not(.router-link-active) .menu-item:hover {
				background: linear-gradient(135deg,
						var(--im-color-primary) 0%,
						var(--im-color-primary-light-2) 100%);
				transform: scale(1.08) translateX(2px);
				box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
				color: white;
			}

			.menu-item {
				position: relative;
				color: rgba(255, 255, 255, 0.8);
				width: 50px;
				height: 50px;
				display: flex;
				justify-content: center;
				align-items: center;
				margin-top: 10px;
				border-radius: 12px;
				transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
				cursor: pointer;

				// 左侧选中指示条
				&::before {
					content: '';
					position: absolute;
					left: -5px;
					width: 3px;
					height: 20px;
					background: white;
					border-radius: 2px;
					opacity: 0;
					transition: all 0.3s ease;
				}

				.icon {
					font-size: var(--icon-font-size);
					transition: all 0.3s ease;
				}

				.unread-text {
					position: absolute;
					background: var(--im-color-danger);
					left: 32px;
					top: 3px;
					color: white;
					border-radius: 10px;
					padding: 1px 6px;
					font-size: 10px;
					font-weight: 600;
					text-align: center;
					white-space: nowrap;
					border: 1px solid rgba(255, 255, 255, 0.9);
					box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
					min-width: 16px;
					height: 16px;
					display: flex;
					align-items: center;
					justify-content: center;
					z-index: 1;
				}
			}
		}

		.bottom-item {
			display: flex;
			justify-content: center;
			align-items: center;
			height: 45px;
			width: 100%;
			cursor: pointer;
			color: rgba(255, 255, 255, 0.7);
			font-size: var(--icon-font-size);
			border-radius: 8px;
			margin: 4px 0;
			transition: all 0.3s ease;
			position: relative;

			.icon {
				font-size: var(--icon-font-size);
				transition: all 0.3s ease;
			}

			&:hover {
				color: white;
				background: rgba(255, 255, 255, 0.1);
				transform: scale(1.05);

				.icon {
					transform: scale(1.1);
				}
			}

			&:active {
				transform: scale(0.95);
			}
		}
	}

	.content-box {
		flex: 1;
		padding: 0;
		background-color: #fff;
		text-align: center;
	}
}
</style>
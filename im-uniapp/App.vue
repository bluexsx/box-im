<script>
import App from './App'
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
			reconnecting: false // 正在重连标志
		}
	},
	methods: {
		init() {
			this.isExit = false;
			// 加载数据
			this.loadStore().then(() => {
				// 初始化websocket
				this.initWebSocket();
			}).catch((e) => {
				console.log(e);
				this.exit();
			})
		},
		initWebSocket() {
			let loginInfo = uni.getStorageSync("loginInfo")
			wsApi.init();
			wsApi.connect(UNI_APP.WS_URL, loginInfo.accessToken);
			wsApi.onConnect(() => {
				// 重连成功提示
				if (this.reconnecting) {
					this.reconnecting = false;
					uni.showToast({
						title: "已重新连接",
						icon: 'none'
					})
				}
				// 加载离线消息
				this.pullPrivateOfflineMessage(this.chatStore.privateMsgMaxId);
				this.pullGroupOfflineMessage(this.chatStore.groupMsgMaxId);
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
					// 私聊消息
					this.handlePrivateMessage(msgInfo);
				} else if (cmd == 4) {
					// 群聊消息
					this.handleGroupMessage(msgInfo);
				} else if (cmd == 5) {
					// 系统消息
					this.handleSystemMessage(msgInfo);
				}
			});
			wsApi.onClose((res) => {
				console.log("ws断开", res);
				// 重新连接
				this.reconnectWs();

			})
		},
		loadStore() {
			return this.userStore.loadUser().then(() => {
				const promises = [];
				promises.push(this.friendStore.loadFriend());
				promises.push(this.groupStore.loadGroup());
				promises.push(this.chatStore.loadChat());
				promises.push(this.configStore.loadConfig());
				return Promise.all(promises);
			})
		},
		unloadStore() {
			this.friendStore.clear();
			this.groupStore.clear();
			this.chatStore.clear();
			this.configStore.clear();
			this.userStore.clear();
		},
		pullPrivateOfflineMessage(minId) {
			this.chatStore.setLoadingPrivateMsg(true)
			http({
				url: "/message/private/pullOfflineMessage?minId=" + minId,
				method: 'GET'
			}).catch(() => {
				this.chatStore.setLoadingPrivateMsg(false)
			})
		},
		pullGroupOfflineMessage(minId) {
			this.chatStore.setLoadingGroupMsg(true)
			http({
				url: "/message/group/pullOfflineMessage?minId=" + minId,
				method: 'GET'
			}).catch(() => {
				this.chatStore.setLoadingGroupMsg(false)
			})
		},
		handlePrivateMessage(msg) {
			// 消息加载标志
			if (msg.type == enums.MESSAGE_TYPE.LOADING) {
				this.chatStore.setLoadingPrivateMsg(JSON.parse(msg.content))
				return;
			}
			// 消息已读处理，清空已读数量
			if (msg.type == enums.MESSAGE_TYPE.READED) {
				this.chatStore.resetUnreadCount({
					type: 'PRIVATE',
					targetId: msg.recvId
				})
				return;
			}
			// 消息回执处理,改消息状态为已读
			if (msg.type == enums.MESSAGE_TYPE.RECEIPT) {
				this.chatStore.readedMessage({
					friendId: msg.sendId
				})
				return;
			}
			// 标记这条消息是不是自己发的
			msg.selfSend = msg.sendId == this.userStore.userInfo.id;
			// 好友id
			let friendId = msg.selfSend ? msg.recvId : msg.sendId;
			this.loadFriendInfo(friendId, (friend) => {
				this.insertPrivateMessage(friend, msg);
			})
		},
		insertPrivateMessage(friend, msg) {
			// 单人视频信令
			if (msgType.isRtcPrivate(msg.type)) {
				// #ifdef MP-WEIXIN
				// 小程序不支持音视频
				return;
				// #endif
				// 被呼叫，弹出视频页面
				let delayTime = 100;
				if (msg.type == enums.MESSAGE_TYPE.RTC_CALL_VOICE ||
					msg.type == enums.MESSAGE_TYPE.RTC_CALL_VIDEO) {
					let mode = msg.type == enums.MESSAGE_TYPE.RTC_CALL_VIDEO ? "video" : "voice";
					let pages = getCurrentPages();
					let curPage = pages[pages.length - 1].route;
					if (curPage != "pages/chat/chat-private-video") {
						const friendInfo = encodeURIComponent(JSON.stringify(friend));
						uni.navigateTo({
							url: `/pages/chat/chat-private-video?mode=${mode}&friend=${friendInfo}&isHost=false`
						})
						delayTime = 500;
					}
				}
				setTimeout(() => {
					uni.$emit('WS_RTC_PRIVATE', msg);
				}, delayTime)
				return;
			}
			let chatInfo = {
				type: 'PRIVATE',
				targetId: friend.id,
				showName: friend.nickName,
				headImage: friend.headImage
			};
			// 打开会话
			this.chatStore.openChat(chatInfo);
			// 插入消息
			this.chatStore.insertMessage(msg);
			// 播放提示音
			this.playAudioTip();

		},
		handleGroupMessage(msg) {
			// 消息加载标志
			if (msg.type == enums.MESSAGE_TYPE.LOADING) {
				this.chatStore.setLoadingGroupMsg(JSON.parse(msg.content))
				return;
			}
			// 消息已读处理
			if (msg.type == enums.MESSAGE_TYPE.READED) {
				// 我已读对方的消息，清空已读数量
				let chatInfo = {
					type: 'GROUP',
					targetId: msg.groupId
				}
				this.chatStore.resetUnreadCount(chatInfo)
				return;
			}
			// 消息回执处理
			if (msg.type == enums.MESSAGE_TYPE.RECEIPT) {
				// 更新消息已读人数
				let msgInfo = {
					id: msg.id,
					groupId: msg.groupId,
					readedCount: msg.readedCount,
					receiptOk: msg.receiptOk
				};
				this.chatStore.updateMessage(msgInfo)
				return;
			}
			// 标记这条消息是不是自己发的
			msg.selfSend = msg.sendId == this.userStore.userInfo.id;
			this.loadGroupInfo(msg.groupId, (group) => {
				// 插入群聊消息
				this.insertGroupMessage(group, msg);
			})
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
		insertGroupMessage(group, msg) {
			// 群视频信令
			if (msgType.isRtcGroup(msg.type)) {
				// #ifdef MP-WEIXIN
				// 小程序不支持音视频
				return;
				// #endif
				// 被呼叫，弹出视频页面
				let delayTime = 100;
				if (msg.type == enums.MESSAGE_TYPE.RTC_GROUP_SETUP) {
					let pages = getCurrentPages();
					let curPage = pages[pages.length - 1].route;
					if (curPage != "pages/chat/chat-group-video") {
						const userInfos = encodeURIComponent(msg.content);
						const inviterId = msg.sendId;
						const groupId = msg.groupId
						uni.navigateTo({
							url: `/pages/chat/chat-group-video?groupId=${groupId}&isHost=false
									&inviterId=${inviterId}&userInfos=${userInfos}`
						})
						delayTime = 500;
					}
				}
				// 消息转发到chat-group-video页面进行处理
				setTimeout(() => {
					uni.$emit('WS_RTC_GROUP', msg);
				}, delayTime)
				return;
			}

			let chatInfo = {
				type: 'GROUP',
				targetId: group.id,
				showName: group.showGroupName,
				headImage: group.headImageThumb
			};
			// 打开会话
			this.chatStore.openChat(chatInfo);
			// 插入消息
			this.chatStore.insertMessage(msg);
			// 播放提示音
			this.playAudioTip();
		},
		loadFriendInfo(id, callback) {
			let friend = this.friendStore.findFriend(id);
			if (friend) {
				callback(friend);
			} else {
				http({
					url: `/friend/find/${id}`,
					method: 'GET'
				}).then((friend) => {
					this.friendStore.addFriend(friend);
					callback(friend)
				})
			}
		},
		loadGroupInfo(id, callback) {
			let group = this.groupStore.findGroup(id);
			if (group) {
				callback(group);
			} else {
				http({
					url: `/group/find/${id}`,
					method: 'GET'
				}).then((group) => {
					this.groupStore.addGroup(group);
					callback(group)
				})
			}
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
			// 音频播放无法成功
			// this.audioTip = uni.createInnerAudioContext();
			// this.audioTip.src =  "/static/audio/tip.wav";
			// this.audioTip.play();
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
			this.reloadUserInfo().then((userInfo) => {
				uni.showToast({
					title: '连接已断开，尝试重新连接...',
					icon: 'none',
				})
				this.userStore.setUserInfo(userInfo);
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
		reloadUserInfo() {
			return http({
				url: '/user/self',
				method: 'GET'
			})
		}
	},
	onLaunch() {
		this.$mountStore();
		// 登录状态校验
		let loginInfo = uni.getStorageSync("loginInfo")
		this.refreshToken(loginInfo).then(() => {
			// 初始化
			this.init();
			// 跳转到聊天页面
			uni.switchTab({
				url: "/pages/chat/chat"
			})
		}).catch(() => {
			// 跳转到登录页
			// #ifdef H5
			uni.navigateTo({
				url: "/pages/login/login"
			})
			// #endif
		})
	}
}
</script>

<style lang="scss">
@import "@/uni_modules/uview-plus/index.scss";
@import "@/im.scss";
@import url('./static/icon/iconfont.css');

// #ifdef H5 
uni-page-head {
	display: none; // h5浏览器本身就有标题
}

// #endif

.tab-page {
	position: relative;
	display: flex;
	flex-direction: column;
	// #ifdef H5
	height: calc(100vh - 50px - $im-nav-bar-height); // h5平台100vh是包含了底部高度，需要减去
	top: $im-nav-bar-height;
	// #endif

	// #ifndef H5
	height: calc(100vh - var(--status-bar-height) - $im-nav-bar-height); // app平台还要减去顶部手机状态栏高度
	top: calc($im-nav-bar-height + var(--status-bar-height));
	// #endif
	color: $im-text-color;
	background-color: $im-bg;
	font-size: $im-font-size;
	font-family: $font-family;
}

.page {
	position: relative;
	// #ifdef H5
	height: calc(100vh - $im-nav-bar-height); // app平台还要减去顶部手机状态栏高度
	top: $im-nav-bar-height;
	// #endif
	// #ifndef H5
	height: calc(100vh - var(--status-bar-height) - $im-nav-bar-height); // app平台还要减去顶部手机状态栏高度
	top: calc($im-nav-bar-height + var(--status-bar-height));
	// #endif
	color: $im-text-color;
	background-color: $im-bg;
	font-size: $im-font-size;
	font-family: $font-family;
}
</style>
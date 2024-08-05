<script>
	import store from './store';
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
				store.dispatch("load").then(() => {
					// 初始化websocket
					this.initWebSocket();
					this.initUniPush();
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
					this.pullPrivateOfflineMessage(store.state.chatStore.privateMsgMaxId);
					this.pullGroupOfflineMessage(store.state.chatStore.groupMsgMaxId);
				});
				wsApi.onMessage((cmd, msgInfo) => {
					if (cmd == 2) {
						// 异地登录，强制下线
						uni.showModal({
							content: '您已在其他地方登陆，将被强制下线',
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
			pullPrivateOfflineMessage(minId) {
				store.commit("loadingPrivateMsg", true)
				http({
					url: "/message/private/pullOfflineMessage?minId=" + minId,
					method: 'GET'
				}).catch(() => {
					store.commit("loadingPrivateMsg", false)
				})
			},
			pullGroupOfflineMessage(minId) {
				store.commit("loadingGroupMsg", true)
				http({
					url: "/message/group/pullOfflineMessage?minId=" + minId,
					method: 'GET'
				}).catch(() => {
					store.commit("loadingGroupMsg", false)
				})
			},
			handlePrivateMessage(msg) {
				// 消息加载标志
				if (msg.type == enums.MESSAGE_TYPE.LOADING) {
					store.commit("loadingPrivateMsg", JSON.parse(msg.content))
					return;
				}
				// 消息已读处理，清空已读数量
				if (msg.type == enums.MESSAGE_TYPE.READED) {
					store.commit("resetUnreadCount", {
						type: 'PRIVATE',
						targetId: msg.recvId
					})
					return;
				}
				// 消息回执处理,改消息状态为已读
				if (msg.type == enums.MESSAGE_TYPE.RECEIPT) {
					store.commit("readedMessage", {
						friendId: msg.sendId
					})
					return;
				}
				// 标记这条消息是不是自己发的
				msg.selfSend = msg.sendId == store.state.userStore.userInfo.id;
				// 好友id
				let friendId = msg.selfSend ? msg.recvId : msg.sendId;
				this.loadFriendInfo(friendId).then((friend) => {
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
				store.commit("openChat", chatInfo);
				// 插入消息
				store.commit("insertMessage", msg);
				// 播放提示音
				this.playAudioTip();

			},
			handleGroupMessage(msg) {
				// 消息加载标志
				if (msg.type == enums.MESSAGE_TYPE.LOADING) {
					store.commit("loadingGroupMsg", JSON.parse(msg.content))
					return;
				}
				// 消息已读处理
				if (msg.type == enums.MESSAGE_TYPE.READED) {
					// 我已读对方的消息，清空已读数量
					let chatInfo = {
						type: 'GROUP',
						targetId: msg.groupId
					}
					store.commit("resetUnreadCount", chatInfo)
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
					store.commit("updateMessage", msgInfo)
					return;
				}
				// 标记这条消息是不是自己发的
				msg.selfSend = msg.sendId == store.state.userStore.userInfo.id;
				this.loadGroupInfo(msg.groupId).then((group) => {
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
				store.commit("openChat", chatInfo);
				// 插入消息
				store.commit("insertMessage", msg);
				// 播放提示音
				this.playAudioTip();
			},
			loadFriendInfo(id) {
				return new Promise((resolve, reject) => {
					let friend = store.getters.findFriend(id);
					if (friend) {
						resolve(friend);
					} else {
						http({
							url: `/friend/find/${id}`,
							method: 'GET'
						}).then((friend) => {
							store.commit("addFriend", friend);
							resolve(friend)
						})
					}
				});
			},
			loadGroupInfo(id) {
				return new Promise((resolve, reject) => {
					let group = store.state.groupStore.groups.find((g) => g.id == id);
					if (group) {
						resolve(group);
					} else {
						http({
							url: `/group/find/${id}`,
							method: 'GET'
						}).then((group) => {
							resolve(group)
							store.commit("addGroup", group);
						})
					}
				});
			},
			exit() {
				console.log("exit");
				this.isExit = true;
				wsApi.close(3099);
				uni.removeStorageSync("loginInfo");
				uni.reLaunch({
					url: "/pages/login/login"
				})
				store.dispatch("unload");
			},
			playAudioTip() {
				// 音频播放无法成功
				// this.audioTip = uni.createInnerAudioContext();
				// this.audioTip.src =  "/static/audio/tip.wav";
				// this.audioTip.play();
			},
			isExpired(loginInfo) {
				if (!loginInfo || !loginInfo.expireTime) {
					return true;
				}
				return loginInfo.expireTime < new Date().getTime();
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
					store.commit("setUserInfo", userInfo);
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
			},
			initUniPush(){
				// #ifdef APP-PLUS  
					plus.push.setAutoNotification(true);
					const clientInfo = plus.push.getClientInfo();
					console.log("clientInfo",clientInfo);
					plus.push.addEventListener('click', (message)=>{
						const messages = plus.push.getAllMessage();
						console.log("messages",messages)
						console.log("click",message)
					});  
					plus.push.addEventListener('receive', (message)=>{
						console.log("receive",message)
					});  
				// #endif  
			}
		},
		onLaunch() {
			// 登录状态校验
			let loginInfo = uni.getStorageSync("loginInfo")
			if (!this.isExpired(loginInfo)) {
				console.log("初始化")
				// 初始化
				this.init();
				// 跳转到聊天页面
				uni.switchTab({
					url: "/pages/chat/chat"
				})
			} else {
				// 跳转到登录页
				// #ifdef H5
				uni.navigateTo({
					url: "/pages/login/login"
				})
				// #endif
			}
			
		}
	}
</script>

<style lang="scss">
	@import "@/uni_modules/uview-plus/index.scss";
	@import url('./static/icon/iconfont.css');

	// #ifdef H5 
	uni-page-head {
		display: none; // h5浏览器本身就有标题
	}
	// #endif

	.tab-page {
		// #ifdef H5
		height: calc(100vh  - 50px); // h5平台100vh是包含了底部高度，需要减去
		// #endif
		// #ifndef H5
		height: calc(100vh);
		// #endif
		background-color: #f8f8f8;
	}

	.page {
		height: calc(100vh);
		background-color: #f8f8f8;
	}
</style>
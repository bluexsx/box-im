<script>
	import store from './store';
	import http from './common/request';
	import * as enums from './common/enums';
	import * as wsApi from './common/wssocket';

	export default {
		data() {
			return {
				audioTip: null
			}
		},
		methods: {
			init(loginInfo) {
				// 加载数据
				store.dispatch("load").then(() => {
					// 初始化websocket
					this.initWebSocket(loginInfo);
				}).catch((e) => {
					this.exit();
				})
			},
			initWebSocket(loginInfo) {
				let userId = store.state.userStore.userInfo.id;
				wsApi.createWebSocket(process.env.WS_URL, loginInfo.accessToken);
				wsApi.onopen(() => {
					this.pullUnreadMessage();
				});
				wsApi.onmessage((cmd, msgInfo) => {
					if (cmd == 2) {
						// 异地登录，强制下线
						uni.showModal({
							content: '您已在其他地方登陆，将被强制下线',
							showCancel: false,
						})
						this.exit();
					} else if (cmd == 3) {
						// 标记这条消息是不是自己发的
						msgInfo.selfSend = userId == msgInfo.sendId;
						// 插入私聊消息
						this.handlePrivateMessage(msgInfo);
					} else if (cmd == 4) {
						// 标记这条消息是不是自己发的
						msgInfo.selfSend = userId == msgInfo.sendId;
						// 插入群聊消息
						this.handleGroupMessage(msgInfo);
					}
				})
			},
			pullUnreadMessage() {
				// 拉取未读私聊消息
				http({
					url: "/message/private/pullUnreadMessage",
					method: 'POST'
				});
				// 拉取未读群聊消息
				http({
					url: "/message/group/pullUnreadMessage",
					method: 'POST'
				});
			},
			handlePrivateMessage(msg) {
				let friendId = msg.selfSend ? msg.recvId : msg.sendId;
				let friend = store.state.friendStore.friends.find((f) => f.id == friendId);
				if (!friend) {
					http({
						url: `/friend/find/${msg.sendId}`,
						method: 'GET'
					}).then((friend) => {
						this.insertPrivateMessage(friend, msg);
						store.commit("addFriend", friend);
					})
				} else {
					// 好友列表不存在好友信息，则发请求获取好友信息
					this.insertPrivateMessage(friend, msg);
				}

			},
			insertPrivateMessage(friend, msg) {
				// webrtc 信令
				if (msg.type >= enums.MESSAGE_TYPE.RTC_CALL &&
					msg.type <= enums.MESSAGE_TYPE.RTC_CANDIDATE) {}

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
				!msg.selfSend && this.playAudioTip();

			},
			handleGroupMessage(msg) {
				let group = store.state.groupStore.groups.find((g) => g.id == msg.groupId);
				if (!group) {
					http({
						url: `/group/find/${msg.groupId}`,
						method: 'get'
					}).then((group) => {
						this.insertGroupMessage(group, msg);
						store.commit("addGroup", group);
					})
				} else {
					// 群聊缓存存在，直接插入群聊消息
					this.insertGroupMessage(group, msg);
				}

			},
			insertGroupMessage(group, msg) {
				let chatInfo = {
					type: 'GROUP',
					targetId: group.id,
					showName: group.remark,
					headImage: group.headImageThumb
				};
				// 打开会话
				store.commit("openChat", chatInfo);
				// 插入消息
				store.commit("insertMessage", msg);
				// 播放提示音
				!msg.selfSend && this.playAudioTip();
			},
			exit() {
				console.log("exit");
				wsApi.closeWebSocket();
				uni.removeStorageSync("loginInfo");
				uni.navigateTo({
					url: "/pages/login/login"
				})
			},
			playAudioTip() {
				// 音频播放无法成功
				// this.audioTip = uni.createInnerAudioContext();
				// this.audioTip.src =  "/static/audio/tip.wav";
				// this.audioTip.play();
			}
		},
		onLaunch() {
			// 登录状态校验
			let loginInfo = uni.getStorageSync("loginInfo");
			if (loginInfo) {
				// 初始化
				this.init(loginInfo)
			} else {
				// 跳转到登录页
				uni.navigateTo({
					url: "/pages/login/login"
				})
			}
		}
	}
</script>

<style lang="scss">
	@import url('./static/icon/iconfont.css');

	.tab-page {
		// #ifdef H5
		height: calc(100vh - 46px - 50px); // h5平台100vh是包含了顶部和底部，需要减去
		// #endif
		// #ifndef H5
		height: calc(100vh);
		// #endif
		background-color: #f8f8f8;
	}

	.page {
		// #ifdef H5
		height: calc(100vh - 45px); // h5平台100vh是包含了顶部，需要减去
		// #endif
		// #ifndef H5
		height: calc(100vh);
		// #endif
		background-color: #f8f8f8;
	}
</style>
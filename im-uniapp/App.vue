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
			init() {
				// 加载数据
				store.dispatch("load").then(() => {
					// 审核
					this.initAudit();
					// 初始化websocket
					this.initWebSocket();
					// 加载离线消息
					this.loadPrivateMessage(store.state.chatStore.privateMsgMaxId);
					this.loadGroupMessage(store.state.chatStore.groupMsgMaxId);
				}).catch((e) => {
					console.log(e);
					this.exit();
				})
			},
			initWebSocket() {
				let loginInfo = uni.getStorageSync("loginInfo")
				let userId = store.state.userStore.userInfo.id;
				wsApi.init(process.env.WS_URL, loginInfo.accessToken);
				wsApi.connect();
				wsApi.onOpen()
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
					}
				});
				wsApi.onClose((res) => {
					// 1006是服务器主动断开，3000是APP主动关闭
					if (res.code == 1006) {
						uni.showToast({
							title: '连接已断开，请重新登录',
							icon: 'none',
						})
						this.exit();
					} else if (res.code != 3000) {
						// 重新连接
						wsApi.connect();
					}
				})
			},
			loadPrivateMessage(minId) {
				store.commit("loadingPrivateMsg", true)
				http({
					url: "/message/private/loadMessage?minId=" + minId,
					method: 'get'
				}).then((msgInfos) => {
					msgInfos.forEach((msgInfo) => {
						msgInfo.selfSend = msgInfo.sendId == store.state.userStore.userInfo.id;
						let friendId = msgInfo.selfSend ? msgInfo.recvId : msgInfo.sendId;
						let friend = store.state.friendStore.friends.find((f) => f.id == friendId);
						if(friend){
							this.insertPrivateMessage(friend,msgInfo);
						}	
					})
					if (msgInfos.length == 100) {
						// 继续拉取
						this.loadPrivateMessage(msgInfos[99].id);
					} else {
						store.commit("loadingPrivateMsg", false)
					}
				})
			},
			loadGroupMessage(minId) {
				store.commit("loadingGroupMsg", true)
				http({
					url: "/message/group/loadMessage?minId=" + minId,
					method: 'get'
				}).then((msgInfos) => {
					msgInfos.forEach((msgInfo) => {
						msgInfo.selfSend = msgInfo.sendId == store.state.userStore.userInfo.id;
						let groupId = msgInfo.groupId;
						let group = store.state.groupStore.groups.find((g) => g.id == groupId);
						if(group){
							this.insertGroupMessage(group,msgInfo);
						}
					})
					if (msgInfos.length == 100) {
						// 继续拉取
						this.loadGroupMessage(msgInfos[99].id);
					} else {
						store.commit("loadingGroupMsg", false)
					}
				})
			},
			handlePrivateMessage(msg) {
				// 标记这条消息是不是自己发的
				msg.selfSend = msg.sendId == store.state.userStore.userInfo.id;
				// 好友id
				let friendId = msg.selfSend ? msg.recvId : msg.sendId;
				// 消息已读处理
				if (msg.type == enums.MESSAGE_TYPE.READED) {
					if (msg.selfSend) {
						// 我已读对方的消息，清空已读数量
						let chatInfo = {
							type: 'PRIVATE',
							targetId: friendId
						}
						store.commit("resetUnreadCount", chatInfo)
					} else {
						// 对方已读我的消息，修改消息状态为已读
						store.commit("readedMessage", friendId)
						
					}
					return;
				}
				this.loadFriendInfo(friendId).then((friend) => {
					this.insertPrivateMessage(friend, msg);
				})

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
				// 标记这条消息是不是自己发的
				msg.selfSend = msg.sendId == store.state.userStore.userInfo.id;
				let groupId = msg.groupId;
				// 消息已读处理
				if (msg.type == enums.MESSAGE_TYPE.READED) {
					// 我已读对方的消息，清空已读数量
					let chatInfo = {
						type: 'GROUP',
						targetId: groupId
					}
					store.commit("resetUnreadCount", chatInfo)
					return;
				}
				this.loadGroupInfo(groupId).then((group) => {
					// 插入群聊消息
					this.insertGroupMessage(group, msg);
				})

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
			loadFriendInfo(id) {
				return new Promise((resolve, reject) => {
					let friend = store.state.friendStore.friends.find((f) => f.id == id);
					if (friend) {
						resolve(friend);
					} else {
						http({
							url: `/friend/find/${id}`,
							method: 'get'
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
							method: 'get'
						}).then((group) => {
							resolve(group)
							store.commit("addGroup", group);
						})
					}
				});
			},
			exit() {
				console.log("exit");
				wsApi.close();
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
			initAudit() {
				console.log("initAudit")
				if (store.state.userStore.userInfo.type == 1) {
					// 显示群组功能
					uni.setTabBarItem({
						index: 2,
						text: "群聊"
					})
				} else {
					// 隐藏群组功能
					uni.setTabBarItem({
						index: 2,
						text: "搜索"
					})
				}
			}
		},
		onLaunch() {

			// 登录状态校验
			if (uni.getStorageSync("loginInfo")) {
				// 初始化
				this.init()
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
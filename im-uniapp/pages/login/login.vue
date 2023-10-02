<template>
	<view class="login-form">
		<view class="login-title">欢迎登录</view>
		<uni-forms style="margin-top: 100px;" :modelValue="loginForm" :rules="rules" validate-trigger="bind">
			<uni-forms-item name="userName">
				<uni-easyinput type="text" v-model="loginForm.userName" prefix-icon="person" focus placeholder="用户名" />
			</uni-forms-item>
			<uni-forms-item name="password">
				<uni-easyinput type="password" v-model="loginForm.password" prefix-icon="locked" placeholder="密码" />
			</uni-forms-item>
			<button @click="submit" type="primary">登录</button>
		</uni-forms>

	</view>
</template>

<script>
	export default {
		data() {
			return {
				loginForm: {
					terminal: 1, // APP终端
					userName: 'blue',
					password: '123456'
				},
				rules: {
					userName: {
						rules: [{
							required: true,
							errorMessage: '请输入用户名',
						}]
					},
					password: {
						rules: [{
							required: true,
							errorMessage: '请输入密码',
						}]
					}
				}
			}
		},
		methods: {
			submit() {
				this.$http({
					url: '/login',
					data: this.loginForm,
					method: 'POST'
				}).then(data => {
					console.log("登录成功,自动跳转到聊天页面...")
					uni.setStorageSync("loginInfo", data);
					this.init(data);
				})
			},
			init(loginInfo) {
				// 加载数据
				this.$store.dispatch("load").then(() => {
					// 初始化websocket
					this.initWebSocket(loginInfo);
				}).catch((e) => {
					console.log(e);
					this.quit();
				})
				// 跳转到登录界面
				uni.switchTab({
					url: "/pages/chat/chat"
				})
			},
			initWebSocket(loginInfo) {
				let userId = this.$store.state.userStore.userInfo.id;
				this.$wsApi.createWebSocket(process.env.WS_URL, loginInfo.accessToken);
				this.$wsApi.onopen(() => {
					this.pullUnreadMessage();
				});
				this.$wsApi.onmessage((cmd, msgInfo) => {
					if (cmd == 2) {
						// 异地登录，强制下线
						uni.showModal({
							content: '您已在其他地方登陆，将被强制下线',  
							showCancel: false,
						})
						this.quit();
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
				this.$http({
					url: "/message/private/pullUnreadMessage",
					method: 'POST'
				});
				// 拉取未读群聊消息
				this.$http({
					url: "/message/group/pullUnreadMessage",
					method: 'POST'
				});
			},
			handlePrivateMessage(msg) {
				// 好友列表存在好友信息，直接插入私聊消息
				let friendId = msg.selfSend ? msg.recvId : msg.sendId;
				let friend = this.$store.state.friendStore.friends.find((f) => f.id == friendId);
				if (!friend) {
					// 好友列表不存在好友信息，则发请求获取好友信息
					this.$http({
						url: `/friend/find/${msg.sendId}`,
						method: 'get'
					}).then((friend) => {
						this.insertPrivateMessage(friend, msg);
						this.$store.commit("addFriend", friend);
					})
				} else {
					this.insertPrivateMessage(friend, msg);
				}

			},
			insertPrivateMessage(friend, msg) {
				// webrtc 信令
				if (msg.type >= this.$enums.MESSAGE_TYPE.RTC_CALL &&
					msg.type <= this.$enums.MESSAGE_TYPE.RTC_CANDIDATE) {

					// // 呼叫
					// if (msg.type == this.$enums.MESSAGE_TYPE.RTC_CALL ||
					// 	msg.type == this.$enums.MESSAGE_TYPE.RTC_CANCEL) {
					// 	this.$store.commit("showVideoAcceptorBox", friend);
					// 	this.$refs.videoAcceptor.handleMessage(msg)
					// } else {
					// 	this.$refs.videoAcceptor.close()
					// 	this.$refs.privateVideo.handleMessage(msg)
					// }
					// return;
				}

				let chatInfo = {
					type: 'PRIVATE',
					targetId: friend.id,
					showName: friend.nickName,
					headImage: friend.headImage
				};
				// 打开会话
				this.$store.commit("openChat", chatInfo);
				// 插入消息
				this.$store.commit("insertMessage", msg);
				// 播放提示音
				!msg.selfSend && this.playAudioTip();

			},
			handleGroupMessage(msg) {
				// 群聊缓存存在，直接插入群聊消息
				let group = this.$store.state.groupStore.groups.find((g) => g.id == msg.groupId);
				if (!group) {
					// 群聊缓存存在，直接插入群聊消息
					this.$http({
						url: `/group/find/${msg.groupId}`,
						method: 'get'
					}).then((group) => {
						this.insertGroupMessage(group, msg);
						this.$store.commit("addGroup", group);
					})
				} else {
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
				this.$store.commit("openChat", chatInfo);
				// 插入消息
				this.$store.commit("insertMessage", msg);
				// 播放提示音
				!msg.selfSend && this.playAudioTip();
			},
			quit() {
				uni.showToast({
					title: "退出登录"
				})
				console.log("退出登录")
				this.$wsApi.closeWebSocket();
				uni.removeStorageSync("loginInfo");
				uni.navigateTo({
					url:"/pages/login/login"
				})
			},
			playAudioTip() {
				let audio = new Audio();
				let url = "/static/audio/tip.wav";
				audio.src = url;
				audio.play();
			}
		},
		mounted() {
			console.log("login  mounted")
			let loginInfo = uni.getStorageSync("loginInfo");
			if (loginInfo) {
				// 已登录,加载数据后跳转
				this.init(loginInfo);
				
			}
		}
	}
</script>

<style lang="scss" scoped>
	.login-form {
		margin: 50rpx;

		.login-title {
			margin-top: 100rpx;
			margin-bottom: 100rpx;
			color: royalblue;
			text-align: center;
			font-size: 60rpx;
			font-weight: 600;
		}
	}
</style>
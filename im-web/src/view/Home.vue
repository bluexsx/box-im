<template>
	<div class="home-page">
		<div class="app-container" :class="{ fullscreen: isFullscreen }">
			<div class="navi-bar">
				<div class="navi-bar-box">
					<div class="top">
						<div class="user-head-image">
							<head-image :name="$store.state.userStore.userInfo.nickName" :size="38"
								:url="$store.state.userStore.userInfo.headImageThumb"
								@click.native="showSettingDialog = true">
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
						<div class="botoom-item" @click="isFullscreen = !isFullscreen">
							<i class="el-icon-full-screen"></i>
						</div>
						<div class="botoom-item" @click="showSetting">
							<span class="icon iconfont icon-setting" style="font-size: 20px"></span>
						</div>
						<div class="botoom-item" @click="onExit()" title="退出">
							<span class="icon iconfont icon-exit"></span>
						</div>
					</div>
				</div>
			</div>
			<div class="content-box">
				<router-view></router-view>
			</div>
			<setting :visible="showSettingDialog" @close="closeSetting()"></setting>
			<user-info v-show="uiStore.userInfo.show" :pos="uiStore.userInfo.pos" :user="uiStore.userInfo.user"
				@close="$store.commit('closeUserInfoBox')"></user-info>
			<full-image :visible="uiStore.fullImage.show" :url="uiStore.fullImage.url"
				@close="$store.commit('closeFullImageBox')"></full-image>
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
			isFullscreen: true
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

			this.$store.dispatch("load").then(() => {
				// ws初始化
				this.$wsApi.connect(process.env.VUE_APP_WS_URL, sessionStorage.getItem("accessToken"));
				this.$wsApi.onConnect(() => {
					// 加载离线消息
					this.pullPrivateOfflineMessage(this.$store.state.chatStore.privateMsgMaxId);
					this.pullGroupOfflineMessage(this.$store.state.chatStore.groupMsgMaxId);
				});
				this.$wsApi.onMessage((cmd, msgInfo) => {
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
						// 插入私聊消息
						this.handlePrivateMessage(msgInfo);
					} else if (cmd == 4) {
						// 插入群聊消息
						this.handleGroupMessage(msgInfo);
					} else if (cmd == 5) {
						// 处理系统消息
						this.handleSystemMessage(msgInfo);
					}
				});
				this.$wsApi.onClose((e) => {
					console.log(e);
					if (e.code != 3000) {
						// 断线重连
						this.$message.error("连接断开，正在尝试重新连接...");
						this.$wsApi.reconnect(process.env.VUE_APP_WS_URL, sessionStorage.getItem(
							"accessToken"));
					}
				});
			}).catch((e) => {
				console.log("初始化失败", e);
			})
		},
		pullPrivateOfflineMessage(minId) {
			this.$store.commit("loadingPrivateMsg", true)
			this.$http({
				url: "/message/private/pullOfflineMessage?minId=" + minId,
				method: 'GET'
			}).catch(() => {
				this.$store.commit("loadingPrivateMsg", false)
			})
		},
		pullGroupOfflineMessage(minId) {
			this.$store.commit("loadingGroupMsg", true)
			this.$http({
				url: "/message/group/pullOfflineMessage?minId=" + minId,
				method: 'GET'
			}).catch(() => {
				this.$store.commit("loadingGroupMsg", false)
			})
		},
		handlePrivateMessage(msg) {
			// 标记这条消息是不是自己发的
			msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
			// 好友id
			let friendId = msg.selfSend ? msg.recvId : msg.sendId;
			// 会话信息
			let chatInfo = {
				type: 'PRIVATE',
				targetId: friendId
			}
			// 消息加载标志
			if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
				this.$store.commit("loadingPrivateMsg", JSON.parse(msg.content))
				return;
			}
			// 消息已读处理，清空已读数量
			if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
				this.$store.commit("resetUnreadCount", chatInfo)
				return;
			}
			// 消息回执处理,改消息状态为已读
			if (msg.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
				this.$store.commit("readedMessage", {
					friendId: msg.sendId
				})
				return;
			}
			// 消息撤回
			if (msg.type == this.$enums.MESSAGE_TYPE.RECALL) {
				this.$store.commit("recallMessage", [msg, chatInfo])
				return;
			}
			// 单人webrtc 信令
			if (this.$msgType.isRtcPrivate(msg.type)) {
				this.$refs.rtcPrivateVideo.onRTCMessage(msg)
				return;
			}
			// 好友id
			this.loadFriendInfo(friendId).then((friend) => {
				this.insertPrivateMessage(friend, msg);
			})
		},
		insertPrivateMessage(friend, msg) {

			let chatInfo = {
				type: 'PRIVATE',
				targetId: friend.id,
				showName: friend.nickName,
				headImage: friend.headImage
			};
			// 打开会话
			this.$store.commit("openChat", chatInfo);
			// 插入消息
			this.$store.commit("insertMessage", [msg, chatInfo]);
			// 播放提示音
			if (!msg.selfSend && this.$msgType.isNormal(msg.type) &&
				msg.status != this.$enums.MESSAGE_STATUS.READED) {
				this.playAudioTip();
			}
		},
		handleGroupMessage(msg) {
			// 标记这条消息是不是自己发的
			msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
			let chatInfo = {
				type: 'GROUP',
				targetId: msg.groupId
			}
			// 消息加载标志
			if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
				this.$store.commit("loadingGroupMsg", JSON.parse(msg.content))
				return;
			}
			// 消息已读处理
			if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
				// 我已读对方的消息，清空已读数量
				this.$store.commit("resetUnreadCount", chatInfo)
				return;
			}
			// 消息回执处理
			if (msg.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
				// 更新消息已读人数
				let msgInfo = {
					id: msg.id,
					groupId: msg.groupId,
					readedCount: msg.readedCount,
					receiptOk: msg.receiptOk
				};
				this.$store.commit("updateMessage", [msgInfo, chatInfo])
				return;
			}
			// 消息撤回
			if (msg.type == this.$enums.MESSAGE_TYPE.RECALL) {
				this.$store.commit("recallMessage", [msg, chatInfo])
				return;
			}
			// 群视频信令
			if (this.$msgType.isRtcGroup(msg.type)) {
				this.$nextTick(() => {
					this.$refs.rtcGroupVideo.onRTCMessage(msg);
				})
				return;
			}
			this.loadGroupInfo(msg.groupId).then((group) => {
				// 插入群聊消息
				this.insertGroupMessage(group, msg);
			})
		},
		insertGroupMessage(group, msg) {
			let chatInfo = {
				type: 'GROUP',
				targetId: group.id,
				showName: group.showGroupName,
				headImage: group.headImageThumb
			};
			// 打开会话
			this.$store.commit("openChat", chatInfo);
			// 插入消息
			this.$store.commit("insertMessage", [msg, chatInfo]);
			// 播放提示音
			if (!msg.selfSend && msg.type <= this.$enums.MESSAGE_TYPE.VIDEO &&
				msg.status != this.$enums.MESSAGE_STATUS.READED) {
				this.playAudioTip();
			}
		},
		handleSystemMessage(msg) {
			// 用户被封禁

			if (msg.type == this.$enums.MESSAGE_TYPE.USER_BANNED) {
				this.$wsApi.close(3000);
				this.$alert("您的账号已被管理员封禁,原因:" + msg.content, "账号被封禁", {
					confirmButtonText: '确定',
					callback: action => {
						this.onExit();
					}
				});
				return;
			}
		},
		onExit() {
			this.$wsApi.close(3000);
			sessionStorage.removeItem("accessToken");
			location.href = "/";
		},
		playAudioTip() {
			// 离线消息不播放铃声
			if (this.$store.getters.isLoading()) {
				return;
			}
			// 防止过于密集播放
			if (new Date().getTime() - this.lastPlayAudioTime > 1000) {
				this.lastPlayAudioTime = new Date().getTime();
				let audio = new Audio();
				let url = require(`@/assets/audio/tip.wav`);
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
			return new Promise((resolve, reject) => {
				let friend = this.$store.state.friendStore.friends.find((f) => f.id == id);
				if (friend) {
					resolve(friend);
				} else {
					this.$http({
						url: `/friend/find/${id}`,
						method: 'get'
					}).then((friend) => {
						this.$store.commit("addFriend", friend);
						resolve(friend)
					})
				}
			});
		},
		loadGroupInfo(id) {
			return new Promise((resolve, reject) => {
				let group = this.$store.state.groupStore.groups.find((g) => g.id == id);
				if (group) {
					resolve(group);
				} else {
					this.$http({
						url: `/group/find/${id}`,
						method: 'get'
					}).then((group) => {
						resolve(group)
						this.$store.commit("addGroup", group);
					})
				}
			});
		}
	},
	computed: {
		uiStore() {
			return this.$store.state.uiStore;
		},
		unreadCount() {
			let unreadCount = 0;
			let chats = this.$store.state.chatStore.chats;
			chats.forEach((chat) => {
				if (!chat.delete) {
					unreadCount += chat.unreadCount
				}
			});
			return unreadCount;
		}
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
	//background-image: url('../assets/image/background.jpg');

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
		--width: 60px;
		width: var(--width);
		background: var(--im-color-primary-light-1);
		padding-top: 20px;

		.navi-bar-box {
			height: 100%;
			display: flex;
			flex-direction: column;
			justify-content: space-between;

			.botoom {
				margin-bottom: 30px;
			}
		}

		.user-head-image {
			display: flex;
			justify-content: center;
		}

		.menu {
			height: 200px;
			//margin-top: 10px;
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-content: center;

			.link {
				text-decoration: none;
			}

			.router-link-active .menu-item {
				color: #fff;
				background: var(--im-color-primary-light-2);
			}

			.link:not(.router-link-active) .menu-item:hover {
				color: var(--im-color-primary-light-7);
			}

			.menu-item {
				position: relative;
				color: var(--im-color-primary-light-4);
				width: var(--width);
				height: 46px;
				display: flex;
				justify-content: center;
				align-items: center;
				margin-bottom: 12px;

				.icon {
					font-size: var(--icon-font-size)
				}

				.unread-text {
					position: absolute;
					background-color: var(--im-color-danger);
					left: 28px;
					top: 8px;
					color: white;
					border-radius: 30px;
					padding: 0 5px;
					font-size: var(--im-font-size-smaller);
					text-align: center;
					white-space: nowrap;
					border: 1px solid #f1e5e5;
				}
			}
		}

		.botoom-item {
			display: flex;
			justify-content: center;
			align-items: center;
			height: 50px;
			width: 100%;
			cursor: pointer;
			color: var(--im-color-primary-light-4);
			font-size: var(--icon-font-size);

			.icon {
				font-size: var(--icon-font-size)
			}

			&:hover {
				font-weight: 600;
				color: var(--im-color-primary-light-7);
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
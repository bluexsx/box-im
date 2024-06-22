<template>
	<el-container class="home-page">
		<el-aside width="80px" class="navi-bar">
			<div class="user-head-image">
				<head-image :name="$store.state.userStore.userInfo.nickName"
					:url="$store.state.userStore.userInfo.headImageThumb" :size="50"
					@click.native="showSettingDialog = true">
				</head-image>
			</div>
			<el-menu background-color="#19082f" style="margin-top: 25px;">
				<el-menu-item title="聊天">
					<router-link class="link" v-bind:to="'/home/chat'">
						<span class="icon iconfont icon-chat"></span>
						<div v-show="unreadCount > 0" class="unread-text">{{ unreadCount }}</div>
					</router-link>
				</el-menu-item>
				<el-menu-item title="好友">
					<router-link class="link" v-bind:to="'/home/friend'">
						<span class="icon iconfont icon-friend"></span>
					</router-link>
				</el-menu-item>
				<el-menu-item title="群聊">
					<router-link class="link" v-bind:to="'/home/group'">
						<span class="icon iconfont icon-group"></span>
					</router-link>
				</el-menu-item>
				<el-menu-item title="设置" @click="showSetting()">
					<span class="icon iconfont icon-setting"></span>
				</el-menu-item>
			</el-menu>
			<div class="exit-box" @click="onExit()" title="退出">
				<span class="icon iconfont icon-exit"></span>
			</div>
		</el-aside>
		<el-main class="content-box">
			<router-view></router-view>
		</el-main>
		<setting :visible="showSettingDialog" @close="closeSetting()"></setting>
		<user-info v-show="uiStore.userInfo.show" :pos="uiStore.userInfo.pos" :user="uiStore.userInfo.user"
			@close="$store.commit('closeUserInfoBox')"></user-info>
		<full-image :visible="uiStore.fullImage.show" :url="uiStore.fullImage.url"
			@close="$store.commit('closeFullImageBox')"></full-image>
		<rtc-private-video ref="rtcPrivateVideo"></rtc-private-video>
		<rtc-group-video ref="rtcGroupVideo"></rtc-group-video>
	</el-container>
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
				lastPlayAudioTime: new Date().getTime() - 1000
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
							this.$alert("您已在其他地方登陆，将被强制下线", "强制下线通知", {
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
				this.$http({
					url: "/message/private/pullOfflineMessage?minId=" + minId,
					method: 'get'
				});
			},
			pullGroupOfflineMessage(minId) {
				this.$http({
					url: "/message/group/pullOfflineMessage?minId=" + minId,
					method: 'get'
				});
			},
			handlePrivateMessage(msg) {
				// 消息加载标志
				if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
					this.$store.commit("loadingPrivateMsg", JSON.parse(msg.content))
					return;
				}
				// 消息已读处理，清空已读数量
				if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
					this.$store.commit("resetUnreadCount", {
						type: 'PRIVATE',
						targetId: msg.recvId
					})
					return;
				}
				// 消息回执处理,改消息状态为已读
				if (msg.type == this.$enums.MESSAGE_TYPE.RECEIPT) {
					this.$store.commit("readedMessage", {
						friendId: msg.sendId
					})
					return;
				}
				// 标记这条消息是不是自己发的
				msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
				// 单人webrtc 信令
				if (this.$msgType.isRtcPrivate(msg.type)) {
					this.$refs.rtcPrivateVideo.onRTCMessage(msg)
					return;
				}
				// 好友id
				let friendId = msg.selfSend ? msg.recvId : msg.sendId;
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
				this.$store.commit("insertMessage", msg);
				// 播放提示音
				if (!msg.selfSend && this.$msgType.isNormal(msg.type) &&
					msg.status != this.$enums.MESSAGE_STATUS.READED) {
					this.playAudioTip();
				}
			},
			handleGroupMessage(msg) {
				// 消息加载标志
				if (msg.type == this.$enums.MESSAGE_TYPE.LOADING) {
					this.$store.commit("loadingGroupMsg", JSON.parse(msg.content))
					return;
				}
				// 消息已读处理
				if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
					// 我已读对方的消息，清空已读数量
					let chatInfo = {
						type: 'GROUP',
						targetId: msg.groupId
					}
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
					this.$store.commit("updateMessage", msgInfo)
					return;
				}
				// 标记这条消息是不是自己发的
				msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
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
					showName: group.remark,
					headImage: group.headImageThumb
				};
				// 打开会话
				this.$store.commit("openChat", chatInfo);
				// 插入消息
				this.$store.commit("insertMessage", msg);
				// 播放提示音
				if (!msg.selfSend && msg.type <= this.$enums.MESSAGE_TYPE.VIDEO &&
					msg.status != this.$enums.MESSAGE_STATUS.READED) {
					this.playAudioTip();
				}
			},
			onExit() {
				this.$wsApi.close(3000);
				sessionStorage.removeItem("accessToken");
				location.href = "/";
			},
			playAudioTip() {
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
					unreadCount += chat.unreadCount
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
	.navi-bar {
		background: #19082f;
		padding: 15px;
		padding-top: 50px;

		.el-menu {
			border: none;
			flex: 1;

			.el-menu-item {
				margin: 20px 0;
				background-color: #19082f !important;
				padding: 0 !important;
				text-align: center;

				.link {
					text-decoration: none;

					&.router-link-active .icon {
						color: #ba785a;
					}
				}

				.icon {
					font-size: 26px !important;
					color: #ddd;
				}

				.unread-text {
					position: absolute;
					line-height: 20px;
					background-color: #f56c6c;
					left: 36px;
					top: 7px;
					color: white;
					border-radius: 30px;
					padding: 0 5px;
					font-size: 10px;
					text-align: center;
					white-space: nowrap;
					border: 1px solid #f1e5e5;
				}
			}
		}

		.exit-box {
			position: absolute;
			width: 60px;
			bottom: 40px;
			color: #ccc;
			text-align: center;
			cursor: pointer;

			.icon {
				font-size: 28px;
			}

			&:hover {
				color: white;
			}
		}
	}

	.content-box {
		padding: 0;
		background-color: #f8f8f8;
		color: black;
		text-align: center;
	}
</style>
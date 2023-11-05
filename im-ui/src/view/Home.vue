<template>
	<el-container>
		<el-aside width="80px" class="navi-bar">
			<div class="user-head-image">
				<head-image :name="$store.state.userStore.userInfo.nickName"
					:url="$store.state.userStore.userInfo.headImageThumb" :size="60"
					@click.native="showSettingDialog=true">
				</head-image>
			</div>

			<el-menu background-color="#333333" text-color="#ddd" style="margin-top: 30px;">
				<el-menu-item title="聊天">
					<router-link v-bind:to="'/home/chat'">
						<span class="el-icon-chat-dot-round"></span>
						<div v-show="unreadCount>0" class="unread-text">{{unreadCount}}</div>
					</router-link>
				</el-menu-item>
				<el-menu-item title="好友">
					<router-link v-bind:to="'/home/friend'">
						<span class="el-icon-user"></span>
					</router-link>
				</el-menu-item>
				<el-menu-item title="群聊">
					<router-link v-bind:to="'/home/group'">
						<span class="icon iconfont icon-group_fill"></span>
					</router-link>
				</el-menu-item>

				<el-menu-item title="设置" @click="showSetting()">
					<span class="el-icon-setting"></span>
				</el-menu-item>
			</el-menu>

			<div class="exit-box" @click="handleExit()" title="退出">
				<span class="el-icon-circle-close"></span>
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
		<chat-private-video ref="privateVideo" :visible="uiStore.chatPrivateVideo.show"
			:friend="uiStore.chatPrivateVideo.friend" :master="uiStore.chatPrivateVideo.master"
			:offer="uiStore.chatPrivateVideo.offer" @close="$store.commit('closeChatPrivateVideoBox')">
		</chat-private-video>
		<chat-video-acceptor ref="videoAcceptor" v-show="uiStore.videoAcceptor.show"
			:friend="uiStore.videoAcceptor.friend" @close="$store.commit('closeVideoAcceptorBox')">
		</chat-video-acceptor>
	</el-container>
</template>

<script>
	import HeadImage from '../components/common/HeadImage.vue';
	import Setting from '../components/setting/Setting.vue';
	import UserInfo from '../components/common/UserInfo.vue';
	import FullImage from '../components/common/FullImage.vue';
	import ChatPrivateVideo from '../components/chat/ChatPrivateVideo.vue';
	import ChatVideoAcceptor from '../components/chat/ChatVideoAcceptor.vue';


	export default {
		components: {
			HeadImage,
			Setting,
			UserInfo,
			FullImage,
			ChatPrivateVideo,
			ChatVideoAcceptor
		},
		data() {
			return {
				showSettingDialog: false,
				lastPlayAudioTime: new Date()-1000
			}
		},
		methods: {
			init() {
				this.$store.dispatch("load").then(() => {
					// 加载离线消息
					this.loadPrivateMessage(this.$store.state.chatStore.privateMsgMaxId);
					this.loadGroupMessage(this.$store.state.chatStore.groupMsgMaxId);
					// ws初始化
					this.$wsApi.init(process.env.VUE_APP_WS_URL, sessionStorage.getItem("accessToken"));
					this.$wsApi.connect();
					this.$wsApi.onOpen();
					this.$wsApi.onMessage((cmd, msgInfo) => {
						if (cmd == 2) {
							// 异地登录，强制下线
							this.$message.error("您已在其他地方登陆，将被强制下线");
							setTimeout(() => {
								location.href = "/";
							}, 1000)
						} else if (cmd == 3) {
							// 插入私聊消息
							this.handlePrivateMessage(msgInfo);
						} else if (cmd == 4) {
							// 插入群聊消息
							this.handleGroupMessage(msgInfo);
						}
					})
					this.$wsApi.onClose((e) => {
						console.log(e);
						if (e.code == 1006) {
							// 服务器主动断开
							this.$message.error("连接已断开，请重新登录");
							location.href = "/";
						} else {
							this.$wsApi.connect();
						}
					});
				}).catch((e) => {
					console.log("初始化失败",e);
				})
			},
			loadPrivateMessage(minId) {
				this.$store.commit("loadingPrivateMsg",true)
				this.$http({
					url: "/message/private/loadMessage?minId=" + minId,
					method: 'get'
				}).then((msgInfos) => {
					msgInfos.forEach((msgInfo) => {
						msgInfo.selfSend = msgInfo.sendId == this.$store.state.userStore.userInfo.id;
						let friendId = msgInfo.selfSend ? msgInfo.recvId : msgInfo.sendId;
						let friend = this.$store.state.friendStore.friends.find((f) => f.id == friendId);
						if(friend){
							this.insertPrivateMessage(friend,msgInfo);
						}	
					})
					if (msgInfos.length == 100) {
						// 继续拉取
						this.loadPrivateMessage(msgInfos[99].id);
					}else{
						this.$store.commit("loadingPrivateMsg",false)
					}
				})
			},
			loadGroupMessage(minId) {
				this.$store.commit("loadingGroupMsg",true)
				this.$http({
					url: "/message/group/loadMessage?minId=" + minId,
					method: 'get'
				}).then((msgInfos) => {
					msgInfos.forEach((msgInfo) => {
						// 标记这条消息是不是自己发的
						msgInfo.selfSend = msgInfo.sendId == this.$store.state.userStore.userInfo.id;
						let groupId = msgInfo.groupId;
						let group = this.$store.state.groupStore.groups.find((g) => g.id == groupId);
						this.handleGroupMessage(group,msgInfo);
					})
					if (msgInfos.length == 100) {
						// 继续拉取
						this.loadGroupMessage(msgInfos[99].id);
					}else{
						this.$store.commit("loadingGroupMsg",false)
					}
				})
			},
			handlePrivateMessage(msg) {
				// 标记这条消息是不是自己发的
				msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
				// 好友id
				let friendId = msg.selfSend ? msg.recvId : msg.sendId;
				// 消息已读处理
				if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
					if (msg.selfSend) {
						// 我已读对方的消息，清空已读数量
						let chatInfo = {
							type: 'PRIVATE',
							targetId: friendId
						}
						this.$store.commit("resetUnreadCount", chatInfo)
					} else {
						// 对方已读我的消息，修改消息状态为已读
						this.$store.commit("readedMessage", friendId)
					}
					return;
				}

				this.loadFriendInfo(friendId).then((friend) => {
					this.insertPrivateMessage(friend, msg);
				})
			},
			insertPrivateMessage(friend, msg) {
				// webrtc 信令
				if (msg.type >= this.$enums.MESSAGE_TYPE.RTC_CALL &&
					msg.type <= this.$enums.MESSAGE_TYPE.RTC_CANDIDATE) {
					// 呼叫
					if (msg.type == this.$enums.MESSAGE_TYPE.RTC_CALL ||
						msg.type == this.$enums.MESSAGE_TYPE.RTC_CANCEL) {
						this.$store.commit("showVideoAcceptorBox", friend);
						this.$refs.videoAcceptor.handleMessage(msg)
					} else {
						this.$refs.videoAcceptor.close()
						this.$refs.privateVideo.handleMessage(msg)
					}
					return;
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
				if(!msg.selfSend && msg.status != this.$enums.MESSAGE_STATUS.READED){
					this.playAudioTip();
				}
			},
			handleGroupMessage(msg) {
				// 标记这条消息是不是自己发的
				msg.selfSend = msg.sendId == this.$store.state.userStore.userInfo.id;
				let groupId = msg.groupId;
				// 消息已读处理
				if (msg.type == this.$enums.MESSAGE_TYPE.READED) {
					// 我已读对方的消息，清空已读数量
					let chatInfo = {
						type: 'GROUP',
						targetId: groupId
					}
					this.$store.commit("resetUnreadCount", chatInfo)
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
				this.$store.commit("openChat", chatInfo);
				// 插入消息
				this.$store.commit("insertMessage", msg);
				// 播放提示音
				if(!msg.selfSend && msg.status != this.$enums.MESSAGE_STATUS.READED){
					this.playAudioTip();
				}
			},
			handleExit() {
				this.$wsApi.close();
				sessionStorage.removeItem("accessToken");
				location.href = "/";
			},
			playAudioTip() {
				if(new Date() - this.lastPlayAudioTime > 1000){
					this.lastPlayAudioTime = new Date();
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
		background: #333333;
		padding: 10px;
		padding-top: 50px;

		.el-menu {
			border: none;
			flex: 1;

			.el-menu-item {
				margin: 25px 0;

				.router-link-exact-active span {
					color: white !important;
				}



				span {
					font-size: 24px !important;
					color: #aaaaaa;

					&:hover {
						color: white !important;
					}
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
			color: #aaaaaa;
			font-size: 24px;
			text-align: center;
			cursor: pointer;

			&:hover {
				color: white !important;
			}
		}
	}

	.content-box {
		padding: 0;
		background-color: #E9EEF3;
		color: #333;
		text-align: center;

	}
</style>
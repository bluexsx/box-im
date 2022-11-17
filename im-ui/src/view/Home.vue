<template>
	<el-container>
		<el-aside width="80px" class="navi-bar">
			<div class="user-head-image">
				<head-image :url="$store.state.userStore.userInfo.headImageThumb" :size="60" @click.native="showSettingDialog=true">
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
		<user-info v-show="uiStore.userInfo.show" :pos="uiStore.userInfo.pos" :user="uiStore.userInfo.user" @close="$store.commit('closeUserInfoBox')"></user-info>
		<full-image :visible="uiStore.fullImage.show" :url="uiStore.fullImage.url" @close="$store.commit('closeFullImageBox')"></full-image>
	</el-container>
</template>

<script>
	import HeadImage from '../components/common/HeadImage.vue';
	import Setting from '../components/setting/Setting.vue';
	import UserInfo from '../components/common/UserInfo.vue';
	import FullImage from '../components/common/FullImage.vue';

	export default {
		components: {
			HeadImage,
			Setting,
			UserInfo,
			FullImage
		},
		data() {
			return {
				showSettingDialog: false
			}
		},
		methods: {
			init(userInfo) {
				this.$store.commit("setUserInfo", userInfo);
				this.$store.commit("initStore");
				this.$wsApi.createWebSocket(process.env.VUE_APP_WS_URL, this.$store);
				this.$wsApi.onopen(() => {
					this.pullUnreadMessage();
				});
				this.$wsApi.onmessage((e) => {
					if (e.cmd == 2) {
						// 异地登录，强制下线
						this.$message.error("您已在其他地方登陆，将被强制下线");
						setTimeout(() => {
							location.href = "/";
						}, 1000)

					} else if (e.cmd == 3) {
						// 插入私聊消息
						this.handlePrivateMessage(e.data);
					} else if (e.cmd == 4) {
						// 插入群聊消息
						this.handleGroupMessage(e.data);
					}
				})
			},
			pullUnreadMessage() {
				// 拉取未读私聊消息
				this.$http({
					url: "/message/private/pullUnreadMessage",
					method: 'post'
				});
				// 拉取未读群聊消息
				this.$http({
					url: "/message/group/pullUnreadMessage",
					method: 'post'
				});
			},
			handlePrivateMessage(msg) {
				// 好友列表存在好友信息，直接插入私聊消息
				let friend = this.$store.state.friendStore.friends.find((f) => f.id == msg.sendId);
				if (friend) {
					this.insertPrivateMessage(friend, msg);
					return;
				}
				// 好友列表不存在好友信息，则发请求获取好友信息
				this.$http({
					url: `/friend/find/${msg.sendId}`,
					method: 'get'
				}).then((friend) => {
					this.insertPrivateMessage(friend, msg);
					this.$store.commit("addFriend", friend);
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
			},
			handleGroupMessage(msg) {
				// 群聊缓存存在，直接插入群聊消息
				let group = this.$store.state.groupStore.groups.find((g) => g.id == msg.groupId);
				if (group) {
					this.insertGroupMessage(group, msg);
					return;
				}
				// 群聊缓存存在，直接插入群聊消息
				this.$http({
					url: `/group/find/${msg.groupId}`,
					method: 'get'
				}).then((group) => {
					this.insertGroupMessage(group, msg);
					this.$store.commit("addGroup", group);
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
			},
			handleExit() {
				this.$http({
					url: "/logout",
					method: 'get'
				}).then(() => {
					this.$wsApi.closeWebSocket();
					location.href = "/";
				})
			},
			showSetting() {
				this.showSettingDialog = true;
			},
			closeSetting() {
				this.showSettingDialog = false;
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
			this.$http({
				url: "/user/self",
				methods: 'get'
			}).then((userInfo) => {
				this.init(userInfo);
			})
		},
		unmounted() {
			this.$wsApi.closeWebSocket();
		}
	}
</script>

<style scoped lang="scss">
	.navi-bar {
		background: #333333;
		padding: 10px;
		padding-top: 50px;

		.user-head-image {
			position: relative;
			width: 50px;
			height: 50px;
		}

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

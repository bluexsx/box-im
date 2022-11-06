<template>
	<el-container>
		<el-aside width="250px" class="l-chat-box">
			<div class="l-chat-header">
				<el-input width="200px" placeholder="搜索" v-model="searchText">
					<el-button slot="append" icon="el-icon-search"></el-button>
				</el-input>
			</div>
			<el-scrollbar class="l-chat-list" >
				<div v-for="(chat,index) in chatStore.chats" :key="chat.type+chat.targetId">
					<chat-item :chat="chat" :index="index" @click.native="handleActiveItem(index)" @del="handleDelItem(chat,index)"
					 :active="index === chatStore.activeIndex"></chat-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="r-chat-box">
			<chat-private :chat="activeChat" v-if="activeChat.type=='PRIVATE'"></chat-private>
			<chat-Group :chat="activeChat" v-if="activeChat.type=='GROUP'"></chat-Group>
		</el-container>
	</el-container>
</template>

<script>
	import ChatItem from "../components/chat/ChatItem.vue";
	import ChatTime from "../components/chat/ChatTime.vue";
	import MessageItem from "../components/chat/MessageItem.vue";
	import HeadImage from "../components/common/HeadImage.vue";
	import FileUpload from "../components/common/FileUpload.vue";
	import ChatPrivate from "../components/chat/ChatPrivate.vue";
	import ChatGroup from "../components/chat/ChatGroup.vue";
	
	export default {
		name: "chat",
		components: {
			ChatItem,
			ChatTime,
			HeadImage,
			FileUpload,
			MessageItem,
			ChatPrivate,
			ChatGroup
		},
		data() {
			return {
				searchText: "",
				messageContent: "",
				group: {},
				groupMembers: [] 
			}
		},
		methods: {
			handleActiveItem(index) {
				this.$store.commit("activeChat", index);
				let chat = this.chatStore.chats[index];
				if (chat.type == "PRIVATE") {
					this.refreshNameAndHeadImage(chat);
				} 
			},
			handleDelItem(chat, index) {
				this.$store.commit("removeChat", index);
			},
			sendGroupMessage() {
				let msgInfo = {
					groupId: this.activeChat.targetId,
					content: this.messageContent,
					type: 0
				}
				this.$http({
					url: '/message/group/send',
					method: 'post',
					data: msgInfo
				}).then((data) => {
					this.$message.success("发送成功");
					this.messageContent = "";
					msgInfo.sendTime = new Date().getTime();
					msgInfo.sendId = this.$store.state.userStore.userInfo.id;
					msgInfo.selfSend = true;
					this.$store.commit("insertMessage", msgInfo);
					// 保持输入框焦点
					this.$refs.sendBox.focus();
					// 滚动到底部
					this.scrollToBottom();
				})
			},
			refreshNameAndHeadImage(chat){
				// 获取对方最新信息
				let userId = chat.targetId;
				this.$http({
					url: `/user/find/${userId}`,
					method: 'get'
				}).then((user) => {
					// 如果发现好友的头像和昵称改了，进行更新
					if (user.headImageThumb != chat.headImage ||
						user.nickName != chat.showName) {
						this.updateFriendInfo(user)
						this.$store.commit("updateChatFromUser", user);
					}
				})
			},
			updateFriendInfo(user) {
				let friendInfo = {
					id: user.id,
					nickName: user.nickName,
					headImage: user.headImageThumb
				};
				this.$http({
					url: "/friend/update",
					method: "put",
					data: friendInfo
				}).then(() => {
					this.$store.commit("updateFriend", friendInfo);
				})
			}
			
		},
		computed: {
			chatStore() {
				return this.$store.state.chatStore;
			},
			activeChat() {
				let index = this.chatStore.activeIndex;
				let chats = this.chatStore.chats
				if (index >= 0 && chats.length > 0) {
					return chats[index];
				}
				// 当没有激活任何会话时，创建一个空会话，不然控制台会有很多报错
				let emptyChat = {
					targetId: -1,
					showName: "",
					headImage: "",
					messages: []
				}
				return emptyChat;
			}
		}
	}
</script>

<style lang="scss">
	.el-container {
		.l-chat-box {
			display: flex;
			flex-direction: column;
			border: #dddddd solid 1px;
			background: white;
			width: 3rem;

			.l-chat-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
			}
			
			.l-friend-ist{
				flex: 1;
			}
		}

		.r-chat-box {
			background: white;
			border: #dddddd solid 1px;

			.el-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
				border: #dddddd solid 1px;
			}

			.im-chat-main {
				padding: 0;
				border: #dddddd solid 1px;

				.im-chat-box {
					ul {
						padding: 20px;

						li {
							list-style-type: none;
						}
					}
				}
			}

			.im-chat-footer {
				display: flex;
				flex-direction: column;
				padding: 0;
				border: #dddddd solid 1px;	
				.chat-tool-bar {

					display: flex;
					position: relative;
					width: 100%;
					height: 40px;
					text-align: left;
					box-sizing: border-box;
					border: #dddddd solid 1px;

					>div {
						margin-left: 10px;
						font-size: 22px;
						cursor: pointer;
						color: #333333;
						line-height: 40px;

						&:hover {
							color: black;
						}
					}
				}

				.send-text-area {
					box-sizing: border-box;
					padding: 5px;
					width: 100%;
					flex: 1;
					resize: none;
					background-color: #f8f8f8 !important;
					outline-color: rgba(83, 160, 231, 0.61);
				}

				.im-chat-send {
					text-align: right;
					padding: 7px;
				}
			}
		}
	}
</style>

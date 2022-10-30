<template>
	<el-container>
		<el-aside width="250px" class="l-chat-box">
			<el-header height="60px">
				<el-row>
					<el-input width="200px" placeholder="搜索" v-model="searchText">
						<el-button slot="append" icon="el-icon-search"></el-button>
					</el-input>
				</el-row>
			</el-header>
			<el-main>
				<div v-for="(chat,index) in chatStore.chats" :key="chat.targetId">
					<chat-item :chat="chat" :index="index" @click.native="onClickItem(index)" @del="onDelItem(chat,index)" :active="index === chatStore.activeIndex"></chat-item>
				</div>
			</el-main>
		</el-aside>
		<el-container class="r-chat-box">
			<el-header height="60px">
				{{activeChat.showName}}
			</el-header>
			<el-main class="im-chat-main" id="chatScrollBox">
				<div class="im-chat-box">
					<ul>
						<li v-for="msgInfo in activeChat.messages" :key="msgInfo.id">
							<message-item :mine="msgInfo.sendUserId == $store.state.userStore.userInfo.id" 
							 :headImage="headImage(msgInfo)"
							 :showName="showName(msgInfo)" :msgInfo="msgInfo">
							</message-item>
						</li>
					</ul>
				</div>
			</el-main>
			<el-footer height="25%" class="im-chat-footer">
				<div class="chat-tool-bar">
					<div class="el-icon-service"></div>
					<div>
						<file-upload action="/api/image/upload" 
						:maxSize="5*1024*1024" 
						:fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/gif']"
						@before="handleImageBefore"
						@success="handleImageSuccess"
						@fail="handleImageFail" >
							<i class="el-icon-picture-outline"></i>
						</file-upload>
					</div>
					<div>
						<file-upload action="/api/file/upload"
						:maxSize="10*1024*1024" 
						@before="handleFileBefore"
						@success="handleFileSuccess"
						@fail="handleFileFail" >
							<i class="el-icon-wallet"></i>
						</file-upload>
					</div>
					<div class="el-icon-chat-dot-round"></div>
				</div>
				<textarea v-model="messageContent" ref="sendBox" class="send-text-area" @keyup.enter="onSendMessage()"></textarea>
				<div class="im-chat-send">
					
					<el-button type="primary" @click="onSendMessage()">发送</el-button>
				</div>
			</el-footer>
		</el-container>
	</el-container>
</template>

<script>
	import ChatItem from "../components/chat/ChatItem.vue";
	import ChatTime from "../components/chat/ChatTime.vue";
	import MessageItem from "../components/chat/MessageItem.vue";
	import HeadImage from "../components/common/HeadImage.vue";
	import FileUpload from "../components/common/FileUpload.vue";
	
	export default {
		name: "chat",
		components: {
			ChatItem,
			ChatTime,
			HeadImage,
			FileUpload,
			MessageItem
		},
		data() {
			return {
				searchText: "",
				messageContent: ""
			}
		},
		methods: {
			onClickItem(index) {
				this.$store.commit("activeChat", index);
				// 获取对方
				let userId = this.chatStore.chats[index].targetId;
				this.$http({
					url: `/api/user/find/${userId}`,
					method: 'get'
				}).then((userInfo) => {
					// 如果发现好友的头像和昵称改了，进行更新
					let chat = this.chatStore.chats[index];
					if (userInfo.headImageThumb != chat.headImage ||
						userInfo.nickName != chat.showName) {
						this.updateFriendInfo(userInfo, index)
					}
				})
			},
			onSendMessage() {
				let msgInfo = {
					recvUserId: this.activeChat.targetId,
					content: this.messageContent,
					type: 0
				}
				this.sendMessage(msgInfo);

			},
			onDelItem(chat, index) {
				this.$store.commit("removeChat", index);
			},

			handleImageSuccess(res, file) {
				let msgInfo = {
					recvUserId: file.raw.targetId,
					content: JSON.stringify(res.data),
					type: 1
				}
				this.$http({
					url: '/api/message/single/send',
					method: 'post',
					data: msgInfo
				}).then((data) => {
					let info = {
						targetId : file.raw.targetId,
						fileId: file.raw.uid,
						content: JSON.stringify(res.data),
						loadStatus: "ok"
					}
					this.$store.commit("handleFileUpload", info);
				})
			},
			handleImageFail(res,file){
				let info = {
					targetId : file.raw.targetId,
					fileId: file.raw.uid,
					loadStatus: "fail"
				}
				this.$store.commit("handleFileUpload", info);
			},
			handleImageBefore(file) {
				let url = URL.createObjectURL(file);
				let data = {
					originUrl : url,
					thumbUrl: url
				}
				let msgInfo = {
					fileId: file.uid,
					sendUserId: this.$store.state.userStore.userInfo.id,
					recvUserId: this.activeChat.targetId,
					content: JSON.stringify(data),
					sendTime: new Date().getTime(),
					selfSend: true,
					type: 1,
					loadStatus: "loadding"
				}
				// 插入消息
				this.$store.commit("insertMessage", msgInfo);
				// 滚动到底部
				this.scrollToBottom();
				// 借助file对象保存对方id
				file.targetId = this.activeChat.targetId;
			},
			handleFileSuccess(res, file) {
				console.log(res.data);
				let data = {
					name: file.name, 
					size: file.size,
					url: res.data
				}
				let msgInfo = {
					recvUserId: file.raw.targetId,
					content: JSON.stringify(data),
					type: 2
				}
				this.$http({
					url: '/api/message/single/send',
					method: 'post',
					data: msgInfo
				}).then(() => {
					let info = {
						targetId : file.raw.targetId,
						fileId: file.raw.uid,
						content: JSON.stringify(data),
						loadStatus: "ok"
					}
					console.log(info);
					this.$store.commit("handleFileUpload", info);
				})
			},
			handleFileFail(res, file) {
				let info = {
					targetId : file.raw.targetId,
					fileId: file.raw.uid,
					loadStatus: "fail"
				}
				this.$store.commit("handleFileUpload", info);
			},
			handleFileBefore(file) {
				let url = URL.createObjectURL(file);
				let data = {
					name: file.name, 
					size: file.size,
					url: url
				}
				let msgInfo = {
					fileId: file.uid,
					sendUserId: this.$store.state.userStore.userInfo.id,
					recvUserId: this.activeChat.targetId,
					content: JSON.stringify(data),
					sendTime: new Date().getTime(),
					selfSend: true,
					type: 2,
					loadStatus: "loading"
				}
				
				// 插入消息
				this.$store.commit("insertMessage", msgInfo);
				// 滚动到底部
				this.scrollToBottom();
				// 借助file对象保存对方id
				file.targetId = this.activeChat.targetId;
			},
			sendMessage(msgInfo) {
				this.$http({
					url: '/api/message/single/send',
					method: 'post',
					data: msgInfo
				}).then((data) => {
					this.$message.success("发送成功");
					this.messageContent = "";
					msgInfo.sendTime = new Date().getTime();
					msgInfo.sendUserId = this.$store.state.userStore.userInfo.id;
					msgInfo.selfSend = true;
					msgInfo.loadStatus = "ok";
					this.$store.commit("insertMessage", msgInfo);
					// 保持输入框焦点
					this.$refs.sendBox.focus();
					// 滚动到底部
					this.scrollToBottom();
				})
			},
			updateFriendInfo(userInfo, index) {
				let friendsInfo = {
					friendId: userInfo.id,
					friendNickName: userInfo.nickName,
					friendHeadImage: userInfo.headImageThumb
				};
				this.$http({
					url: "/api/friends/update",
					method: "put",
					data: friendsInfo
				}).then(() => {
					this.$store.commit("updateFriends", friendsInfo);
					this.$store.commit("setChatUserInfo", userInfo);
				})
			},
			showName(msg) {
				if (msg.sendUserId == this.$store.state.userStore.userInfo.id) {
					return this.$store.state.userStore.userInfo.nickName;
				}
				return this.activeChat.showName;
			},
			headImage(msg) {

				if (msg.sendUserId == this.$store.state.userStore.userInfo.id) {
					return this.$store.state.userStore.userInfo.headImageThumb;
				}
				return this.activeChat.headImage;
			},
			scrollToBottom(){
				this.$nextTick(() => {
					const div = document.getElementById("chatScrollBox");
					div.scrollTop = div.scrollHeight;
				});
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
				return this.emptyChat;
			},
			emptyChat() {
				// 当没有激活任何会话时，创建一个空会话，防止报错
				return {
					targetId: -1,
					showName: "",
					headImage: "",
					messages: []
				}
			}
		}
	}
</script>

<style lang="scss">
	.el-container {

		.l-chat-box {
			border: #dddddd solid 1px;
			background: #eeeeee;
			width: 3rem;

			.el-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
			}

			.el-main {
				padding: 0
			}
		}

		.r-chat-box {
			background: white;
			border: #dddddd solid 1px;

			.el-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
			}

			.im-chat-main {
				padding: 0;
				border: #dddddd solid 1px;

				.im-chat-box {
					ul {
						padding: 20px;
						
						li {
							list-style-type:none;
						}
					}
				}
			}



			.im-chat-footer {
				display: flex;
				flex-direction: column;
				padding: 0;

				.chat-tool-bar {
					
					display: flex;
					position: relative;
					width: 100%;
					height: 40px;
					text-align: left;
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

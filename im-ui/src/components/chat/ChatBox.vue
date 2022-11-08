<template>
	<el-container class="chat-box">
		<el-header height="60px">
			<span>{{title}}</span>
			<span title="群聊信息" v-show="this.chat.type=='GROUP'" class="btn-side el-icon-more" @click="showSide=!showSide"></span>
		</el-header>
		<el-container>
			<el-container class="content-box">
				<el-main class="im-chat-main" id="chatScrollBox">
					<div class="im-chat-box">
						<ul>
							<li v-for="msgInfo in chat.messages" :key="msgInfo.id">
								<message-item :mine="msgInfo.sendId == mine.id" :headImage="headImage(msgInfo)" :showName="showName(msgInfo)"
								 :msgInfo="msgInfo">
								</message-item>
							</li>
						</ul>
					</div>
				</el-main>
				<el-footer height="25%" class="im-chat-footer">
					<div class="chat-tool-bar">
						<div title="表情" class="el-icon-eleme" ref="emotion" @click="switchEmotionBox()">
							<emotion v-show="showEmotion" :pos="emoBoxPos" @emotion="handleEmotion"></Emotion>
						</div>
						<div title="发送图片">
							<file-upload :action="imageAction" :maxSize="5*1024*1024" :fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp','image/gif']"
							 @before="handleImageBefore" @success="handleImageSuccess" @fail="handleImageFail">
								<i class="el-icon-picture-outline"></i>
							</file-upload>
						</div>
						<div title="发送文件">
							<file-upload :action="fileAction" :maxSize="10*1024*1024" @before="handleFileBefore" @success="handleFileSuccess"
							 @fail="handleFileFail">
								<i class="el-icon-wallet"></i>
							</file-upload>
						</div>
						<div title="聊天记录" class="el-icon-chat-dot-round"></div>
					</div>
					<textarea v-model="sendText" ref="sendBox" class="send-text-area" @keydown.enter="sendTextMessage()"></textarea>
					<div class="im-chat-send">
						<el-button type="primary" @click="sendTextMessage()">发送</el-button>
					</div>
				</el-footer>
			</el-container>
			<el-aside class="chat-group-side-box" width="20%" v-show="showSide">
				<chat-group-side :group="group" :groupMembers="groupMembers" @reload="loadGroup(group.id)"></chat-group-side>
			</el-aside>
		</el-container>
	</el-container>
</template>

<script>
	import ChatGroupSide from "./ChatGroupSide.vue";
	import MessageItem from "./MessageItem.vue";
	import FileUpload from "../common/FileUpload.vue";
	import Emotion from "../common/Emotion.vue";

	export default {
		name: "chatPrivate",
		components: {
			MessageItem,
			FileUpload,
			ChatGroupSide,
			Emotion
		},
		props: {
			chat: {
				type: Object
			}
		},
		data() {
			return {
				friend: {},
				group: {},
				groupMembers: [],
				sendText: "",
				showSide: false, // 是否显示群聊信息栏
				showEmotion: false, // 是否显示emoji表情
				emoBoxPos: { // emoji表情弹出位置
					x: 0,
					y: 0
				}
			}
		},
		methods: {
			handleImageSuccess(res, file) {
				let msgInfo = {
					recvId: file.raw.targetId,
					content: JSON.stringify(res.data),
					type: 1
				}
				// 填充对方id
				this.setTargetId(msgInfo, this.chat.targetId);
				this.$http({
					url: this.messageAction,
					method: 'post',
					data: msgInfo
				}).then((data) => {
					let info = {
						type: this.chat.type,
						targetId: file.raw.targetId,
						fileId: file.raw.uid,
						content: JSON.stringify(res.data),
						loadStatus: "ok"
					}
					this.$store.commit("handleFileUpload", info);
				})
			},
			handleImageFail(res, file) {
				let info = {
					type: this.chat.type,
					targetId: file.raw.targetId,
					fileId: file.raw.uid,
					loadStatus: "fail"
				}
				this.$store.commit("handleFileUpload", info);
			},
			handleImageBefore(file) {
				let url = URL.createObjectURL(file);
				let data = {
					originUrl: url,
					thumbUrl: url
				}
				let msgInfo = {
					fileId: file.uid,
					sendId: this.mine.id,
					content: JSON.stringify(data),
					sendTime: new Date().getTime(),
					selfSend: true,
					type: 1,
					loadStatus: "loading"
				}
				// 填充对方id
				this.setTargetId(msgInfo, this.chat.targetId);
				// 插入消息
				this.$store.commit("insertMessage", msgInfo);
				// 滚动到底部
				this.scrollToBottom();
				// 借助file对象保存对方id
				file.targetId = this.chat.targetId;
			},
			handleFileSuccess(res, file) {
				let data = {
					name: file.name,
					size: file.size,
					url: res.data
				}
				let msgInfo = {
					content: JSON.stringify(data),
					type: 2
				}
				// 填充对方id
				this.setTargetId(msgInfo, this.chat.targetId);
				this.$http({
					url: this.messageAction,
					method: 'post',
					data: msgInfo
				}).then(() => {
					let info = {
						type: this.chat.type,
						targetId: file.raw.targetId,
						fileId: file.raw.uid,
						content: JSON.stringify(data),
						loadStatus: "ok"
					}
					this.$store.commit("handleFileUpload", info);
				})
			},
			handleFileFail(res, file) {
				let info = {
					type: this.chat.type,
					targetId: file.raw.targetId,
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
					sendId: this.mine.id,
					content: JSON.stringify(data),
					sendTime: new Date().getTime(),
					selfSend: true,
					type: 2,
					loadStatus: "loading"
				}
				// 填充对方id
				this.setTargetId(msgInfo, this.chat.targetId);
				// 插入消息
				this.$store.commit("insertMessage", msgInfo);
				// 滚动到底部
				this.scrollToBottom();
				// 借助file对象保存对方id
				file.targetId = this.chat.targetId;
			},
			handleCloseSide() {
				this.showSide = false;
			},
			switchEmotionBox() {
				this.showEmotion = !this.showEmotion;
				let width = this.$refs.emotion.offsetWidth;
				let left = this.$elm.fixLeft(this.$refs.emotion);
				let top = this.$elm.fixTop(this.$refs.emotion);
				this.emoBoxPos.y = top;
				this.emoBoxPos.x = left + width / 2;
			},
			handleEmotion(emoText) {
				this.sendText += emoText;
				// 保持输入框焦点
				this.$refs.sendBox.focus();
			},
			setTargetId(msgInfo, targetId) {
				if (this.chat.type == "GROUP") {
					msgInfo.groupId = targetId;
				} else {
					msgInfo.recvId = targetId;
				}
			},
			sendTextMessage() {

				if (!this.sendText.trim()) {
					this.$message.error("不能发送空白信息");
					return
				}
				let msgInfo = {
					content: this.sendText,
					type: 0
				}
				// 填充对方id
				this.setTargetId(msgInfo, this.chat.targetId);
				this.$http({
					url: this.messageAction,
					method: 'post',
					data: msgInfo
				}).then((data) => {
					this.$message.success("发送成功");
					this.sendText = "";
					msgInfo.sendTime = new Date().getTime();
					msgInfo.sendId = this.$store.state.userStore.userInfo.id;
					msgInfo.selfSend = true;
					this.$store.commit("insertMessage", msgInfo);
					// 保持输入框焦点
					this.$refs.sendBox.focus();
					// 滚动到底部
					this.scrollToBottom();
				})
				const e = window.event || arguments[0];
				if (e.key === 'Enter' || e.code === 'Enter' || e.keyCode === 13) {
					e.returnValue = false;
					e.preventDefault();
					return false;
				}
			},
			loadGroup(groupId) {
				this.$http({
					url: `/group/find/${groupId}`,
					method: 'get'
				}).then((group) => {
					this.group = group;
					this.$store.commit("updateChatFromGroup", group);
					this.$store.commit("updateGroup", group);

				});

				this.$http({
					url: `/group/members/${groupId}`,
					method: 'get'
				}).then((groupMembers) => {
					this.groupMembers = groupMembers;
				});
			},
			loadFriend(friendId) {
				// 获取对方最新信息
				this.$http({
					url: `/user/find/${friendId}`,
					method: 'get'
				}).then((friend) => {
					this.friend = friend;
					this.$store.commit("updateChatFromFriend", friend);
					this.$store.commit("updateFriend", friend);
				})
			},
			showName(msgInfo) {
				if (this.chat.type == 'Group') {
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.aliasName : "";
				} else {
					return msgInfo.sendId == this.mine.id ? this.mine.nickName : this.chat.showName
				}

			},
			headImage(msgInfo) {
				if (this.chat.type == 'Group') {
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.headImage : "";
				} else {
					return msgInfo.sendId == this.mine.id ? this.mine.headImageThumb : this.chat.headImage
				}
			},
			scrollToBottom() {
				this.$nextTick(() => {
					const div = document.getElementById("chatScrollBox");
					div.scrollTop = div.scrollHeight;
				});
			}
		},
		computed: {
			mine() {
				return this.$store.state.userStore.userInfo;
			},
			title() {
				let title = this.chat.showName;
				if (this.chat.type == "GROUP") {
					let size = this.groupMembers.filter(m => !m.quit).length;
					title += `(${size})`;
				}
				return title;
			},
			imageAction() {
				return `${process.env.VUE_APP_BASE_API}/image/upload`;
			},
			fileAction() {
				return `${process.env.VUE_APP_BASE_API}/file/upload`;
			},
			messageAction() {
				return `/message/${this.chat.type.toLowerCase()}/send`;
			}
		},
		watch: {
			chat: {
				handler(newChat, oldChat) {
					if(newChat.targetId > 0 && (newChat.type != oldChat.type || newChat.targetId != oldChat.targetId)){
						if (this.chat.type == "GROUP") {
							this.loadGroup(this.chat.targetId);
						} else {
							this.loadFriend(this.chat.targetId);
						}
						this.scrollToBottom();
						this.sendText = "";
						// 保持输入框焦点
						this.$refs.sendBox.focus();
					}		
				},
				deep: true
			}
		}
	}
</script>

<style lang="scss">
	.chat-box {
		background: white;
		border: #dddddd solid 1px;

		.el-header {
			padding: 5px;
			background-color: white;
			line-height: 50px;
			font-size: 20px;
			font-weight: 600;
			border: #dddddd solid 1px;

			.btn-side {
				position: absolute;
				right: 20px;
				line-height: 60px;
				font-size: 22px;
				cursor: pointer;

				&:hover {
					font-size: 30px;
				}
			}
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

		.chat-group-side-box {
			border: #dddddd solid 1px;
			animation: rtl-drawer-in .3s 1ms;
		}
	}
</style>

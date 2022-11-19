<template>
	<el-container class="chat-box">
		<el-header height="60px">
			<span>{{title}}</span>
			<span title="群聊信息" v-show="this.chat.type=='GROUP'" class="btn-side el-icon-more"
				@click="showSide=!showSide"></span>
		</el-header>
		<el-main style="padding: 0;">
			<el-container>
				<el-container class="content-box">
					<el-main class="im-chat-main" id="chatScrollBox">
						<div class="im-chat-box">
							<ul>
								<li v-for="(msgInfo,idx) in chat.messages" :key="idx">
									<message-item :mine="msgInfo.sendId == mine.id" :headImage="headImage(msgInfo)"
										:showName="showName(msgInfo)" :msgInfo="msgInfo"
										@delete="deleteMessage"
										@recall="recallMessage">
									</message-item>
								</li>
							</ul>
						</div>
					</el-main>
					<el-footer height="200px" class="im-chat-footer">
						<div class="chat-tool-bar">
							<div title="表情" class="icon iconfont icon-biaoqing" ref="emotion" @click="switchEmotionBox()">
							</div>
							<div title="发送图片">
								<file-upload :action="imageAction" :maxSize="5*1024*1024"
									:fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp','image/gif']"
								 @before="handleImageBefore" @success="handleImageSuccess" @fail="handleImageFail">
									<i class="el-icon-picture-outline"></i>
								</file-upload>
							</div>
							<div title="发送文件">
								<file-upload :action="fileAction" :maxSize="10*1024*1024" @before="handleFileBefore"
									@success="handleFileSuccess" @fail="handleFileFail">
									<i class="el-icon-wallet"></i>
								</file-upload>
							</div>
							<div title="发送语音" class="el-icon-microphone" @click="showVoiceBox()">
							</div>
							<div title="聊天记录" class="el-icon-chat-dot-round" @click="showHistoryBox()"></div>
						</div>
						<textarea v-model="sendText" ref="sendBox" class="send-text-area"
							@keydown.enter="sendTextMessage()"></textarea>
						<div class="im-chat-send">
							<el-button type="primary" @click="sendTextMessage()">发送</el-button>
						</div>
					</el-footer>
				</el-container>
				<el-aside class="chat-group-side-box" width="300px" v-show="showSide">
					<chat-group-side :group="group" :groupMembers="groupMembers" @reload="loadGroup(group.id)">
					</chat-group-side>
				</el-aside>
			</el-container>
		</el-main>
		<emotion v-show="showEmotion" :pos="emoBoxPos" @emotion="handleEmotion"></Emotion>
		<chat-voice :visible="showVoice" @close="closeVoiceBox" @send="handleSendVoice"></chat-voice>
		<chat-history :visible="showHistory" 
		:chat="chat" :friend="friend" :group="group" :groupMembers="groupMembers"
		@close="closeHistoryBox"></chat-history>
	</el-container>
</template>

<script>
	import ChatGroupSide from "./ChatGroupSide.vue";
	import MessageItem from "./MessageItem.vue";
	import FileUpload from "../common/FileUpload.vue";
	import Emotion from "../common/Emotion.vue";
	import ChatVoice from "./ChatVoice.vue";
	import ChatHistory from "./ChatHistory.vue";
	
	
	export default {
		name: "chatPrivate",
		components: {
			MessageItem,
			FileUpload,
			ChatGroupSide,
			Emotion,
			ChatVoice,
			ChatHistory
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
				showVoice: false, // 是否显示语音录制弹窗
				showSide: false, // 是否显示群聊信息栏
				showEmotion: false, // 是否显示emoji表情
				emoBoxPos: { // emoji表情弹出位置
					x: 0,
					y: 0
				},
				showHistory: false // 是否显示历史聊天记录
			}
		},
		methods: {
			handleImageSuccess(res, file) {
				let msgInfo = JSON.parse(JSON.stringify(file.raw.msgInfo));
				msgInfo.content = JSON.stringify(res.data);
				this.$http({
					url: this.messageAction,
					method: 'post',
					data: msgInfo
				}).then((id) => {
					msgInfo.loadStatus = 'ok';
					msgInfo.id = id;
					this.$store.commit("insertMessage", msgInfo);
				})
			},
			handleImageFail(res, file) {
				let msgInfo = JSON.parse(JSON.stringify(file.raw.msgInfo));
				msgInfo.loadStatus = 'fail';
				this.$store.commit("insertMessage", msgInfo);
			},
			handleImageBefore(file) {
				let url = URL.createObjectURL(file);
				let data = {
					originUrl: url,
					thumbUrl: url
				}
				let msgInfo = {
					id:0,
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
				// 借助file对象保存
				file.msgInfo = msgInfo;
			},
			handleFileSuccess(res, file) {
				let data = {
					name: file.name,
					size: file.size,
					url: res.data
				}
				let msgInfo = JSON.parse(JSON.stringify(file.raw.msgInfo));
				msgInfo.content = JSON.stringify(data);
				this.$http({
					url: this.messageAction,
					method: 'post',
					data: msgInfo
				}).then((id) => {
					msgInfo.loadStatus = 'ok';
					msgInfo.id = id;
					this.$store.commit("insertMessage", msgInfo);
				})
			},
			handleFileFail(res, file) {
				let msgInfo = JSON.parse(JSON.stringify(file.raw.msgInfo));
				msgInfo.loadStatus = 'fail';
				this.$store.commit("insertMessage", msgInfo);
			},
			handleFileBefore(file) {
				let url = URL.createObjectURL(file);
				let data = {
					name: file.name,
					size: file.size,
					url: url
				}
				let msgInfo = {
					id: 0,
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
				// 借助file对象透传
				file.msgInfo = msgInfo;
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
				this.showEmotion = false;
				// 保持输入框焦点
				this.$refs.sendBox.focus();
			},
			showVoiceBox() {
				this.showVoice = true;
			},
			closeVoiceBox() {
				this.showVoice = false;
			},
			showHistoryBox(){
				this.showHistory = true;
			},
			closeHistoryBox(){
				this.showHistory = false;
			},
			handleSendVoice(data) {
				let msgInfo = {
					content: JSON.stringify(data),
					type: 3
				}
				// 填充对方id
				this.setTargetId(msgInfo, this.chat.targetId);
				this.$http({
					url: this.messageAction,
					method: 'post',
					data: msgInfo
				}).then((id) => {
					this.$message.success("发送成功");
					msgInfo.id = id;
					msgInfo.sendTime = new Date().getTime();
					msgInfo.sendId = this.$store.state.userStore.userInfo.id;
					msgInfo.selfSend = true;
					this.$store.commit("insertMessage", msgInfo);
					// 保持输入框焦点
					this.$refs.sendBox.focus();
					// 滚动到底部
					this.scrollToBottom();
					// 关闭录音窗口
					this.showVoice = false;
				})
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
				}).then((id) => {
					this.$message.success("发送成功");
					this.sendText = "";
					msgInfo.id = id;
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
			deleteMessage(msgInfo){
				this.$confirm( '确认删除消息?','删除消息', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$store.commit("deleteMessage",msgInfo);
				});
			},
			recallMessage(msgInfo){
				this.$confirm('确认撤回消息?','撤回消息',  {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					let url = `/message/${this.chat.type.toLowerCase()}/recall/${msgInfo.id}`
					this.$http({
						url: url,
						method: 'delete'
					}).then(() => {
						this.$message.success("消息已撤回");
						msgInfo = JSON.parse(JSON.stringify(msgInfo));
						msgInfo.type = 10;
						msgInfo.content = '你撤回了一条消息';
						this.$store.commit("insertMessage",msgInfo);
					})
				});
				
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
				if (this.chat.type == 'GROUP') {
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.aliasName : "";
				} else {
					return msgInfo.sendId == this.mine.id ? this.mine.nickName : this.chat.showName
				}

			},
			headImage(msgInfo) {
				if (this.chat.type == 'GROUP') {
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
					if (newChat.targetId > 0 && (!oldChat || newChat.type != oldChat.type || newChat.targetId != oldChat.targetId)) {
						if (this.chat.type == "GROUP") {
							this.loadGroup(this.chat.targetId);
						} else {
							this.loadFriend(this.chat.targetId);
						}
						this.scrollToBottom();
						this.sendText = "";
						// 保持输入框焦点
						this.$nextTick(() => {
							this.$refs.sendBox.focus();
						})
					}
				},
				immediate: true
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
				>ul {
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

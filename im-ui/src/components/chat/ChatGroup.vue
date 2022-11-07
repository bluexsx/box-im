<template>
	<el-container class="r-chat-box">
		<el-header height="60px">
			<span>{{title}}</span>
			<span title="群聊信息" class="btn-side el-icon-more" @click="showSide=!showSide"></span>
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
				sendText: "",
				showSide: false,
				group: {},
				groupMembers: [],
				showEmotion: false,
				emoBoxPos: {
					x: 0,
					y: 0
				}
			}
		},
		methods: {
			handleImageSuccess(res, file) {
				let msgInfo = {
					groupId: file.raw.targetId,
					content: JSON.stringify(res.data),
					type: 1
				}
				this.$http({
					url: '/message/group/send',
					method: 'post',
					data: msgInfo
				}).then((data) => {
					let info = {
						type: 'GROUP',
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
					type: 'GROUP',
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
					groupId: this.chat.targetId,
					content: JSON.stringify(data),
					sendTime: new Date().getTime(),
					selfSend: true,
					type: 1,
					loadStatus: "loading"
				}
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
					groupId: file.raw.targetId,
					content: JSON.stringify(data),
					type: 2
				}
				this.$http({
					url: '/message/group/send',
					method: 'post',
					data: msgInfo
				}).then(() => {
					let info = {
						type: 'GROUP',
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
					type: 'GROUP',
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
					groupId: this.chat.targetId,
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
			},
			sendTextMessage() {

				if (!this.sendText.trim()) {
					this.$message.error("不能发送空白信息");
					return
				}
				let msgInfo = {
					groupId: this.chat.targetId,
					content: this.sendText,
					type: 0
				}
				this.$http({
					url: '/message/group/send',
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
				const e = window.event || arguments[0]
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
				});

				this.$http({
					url: `/group/members/${groupId}`,
					method: 'get'
				}).then((groupMembers) => {
					this.groupMembers = groupMembers;
				});

			},
			showName(msgInfo) {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.aliasName : "";
			},
			headImage(msgInfo) {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.headImage : "";
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
				let size = this.groupMembers.filter(m => !m.quit).length;
				return `${this.chat.showName}(${size})`;
			},
			imageAction() {
				return `${process.env.VUE_APP_BASE_API}/image/upload`;
			},
			fileAction() {
				return `${process.env.VUE_APP_BASE_API}/file/upload`;
			}

		},
		mounted() {
			console.log("group mount...")
			this.loadGroup(this.chat.targetId);
			this.scrollToBottom();
		}
	}
</script>

<style>
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


	.chat-group-side-box {
		border: #dddddd solid 1px;
		animation: rtl-drawer-in .3s 1ms;
	}
</style>

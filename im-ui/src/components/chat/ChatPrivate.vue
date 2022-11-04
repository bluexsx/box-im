<template>
	<el-container class="r-chat-box">
		<el-header height="60px">
			{{chat.showName}}
		</el-header>
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
				<div class="el-icon-service"></div>
				<div>
					<file-upload action="/api/image/upload" :maxSize="5*1024*1024" :fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/gif']"
					 @before="handleImageBefore" @success="handleImageSuccess" @fail="handleImageFail">
						<i class="el-icon-picture-outline"></i>
					</file-upload>
				</div>
				<div>
					<file-upload action="/api/file/upload" :maxSize="10*1024*1024" @before="handleFileBefore" @success="handleFileSuccess"
					 @fail="handleFileFail">
						<i class="el-icon-wallet"></i>
					</file-upload>
				</div>
				<div class="el-icon-chat-dot-round"></div>
			</div>
			<textarea v-model="sendText" ref="sendBox" class="send-text-area" @keyup.enter="sendTextMessage()"></textarea>
			<div class="im-chat-send">
				<el-button type="primary" @click="sendTextMessage()">发送</el-button>
			</div>
		</el-footer>
	</el-container>
</template>

<script>
	import MessageItem from "./MessageItem.vue";
	import FileUpload from "../common/FileUpload.vue";

	export default {
		name: "chatPrivate",
		components: {
			MessageItem,
			FileUpload
		},
		props: {
			chat: {
				type: Object
			}
		},
		data() {
			return {
				sendText: ""
			}
		},
		methods: {
			handleImageSuccess(res, file) {
				let msgInfo = {
					recvId: file.raw.targetId,
					content: JSON.stringify(res.data),
					type: 1
				}
				this.$http({
					url: '/api/message/private/send',
					method: 'post',
					data: msgInfo
				}).then((data) => {
					let info = {
						type: 'PRIVATE',
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
					type: 'PRIVATE',
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
					recvId: this.chat.targetId,
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
				console.log(res.data);
				let data = {
					name: file.name,
					size: file.size,
					url: res.data
				}
				let msgInfo = {
					recvId: file.raw.targetId,
					content: JSON.stringify(data),
					type: 2
				}
				this.$http({
					url: '/api/message/private/send',
					method: 'post',
					data: msgInfo
				}).then(() => {
					let info = {
						type: 'PRIVATE',
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
					type: 'PRIVATE',
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
					recvId: this.chat.targetId,
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
			sendTextMessage() {
				let msgInfo = {
					recvId: this.chat.targetId,
					content: this.sendText,
					type: 0
				}
				this.$http({
					url: '/api/message/private/send',
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
			},
			showName(msg) {
				return msg.sendId == this.mine.id ? this.mine.nickName : this.chat.showName
			},
			headImage(msg) {
				return msg.sendId == this.mine.id ? this.mine.headImageThumb : this.chat.headImage
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
			}
		},
		mounted() {
			console.log("private mount...")
			this.scrollToBottom();
		}
	}
</script>

<style>
</style>

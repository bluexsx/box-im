<template>
	<div class="chat-box" @click="closeRefBox()" @mousemove="readedMessage()">
		<el-container>
			<el-header height="50px">
				<span>{{ title }}</span>
				<span title="群聊信息" v-show="this.chat.type == 'GROUP'" class="btn-side el-icon-more"
					@click="showSide = !showSide"></span>
			</el-header>
			<el-main style="padding: 0;">
				<el-container>
					<el-container class="content-box">
						<el-main class="im-chat-main" id="chatScrollBox" @scroll="onScroll">
							<div class="im-chat-box">
								<ul>
									<li v-for="(msgInfo, idx) in chat.messages" :key="idx">
										<chat-message-item v-if="idx >= showMinIdx" @call="onCall(msgInfo.type)"
											:mine="msgInfo.sendId == mine.id" :headImage="headImage(msgInfo)"
											:showName="showName(msgInfo)" :msgInfo="msgInfo"
											:groupMembers="groupMembers" @delete="deleteMessage"
											@recall="recallMessage">
										</chat-message-item>
									</li>
								</ul>
							</div>
						</el-main>
						<el-footer height="220px" class="im-chat-footer">
							<div class="chat-tool-bar">
								<div title="表情" class="icon iconfont icon-emoji" ref="emotion"
									@click.stop="showEmotionBox()">
								</div>
								<div title="发送图片">
									<file-upload :action="'/image/upload'" :maxSize="5 * 1024 * 1024"
										:fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp', 'image/gif']"
										@before="onImageBefore" @success="onImageSuccess" @fail="onImageFail">
										<i class="el-icon-picture-outline"></i>
									</file-upload>
								</div>
								<div title="发送文件">
									<file-upload ref="fileUpload" :action="'/file/upload'" :maxSize="10 * 1024 * 1024"
										@before="onFileBefore" @success="onFileSuccess" @fail="onFileFail">
										<i class="el-icon-wallet"></i>
									</file-upload>
								</div>
								<div title="回执消息" v-show="chat.type == 'GROUP'" class="icon iconfont icon-receipt"
									:class="isReceipt ? 'chat-tool-active' : ''" @click="onSwitchReceipt">
								</div>
								<div title="发送语音" class="el-icon-microphone" @click="showRecordBox()">
								</div>
								<div title="语音通话" v-show="chat.type == 'PRIVATE'" class="el-icon-phone-outline"
									@click="showPrivateVideo('voice')">
								</div>
								<div title="语音通话" v-show="chat.type == 'GROUP'" class="el-icon-phone-outline"
									@click="onGroupVideo()">
								</div>
								<div title="视频通话" v-show="chat.type == 'PRIVATE'" class="el-icon-video-camera"
									@click="showPrivateVideo('video')">
								</div>
								<div title="聊天记录" class="el-icon-chat-dot-round" @click="showHistoryBox()"></div>
							</div>
							<div class="send-content-area">
								<ChatInput :ownerId="group.ownerId" ref="chatInputEditor" :group-members="groupMembers"
									@submit="sendMessage" />
								<div class="send-btn-area">
									<el-button type="primary" icon="el-icon-s-promotion"
										@click="notifySend()">发送</el-button>
								</div>
							</div>
						</el-footer>
					</el-container>
					<el-aside class="chat-group-side-box" width="260px" v-if="showSide">
						<chat-group-side :group="group" :groupMembers="groupMembers" @reload="loadGroup(group.id)">
						</chat-group-side>
					</el-aside>
				</el-container>
			</el-main>
			<emotion ref="emoBox" @emotion="onEmotion"></Emotion>
			<chat-record :visible="showRecord" @close="closeRecordBox" @send="onSendRecord"></chat-record>
			<group-member-selector ref="rtcSel" :groupId="group.id" @complete="onInviteOk"></group-member-selector>
			<rtc-group-join ref="rtcJoin" :groupId="group.id"></rtc-group-join>
			<chat-history :visible="showHistory" :chat="chat" :friend="friend" :group="group"
				:groupMembers="groupMembers" @close="closeHistoryBox"></chat-history>
		</el-container>
	</div>
</template>

<script>
import ChatGroupSide from "./ChatGroupSide.vue";
import ChatMessageItem from "./ChatMessageItem.vue";
import FileUpload from "../common/FileUpload.vue";
import Emotion from "../common/Emotion.vue";
import ChatRecord from "./ChatRecord.vue";
import ChatHistory from "./ChatHistory.vue";
import ChatAtBox from "./ChatAtBox.vue"
import GroupMemberSelector from "../group/GroupMemberSelector.vue"
import RtcGroupJoin from "../rtc/RtcGroupJoin.vue"
import ChatInput from "./ChatInput";


export default {
	name: "chatPrivate",
	components: {
		ChatInput,
		ChatMessageItem,
		FileUpload,
		ChatGroupSide,
		Emotion,
		ChatRecord,
		ChatHistory,
		ChatAtBox,
		GroupMemberSelector,
		RtcGroupJoin
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
			sendImageUrl: "",
			sendImageFile: "",
			placeholder: "",
			isReceipt: true,
			showRecord: false, // 是否显示语音录制弹窗
			showSide: false, // 是否显示群聊信息栏
			showHistory: false, // 是否显示历史聊天记录
			lockMessage: false, // 是否锁定发送，
			showMinIdx: 0, // 下标低于showMinIdx的消息不显示，否则页面会很卡置
			reqQueue: [],
			isSending: false
		}
	},
	methods: {
		moveChatToTop() {
			let chatIdx = this.$store.getters.findChatIdx(this.chat);
			this.$store.commit("moveTop", chatIdx);
		},
		closeRefBox() {
			this.$refs.emoBox.close();
			// this.$refs.atBox.close();
		},
		onCall(type) {
			if (type == this.$enums.MESSAGE_TYPE.ACT_RT_VOICE) {
				this.showPrivateVideo('voice');
			} else if (type == this.$enums.MESSAGE_TYPE.ACT_RT_VIDEO) {
				this.showPrivateVideo('video');
			}
		},
		onSwitchReceipt() {
			this.isReceipt = !this.isReceipt;
			this.refreshPlaceHolder();
		},
		onImageSuccess(data, file) {
			let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
			msgInfo.content = JSON.stringify(data);
			msgInfo.receipt = this.isReceipt;
			this.sendMessageRequest(msgInfo).then((m) => {
				msgInfo.loadStatus = 'ok';
				msgInfo.id = m.id;
				this.isReceipt = false;
				this.$store.commit("insertMessage", msgInfo);
			})
		},
		onImageFail(e, file) {
			let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
			msgInfo.loadStatus = 'fail';
			this.$store.commit("insertMessage", msgInfo);
		},
		onImageBefore(file) {
			let url = URL.createObjectURL(file);
			let data = {
				originUrl: url,
				thumbUrl: url
			}
			let msgInfo = {
				id: 0,
				tmpId: this.generateId(),
				fileId: file.uid,
				sendId: this.mine.id,
				content: JSON.stringify(data),
				sendTime: new Date().getTime(),
				selfSend: true,
				type: 1,
				readedCount: 0,
				loadStatus: "loading",
				status: this.$enums.MESSAGE_STATUS.UNSEND
			}
			// 填充对方id
			this.fillTargetId(msgInfo, this.chat.targetId);
			// 插入消息
			this.$store.commit("insertMessage", msgInfo);
			// 会话置顶
			this.moveChatToTop();
			// 滚动到底部
			this.scrollToBottom();
			// 借助file对象保存
			file.msgInfo = msgInfo;
		},
		onFileSuccess(url, file) {
			let data = {
				name: file.name,
				size: file.size,
				url: url
			}
			let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
			msgInfo.content = JSON.stringify(data);
			msgInfo.receipt = this.isReceipt
			this.sendMessageRequest(msgInfo).then((m) => {
				msgInfo.loadStatus = 'ok';
				msgInfo.id = m.id;
				this.isReceipt = false;
				this.refreshPlaceHolder();
				this.$store.commit("insertMessage", msgInfo);
			})
		},
		onFileFail(e, file) {
			let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
			msgInfo.loadStatus = 'fail';
			this.$store.commit("insertMessage", msgInfo);
		},
		onFileBefore(file) {
			let url = URL.createObjectURL(file);
			let data = {
				name: file.name,
				size: file.size,
				url: url
			}
			let msgInfo = {
				id: 0,
				tmpId: this.generateId(),
				sendId: this.mine.id,
				content: JSON.stringify(data),
				sendTime: new Date().getTime(),
				selfSend: true,
				type: 2,
				loadStatus: "loading",
				readedCount: 0,
				status: this.$enums.MESSAGE_STATUS.UNSEND
			}
			// 填充对方id
			this.fillTargetId(msgInfo, this.chat.targetId);
			// 插入消息
			this.$store.commit("insertMessage", msgInfo);
			// 会话置顶
			this.moveChatToTop();
			// 滚动到底部
			this.scrollToBottom();
			// 借助file对象透传
			file.msgInfo = msgInfo;
		},
		onCloseSide() {
			this.showSide = false;
		},
		onScrollToTop() {
			// 多展示10条信息
			this.showMinIdx = this.showMinIdx > 10 ? this.showMinIdx - 10 : 0;
		},
		onScroll(e) {
			let scrollElement = e.target
			let scrollTop = scrollElement.scrollTop
			if (scrollTop < 30) { // 在顶部,不滚动的情况
				// 多展示20条信息
				this.showMinIdx = this.showMinIdx > 20 ? this.showMinIdx - 20 : 0;
			}
		},
		showEmotionBox() {
			let width = this.$refs.emotion.offsetWidth;
			let left = this.$elm.fixLeft(this.$refs.emotion);
			let top = this.$elm.fixTop(this.$refs.emotion);
			this.$refs.emoBox.open({
				x: left + width / 2,
				y: top
			})
		},
		onEmotion(emoText) {
			this.$refs.chatInputEditor.insertEmoji(emoText);
		},
		showRecordBox() {
			this.showRecord = true;
		},
		closeRecordBox() {
			this.showRecord = false;
		},
		showPrivateVideo(mode) {
			let rtcInfo = {
				mode: mode,
				isHost: true,
				friend: this.friend,
			}
			// 通过home.vue打开单人视频窗口
			this.$eventBus.$emit("openPrivateVideo", rtcInfo);
		},
		onGroupVideo() {
			// 邀请成员发起通话
			let ids = [this.mine.id];
			let maxChannel = this.$store.state.configStore.webrtc.maxChannel;
			this.$refs.rtcSel.open(maxChannel, ids, ids);
		},
		onInviteOk(members) {
			if (members.length < 2) {
				return;
			}
			let userInfos = [];
			members.forEach(m => {
				userInfos.push({
					id: m.userId,
					nickName: m.showNickName,
					headImage: m.headImage,
					isCamera: false,
					isMicroPhone: true
				})
			})
			let rtcInfo = {
				isHost: true,
				groupId: this.group.id,
				inviterId: this.mine.id,
				userInfos: userInfos
			}
			// 通过home.vue打开多人视频窗口
			this.$eventBus.$emit("openGroupVideo", rtcInfo);
		},
		showHistoryBox() {
			this.showHistory = true;
		},
		closeHistoryBox() {
			this.showHistory = false;
		},
		onSendRecord(data) {
			let msgInfo = {
				content: JSON.stringify(data),
				type: 3,
				receipt: this.isReceipt
			}
			// 填充对方id
			this.fillTargetId(msgInfo, this.chat.targetId);
			this.sendMessageRequest(msgInfo).then((m) => {
				m.selfSend = true;
				this.$store.commit("insertMessage", m);
				// 会话置顶
				this.moveChatToTop();
				// 保持输入框焦点
				this.$refs.chatInputEditor.focus();
				// 滚动到底部
				this.scrollToBottom();
				// 关闭录音窗口
				this.showRecord = false;
				this.isReceipt = false;
				this.refreshPlaceHolder();
			})
		},
		fillTargetId(msgInfo, targetId) {
			if (this.chat.type == "GROUP") {
				msgInfo.groupId = targetId;
			} else {
				msgInfo.recvId = targetId;
			}
		},
		notifySend() {
			this.$refs.chatInputEditor.submit();
		},
		async sendMessage(fullList) {
			this.resetEditor();
			this.readedMessage();
			let sendText = this.isReceipt ? "【回执消息】" : "";
			let promiseList = [];
			for (let i = 0; i < fullList.length; i++) {
				let msg = fullList[i];
				switch (msg.type) {
					case "text":
						await this.sendTextMessage(sendText + msg.content, msg.atUserIds);
						break;
					case "image":
						await this.sendImageMessage(msg.content.file);
						break;
					case "file":
						await this.sendFileMessage(msg.content.file);
						break;
				}

			}
		},
		sendImageMessage(file) {
			return new Promise((resolve, reject) => {
				this.onImageBefore(file);
				let formData = new FormData()
				formData.append('file', file)
				this.$http.post("/image/upload", formData, {
					headers: {
						'Content-Type': 'multipart/form-data'
					}
				}).then((data) => {
					this.onImageSuccess(data, file);
					resolve();
				}).catch((res) => {
					this.onImageFail(res, file);
					reject();
				})
				this.$nextTick(() => this.$refs.chatInputEditor.focus());
				this.scrollToBottom();
			});
		},
		sendTextMessage(sendText, atUserIds) {
			return new Promise((resolve, reject) => {
				if (!sendText.trim()) {
					reject();
				}
				let msgInfo = {
					content: sendText,
					type: 0
				}
				// 填充对方id
				this.fillTargetId(msgInfo, this.chat.targetId);
				// 被@人员列表
				if (this.chat.type == "GROUP") {
					msgInfo.atUserIds = atUserIds;
					msgInfo.receipt = this.isReceipt;
				}
				this.lockMessage = true;
				this.sendMessageRequest(msgInfo).then((m) => {
					m.selfSend = true;
					this.$store.commit("insertMessage", m);
					// 会话置顶
					this.moveChatToTop();
				}).finally(() => {
					// 解除锁定
					this.scrollToBottom();
					this.isReceipt = false;
					resolve();
				});
			});
		},
		sendFileMessage(file) {
			return new Promise((resolve, reject) => {
				let check = this.$refs.fileUpload.beforeUpload(file);
				if (check) {
					this.$refs.fileUpload.onFileUpload({ file });
				}
			})
		},
		deleteMessage(msgInfo) {
			this.$confirm('确认删除消息?', '删除消息', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				this.$store.commit("deleteMessage", msgInfo);
			});
		},
		recallMessage(msgInfo) {
			this.$confirm('确认撤回消息?', '撤回消息', {
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
					msgInfo.status = this.$enums.MESSAGE_STATUS.RECALL;
					this.$store.commit("insertMessage", msgInfo);
				})
			});
		},
		readedMessage() {
			if (this.chat.unreadCount == 0) {
				return;
			}
			this.$store.commit("resetUnreadCount", this.chat)
			if (this.chat.type == "GROUP") {
				var url = `/message/group/readed?groupId=${this.chat.targetId}`
			} else {
				url = `/message/private/readed?friendId=${this.chat.targetId}`
			}
			this.$http({
				url: url,
				method: 'put'
			}).then(() => { })
		},
		loadReaded(fId) {
			this.$http({
				url: `/message/private/maxReadedId?friendId=${fId}`,
				method: 'get'
			}).then((id) => {
				this.$store.commit("readedMessage", {
					friendId: fId,
					maxId: id
				});
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
				return member ? member.showNickName : "";
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
		resetEditor() {

			this.$nextTick(() => {
				this.$refs.chatInputEditor.clear();
				this.$refs.chatInputEditor.focus();
			});
		},
		scrollToBottom() {
			this.$nextTick(() => {
				let div = document.getElementById("chatScrollBox");
				div.scrollTop = div.scrollHeight;
			});
		},
		refreshPlaceHolder() {
			if (this.isReceipt) {
				this.placeholder = "【回执消息】"
			} else if (this.$refs.editBox && this.$refs.editBox.innerHTML) {
				this.placeholder = ""
			} else {
				this.placeholder = "聊点什么吧~";
			}
		},
		sendMessageRequest(msgInfo) {
			return new Promise((resolve, reject) => {
				// 请求入队列，防止请求"后发先至"，导致消息错序
				this.reqQueue.push({ msgInfo, resolve, reject });
				this.processReqQueue();
			})
		},
		processReqQueue() {
			if (this.reqQueue.length && !this.isSending) {
				this.isSending = true;
				const reqData = this.reqQueue.shift();
				this.$http({
					url: this.messageAction,
					method: 'post',
					data: reqData.msgInfo
				}).then((res) => {
					reqData.resolve(res)
				}).catch((e) => {
					reqData.reject(e)
				}).finally(() => {
					this.isSending = false;
					// 发送下一条请求
					this.processReqQueue();
				})
			}
		},
		generateId() {
			// 生成临时id
			return String(new Date().getTime()) + String(Math.floor(Math.random() * 1000));
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
		messageAction() {
			return `/message/${this.chat.type.toLowerCase()}/send`;
		},
		unreadCount() {
			return this.chat.unreadCount;
		},
		messageSize() {
			if (!this.chat || !this.chat.messages) {
				return 0;
			}
			return this.chat.messages.length;
		}
	},
	watch: {
		chat: {
			handler(newChat, oldChat) {
				if (newChat.targetId > 0 && (!oldChat || newChat.type != oldChat.type ||
					newChat.targetId != oldChat.targetId)) {
					if (this.chat.type == "GROUP") {
						this.loadGroup(this.chat.targetId);
					} else {
						this.loadFriend(this.chat.targetId);
						// 加载已读状态
						this.loadReaded(this.chat.targetId)
					}
					// 滚到底部
					this.scrollToBottom();
					this.showSide = false;
					// 消息已读
					this.readedMessage()
					// 初始状态只显示30条消息
					let size = this.chat.messages.length;
					this.showMinIdx = size > 30 ? size - 30 : 0;
					// 重置输入框
					this.resetEditor();
					// 复位回执消息
					this.isReceipt = false;
					// 更新placeholder
					this.refreshPlaceHolder();
				}
			},
			immediate: true
		},
		messageSize: {
			handler(newSize, oldSize) {
				if (newSize > oldSize) {
					// 拉至底部
					this.scrollToBottom();
				}
			}
		}
	},
	mounted() {
		let div = document.getElementById("chatScrollBox");
		div.addEventListener('scroll', this.onScroll)
	}
}
</script>

<style lang="scss">
.chat-box {
	position: relative;
	width: 100%;
	background: #fff;

	.el-header {
		display: flex;
		justify-content: space-between;
		padding: 0 12px;
		line-height: 50px;
		font-size: var(--im-font-size-larger);
		border-bottom: var(--im-border);


		.btn-side {
			position: absolute;
			right: 20px;
			line-height: 50px;
			font-size: 20px;
			cursor: pointer;
			color: var(--im-text-color-light);
		}
	}

	.im-chat-main {
		padding: 0;
		background-color: #fff;

		.im-chat-box {
			>ul {
				padding: 0 20px;

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

		.chat-tool-bar {
			display: flex;
			position: relative;
			width: 100%;
			height: 36px;
			text-align: left;
			box-sizing: border-box;
			border-top: var(--im-border);
			padding: 4px 2px 2px 8px;

			>div {
				font-size: 22px;
				cursor: pointer;
				line-height: 30px;
				width: 30px;
				height: 30px;
				text-align: center;
				border-radius: 2px;
				margin-right: 8px;
				color: #999;
				transition: 0.3s;

				&.chat-tool-active {
					font-weight: 600;
					color: var(--im-color-primary);
					background-color: #ddd;
				}
			}

			>div:hover {
				color: #333;
			}
		}

		.send-content-area {
			position: relative;
			display: flex;
			flex-direction: column;
			height: 100%;
			background-color: white !important;

			.send-text-area {
				box-sizing: border-box;
				padding: 5px;
				width: 100%;
				flex: 1;
				resize: none;
				font-size: 16px;
				outline: none;

				text-align: left;
				line-height: 30px;

				&:before {
					content: attr(placeholder);
					color: gray;
				}

				.at {
					color: blue;
					font-weight: 600;
				}

				.receipt {
					color: darkblue;
					font-size: 15px;
					font-weight: 600;
				}

				.emo {
					width: 30px;
					height: 30px;
					vertical-align: bottom;
				}
			}

			.send-image-area {
				text-align: left;
				border: #53a0e7 solid 1px;

				.send-image-box {
					position: relative;
					display: inline-block;

					.send-image {
						max-height: 180px;
						border: 1px solid #ccc;
						border-radius: 2%;
						margin: 2px;
					}

					.send-image-close {
						position: absolute;
						padding: 3px;
						right: 7px;
						top: 7px;
						color: white;
						cursor: pointer;
						font-size: 15px;
						font-weight: 600;
						background-color: #aaa;
						border-radius: 50%;
						border: 1px solid #ccc;
					}
				}
			}

			.send-btn-area {
				padding: 10px;
				position: absolute;
				bottom: 4px;
				right: 6px;
			}
		}
	}

	.chat-group-side-box {
		border-left: var(--im-border);
		//animation: rtl-drawer-in .3s 1ms;
	}

}
</style>

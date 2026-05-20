<template>
	<div class="chat-box" @click="onClickChatBox()" @mousemove="readedMessage()">
		<el-container>
			<el-header height="50px">
				<span>{{ title }}</span>
				<span title="群聊信息" v-show="isGroup" class="btn-side el-icon-more" @click="showSide = !showSide"></span>
			</el-header>
			<el-main style="padding: 0;">
				<el-container>
					<el-container class="content-box">
						<el-main class="im-chat-main" id="chatScrollBox" @scroll="onScroll">
							<div class="im-chat-box">
								<div v-for="m in messages" :key="m.localId">
									<chat-message-item :id="m.localId" :active="activeMessageLocalId == m.localId"
										@call="onCall(m.type)" :mine="m.sendId == mine.id" :headImage="headImage(m)"
										:showName="showName(m)" :group="group" :conversation="conversation" :message="m"
										:groupMemberMap="groupMemberMap" @resend="onResendMessage"
										@delete="deleteMessage" @recall="recallMessage">
									</chat-message-item>
								</div>
							</div>
						</el-main>
						<div v-if="conversation.atMe || conversation.atAll" class="locate-tip"
							@click="scrollToAtMessage">
							有人@我 </div>
						<div v-else-if="!chatStore.isInBottom" class="locate-tip" @click="onScrollToBottom">
							{{ chatStore.newMessageSize > 0 ? chatStore.newMessageSize + '条新消息' : '回到底部' }}
						</div>
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
								<div title="回执消息" v-show="isGroup" class="icon iconfont icon-receipt"
									:class="isReceipt ? 'chat-tool-active' : ''" @click="onSwitchReceipt">
								</div>
								<div title="发送语音" class="el-icon-microphone" @click="showRecordBox()">
								</div>
								<div title="语音通话" v-show="isPrivate" class="el-icon-phone-outline"
									@click="showPrivateVideo('voice')">
								</div>
								<div title="语音通话" v-show="isGroup" class="el-icon-phone-outline"
									@click="onGroupVideo()">
								</div>
								<div title="视频通话" v-show="isPrivate" class="el-icon-video-camera"
									@click="showPrivateVideo('video')">
								</div>
							</div>
							<div class="send-content-area">
								<ChatInput :ownerId="group.ownerId" ref="chatInputEditor" :group-members="groupMembers"
									@submit="sendMessage" />
								<div class="send-btn-area">
									<el-button type="primary" icon="el-icon-s-promotion"
										@click="notifySend()">发送</el-button>
								</div>
							</div>
							<div class="chat-editer-mask" v-if="notAllowInputTip">
								<span class="icon el-icon-warning"></span>
								<span>{{ notAllowInputTip }}</span>
							</div>
						</el-footer>
					</el-container>
					<el-aside class="side-box" width="320px" v-if="showSide">
						<chat-group-side :group="group" :groupMembers="groupMembers" @reload="loadGroup(group.id)">
						</chat-group-side>
					</el-aside>
				</el-container>
			</el-main>
			<emotion ref="emoBox" @emotion="onEmotion"></Emotion>
			<chat-record :visible="showRecord" @close="closeRecordBox" @send="onSendRecord"></chat-record>
			<group-member-selector ref="rtcSel" :group="group" @complete="onInviteOk"></group-member-selector>
			<rtc-group-join ref="rtcJoin" :groupId="group.id"></rtc-group-join>
		</el-container>
	</div>
</template>

<script>
import ChatGroupSide from "./ChatGroupSide.vue";
import ChatMessageItem from "./ChatMessageItem.vue";
import FileUpload from "../common/FileUpload.vue";
import Emotion from "../common/Emotion.vue";
import ChatRecord from "./ChatRecord.vue";
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
		ChatAtBox,
		GroupMemberSelector,
		RtcGroupJoin
	},
	props: {
		conversation: {
			type: Object
		}
	},
	data() {
		return {
			userInfo: {},
			groupId: null,
			isReceipt: true,
			showRecord: false, // 是否显示语音录制弹窗
			showSide: false, // 是否显示群聊信息栏
			activeMessageLocalId: '', //选中消息
			reqQueue: [], // 等待发送的请求队列
			isSending: false, // 是否正在发消息
			lastScrollTime: 0
		}
	},
	methods: {
		onClickChatBox() {
			// 关闭表情窗口
			this.$refs.emoBox.close();
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
		},
		async onImageBefore(file) {
			const url = URL.createObjectURL(file);
			const data = {
				originUrl: url,
				thumbUrl: url
			}
			const message = {
				localId: this.$nextSnowflakeId(),
				content: JSON.stringify(data),
				type: this.$enums.MESSAGE_TYPE.IMAGE,
				receipt: this.isReceipt
			}
			// 填充对方id
			this.fillTargetId(message, this.conversation.targetId);
			this.isReceipt = false;
			// 本地消息
			const localMessage = this.buildLocalMessage(message);
			// 插入消息
			await this.insertMessage(localMessage)
			// 借助file对象保存
			file.message = message;
			file.localMessage = localMessage;
			file.conversation = this.conversation;
			// 更新图片尺寸
			const size = await this.getImageSize(file)
			data.width = size.width;
			data.height = size.height;
			localMessage.content = JSON.stringify(data)
			await this.chatStore.updateMessage(this.conversation.key, localMessage);
			this.scrollToBottom();
		},
		async onImageSuccess(data, file) {
			const message = file.message;
			message.content = JSON.stringify(data);
			await this.processSendMessage(file.conversation, message, file.localMessage);
		},
		async onImageFail(e, file) {
			const localMessage = file.localMessage;
			localMessage.status = this.$enums.MESSAGE_STATUS.FAILED;
			await this.chatStore.updateMessage(this.conversation.key, localMessage);
		},
		async onFileBefore(file) {
			const url = URL.createObjectURL(file);
			const data = {
				name: file.name,
				size: file.size,
				url: url
			}
			const message = {
				localId: this.$nextSnowflakeId(),
				content: JSON.stringify(data),
				type: this.$enums.MESSAGE_TYPE.FILE,
				receipt: this.isReceipt
			}
			// 填充对方id
			this.fillTargetId(message, this.conversation.targetId);
			this.isReceipt = false;
			// 本地消息
			const localMessage = this.buildLocalMessage(message);
			// 插入消息
			await this.insertMessage(localMessage)
			// 借助file对象保存
			file.message = message;
			file.localMessage = localMessage;
			file.conversation = this.conversation;
		},
		async onFileSuccess(url, file) {
			const data = {
				name: file.name,
				size: file.size,
				url: url
			}
			const message = file.message;
			message.content = JSON.stringify(data);
			await this.processSendMessage(file.conversation, message, file.localMessage);
		},
		async onFileFail(e, file) {
			const localMessage = file.localMessage;
			localMessage.status = this.$enums.MESSAGE_STATUS.FAILED;
			await this.chatStore.updateMessage(this.conversation.key, localMessage);
		},
		onCloseSide() {
			this.showSide = false;
		},
		async onScroll(e) {
			let scrollElement = e.target
			let scrollTop = scrollElement.scrollTop
			// 滚到顶部
			if (scrollTop < 30) {
				if (new Date().getTime() - this.lastScrollTime < 500) {
					return;
				}
				this.lastScrollTime = new Date().getTime();
				if (!this.chatStore.hasMoreLastMessage) {
					this.$message.success("没有更多消息了");
					return;
				}
				const hst = scrollElement.scrollHeight;
				await this.chatStore.loadLastPageMessage(this.conversation.key, 30);
				await this.$nextTick();
				// 恢复滚动条位置
				scrollElement.scrollTop = scrollElement.scrollHeight - hst;
				// 清除底部标志
				this.chatStore.setIsInBottom(false);
			}
			// 滚到底部
			if (scrollTop + scrollElement.clientHeight >= scrollElement.scrollHeight - 30) {
				if (new Date().getTime() - this.lastScrollTime < 500) {
					return;
				}
				this.lastScrollTime = new Date().getTime();
				if (this.chatStore.hasMoreNextMessage) {
					// 向下翻页
					await this.chatStore.loadNextPageMessage(this.conversation.key, 30);
				}
				// 设置底部标志
				this.chatStore.setIsInBottom(!this.chatStore.hasMoreNextMessage);
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
			if (this.notAllowInputTip) {
				this.$message.warning(this.notAllowInputTip);
				return;
			}
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
			let maxChannel = this.configStore.webrtc.maxChannel;
			this.$refs.rtcSel.open(maxChannel, ids, ids, []);
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
					isMicroPhone: true,
					isShareScreen: false
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
		async onSendRecord(data) {
			const message = {
				localId: this.$nextSnowflakeId(),
				content: JSON.stringify(data),
				type: this.$enums.MESSAGE_TYPE.AUDIO,
				receipt: this.isReceipt
			}
			// 填充对方id
			this.fillTargetId(message, this.conversation.targetId);
			this.isReceipt = false;
			// 本地消息	
			const localMessage = this.buildLocalMessage(message);
			await this.insertMessage(localMessage);
			await this.processSendMessage(this.conversation, message, localMessage);
			// 关闭录音窗口
			this.showRecord = false;
			// 保持输入框焦点
			this.$refs.chatInputEditor.focus();
		},
		fillTargetId(message, targetId) {
			if (this.isGroup) {
				message.groupId = targetId;
			} else {
				message.recvId = targetId;
			}
		},
		notifySend() {
			this.$refs.chatInputEditor.submit();
		},
		async sendMessage(fullList) {
			this.resetEditor();
			this.readedMessage();
			let sendText = this.isReceipt ? '【回执消息】' : "";
			fullList.forEach(async msg => {
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
			})
		},
		async sendImageMessage(file) {
			await this.onImageBefore(file);
			const formData = new FormData()
			formData.append('file', file)
			this.$http.post("/image/upload?isPermanent=false", formData, {
				headers: {
					'Content-Type': 'multipart/form-data'
				}
			}).then((data) => {
				this.onImageSuccess(data, file);
			}).catch((res) => {
				this.onImageFail(res, file);
			})
			this.$nextTick(() => this.$refs.chatInputEditor.focus());
			this.scrollToBottom();
		},
		async sendTextMessage(sendText, atUserIds) {
			if (!sendText.trim()) {
				return;
			}
			const message = {
				localId: this.$nextSnowflakeId(),
				content: sendText,
				type: this.$enums.MESSAGE_TYPE.TEXT
			}
			// 填充对方id
			this.fillTargetId(message, this.conversation.targetId);
			// 被@人员列表
			if (this.isGroup) {
				message.atUserIds = atUserIds;
				message.receipt = this.isReceipt;
			}
			// 本地消息
			const localMessage = this.buildLocalMessage(message);
			await this.insertMessage(localMessage);
			// 清空标志
			this.isReceipt = false;
			// 发送
			await this.processSendMessage(this.conversation, message, localMessage);
		},
		async sendFileMessage(file) {
			let check = this.$refs.fileUpload.beforeUpload(file);
			if (check) {
				this.$refs.fileUpload.onFileUpload({ file });
			}
		},
		async onResendMessage(message) {
			if (message.type != this.$enums.MESSAGE_TYPE.TEXT) {
				this.$message.error('该消息不支持自动重新发送，建议手动重新发送')
				return;
			}
			// 删除旧消息
			await this.chatStore.deleteMessage(this.conversation.key, message);
			// 重新推送
			const sendMessage = JSON.parse(JSON.stringify(message));
			sendMessage.localId = this.$nextSnowflakeId();
			const localMessage = this.buildLocalMessage(sendMessage);
			await this.insertMessage(localMessage);
			await this.processSendMessage(this.conversation, sendMessage, localMessage);
		},
		deleteMessage(message) {
			this.$confirm('确认删除消息?', '删除消息', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(async () => {
				const convKey = this.conversation.key;
				if (message.id) {
					const data = {
						chatId: this.conversation.targetId,
						messageIds: [message.id]
					}
					await this.$http({
						url: `/message/${this.chatTypeText(this.conversation)}/deleteMessage`,
						method: 'delete',
						data: data
					});
				}
				this.chatStore.deleteMessage(convKey, message);
			});
		},
		recallMessage(message) {
			this.$confirm('确认撤回消息?', '撤回消息', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
			    const convKey = this.conversation.key;
				const url = `/message/${this.chatTypeText()}/recall/${message.id}`
				this.$http({
					url: url,
					method: 'delete'
				}).then((m) => {
					this.$message.success("消息已撤回");
					m.selfSend = true;
					this.chatStore.recallMessage(convKey, m);
				})
			});
		},
		async locateMessage(message) {
			const localId = message.localId;
			const locateMessage = await this.$db.findMessageByLocalId(localId);
			if (!locateMessage || locateMessage.deleted || this.isRecall(locateMessage)) {
				this.$message.error('无法定位原消息');
				return;
			}
			await this.chatStore.locateToMessage(this.conversation.key, locateMessage)
			// 定位消息
			this.scrollToMessage(localId, 100, 0);
			// 选中消息
			this.activeMessageLocalId = localId;
			// 设置底部标记
			this.chatStore.setIsInBottom(!this.chatStore.hasMoreNextMessage);
		},
		scrollToMessage(id, delay, times) {
			setTimeout(() => {
				const messgaeItem = document.getElementById(id);
				if (messgaeItem) {
					messgaeItem.scrollIntoView({ behavior: 'smooth' });
				} else if (times < 3) {
					this.scrollToMessage(id, delay * 3, times + 1)
				} else {
					console.log("消息定位失败", delay)
				}
			}, delay)
		},
		async scrollToAtMessage() {
			if (this.conversation.lastAtMessageId < 0) {
				return;
			}
			const atMessage = await this.$db.findMessageById(this.conversation.lastAtMessageId);
			if (!atMessage) {
				this.$message.error('无法定位原消息');
				return;
			}
			await this.locateMessage(atMessage);
			await this.chatStore.resetAtMessage(this.conversation.key);
		},
		async readedMessage() {
			if (this.conversation.unreadCount > 0) {
				const convKey = this.conversation.key;
				this.chatStore.resetUnreadCount(convKey);
				const tid = this.conversation.targetId;
				let url = "";
				if (this.isGroup) {
					url = `/message/group/readed?groupId=${tid}&messageId=${this.maxMessageId}`;
				} else {
					url = `/message/private/readed?friendId=${tid}&messageId=${this.maxMessageId}`;
				}
				await this.$http({
					url: url,
					method: 'put'
				})
			}
		},
		async loadReaded(fId) {
			const convKey = this.conversation.key;
			const messageId = await this.$http({
				url: `/message/private/maxReadedId?friendId=${fId}`,
				method: 'get'
			})
			this.chatStore.readedMessage(convKey, messageId);
		},
		async loadGroup(groupId) {
			this.groupId = groupId;
			const group = await this.$http({
				url: `/group/find/${groupId}`,
				method: 'get'
			})
			await this.chatStore.updateFromGroup(group);
			this.groupStore.updateGroup(group);
			this.groupStore.refreshMember(groupId);
		},
		async updateFriendInfo() {
			if (this.isFriend) {
				// store的数据不能直接修改，深拷贝一份store的数据
				const friend = JSON.parse(JSON.stringify(this.friend));
				friend.headImage = this.userInfo.headImageThumb;
				friend.nickName = this.userInfo.nickName;
				await this.chatStore.updateFromFriend(friend);
				this.friendStore.updateFriend(friend);
			} else {
				await this.chatStore.updateFromUser(this.userInfo);
			}
		},
		async loadFriend(friendId) {
			// 获取好友信息
			const userInfo = await this.$http({
				url: `/user/find/${friendId}`,
				method: 'GET'
			});
			this.userInfo = userInfo;
			await this.updateFriendInfo();
		},
		showName(message) {
			if (!message) return "";
			if (this.isGroup) {
				const member = this.groupMemberMap.get(message.sendId);
				return member ? member.showNickName : "";
			} else if (message.sendId == this.mine.id) {
				return this.mine.nickName;
			} else {
				return this.conversation.showName;
			}
		},
		headImage(message) {
			if (this.isGroup) {
				const member = this.groupMemberMap.get(message.sendId);
				return member ? member.headImage : "";
			} else {
				return message.selfSend ? this.mine.headImageThumb : this.conversation.headImage
			}
		},
		resetEditor() {
			this.$nextTick(() => {
				this.$refs.chatInputEditor.clear();
				this.$refs.chatInputEditor.focus();
			});
		},
		async onScrollToBottom() {
			await this.chatStore.resetMessages(this.conversation.key);
			this.scrollToBottom();
		},
		async scrollToBottom() {
			this.$nextTick(() => {
				let div = document.getElementById("chatScrollBox");
				div.scrollTop = div.scrollHeight;
				this.chatStore.setIsInBottom(true);
			});
		},
		async processSendMessage(conv, message, localMessage) {
			// 发送
			const m = await this.sendMessageRequest(conv, message).catch(async (e) => {
				// 更新本地消息
				localMessage.status = this.$enums.MESSAGE_STATUS.FAILED;
				await this.chatStore.updateMessage(conv.key, localMessage);
			})
			if (m) {
				// 更新本地消息
				m.selfSend = true;
				m.convKey = conv.key;
				await this.chatStore.updateMessage(conv.key, m);
			}
		},
		sendMessageRequest(conv, message) {
			return new Promise((resolve, reject) => {
				// 请求入队列，防止请求"后发先至"，导致消息错序
				const action = this.messageAction(conv)
				this.reqQueue.push({ action, message, resolve, reject });
				this.processReqQueue();
			})
		},
		processReqQueue() {
			if (this.reqQueue.length && !this.isSending) {
				this.isSending = true;
				const reqData = this.reqQueue.shift();
				this.$http({
					url: reqData.action,
					method: 'post',
					data: reqData.message
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
		buildLocalMessage(message) {
			const m = JSON.parse(JSON.stringify(message));
			m.convKey = this.conversation.key;
			m.seqNo = Math.max(1, this.conversation.maxSeqNo);
			m.sendId = this.mine.id;
			m.sendTime = new Date().getTime();
			m.status = this.$enums.MESSAGE_STATUS.SENDING;
			m.selfSend = true;
			if (this.isGroup) {
				m.readedCount = 0;
			}
			return m;
		},
		getImageSize(file) {
			return new Promise((resolve, reject) => {
				const reader = new FileReader();
				reader.onload = function (event) {
					const img = new Image();
					img.onload = function () {
						resolve({ width: img.width, height: img.height });
					};
					img.onerror = function () {
						reject(new Error('无法加载图片'));
					};
					img.src = event.target.result;
				};
				reader.onerror = function () {
					reject(new Error('无法读取文件'));
				};
				reader.readAsDataURL(file);
			});
		},
		async insertMessage(message) {
			if (!this.chatStore.isInBottom) {
				await this.chatStore.resetMessages(this.conversation.key);
			}
			await this.chatStore.insertMessage(this.conversation.key, message)
			await this.chatStore.moveTop(this.conversation.key);
			this.scrollToBottom();
		},
		messageAction(conv) {
			return `/message/${this.chatTypeText(conv)}/send`;
		},
		chatTypeText(conv) {
			conv = conv ? conv : this.conversation;
			return conv.type == this.$enums.CONVERSATION_TYPE.PRIVATE ? "private" : "group";
		},
		isRecall(m) {
			return m.status == this.$enums.MESSAGE_STATUS.RECALL;
		}
	},
	computed: {
		mine() {
			return this.userStore.userInfo;
		},
		isFriend() {
			return this.friendStore.isFriend(this.userInfo.id);
		},
		friend() {
			return this.friendStore.findFriend(this.userInfo.id) || {}
		},
		group() {
			return this.groupStore.findGroup(this.groupId) || {}
		},
		groupMembers() {
			return this.group.members || [];
		},
		groupMemberMap() {
			return new Map(this.groupMembers.map(m => [m.userId, m]));
		},
		title() {
			let title = this.conversation.showName;
			if (this.isGroup) {
				let size = this.groupMembers.filter(m => !m.quit).length;
				title += `(${size})`;
			}
			return title;
		},
		unreadCount() {
			return this.conversation.unreadCount;
		},
		messages() {
			return this.chatStore.messages;
		},
		memberSize() {
			return this.groupMembers.filter(m => !m.quit).length;
		},
		isPrivate() {
			return this.$enums.CONVERSATION_TYPE.PRIVATE == this.conversation.type
		},
		isGroup() {
			return this.$enums.CONVERSATION_TYPE.GROUP == this.conversation.type
		},
		loading() {
			return this.chatStore.loading;
		},
		maxMessageId() {
			return this.conversation.maxMessageId;
		},
		notAllowInputTip() {
			if (this.isGroup) {
				if (this.group.dissolve) {
					return '群聊已解散';
				} else if (this.group.quit) {
					return '您已不在群聊中';
				} else if (this.group.isBanned) {
					return '群聊已被封禁' + (this.group.reason ? '，原因：' + this.group.reason : '');
				}
			} else if (this.userInfo.isBanned) {
				return '对方账号已被封禁' + (this.userInfo.reason ? '，原因：' + this.userInfo.reason : '');
			}
			return '';
		}
	},
	watch: {
		conversation: {
			async handler(newConv, oldConv) {
				if (!oldConv || newConv.key != oldConv.key) {
					this.userInfo = {}
					this.groupId = null;
					if (this.isGroup) {
						this.loadGroup(this.conversation.targetId);
					} else {
						this.loadFriend(this.conversation.targetId);
						// 加载已读状态
						this.loadReaded(this.conversation.targetId)
					}
					await this.chatStore.resetMessages(this.conversation.key)
					// 滚到底部
					this.scrollToBottom();
					this.showSide = false;
					// 消息已读
					this.readedMessage();
					// 重置输入框
					this.resetEditor();
					// 复位回执消息
					this.isReceipt = false;
				}
			},
			immediate: true
		},
		loading: {
			async handler(newLoading, oldLoading) {
				if (newLoading) return;
				// 断线重连后，需要更新一下已读状态	
				if (this.isPrivate) {
					this.loadReaded(this.conversation.targetId);
				}
				// 如果用户所在的会话拉到了新的离线消息，需重置会话内的消息，否则新消息会不显示
				if (this.chatStore.hasMoreLastMessage) {
					await this.chatStore.resetMessages(this.conversation.key);
					this.scrollToBottom();
				}
			}
		}
	},
	mounted() {
		let div = document.getElementById("chatScrollBox");
		div.addEventListener('scroll', this.onScroll)
		this.$eventBus.$on('newMessage', async (message) => {
			// 收到新消息,则滚动至底部
			if (this.$msgType.isNormal(message.type) || this.$msgType.isAction(message.type)) {
				// 新消息来时，如果用户本来就在底部不远位置，则直接拉到底部
				if (this.chatStore.isInBottom || message.selfSend) {
					this.scrollToBottom();
				}
			}
		});
	}
}
</script>

<style lang="scss" scoped>
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

	.content-box {
		position: relative;

		.im-chat-main {
			padding: 0;
			background-color: #f4f5f6;

			.im-chat-box {
				>ul {
					padding: 0 20px;

					li {
						list-style-type: none;
					}
				}
			}
		}

		.locate-tip {
			text-align: center;
			position: absolute;
			right: 20px;
			bottom: 230px;
			color: var(--im-color-primary);
			font-size: var(--im-font-size);
			font-weight: 600;
			background: white;
			padding: 8px 16px;
			border-radius: 18px;
			cursor: pointer;
			z-index: 99;
			box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
			transition: all 0.3s ease;
			border: 1px solid rgba(0, 0, 0, 0.06);

			&:hover {
				transform: translateY(-1px);
				box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
			}
		}

		.im-chat-footer {
			position: relative;
			display: flex;
			flex-direction: column;
			padding: 0;

			.chat-tool-bar {
				display: flex;
				position: relative;
				width: 100%;
				height: 44px;
				text-align: left;
				box-sizing: border-box;
				border-top: 2px solid #EBEEF5;
				padding: 6px 8px;
				align-items: center;
				background: var(--im-background-active);
				color: black;
				gap: 8px;
				opacity: 0.85;

				// 统一所有按钮的样式，参考新版本
				>div {
					font-size: 20px;
					cursor: pointer;
					width: 32px;
					height: 32px;
					line-height: 32px;
					text-align: center;
					border-radius: 6px;
					display: flex;
					align-items: center;
					justify-content: center;
					position: relative;
					transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

					&.chat-tool-active {
						color: var(--im-color-primary);
						background: var(--im-background-active-dark);
						transform: scale(1.02);
					}

					&:hover {
						color: var(--im-color-primary);
						background: var(--im-background-active);
						transform: translateY(-1px);
						box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
					}
				}
			}

			.send-content-area {
				position: relative;
				display: flex;
				flex-direction: column;
				height: 100%;
				background-color: white !important;

				.send-btn-area {
					padding: 10px;
					position: absolute;
					bottom: 4px;
					right: 6px;
				}
			}

			.chat-editer-mask {
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				background: #f8f8f8d0;
				font-size: var(--im-font-size-large);
				color: var(--im-text-color-light);
				display: flex;
				justify-content: center;
				align-items: center;
				border-radius: 10px;
				border: 1px solid #ddd;
				z-index: 10;

				.icon {
					font-size: var(--im-font-size-larger);
					margin-right: 3px;
				}
			}
		}
	}

	.side-box {
		border-left: var(--im-border);
	}

}
</style>
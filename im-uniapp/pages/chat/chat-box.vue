<template>
	<view class="page chat-box" id="chatBox">
		<nav-bar back more @more="onShowMore">{{ title }}</nav-bar>
		<view class="chat-main-box" :style="{height: chatMainHeight+'px'}">
			<view class="chat-message" @click="switchChatTabBox('none')">
				<scroll-view class="scroll-box" scroll-y="true" upper-threshold="200" @scrolltoupper="onScrollToTop"
					@scrolltolower="onScrollToBottom" :scroll-into-view="'m-' + chatStore.scrollMessageLocalId">
					<view v-if="conversation" class="chat-wrap">
						<long-press-menu ref="messageMenu" @select="onSelectMessageMenu">
							<view v-for="m in messages" :key="m.localId">
								<chat-message-item :ref="m.localId" :id="'m-' +m.localId" :active="m.localId == activeMessageLocalId"
									:headImage="headImage(m)" :showName="showName(m)" @call="onRtCall(m)" @resend="onResendMessage"
									@receipt="onShowReceipt(m)" @longPressHead="onLongPressHead(m)" @audioStateChange="onAudioStateChange"
									:conversation="conversation" :message="m" @longPressMenu="onShowMessageMenu">
								</chat-message-item>
							</view>
						</long-press-menu>
					</view>
				</scroll-view>
				<view v-if="conversation.atMe || conversation.atAll" class="locate-tip" @click="scrollToAtMessage">
					有人@我
				</view>
				<view v-else-if="!chatStore.isInBottom" class="locate-tip" @click="onClickToBottom">
					{{ chatStore.newMessageSize > 0 ? chatStore.newMessageSize+'条新消息' :'回到底部' }}
				</view>
			</view>
			<view v-if="atUserIds.length > 0" class="chat-at-bar" @click="openAtBox()">
				<view class="iconfont icon-at">&nbsp;</view>
				<scroll-view v-if="atUserIds.length > 0" class="chat-at-scroll-box" scroll-x="true" scroll-left="120">
					<view class="chat-at-items">
						<view v-for="m in atUserItems" class="chat-at-item" :key="m.userId">
							<head-image :name="m.showNickName" :url="m.headImage" size="minier"></head-image>
						</view>
					</view>
				</scroll-view>
			</view>
			<view class="send-bar">
				<view v-if="!showRecord" class="iconfont icon-voice-circle" @click="onRecorderInput()"></view>
				<view v-else class="iconfont icon-keyboard" @click="onKeyboardInput()"></view>
				<chat-record v-if="showRecord" class="chat-record" @send="onSendRecord"></chat-record>
				<view v-else class="send-text">
					<editor id="editor" class="send-text-area" :placeholder="isReceipt ? '[回执消息]' : ''" :read-only="isReadOnly"
						@focus="onEditorFocus" @ready="onEditorReady" @input="onTextInput">
					</editor>
				</view>
				<view v-if="isGroup" class="iconfont icon-at" @click="openAtBox()"></view>
				<view class="iconfont icon-icon_emoji" @click="onShowEmoChatTab()"></view>
				<view v-if="isEmpty" class="iconfont icon-add" @click="onShowToolsChatTab()">
				</view>
				<button v-if="!isEmpty || atUserIds.length" class="btn-send" type="primary"
					@touchend.prevent="sendTextMessage()" size="mini">发送</button>
				<view class="chat-editer-mask" v-if="notAllowInputTip">
					<text class="icon iconfont icon-warning-circle-empty"></text>
					<text>{{ notAllowInputTip }}</text>
				</view>
			</view>
		</view>
		<view class="chat-tab-bar">
			<scroll-view v-if="chatTabBox == 'tools'" class="chat-tools" :style="{height: keyboardHeight+'px'}">
				<view class="chat-tools-list">
					<view class="chat-tools-item">
						<file-upload ref="fileUpload" :onBefore="onUploadFileBefore" :onSuccess="onUploadFileSuccess"
							:onError="onUploadFileFail">
							<view class="tool-icon iconfont icon-folder"></view>
						</file-upload>
						<view class="tool-name">文件</view>
					</view>
					<view class="chat-tools-item">
						<image-upload :maxCount="9" sourceType="album" :onBefore="onUploadImageBefore"
							:onSuccess="onUploadImageSuccess" :onError="onUploadImageFail">
							<view class="tool-icon iconfont icon-picture"></view>
						</image-upload>
						<view class="tool-name">相册</view>
					</view>
					<view class="chat-tools-item">
						<image-upload sourceType="camera" :onBefore="onUploadImageBefore" :onSuccess="onUploadImageSuccess"
							:onError="onUploadImageFail">
							<view class="tool-icon iconfont icon-camera"></view>
						</image-upload>
						<view class="tool-name">拍摄</view>
					</view>
					<view class="chat-tools-item" @click="onRecorderInput()">
						<view class="tool-icon iconfont icon-microphone"></view>
						<view class="tool-name">语音消息</view>
					</view>
					<view v-if="isGroup" class="chat-tools-item" @click="switchReceipt()">
						<view class="tool-icon iconfont icon-receipt" :class="isReceipt ? 'active' : ''"></view>
						<view class="tool-name">回执消息</view>
					</view>
					<!-- #ifndef MP-WEIXIN -->
					<!-- 音视频不支持小程序 -->
					<view v-if="isPrivate" class="chat-tools-item" @click="onPriviteVideo()">
						<view class="tool-icon iconfont icon-video"></view>
						<view class="tool-name">视频通话</view>
					</view>
					<view v-if="isPrivate" class="chat-tools-item" @click="onPriviteVoice()">
						<view class="tool-icon iconfont icon-call"></view>
						<view class="tool-name">语音通话</view>
					</view>
					<view v-if="isGroup" class="chat-tools-item" @click="onGroupVideo()">
						<view class="tool-icon iconfont icon-call"></view>
						<view class="tool-name">语音通话</view>
					</view>
					<!-- #endif -->
				</view>
			</scroll-view>
			<scroll-view v-if="chatTabBox === 'emo'" class="chat-emotion" scroll-y="true"
				:style="{height: keyboardHeight+'px'}">
				<view class="emotion-item-list">
					<image class="emotion-item emoji-large" :title="emoText" :src="$emo.textToPath(emoText)"
						v-for="(emoText, i) in $emo.emoTextList" :key="i" @click="selectEmoji(emoText)" mode="aspectFit"
						lazy-load="true"></image>
				</view>
			</scroll-view>
		</view>
		<!-- @用户时选择成员 -->
		<chat-at-box ref="atBox" :ownerId="group.ownerId" :members="groupMembers" @complete="onAtComplete"></chat-at-box>
		<!-- 群语音通话时选择成员 -->
		<!-- #ifndef MP-WEIXIN -->
		<group-member-selector ref="selBox" :members="groupMembers" :maxSize="configStore.webrtc.maxChannel"
			@complete="onInviteOk"></group-member-selector>
		<group-rtc-join ref="rtcJoin" :groupId="group.id"></group-rtc-join>
		<!-- #endif -->
		<chat-group-readed ref="chatGroupReaded"></chat-group-readed>
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js';
export default {
	data() {
		return {
			conversation: {},
			userInfo: {},
			groupId: null,
			isReceipt: false, // 是否回执消息
			scrollMessageLocalId: 0, // 滚动条定位为到哪条消息
			chatTabBox: 'none',
			showRecord: false,
			chatMainHeight: 800, // 聊天窗口高度
			keyboardHeight: 290, // 键盘高度
			screenHeight: 1000, // 屏幕高度
			windowHeight: 1000, // 窗口高度
			initHeight: 1000, // h5初始高度
			atUserIds: [],
			showMinIdx: 0, // 下标小于showMinIdx的消息不显示，否则可能很卡
			reqQueue: [], // 请求队列
			isSending: false, // 是否正在发送请求
			isShowKeyBoard: false, // 键盘是否正在弹起 
			editorCtx: null, // 编辑器上下文
			isEmpty: true, // 编辑器是否为空
			isFocus: false, // 编辑器是否焦点
			isReadOnly: false, // 编辑器是否只读
			playingAudio: null, // 当前正在播放的录音消息
			activeMessageLocalId: '', // 选中消息,
		}
	},
	methods: {
		onRecorderInput() {
			this.showRecord = true;
			this.switchChatTabBox('none');
		},
		onKeyboardInput() {
			this.showRecord = false;
			this.switchChatTabBox('none');
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
			this.readedMessage();
		},
		onRtCall(message) {
			if (message.type == this.$enums.MESSAGE_TYPE.ACT_RT_VOICE) {
				this.onPriviteVoice();
			} else if (message.type == this.$enums.MESSAGE_TYPE.ACT_RT_VIDEO) {
				this.onPriviteVideo();
			}
		},
		onPriviteVideo() {
			if (this.notAllowInputTip) {
				uni.showToast({ title: this.notAllowInputTip, icon: 'none' });
				return;
			}
			const friendInfo = encodeURIComponent(JSON.stringify(this.friend));
			uni.navigateTo({
				url: `/pages/chat/chat-private-video?mode=video&friend=${friendInfo}&isHost=true`
			})
		},
		onPriviteVoice() {
			if (this.notAllowInputTip) {
				uni.showToast({ title: this.notAllowInputTip, icon: 'none' });
				return;
			}
			const friendInfo = encodeURIComponent(JSON.stringify(this.friend));
			uni.navigateTo({
				url: `/pages/chat/chat-private-video?mode=voice&friend=${friendInfo}&isHost=true`
			})
		},
		onGroupVideo() {
			// 邀请成员发起通话
			let ids = [this.mine.id];
			this.$refs.selBox.init(ids, ids, []);
			this.$refs.selBox.open();
		},
		onInviteOk(ids) {
			if (ids.length < 2) {
				return;
			}
			let users = [];
			ids.forEach(id => {
				const m = this.groupMemberMap.get(id);
				// 只取部分字段,压缩url长度
				users.push({
					id: m.userId,
					nickName: m.showNickName,
					headImage: m.headImage,
					isCamera: false,
					isMicroPhone: true
				})
			})
			const groupId = this.group.id;
			const inviterId = this.mine.id;
			const userInfos = encodeURIComponent(JSON.stringify(users));
			uni.navigateTo({
				url: `/pages/chat/chat-group-video?groupId=${groupId}&isHost=true
						&inviterId=${inviterId}&userInfos=${userInfos}`
			})
		},
		switchReceipt() {
			this.isReceipt = !this.isReceipt;
		},
		openAtBox() {
			this.$refs.atBox.init(this.atUserIds);
			this.$refs.atBox.open();
		},
		clearAtUsers() {
			this.atUserIds = [];
		},
		onAtComplete(atUserIds) {
			this.atUserIds = atUserIds;
		},
		onLongPressHead(message) {
			if (!message.selfSend && this.isGroup && this.atUserIds.indexOf(message.sendId) < 0) {
				this.atUserIds.push(message.sendId);
			}
		},
		onTouchChat() {
			this.activeMessageLocalId = '';
			this.lockScrollEvent = false;
		},
		setLockScrollEvent(duration) {
			this.lockScrollEvent = true;
			setTimeout(() => this.lockScrollEvent = false, duration)
		},
		headImage(message) {
			if (this.isGroup) {
				const member = this.groupMemberMap.get(message.sendId);
				return member ? member.headImage : "";
			} else {
				return message.selfSend ? this.mine.headImageThumb : this.conversation.headImage
			}
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
		sendTextMessage() {
			this.editorCtx.getContents({
				success: async (e) => {
					// 清空编辑框数据
					this.editorCtx.clear();
					let sendText = "";
					e.delta.ops.forEach((op) => {
						if (op.insert.image) {
							// emo表情
							sendText += `#${op.attributes.alt};`
						} else(
							// 文字
							sendText += op.insert
						)
					})
					// 去除最后的换行符
					sendText = sendText.trim();
					if (!sendText && this.atUserIds.length == 0) {
						return uni.showToast({
							title: '不能发送空白信息',
							icon: "none"
						});
					}
					const receiptText = this.isReceipt ? "【回执消息】 " : "";
					const atText = this.createAtText();
					const message = {
						localId: this.$nextSnowflakeId(),
						content: receiptText + sendText + atText,
						atUserIds: this.atUserIds,
						receipt: this.isReceipt,
						type: this.$enums.MESSAGE_TYPE.TEXT
					}
					// 填充对方id
					this.fillTargetId(message, this.conversation.targetId);
					// 防止发送期间用户切换会话导致串扰
					const conv = this.conversation;
					// 本地消息
					const localMessage = this.buildLocalMessage(message);
					await this.insertMessage(localMessage);
					// 清空@成员列表、回执标记、引用消息
					this.isReceipt = false;
					this.atUserIds = [];
					// 发送
					await this.processSendMessage(this.conversation, message, localMessage);
					this.readedMessage();
				}
			})
		},
		async insertMessage(message) {
			await this.resetMessages();
			await this.chatStore.insertMessage(this.conversation.key, message);
			await this.chatStore.moveTop(this.conversation.key);
			this.scrollToBottom();
		},
		createAtText() {
			let atText = "";
			this.atUserIds.forEach((id) => {
				if (id == -1) {
					atText += ` @全体成员`;
				} else {
					const member = this.groupMemberMap.get(id);
					if (member) {
						atText += ` @${member.showNickName}`;
					}
				}
			})
			return atText;
		},
		fillTargetId(message, targetId) {
			if (this.isGroup) {
				message.groupId = targetId;
			} else {
				message.recvId = targetId;
			}
		},
		async scrollToBottom() {
			const length = this.chatStore.messages.length;
			if (length == 0) return;
			const lastMessage = this.chatStore.messages[length - 1];
			this.scrollToMessage(lastMessage.localId);
			this.chatStore.setIsInBottom(true);
		},
		scrollToMessage(localId) {
			// 手动定位时，禁止滚动事件触发，避免干扰
			this.setLockScrollEvent(500);
			this.chatStore.scrollToMessage(localId)
		},
		onShowEmoChatTab() {
			this.showRecord = false;
			this.switchChatTabBox('emo')
		},
		onShowToolsChatTab() {
			this.showRecord = false;
			this.switchChatTabBox('tools')
		},
		switchChatTabBox(chatTabBox) {
			if (this.chatTabBox != chatTabBox) {
				this.chatTabBox = chatTabBox;
				if (chatTabBox != 'tools' && this.$refs.fileUpload) {
					this.$refs.fileUpload.hide()
				}
				setTimeout(() => this.reCalChatMainHeight(), 30);
			}
		},
		selectEmoji(emoText) {
			let path = this.$emo.textToPath(emoText)
			// 先把键盘禁用了，否则会重新弹出键盘
			this.isReadOnly = true;
			this.isEmpty = false;
			this.$nextTick(() => {
				this.editorCtx.insertImage({
					src: path,
					alt: emoText,
					extClass: 'emoji-small',
					nowrap: true,
					complete: () => {
						this.isReadOnly = false;
						this.editorCtx.blur();
					}
				});
			})
		},
		async onUploadImageBefore(file) {
			const data = {
				originUrl: file.path,
				thumbUrl: file.path
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
			return true;
		},
		async onUploadImageSuccess(file, res) {
			const message = file.message;
			message.content = JSON.stringify(res.data);
			await this.processSendMessage(file.conversation, message, file.localMessage);
		},
		async onUploadImageFail(file, err) {
			const localMessage = file.localMessage;
			localMessage.status = this.$enums.MESSAGE_STATUS.FAILED;
			await this.chatStore.updateMessage(this.conversation.key, localMessage);
		},
		async onUploadFileBefore(file) {
			const data = {
				name: file.name,
				size: file.size,
				url: file.path
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
			return true;
		},
		async onUploadFileSuccess(file, res) {
			const data = {
				name: file.name,
				size: file.size,
				url: res.data
			}
			const message = file.message;
			message.content = JSON.stringify(data);
			await this.processSendMessage(file.conversation, message, file.localMessage);
		},
		async onUploadFileFail(file, res) {
			const localMessage = file.localMessage;
			localMessage.status = this.$enums.MESSAGE_STATUS.FAILED;
			await this.chatStore.updateMessage(this.conversation.key, localMessage);
		},
		async onResendMessage(message) {
			if (message.type != this.$enums.MESSAGE_TYPE.TEXT) {
				uni.showToast({
					title: "该消息不支持自动重新发送，建议手动重新发送",
					icon: "none"
				})
				return;
			}
			// 删除旧消息
			await this.chatStore.deleteMessage(this.conversation.key, message);
			// 重新推送
			const sendMessage = JSON.parse(JSON.stringify(message));
			sendMessage.localId = this.$nextSnowflakeId();
			const localMessage = this.buildLocalMessage(sendMessage);
			await this.insertMessage(localMessage);
			// 发送
			await this.processSendMessage(this.conversation, sendMessage, localMessage);
		},
		onShowMessageMenu(e, message, menuItems) {
			this.menuMessage = message;
			this.$refs.messageMenu.open(e, menuItems);
		},
		onSelectMessageMenu(item) {
			switch (item.key) {
				case 'COPY':
					this.onCopyMessage(this.menuMessage);
					break;
				case 'RECALL':
					this.onRecallMessage(this.menuMessage);
					break;
				case 'DELETE':
					this.onDeleteMessage(this.menuMessage);
					break;
				case 'DOWNLOAD':
					this.onDownloadFile(this.menuMessage);
					break;
				default:
					break;
			}
		},
		onDeleteMessage(message) {
			this.$refs.modal.open({
				title: '删除消息',
				content: '确认删除消息?',
				success: async (res) => {
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
				}
			})
		},
		onRecallMessage(message) {
			this.$refs.modal.open({
				title: '撤回消息',
				content: '确认撤回消息?',
				success: () => {
					const convKey = this.conversation.key
					const url = `/message/${this.chatTypeText()}/recall/${message.id}`
					this.$http({
						url: url,
						method: 'DELETE'
					}).then((m) => {
						m.selfSend = true;
						this.chatStore.recallMessage(convKey, m);
					})
				}
			})
		},
		onCopyMessage(message) {
			uni.setClipboardData({
				data: message.content,
				success: () => {
					uni.showToast({ title: '复制成功' });
				},
				fail: () => {
					uni.showToast({ title: '复制失败', icon: 'none' });
				}
			});
		},
		onShowReceipt(message) {
			this.$refs.chatGroupReaded.open(message, this.groupMembers);
		},
		async locateMessage(localId) {
			const locateMessage = await this.$db.findMessageByLocalId(localId);
			if (!locateMessage || locateMessage.deleted || this.isRecall(locateMessage)) {
				uni.showToast({
					title: "无法定位原消息",
					icon: 'none'
				})
				return;
			}
			await this.chatStore.locateToMessage(this.conversation.key, locateMessage)
			// 定位消息
			this.scrollToMessage(localId);
			// 选中消息
			this.activeMessageLocalId = localId;
			// 设置底部标记
			this.chatStore.setIsInBottom(!this.chatStore.hasMoreNextMessage);
		},
		onDownloadFile(message) {
			const url = JSON.parse(message.content).url;
			uni.downloadFile({
				url: url,
				success(res) {
					if (res.statusCode === 200) {
						var filePath = encodeURI(res.tempFilePath);
						uni.openDocument({
							filePath: filePath,
							showMenu: true
						});
					}
				},
				fail(e) {
					uni.showToast({
						title: "文件下载失败",
						icon: "none"
					})
				}
			});
		},
		async onClickToBottom() {
			this.setLockScrollEvent(500);
			await this.resetMessages();
			setTimeout(() => this.scrollToBottom(), 100);
		},
		async onScrollToTop(e) {
			// app端：当用户从一个聊天页面跳转到另一个聊天页面时，会触发旧页面的顶部事件，原因不详
			if (this.lockScrollEvent || !this.chatStore.isActive(this.conversation.key)) {
				return;
			}
			if (!this.chatStore.hasMoreLastMessage) {
				uni.showToast({
					title: "没有更多消息了",
					icon: 'none'
				})
				return;
			}
			// 多展示30条信息
			await this.chatStore.loadLastPageMessage(this.conversation.key, 30);
			// 清除底部标志
			this.chatStore.setIsInBottom(false);
			// 锁定500ms,防止重复触发
			this.setLockScrollEvent(500);
		},
		async onScrollToBottom(e) {
			if (this.lockScrollEvent || !this.chatStore.isActive(this.conversation.key)) {
				return;
			}
			if (this.chatStore.hasMoreNextMessage) {
				// 向下翻页
				await this.chatStore.loadNextPageMessage(this.conversation.key, 30);
			}
			// 设置底部标志
			this.chatStore.setIsInBottom(!this.chatStore.hasMoreNextMessage);
			// 锁定500ms,防止重复触发
			this.setLockScrollEvent(500)
		},
		onShowMore() {
			if (this.isGroup) {
				uni.navigateTo({
					url: "/pages/group/group-info?id=" + this.group.id
				})
			} else {
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + this.userInfo.id
				})
			}
		},
		onTextInput(e) {
			this.isEmpty = e.detail.html == '<p><br></p>'
			this.readedMessage();
		},
		onEditorReady() {
			this.$nextTick(() => {
				const query = uni.createSelectorQuery().in(this);
				query.select('#editor').context((res) => {
					this.editorCtx = res.context
				}).exec()
			})
		},
		async onEditorFocus(e) {
			await this.resetMessages();
			this.scrollToBottom()
			this.switchChatTabBox('none')
			setTimeout(() => this.scrollToBottom(), 100);
		},
		onAudioStateChange(state, message) {
			const playingAudio = this.$refs[message.localId][0]
			if (state == 'PLAYING' && playingAudio != this.playingAudio) {
				// 停止之前的录音
				this.playingAudio && this.playingAudio.stopPlayAudio();
				// 记录当前正在播放的消息
				this.playingAudio = playingAudio;
			}
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
			await this.locateMessage(atMessage.localId);
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
			const group = await this.$http({ url: `/group/find/${groupId}` })
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
			const userInfo = await this.$http({ url: `/user/find/${friendId}` });
			this.userInfo = userInfo;
			await this.updateFriendInfo();
		},
		rpxTopx(rpx) {
			// rpx转换成px
			const info = uni.getSystemInfoSync()
			const px = info.windowWidth * rpx / 750;
			return Math.floor(rpx);
		},
		async processSendMessage(conv, message, localMessage) {
			// 发送
			const m = await this.sendMessageRequest(conv, message).catch(async (e) => {
				// 更新本地消息
				console.log(e)
				// 深拷贝一份消息，否则界面不会刷新
				localMessage = JSON.parse(JSON.stringify(localMessage))
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
		async resetMessages() {
			// 下方如果没有消息，没有必要重置，反而会造成页面闪烁
			if (this.chatStore.hasMoreNextMessage) {
				await this.chatStore.resetMessages(this.conversation.key);
			}
		},
		reCalChatMainHeight() {
			let sysInfo = uni.getSystemInfoSync();
			let h = this.windowHeight;
			// 减去标题栏高度
			h -= 50;
			// 减去键盘高度
			if (this.isShowKeyBoard || this.chatTabBox != 'none') {
				h -= this.keyboardHeight;
			}
			// APP需要减去状态栏高度
			// #ifdef APP-PLUS
			h -= sysInfo.statusBarHeight;
			// #endif
			this.chatMainHeight = h;
			// #ifndef APP
			// ios浏览器键盘把页面顶起后，页面长度不会变化，这里把页面拉到顶部适配一下
			if (uni.getSystemInfoSync().platform == 'ios') {
				// 不同手机需要的延时时间不一致，采用分段延时的方式处理
				const delays = [50, 100, 500];
				delays.forEach((delay) => {
					setTimeout(() => {
						uni.pageScrollTo({
							scrollTop: 0,
							duration: 10
						});
					}, delay);
				})
			}
			// #endif
		},
		listenKeyBoard() {
			// #ifdef H5
			if (navigator.platform == "Win32") {
				// 电脑端不需要弹出键盘
				console.log("navigator.platform:", navigator.platform)
				return;
			}
			if (uni.getSystemInfoSync().platform == 'ios') {
				// 监听键盘高度，ios13以上开始支持
				if (window.visualViewport) {
					window.visualViewport.addEventListener('resize', this.resizeListener);
				} else {
					// ios h5实现键盘监听
					window.addEventListener('focusin', this.focusInListener);
					window.addEventListener('focusout', this.focusOutListener);
				}
			} else {
				// 安卓h5实现键盘监听
				window.addEventListener('resize', this.resizeListener);
			}
			// #endif
			// #ifndef H5
			// app实现键盘监听
			uni.onKeyboardHeightChange(this.keyBoardListener);
			// #endif
		},
		unListenKeyboard() {
			// #ifdef H5
			window.removeEventListener('focusin', this.focusInListener);
			window.removeEventListener('focusout', this.focusOutListener);
			window.removeEventListener('resize', this.resizeListener);
			if (window.visualViewport) {
				window.visualViewport.removeEventListener('resize', this.resizeListener);
			}
			// #endif
			// #ifndef H5
			uni.offKeyboardHeightChange(this.keyBoardListener);
			// #endif
		},
		keyBoardListener(res) {
			this.isShowKeyBoard = res.height > 0;
			if (this.isShowKeyBoard) {
				this.keyboardHeight = res.height; // 获取并保存键盘高度
				// #ifdef APP-PLUS
				// ios app的键盘高度不准，需要减去屏幕和窗口差
				let sysInfo = uni.getSystemInfoSync();
				if (sysInfo.platform == 'ios') {
					this.keyboardHeight -= this.screenHeight - this.windowHeight;
				}
				// #endif
			}
			setTimeout(() => this.reCalChatMainHeight(), 30);
		},
		resizeListener() {
			let keyboardHeight = this.initHeight - window.innerHeight;
			// 兼容部分ios浏览器
			if (window.visualViewport && uni.getSystemInfoSync().platform == 'ios') {
				keyboardHeight = this.initHeight - window.visualViewport.height;
			}
			let isShowKeyBoard = keyboardHeight > 150;
			if (isShowKeyBoard) {
				this.keyboardHeight = keyboardHeight;
			}
			if (this.isShowKeyBoard != isShowKeyBoard) {
				this.isShowKeyBoard = isShowKeyBoard;
				setTimeout(() => this.reCalChatMainHeight(), 30);
			}
		},
		focusInListener() {
			this.isShowKeyBoard = true;
			setTimeout(() => this.reCalChatMainHeight(), 30);
		},
		focusOutListener() {
			this.isShowKeyBoard = false;
			setTimeout(() => this.reCalChatMainHeight(), 30);
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
			return new Promise((resolve) => {
				uni.getImageInfo({
					src: file.path,
					success: (res) => {
						resolve(res);
					},
					fail: (err) => {
						console.error('获取图片信息失败', err);
					}
				});
			});
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
			return this.friendStore.findFriend(this.userInfo.id);
		},
		group() {
			return this.groupStore.findGroup(this.groupId) || {}
		},
		groupMembers() {
			return this.group.members || []
		},
		groupMemberMap() {
			return new Map(this.groupMembers.map(m => [m.userId, m]));
		},
		title() {
			if (!this.conversation) return "";
			let title = this.conversation.showName;
			if (this.isGroup) {
				let size = this.groupMembers.filter(m => !m.quit).length;
				title += `(${size})`;
			}
			return title;
		},
		messages() {
			return this.chatStore.messages;
		},
		unreadCount() {
			if (!this.conversation || !this.conversation.unreadCount) {
				return 0;
			}
			return this.conversation.unreadCount;
		},
		atUserItems() {
			let atUsers = [];
			this.atUserIds.forEach((id) => {
				if (id == -1) {
					atUsers.push({
						id: -1,
						showNickName: "全体成员"
					})
					return;
				}
				let member = this.groupMembers.find((m) => m.userId == id);
				if (member) {
					atUsers.push(member);
				}
			})
			return atUsers;
		},
		isGroup() {
			return this.conversation && this.conversation.type == this.$enums.CONVERSATION_TYPE.GROUP;
		},
		isPrivate() {
			return this.conversation && this.conversation.type == this.$enums.CONVERSATION_TYPE.PRIVATE;
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
		loading: {
			async handler(newLoading, oldLoading) {
				if (newLoading) return;
				// 断线重连后，需要更新一下已读状态
				if (this.isPrivate) {
					this.loadReaded(this.conversation.targetId)
				}
				// 如果用户所在的会话拉到了新的离线消息，需重置会话内的消息，否则新消息会不显示
				if (this.chatStore.hasMoreLastMessage) {
					await this.chatStore.resetMessages(this.conversation.key)
					this.scrollToBottom();
				}
			}
		}
	},
	onLoad(options) {
		// 聊天数据
		this.chatStore.setActive(options.convKey);
		this.conversation = this.chatStore.activeConversation;
		this.scrollMessageLocalId = '';
		// 加载好友或群聊信息 
		if (this.isGroup) {
			this.loadGroup(this.conversation.targetId);
		} else {
			this.loadFriend(this.conversation.targetId);
			this.loadReaded(this.conversation.targetId)
		}
		// 消息已读
		this.readedMessage()
		// 清空未读
		this.chatStore.resetUnreadCount(this.conversation.key);
		// 复位回执消息
		this.isReceipt = false;
		// 监听键盘高度
		this.listenKeyBoard();
		// 计算聊窗口高度
		this.windowHeight = uni.getSystemInfoSync().windowHeight;
		this.screenHeight = uni.getSystemInfoSync().screenHeight;
		this.reCalChatMainHeight();
		this.$nextTick(async () => {
			// 上面获取的windowHeight可能不准，重新计算一次聊天窗口高度
			this.windowHeight = uni.getSystemInfoSync().windowHeight;
			this.reCalChatMainHeight();
			// 消息拉到底部
			await this.chatStore.resetMessages(this.conversation.key)
			this.scrollToBottom();
			// 有时页面渲染得慢，会导致无法正常滚到底部，这里再滚一次
			setTimeout(() => this.scrollToBottom(), 100);
			// #ifdef H5
			this.initHeight = window.innerHeight;
			// 兼容ios的h5:禁止页面滚动
			const chatBox = document.getElementById('chatBox')
			chatBox.addEventListener('touchmove', e => {
				e.preventDefault()
			}, { passive: false });
			// #endif
		});
		// 监听新消息
		uni.$on('newMessage', (message) => {
			// 收到新消息,则滚动至底部
			if (this.$msgType.isNormal(message.type) || this.$msgType.isAction(message.type)) {
				// 新消息来时，如果用户本来就在底部不远位置，则直接拉到底部
				if (this.chatStore.isInBottom || message.selfSend) {
					this.scrollToBottom();
				}
			}
		});
	},
	async onShow() {
		if (this.conversation && !this.chatStore.isActive(this.conversation.key)) {
			// 防止聊天页面被切换后消息错乱
			this.chatStore.setActive(this.conversation.key);
			await this.chatStore.resetMessages(this.conversation.key)
			this.scrollToBottom();
		}
	},
	onUnload() {
		this.unListenKeyboard();
		// 停止监听
		uni.$off('newMessage');
	}
}

</script>

<style lang="scss">
.chat-box {
	$icon-color: rgba(0, 0, 0, 0.88);
	position: relative;
	background-color: #fafafa;

	.header {
		display: flex;
		justify-content: center;
		align-items: center;
		height: 60rpx;
		padding: 5px;
		background-color: #fafafa;
		line-height: 50px;
		font-size: $im-font-size-large;
		box-shadow: $im-box-shadow-lighter;
		z-index: 1;

		.btn-side {
			position: absolute;
			line-height: 60rpx;
			cursor: pointer;

			&.right {
				right: 30rpx;
			}
		}
	}

	.chat-main-box {
		// #ifndef APP-PLUS
		top: $im-nav-bar-height;
		// #endif
		// #ifdef APP-PLUS
		top: calc($im-nav-bar-height + var(--status-bar-height));
		// #endif
		position: fixed;
		width: 100%;
		display: flex;
		flex-direction: column;
		z-index: 2;

		.chat-message {
			flex: 1;
			padding: 0;
			overflow: hidden;
			position: relative;
			background-color: white;

			.scroll-box {
				height: 100%;
			}

			.locate-tip {
				position: absolute;
				right: 30rpx;
				bottom: 30rpx;
				font-size: $im-font-size;
				color: $im-color-primary;
				font-weight: 600;
				background: white;
				padding: 10rpx 30rpx;
				border-radius: 25rpx;
				box-shadow: $im-box-shadow-dark;
			}
		}

		.chat-at-bar {
			display: flex;
			align-items: center;
			padding: 0 10rpx;

			.icon-at {
				font-size: $im-font-size-larger;
				color: $im-color-primary;
				font-weight: bold;
			}

			.chat-at-scroll-box {
				flex: 1;
				width: 80%;

				.chat-at-items {
					display: flex;
					align-items: center;
					height: 70rpx;

					.chat-at-item {
						padding: 0 3rpx;
					}
				}
			}
		}

		.send-bar {
			position: relative;
			display: flex;
			align-items: center;
			padding: 10rpx;
			border-top: $im-border solid 1px;
			background-color: $im-bg;
			min-height: 80rpx;
			padding-bottom: 14rpx;

			.iconfont {
				font-size: 60rpx;
				margin: 0 10rpx;
				color: $icon-color;
			}

			.chat-record {
				flex: 1;
			}

			.send-text {
				flex: 1;
				overflow: auto;
				padding: 14rpx 20rpx;
				background-color: #fff;
				border-radius: 8rpx;
				font-size: $im-font-size;
				box-sizing: border-box;
				margin: 0 10rpx;
				position: relative;

				.send-text-area {
					width: 100%;
					height: 100%;
					min-height: 40rpx;
					max-height: 200rpx;
					font-size: 30rpx;
				}
			}

			.btn-send {
				margin: 5rpx;
			}

			.chat-editer-mask {
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				background: $im-bg;
				font-size: $im-font-size-small;
				color: $im-text-color-light;
				display: flex;
				justify-content: center;
				align-items: center;
				word-break: break-all;
				padding: 0 15rpx;
				overflow: hidden;
				border-radius: 10rpx;
				box-sizing: border-box;
				z-index: 10;

				.icon {
					font-size: 32rpx;
					margin-right: 10rpx;
				}
			}
		}
	}

	.chat-tab-bar {
		position: fixed;
		bottom: 0;
		background-color: $im-bg;

		.chat-tools {
			padding: 40rpx;
			box-sizing: border-box;

			.chat-tools-list {
				display: flex;
				flex-wrap: wrap;
				align-content: center;

				.chat-tools-item {
					width: 25%;
					padding: 16rpx;
					box-sizing: border-box;
					display: flex;
					flex-direction: column;
					align-items: center;

					.tool-icon {
						padding: 26rpx;
						font-size: 54rpx;
						border-radius: 20%;
						background-color: white;
						color: $icon-color;

						&:active {
							background-color: $im-bg-active;
						}
					}

					.tool-name {
						height: 60rpx;
						line-height: 60rpx;
						font-size: 28rpx;
					}
				}
			}
		}

		.chat-emotion {
			padding: 40rpx;
			box-sizing: border-box;

			.emotion-item-list {
				display: flex;
				flex-wrap: wrap;
				justify-content: space-between;
				align-content: center;

				.emotion-item {
					text-align: center;
					cursor: pointer;
					padding: 5px;
				}
			}
		}
	}
}

</style>

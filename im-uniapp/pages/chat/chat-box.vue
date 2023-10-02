<template>
	<view class="chat-box">
		<view class="header">
			<text class="title">{{title}}</text>
			<uni-icons class="btn-side" type="more-filled" size="30"></uni-icons>
		</view>
		<view class="chat-msg" @click="showToolBox(false)">
			<scroll-view class="scroll-box" scroll-y="true" :scroll-into-view="'chat-item-'+scrollMsgIdx">
				<view v-for="(msgInfo,idx) in chat.messages" :key="idx">
					<chat-message-item :headImage="headImage(msgInfo)" :showName="showName(msgInfo)"
						:id="'chat-item-'+idx" :msgInfo="msgInfo">
					</chat-message-item>
				</view>
			</scroll-view>
		</view>

		<view class="send-bar">
			<view class="iconfont icon-voice-circle"></view>
			<view class="send-text">
				<textarea class="send-text-area" v-model="sendText" ref="sendBox" auto-height  :show-confirm-bar="false"
					cursor-spacing="10"  @keydown.enter="sendTextMessage()" @click="showToolBox(false)"></textarea>
			</view>
			<view class="iconfont icon-icon_emoji"></view>
			<view v-show="sendText==''" class="iconfont icon-add-circle"  @click="showToolBox(true)" ></view>
			<button v-show="sendText!=''" class="btn-send" type="primary" @click="sendTextMessage()" size="mini">发送</button>
		</view>

		<view v-show="showTools" class="chat-tools" >
			<view class="chat-tools-item">
				<image-upload :onBefore="onUploadImageBefore" :onSuccess="onUploadImageSuccess"
					:onError="onUploadImageFail">
					<view class="tool-icon iconfont icon-picture"></view>
				</image-upload>
				<view class="tool-name">相册</view>
			</view>
			<view class="chat-tools-item" v-for="(tool, idx) in tools" @click="onClickTool(tool)">
				<view class="tool-icon iconfont" :class="tool.icon"></view>
				<view class="tool-name">{{ tool.name }}</view>
			</view>
		</view>



		<!--
		<emotion v-show="showEmotion" :pos="emoBoxPos" @emotion="handleEmotion"></Emotion>
		<chat-voice :visible="showVoice" @close="closeVoiceBox" @send="handleSendVoice"></chat-voice>
		<chat-history :visible="showHistory" :chat="chat" :friend="friend" :group="group" :groupMembers="groupMembers" @close="closeHistoryBox"></chat-history>
		-->
	</view>
</template>

<script>
	export default {
		data() {
			return {
				chat: {},
				friend: {},
				group: {},
				groupMembers: [],
				sendText: "",
				showVoice: false, // 是否显示语音录制弹窗
				showSide: false, // 是否显示群聊信息栏
				showEmotion: false, // 是否显示emoji表情
				showHistory: false, // 是否显示历史聊天记录
				scrollMsgIdx: 0, // 滚动条定位为到哪条消息
				showTools: false,
				tools: [{
						name: "拍摄",
						icon: "icon-camera"
					},
					{
						name: "语音输入",
						icon: "icon-microphone"
					},
					{
						name: "文件",
						icon: "icon-folder"
					},
					{
						name: "呼叫",
						icon: "icon-call"
					}
				]
			}
		},
		methods: {
			headImage(msgInfo) {
				if (this.chat.type == 'GROUP') {
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.headImage : "";
				} else {
					return msgInfo.selfSend ? this.mine.headImageThumb : this.chat.headImage
				}
			},
			showName(msgInfo) {
				if (this.chat.type == 'GROUP') {
					let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
					return member ? member.aliasName : "";
				} else {
					return msgInfo.selfSend ? this.mine.nickName : this.chat.showName
				}

			},
			sendTextMessage() {
				if (!this.sendText.trim()) {
					return uni.showToast({
						title: "不能发送空白信息",
						icon: "none"
					});
				}
				let msgInfo = {
					content: this.sendText,
					type: 0
				}
				// 填充对方id
				this.fillTargetId(msgInfo, this.chat.targetId);
				this.sendText = "";
				this.$http({
					url: this.messageAction,
					method: 'POST',
					data: msgInfo
				}).then((id) => {
					msgInfo.id = id;
					msgInfo.sendTime = new Date().getTime();
					msgInfo.sendId = this.$store.state.userStore.userInfo.id;
					msgInfo.selfSend = true;
					this.$store.commit("insertMessage", msgInfo);
					uni.showToast({
						title: "发送成功",
						icon: "none"
					})
				}).finally(() => {
					// 滚动到底部
					this.scrollToBottom();
				});
			},
			fillTargetId(msgInfo, targetId) {
				if (this.chat.type == "GROUP") {
					msgInfo.groupId = targetId;
				} else {
					msgInfo.recvId = targetId;
				}
			},
			scrollToBottom() {
				this.$nextTick(() => {
					this.scrollMsgIdx = this.chat.messages.length - 1;
				})

			},
			showToolBox(v){
				this.showTools = v;
			},
			onUploadImageBefore(file) {
				let data = {
					originUrl: file.path,
					thumbUrl: file.path
				}
				let msgInfo = {
					id: 0,
					fileId: file.uid,
					sendId: this.mine.id,
					content: JSON.stringify(data),
					sendTime: new Date().getTime(),
					selfSend: true,
					type: 1,
					loadStatus: "loading"
				}
				// 填充对方id
				this.fillTargetId(msgInfo, this.chat.targetId);
				// 插入消息
				this.$store.commit("insertMessage", msgInfo);
				// 借助file对象保存
				file.msgInfo = msgInfo;
				// 滚到最低部
				this.scrollToBottom();
				return true;
			},
			onUploadImageSuccess(file, res) {
				console.log("onUploadImageSuccess")
				let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
				msgInfo.content = JSON.stringify(res.data);
				this.$http({
					url: this.messageAction,
					method: 'POST',
					data: msgInfo
				}).then((id) => {
					msgInfo.loadStatus = 'ok';
					msgInfo.id = id;
					this.$store.commit("insertMessage", msgInfo);
				})
			},
			onUploadImageFail(file, err) {
				let msgInfo = JSON.parse(JSON.stringify(file.msgInfo));
				msgInfo.loadStatus = 'fail';
				this.$store.commit("insertMessage", msgInfo);
			},
			onClickTool(tool) {
				switch (tool.name) {
					case "相册":
						break;

				}
				console.log(tool);
			}
		},
		computed: {
			mine() {
				return this.$store.state.userStore.userInfo;
			},
			title() {
				if (!this.chat) {
					return "";
				}
				let title = this.chat.showName;
				if (this.chat.type == "GROUP") {
					let size = this.groupMembers.filter(m => !m.quit).length;
					title += `(${size})`;
				}
				return title;
			},
			messageAction() {
				return `/message/${this.chat.type.toLowerCase()}/send`;
			}
		},
		onLoad(options) {
			console.log("onLoad")
			let chatIdx = options.chatIdx;
			this.chat = this.$store.state.chatStore.chats[chatIdx];
			this.scrollToBottom();
		}
	}
</script>

<style lang="scss" scoped>
	.chat-box {
		position: relative;
		background: white;
		border: #dddddd solid 1px;
		display: flex;
		flex-direction: column;
		height: calc(100vh - 44px);
	
		.header {
			display: flex;
			justify-content: center;
			align-items: center;
			height: 60rpx;
			padding: 5px;
			background-color: white;
			line-height: 50px;
			font-size: 40rpx;
			font-weight: 600;
			border: #dddddd solid 1px;


			.btn-side {
				position: absolute;
				right: 30rpx;
				line-height: 60rpx;
				font-size: 28rpx;
				cursor: pointer;
			}
		}


		.chat-msg {
			flex: 1;
			padding: 0;
			border: #dddddd solid 1px;
			overflow: hidden;
			position: relative;

			.scroll-box {
				height: 100%;
			}
		}

		.send-bar {
			display: flex;
			align-items: center;
			padding: 3rpx;
			border: #dddddd solid 1px;
			background-color: #eeeeee;
				
			.iconfont{
				font-size: 50rpx;
			}	
			.send-text {
				flex: 1;
				background-color: #f8f8f8 !important;
				overflow: auto;
				margin: 0rpx 12rpx;
				padding: 12rpx;
				background-color: #fff;
				border-radius: 10rpx;
				max-height: 225rpx;
				min-height: 65rpx;
				box-sizing: border-box;

				.send-text-area {
					width: 100%;
				}

			}

			.btn-send {
				text-align: right;
			}
		}

		.chat-tools {
			display: flex;
			flex-wrap: wrap;
			justify-content: space-between;
			background-color: whitesmoke;

			.chat-tools-item {
				width: 140rpx;
				padding: 20rpx;
				display: flex;
				flex-direction: column;
				align-items: center;

				.tool-icon {
					padding: 15rpx;
					font-size: 60rpx;
					background-color: white;
					border-radius: 13%;
				}

				.tool-name {
					height: 50rpx;
					line-height: 50rpx;
					font-size: 25rpx;
					flex: 3;
				}
			}
		}



	}
</style>
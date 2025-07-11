<template>
	<view class="chat-item" :class="active ? 'active' : ''">
		<!--rich-text中的表情包会屏蔽事件，所以这里用一个遮罩层捕获点击事件 -->
		<view class="mask" @tap="showChatBox()"></view>
		<view class="left">
			<head-image :url="chat.headImage" :name="chat.showName"></head-image>
		</view>
		<view class="chat-right">
			<view class="chat-name">
				<view class="chat-name-text">
					<view>{{ chat.showName }}</view>
				</view>
				<view class="chat-time">{{ $date.toTimeText(chat.lastSendTime, true) }}</view>
			</view>
			<view class="chat-content">
				<view class="chat-at-text">{{ atText }}</view>
				<view class="chat-send-name" v-if="isShowSendName">{{ chat.sendNickName + ':&nbsp;' }}</view>
				<view v-if="!isTextMessage" class="chat-content-text">{{chat.lastContent}}</view>
				<rich-text v-else class="chat-content-text" :nodes="nodesText"></rich-text>
				<view v-if="chat.isDnd" class="icon iconfont icon-dnd"></view>
				<uni-badge v-else-if="chat.unreadCount > 0" :max-num="99" :text="chat.unreadCount" />
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: "chatItem",
	data() {
		return {}
	},
	props: {
		chat: {
			type: Object
		},
		index: {
			type: Number
		},
		active: {
			type: Boolean,
			default: false
		}
	},
	methods: {
		showChatBox() {
			// 初始化期间进入会话会导致消息不刷新
			if (!getApp().$vm.isInit || this.chatStore.isLoading()) {
				uni.showToast({
					title: "正在初始化页面,请稍后...",
					icon: 'none'
				})
				return;
			}
			uni.navigateTo({
				url: "/pages/chat/chat-box?chatIdx=" + this.index
			})
		}
	},
	computed: {
		isShowSendName() {
			if (!this.chat.sendNickName) {
				return false;
			}
			let size = this.chat.messages.length;
			if (size == 0) {
				return false;
			}
			// 只有群聊的普通消息需要显示名称
			let lastMsg = this.chat.messages[size - 1];
			return this.$msgType.isNormal(lastMsg.type)
		},
		atText() {
			if (this.chat.atMe) {
				return "[有人@我]"
			} else if (this.chat.atAll) {
				return "[@全体成员]"
			}
			return "";
		},
		isTextMessage() {
			let idx = this.chat.messages.length - 1;
			let messageType = this.chat.messages[idx].type;
			return messageType == this.$enums.MESSAGE_TYPE.TEXT;
		},
		nodesText() {
			let text = this.$str.html2Escape(this.chat.lastContent);
			return this.$emo.transform(text, 'emoji-small')
		}
	}
}
</script>

<style scoped lang="scss">
.chat-item {
	height: 96rpx;
	display: flex;
	margin-bottom: 2rpx;
	position: relative;
	padding: 18rpx 20rpx;
	align-items: center;
	background-color: white;
	white-space: nowrap;

	&:hover {
		background-color: $im-bg-active;
	}

	&.active {
		background-color: $im-bg-active;
	}

	.mask {
		position: absolute;
		width: 100%;
		height: 100%;
		left: 0;
		right: 0;
		z-index: 99;
	}

	.left {
		position: relative;
		display: flex;
		justify-content: center;
		align-items: center;
		width: 100rpx;
		height: 100rpx;
	}

	.chat-right {
		height: 100%;
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
		padding-left: 20rpx;
		text-align: left;
		overflow: hidden;

		.chat-name {
			display: flex;

			.chat-name-text {
				flex: 1;
				font-size: $im-font-size-large;
				white-space: nowrap;
				overflow: hidden;
				display: flex;
				align-items: center;
			}

			.chat-time {
				font-size: $im-font-size-smaller-extra;
				color: $im-text-color-lighter;
				text-align: right;
				white-space: nowrap;
				overflow: hidden;
			}
		}

		.chat-content {
			display: flex;
			font-size: $im-font-size-smaller;
			color: $im-text-color-lighter;
			padding-top: 8rpx;
			align-items: center;

			.chat-at-text {
				color: $im-color-danger;
			}

			.chat-send-name {
				font-size: $im-font-size-smaller;
			}

			.chat-content-text {
				flex: 1;
				white-space: nowrap;
				overflow: hidden;
				text-overflow: ellipsis;
			}

			.icon {
				font-size: $im-font-size;
			}
		}
	}
}
</style>
<template>
	<view class="chat-item" :class="active ? 'active' : ''">
		<!--rich-text中的表情包会屏蔽事件，所以这里用一个遮罩层捕获点击事件 -->
		<view class="mask" @tap="showChatBox()"></view>
		<view class="left">
			<head-image :id="conversation.targetId" :url="conversation.headImage" :name="conversation.showName"
				:online="online"></head-image>
		</view>
		<view class="chat-right">
			<view class="chat-name">
				<view class="chat-name-text">
					<view>{{ conversation.showName }}</view>
				</view>
				<view class="chat-time">{{ $date.toTimeText(conversation.lastSendTime, true) }}</view>
			</view>
			<view class="chat-content">
				<view class="chat-at-text">{{ atText }}</view>
				<view class="chat-send-name" v-if="isShowSendName">{{ conversation.sendNickName + ':&nbsp;' }}</view>
				<rich-text class="chat-content-text" :nodes="nodesText"></rich-text>
				<view v-if="conversation.isDnd" class="icon iconfont icon-dnd"></view>
				<uni-badge v-else-if="conversation.unreadCount > 0" class="chat-unread" :max-num="99"
					:text="conversation.unreadCount" />
			</view>
			<view v-if="conversation.isTop" class="chat-top">
				<text class="icon iconfont icon-top-message"></text>
			</view>
		</view>
	</view>
</template>

<script>
import { friendStore } from '@/store/stores.js'
export default {
	name: "chatItem",
	data() {
		return {}
	},
	props: {
		conversation: {
			type: Object
		},
		active: {
			type: Boolean,
			default: false
		}
	},
	methods: {
		showChatBox() {
			uni.navigateTo({
				url: "/pages/chat/chat-box?convKey=" + this.conversation.key
			})
		}
	},
	computed: {
		isShowSendName() {
			return this.conversation.sendNickName;
		},
		atText() {
			if (this.conversation.atMe) {
				return "[有人@我]"
			} else if (this.conversation.atAll) {
				return "[@全体成员]"
			}
			return "";
		},
		nodesText() {
			let text = this.$str.html2Escape(this.conversation.lastContent);
			return this.$emo.transform(text, 'emoji-small')
		},
		online() {
			if (this.isPrivate) {
				const friend = friendStore.findFriend(this.conversation.targetId);
				return friend && friend.online;
			}
			return false;
		},
		isPrivate() {
			return this.$enums.CONVERSATION_TYPE.PRIVATE == this.conversation.type
		},
		isGroup() {
			return this.$enums.CONVERSATION_TYPE.GROUP == this.conversation.type
		},
	}
}

</script>

<style scoped lang="scss">
.chat-item {
	height: 136rpx;
	display: flex;
	position: relative;
	padding: 0 24rpx;
	align-items: center;
	background: white;
	white-space: nowrap;
	border-radius: 20rpx;
	box-sizing: border-box;

	&:hover {
		background: $im-bg-active;
	}

	&.active {
		background: $im-bg-active;
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
		width: 108rpx;
		height: 108rpx;
		flex-shrink: 0;
	}

	.chat-right {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
		overflow: hidden;
		padding-left: 20rpx;
		text-align: left;
		box-sizing: border-box;

		.chat-name {
			display: flex;
			align-items: center;
			overflow: hidden;

			.chat-name-text {
				flex: 1;
				font-size: $im-font-size-large;
				color: $im-text-color;
				line-height: 1.2;
				white-space: nowrap;
				overflow: hidden;
				display: flex;
				align-items: center;
			}

			.chat-time {
				font-size: $im-font-size-smaller;
				color: $im-text-color-lighter;
				text-align: right;
				white-space: nowrap;
				overflow: hidden;
				margin-left: 20rpx;
				min-width: 80rpx;
				flex-shrink: 0;
			}
		}

		.chat-content {
			display: flex;
			font-size: $im-font-size-small;
			color: $im-text-color-lighter;
			margin-top: 10rpx;
			align-items: center;
			line-height: 1.3;
			min-height: 36rpx;

			.chat-at-text {
				color: $im-color-danger;
			}

			.chat-send-name {
				font-size: $im-font-size-small;
			}

			.chat-content-text {
				flex: 1;
				white-space: nowrap;
				overflow: hidden;
				text-overflow: ellipsis;
			}

			.chat-unread {
				margin-left: 30rpx;
			}

			.icon {
				margin-left: 30rpx;
				font-size: $im-font-size;
			}
		}

		.chat-top {
			position: absolute;
			top: 3rpx;
			right: 3rpx;
			width: 35rpx;
			height: 35rpx;
			background: linear-gradient(225deg, #ffffff50 25%, #00000060), $im-color-primary;
			clip-path: polygon(0% 0%, 100% 100%, 100% 0%);
			border-radius: 6rpx 0 6rpx 0;

			.icon {
				position: absolute;
				top: 2rpx;
				right: 0;
				color: white;
				text-align: right;
				font-size: 20rpx;
			}
		}
	}
}

</style>

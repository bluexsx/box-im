<template>
	<view class="chat-item" :class="active?'active':''">
		<!--rich-text中的表情包会屏蔽事件，所以这里用一个遮罩层捕获点击事件 -->
		<view class="mask" @tap="showChatBox()"></view>
		<view class="left">
			<head-image :url="chat.headImage" :name="chat.showName" :size="90"></head-image>
		</view>
		<view class="chat-right">
			<view class="chat-name">
				<view class="chat-tag" v-if="chat.type=='GROUP'">
					<uni-tag disabled text="群" size="mini" type="primary"></uni-tag>
				</view>
				<view class="chat-name-text">
					{{chat.showName}}
				</view>
				<view class="chat-time">{{$date.toTimeText(chat.lastSendTime,true)}}</view>
			</view>
			<view class="chat-content">
				<view class="chat-at-text">{{atText}}</view>
				<view class="chat-send-name" v-if="isShowSendName">{{chat.sendNickName+':&nbsp;'}}</view>
				<rich-text class="chat-content-text" :nodes="$emo.transform(chat.lastContent)"></rich-text>
				<uni-badge v-if="chat.unreadCount>0" size="small" :max-num="99" :text="chat.unreadCount" />
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
			}
		}
	}
</script>

<style scoped lang="scss">
	.chat-item {
		height: 100rpx;
		display: flex;
		margin-bottom: 2rpx;
		position: relative;
		padding: 10rpx 20rpx;
		align-items: center;
		background-color: white;
		white-space: nowrap;

		&:hover {
			background-color: #f5f6ff;
		}

		&.active {
			background-color: #f5f6ff;
		}


		.mask {
			position: absolute;
			width: 100%;
			height: 100%;
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
			flex: 1;
			display: flex;
			flex-direction: column;
			padding-left: 20rpx;
			text-align: left;
			overflow: hidden;

			.chat-name {
				display: flex;
				line-height: 44rpx;
				height: 44rpx;

				.chat-tag {
					display: flex;
					align-items: center;
					margin-right: 5rpx;
				}

				.chat-name-text {
					flex: 1;
					font-size: 30rpx;
					font-weight: 600;
					white-space: nowrap;
					overflow: hidden;
				}

				.chat-time {
					font-size: 26rpx;
					text-align: right;
					color: #888888;
					white-space: nowrap;
					overflow: hidden;
				}
			}

			.chat-content {
				display: flex;
				line-height: 60rpx;
				height: 60rpx;

				.chat-at-text {
					color: #c70b0b;
					font-size: 24rpx;
				}

				.chat-send-name {
					font-size: 26rpx;
				}

				.chat-content-text {
					flex: 1;
					font-size: 28rpx;
					white-space: nowrap;
					overflow: hidden;
					text-overflow: ellipsis;
				}

			}
		}
	}
</style>
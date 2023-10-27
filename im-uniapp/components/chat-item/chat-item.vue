<template>
	<view class="chat-item" @click="showChatBox()">
		<view class="left">
			<image class="head-image" :src="chat.headImage"  mode="aspectFill"  lazy-load="true" ></image>
			<view v-show="chat.unreadCount>0" class="unread-text">{{chat.unreadCount}}</view>
		</view>
		<view class="chat-right">
			<view class="chat-name">
				{{ chat.showName}}
			</view>
			<view class="chat-content">
				<view class="chat-content-text" v-html="$emo.transform(chat.lastContent)"></view>
				<view class="chat-time">{{$date.toTimeText(chat.lastSendTime)}}</view>
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
			}
		},
		methods: {
			showChatBox(){
				uni.navigateTo({
					url: "/pages/chat/chat-box?chatIdx="+this.index
				})
			}
		}
	}
</script>

<style scoped lang="scss">
	.chat-item {
		height: 120rpx;
		display: flex;
		margin-bottom: 2rpx;
		position: relative;
		padding-left: 30rpx;
		align-items: center;
		padding-right: 10rpx;
		background-color: white;
		white-space: nowrap;
		&:hover {
			background-color: #eeeeee;
		}
		
		.left {
			position: relative;
			display: flex;
			justify-content: center;
			align-items: center;
			width: 100rpx;
			height: 100rpx;
			
			.head-image{
				width: 100%;
				height: 100%;
				border-radius: 10%;
				border: #eeeeee solid 1px;
			}
			
			.unread-text {
				position: absolute;
				background-color: red;
				right: -12rpx;
				top: -12rpx;
				color: white;
				border-radius: 16rpx;
				padding: 4rpx 12rpx;
				font-size: 20rpx;
				text-align: center;
				white-space: nowrap;
			}
		}

		.chat-right {
			flex: 1;
			display: flex;
			flex-direction: column;
			padding-left: 20rpx;
			text-align: left;

			.chat-name {
				font-size: 30rpx;
				font-weight: 600;
				line-height: 60rpx;
				white-space: nowrap;
				overflow: hidden;
			}

			.chat-content {
				display: flex;

				.chat-content-text {
					flex: 2;
					font-size: 28rpx;
					white-space: nowrap;
					overflow: hidden;
					line-height: 50rpx;
				}

				.chat-time {
					flex: 1;
					font-size: 26rpx;
					text-align: right;
					color: #888888;
					white-space: nowrap;
					overflow: hidden;
				}
			}
		}

		.mid {
			margin-left: 20rpx;
			flex: 2;
			display: flex;
			flex-direction: column;
			height: 100%;
			flex-shrink: 0;
			overflow: hidden;
	
			
			.show-name {
				display: flex;
				justify-content: flex-start;
				align-items: center;
				font-size: 36rpx;
				flex: 3;
			}

			.msg-text {
				flex: 2;
				font-size: 28rpx;
				color: #888888;
				white-space: nowrap;
			}
		}

		.right {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: flex-end;
			height: 80rpx;
			flex-shrink: 0;
			overflow: hidden;
			
			.msg-time {
				display: flex;
				justify-content: flex-start;
				align-items: center;
				font-size: 14px;
				color: #888888;
				white-space: nowrap;
			}
		}
	}

</style>

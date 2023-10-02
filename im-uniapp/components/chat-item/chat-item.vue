<template>
	<view class="chat-item" @click="showChatBox()">
		<view class="left">
			<image class="head-image" :src="chat.headImage"  mode="aspectFill"  lazy-load="true" ></image>
			<view v-show="chat.unreadCount>0" class="unread-text">{{chat.unreadCount}}</view>
		</view>
		<view class="mid">
			<view class="show-name">{{ chat.showName}}</view>
			<view class="msg-text" v-html="$emo.transform(chat.lastContent)"></view>
		</view>
		<view class="right ">
			<view class="msg-time">
				<chat-time :time="chat.lastSendTime"></chat-time>
			</view>
			<view></view>
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

<style  lang="scss" scode>
	.chat-item {
		height: 120rpx;
		display: flex;
		margin-bottom: 1rpx;
		position: relative;
		padding-left: 30rpx;
		align-items: center;
		padding-right: 10rpx;
		background-color: #fafafa;
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
			}
			
			.unread-text {
				position: absolute;
				background-color: red;
				right: -12rpx;
				top: -12rpx;
				color: white;
				border-radius: 30rpx;
				padding: 5rpx 6rpx;
				font-size: 10px;
				text-align: center;
				white-space: nowrap;
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

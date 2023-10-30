<template>
	<view class="friend-item" @click="showFriendInfo()">
		<head-image :name="friend.nickName" :online="friend.online" :url="friend.headImage"
			:size="100"></head-image>
		<view class="friend-info">
			<view class="friend-name">{{ friend.nickName}}</view>
			<view class="friend-online">
				<image v-show="friend.onlineWeb" class="online" src="/static/image/online_web.png"
					title="电脑设备在线" />
				<image v-show="friend.onlineApp" class="online" src="/static/image/online_app.png"
					title="移动设备在线" />
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "frined-item",
		data() {
			return {}
		},
		methods: {
			showFriendInfo() {
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + this.friend.id
				})
			},
		},
		props: {
			friend: {
				type: Object
			}
		}
	}
</script>

<style scope lang="scss">
	.friend-item {
		height: 120rpx;
		display: flex;
		margin-bottom: 1rpx;
		position: relative;
		padding-left: 30rpx;
		align-items: center;
		padding-right: 10rpx;
		background-color: white;
		white-space: nowrap;

		&:hover {
			background-color: #eeeeee;
		}

		.friend-info {
			flex: 1;
			display: flex;
			flex-direction: column;
			padding-left: 20rpx;
			text-align: left;

			.friend-name {
				font-size: 30rpx;
				font-weight: 600;
				line-height: 60rpx;
				white-space: nowrap;
				overflow: hidden;
			}
			
			.friend-online {
				.online {
					padding-right: 4rpx;
					width: 32rpx;
					height: 32rpx;
				
				}
			}
		}
	}
</style>
<template>
	<view class="friend-item" @click="showFriendInfo()">
		<head-image :name="friend.nickName" :online="friend.online" :url="friend.headImage" size="small"></head-image>
		<view class="friend-info">
			<view class="friend-name">{{ friend.nickName }}</view>
			<view class="friend-online">
				<image v-show="friend.onlineWeb" class="online" src="/static/image/online_web.png" title="电脑设备在线" />
				<image v-show="friend.onlineApp" class="online" src="/static/image/online_app.png" title="移动设备在线" />
			</view>
			<slot></slot>
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
			if (this.detail) {
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + this.friend.id
				})
			}
		}
	},
	props: {
		friend: {
			type: Object
		},
		detail: {
			type: Boolean,
			default: true
		}
	}
}
</script>

<style scope lang="scss">
.friend-item {
	height: 90rpx;
	display: flex;
	margin-bottom: 1rpx;
	position: relative;
	padding: 10rpx;
	padding-left: 20rpx;
	align-items: center;
	background-color: white;
	white-space: nowrap;

	&:hover {
		background-color: $im-bg;
	}

	.friend-info {
		flex: 1;
		display: flex;
		padding-left: 20rpx;
		text-align: left;

		.friend-name {
			flex: 1;
			font-size: $im-font-size;
			white-space: nowrap;
			overflow: hidden;
		}

		.friend-online {
			margin-top: 4rpx;

			.online {
				padding-right: 4rpx;
				width: 24rpx;
				height: 24rpx;
			}
		}
	}
}
</style>
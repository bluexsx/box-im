<template>
	<view class="friend-item" @click="showFriendInfo()">
		<head-image :name="friend.nickName" :online="friend.online" :url="friend.headImage"
			:size="100"></head-image>
		<view class="friend-info">
			<view class="friend-name">{{ friend.nickName}}</view>
			<view class="friend-online">
				<text v-show="friend.onlineWeb"  title="网页端在线">【网页端】</text>
				<text v-show="friend.onlineApp"  title="移动端在线">【移动端】</text>			
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
		}
	}
</style>
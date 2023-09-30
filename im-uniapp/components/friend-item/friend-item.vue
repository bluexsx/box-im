<template>
	<view class="friend-item" @click="showFriendInfo()">
		<view class="avatar">
			<image class="head-image" :src="friend.headImage" lazy-load="true"  mode="aspectFill"></image>
		</view>
		<view class="text">
			<view>{{ friend.nickName}}</view>
			<view :class="online ? 'online-status  online':'online-status'">{{ online?"[在线]":"[离线]"}}</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "frinedItem",
		data() {
			return {}
		},
		methods:{
			showFriendInfo(id){
				console.log(id);
				uni.navigateTo({
					url: "/pages/common/user-info?id="+this.friend.id
				})
			},
		},
		props: {
			friend: {
				type: Object
			},
			index: {
				type: Number
			}
		},
		computed: {
			online() {
				return this.$store.state.friendStore.friends[this.index].online;
			}
		}
	}
</script>

<style scope lang="scss">
	.friend-item {
		height: 100rpx;
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

		.avatar {
			display: flex;
			justify-content: center;
			align-items: center;
			width: 80rpx;
			height: 80rpx;
			
			.head-image{
				width: 100%;
				height: 100%;
				border-radius: 10%;
			}
		}

		.text {
			font-size: 30rpx;
			margin-left: 30rpx;
			flex: 1;
			display: flex;
			flex-direction: column;
			justify-content: space-around;
			height: 100%;
			flex-shrink: 0;
			overflow: hidden;

			&>view {
				display: flex;
				justify-content: flex-start;
			}

			.online-status {
				font-size: 22rpx;
				font-weight: 600;

				&.online {
					color: #5fb878;
				}
			}
		}
	}
</style>

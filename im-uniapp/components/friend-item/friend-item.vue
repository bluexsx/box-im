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
		name: "frined-item",
		data() {
			return {}
		},
		methods:{
			showFriendInfo(){
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

		.avatar {
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
		}

		.text {
			font-size: 36rpx;
			margin-left: 30rpx;
			flex: 1;
			display: flex;
			flex-direction: column;
			justify-content: space-around;
			height: 100%;
			flex-shrink: 0;
			overflow: hidden;


			.online-status {
				font-size: 28rpx;
				font-weight: 600;

				&.online {
					color: #5fb878;
				}
			}
		}
	}
</style>

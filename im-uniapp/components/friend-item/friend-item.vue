<template>
	<view class="friend-item" @click="showFriendInfo()">
		<view class="friend-avatar">
			<image class="head-image" :src="friend.headImage" lazy-load="true"  mode="aspectFill"></image>
		</view>
		<view class="friend-info">
			<view class="friend-name">{{ friend.nickName}}</view>
			<view class="friend-online" 
			:class="friend.online ? 'online':''">
			{{ friend.online?"[在线]":"[离线]"}}
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

		.friend-avatar {
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
				font-size: 28rpx;
		
				&.online {
					color: #5fb878;
				}
			}
		}
	}
</style>

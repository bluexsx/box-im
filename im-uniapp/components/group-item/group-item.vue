<template>
	<view class="group-item" @click="showGroupInfo()">
		<view class="group-avatar">
			<image class="head-image" :src="group.headImage" lazy-load="true"  mode="aspectFill"></image>
		</view>
		<view class="group-name">
			<view>{{ group.remark}}</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "group-item",
		data() {
			return {}
		},
		methods:{
			showGroupInfo(){
				uni.navigateTo({
					url:"/pages/group/group-info?id="+this.group.id
				})
			},
		},
		props: {
			group: {
				type: Object
			}
		}
	}
</script>

<style scope lang="scss">
	.group-item {
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

		.group-avatar {
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

		.group-name {
			font-size: 32rpx;
			padding-left: 20rpx;
			font-weight: 600;
			text-align: left;
			white-space: nowrap;
			overflow: hidden;
		}
	}
</style>

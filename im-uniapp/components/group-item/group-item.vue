<template>
	<view class="group-item" @click="showGroupInfo()">
		<view class="avatar">
			<image class="head-image" :src="group.headImage" lazy-load="true"  mode="aspectFill"></image>
		</view>
		<view class="text">
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
		}
	}
</style>

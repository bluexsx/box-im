<template>
	<view class="group-item" @click="showGroupInfo()">
		<head-image :name="group.remark"
			:url="group.headImage" :size="90"></head-image>
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
		height: 100rpx;
		display: flex;
		margin-bottom: 1rpx;
		position: relative;
		padding: 10rpx;
		padding-left: 20rpx;
		align-items: center;
		background-color: white;
		white-space: nowrap;
		&:hover {
			background-color: #f5f6ff;
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

<template>
	<view class="group-item" @click="showGroupInfo()">
		<head-image :name="group.remark"
			:url="group.headImage" :size="100"></head-image>
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

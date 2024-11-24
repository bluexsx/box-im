<template>
	<view class="group-item" @click="showGroupInfo()">
		<head-image :name="group.showGroupName"
			:url="group.headImage" size="small"></head-image>
		<view class="group-name">
			<view>{{ group.showGroupName}}</view>
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
		height: 90rpx;
		display: flex;
    margin-bottom: 2rpx;
		position: relative;
    padding: 18rpx 20rpx;
		align-items: center;
		background-color: white;
		white-space: nowrap;

    &:hover {
      background-color: $im-bg-active;
    }

    &.active {
      background-color: $im-bg-active;
    }

    .group-name {
			font-size: $im-font-size;
			padding-left: 20rpx;
			text-align: left;
			white-space: nowrap;
			overflow: hidden;
		}
	}
</style>

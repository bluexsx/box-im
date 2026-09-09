<template>
	<view class="group-item" @click="showGroupInfo()">
		<head-image :id="group.id" :name="group.showGroupName" :url="group.headImageThumb" size="small"></head-image>
		<view class="group-name">
			<view>{{ group.showGroupName }}</view>
		</view>
	</view>
</template>

<script>
export default {
	name: "group-item",
	data() {
		return {}
	},
	methods: {
		showGroupInfo() {
			uni.navigateTo({
				url: "/pages/group/group-info?id=" + this.group.id
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
	position: relative;
	padding: 10rpx 20rpx;
	align-items: center;
	background: white;
	white-space: nowrap;
	border-radius: 20rpx;
	box-sizing: border-box;

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

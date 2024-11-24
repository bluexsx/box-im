<template>
	<view class="arrow-bar">
		<text class="title">{{ title }}</text>
		<uni-icons class="arrow" type="right" size="16"></uni-icons>
	</view>
</template>

<script>
export default {
	name: "arrow-bar",
	props: {
		title: {
			type: String,
			required: true
		}
	},
}
</script>

<style lang="scss" scoped>
.arrow-bar {
	width: 100%;
	height: 90rpx;
	font-size: 30rpx;
	color: black;
	margin-top: 5rpx;
	background-color: white;
	line-height: 90rpx;
	display: flex;

	.title {
		flex: 1;
		margin-left: 40rpx;
	}

	.arrow {
		margin-right: 40rpx;
	}
}
</style>
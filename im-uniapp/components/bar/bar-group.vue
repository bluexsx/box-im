<template>
	<view class="bar-group">
		<slot></slot>
	</view>
</template>

<script>
	export default{
		name: "bar-group"
	}
</script>

<style lang="scss" scoped>
	.bar-group{
		margin: 20rpx 0;
	}
</style>
<template>
	<view class="bar-group">
		<slot></slot>
	</view>
</template>

<script>
export default {
	name: "bar-group"
}
</script>

<style lang="scss" scoped>
.bar-group {
	margin-bottom: 20rpx;
}
</style>

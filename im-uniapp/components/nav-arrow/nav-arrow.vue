<template>
	<view class="nav-arrow">
		<text class="iconfont icon-nav-arrow"></text>
	</view>
</template>

<script>
export default {
	name: "nav-arrow"
}
</script>

<style lang="scss" scoped>
.nav-arrow {
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;

	.iconfont {
		font-size: 32rpx;
		color: $im-text-color-lighter;
		line-height: 1;
	}
}
</style>

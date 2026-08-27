<template>
	<view class="no-data-tip">
		<image class="image" src="/static/image/no_data.png" mode="aspectFit" />
		<view class="text">{{ displayTip }}</view>
	</view>
</template>

<script>
export default {
	name: "no-data-tip",
	props: {
		tip: {
			type: String,
			default: ''
		}
	},
	data() {
		return {};
	},
	computed: {
		displayTip() {
			return this.tip || '暂无数据';
		}
	}
}
</script>

<style scoped lang="scss">
.no-data-tip {
	display: flex;
	flex: 1;
	height: 100%;
	flex-direction: column;
	align-items: center;
	justify-content: center;

	.image {
		height: 240rpx;
		display: block;
	}

	.text {
		margin-top: 28rpx;
		padding: 0 24rpx;
		max-width: 560rpx;
		text-align: center;
		font-size: $im-font-size;
		color: $im-text-color-light;
		line-height: 1.55;
	}
}
</style>

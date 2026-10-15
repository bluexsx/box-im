<template>
	<view class="arrow-bar">
		<text v-if="icon" class="icon iconfont" :class="icon" :style="{color: iconColor}"></text>
		<text class="title">{{ title }}</text>
		<text v-if="content" class="content">{{ content }}</text>
		<slot></slot>
		<nav-arrow class="arrow"></nav-arrow>
	</view>
</template>

<script>
export default {
	name: "arrow-bar",
	props: {
		title: {
			type: String,
			required: true
		},
		content: {
			type: String,
			default: ''
		},
		icon: {
			type: String,
			default: ''
		},
		iconColor: {
			type: String,
			default: ''
		}
	}
}

</script>

<style lang="scss" scoped>
.arrow-bar {
	height: 106rpx;
	font-size: $im-font-size;
	color: $im-text-color;
	background-color: white;
	display: flex;
	align-items: center;
	padding: 0 32rpx;

	&:active {
		background-color: $im-bg-active;
	}

	.icon {
		font-size: 40rpx;
		margin-right: 24rpx;
	}

	.title {
		flex: 1;
		line-height: 1.3;
	}

	.content {
		font-size: $im-font-size-small;
		color: $im-text-color-lighter;
		overflow: hidden;
		white-space: nowrap;
		text-overflow: ellipsis;
		padding: 0 12rpx;
		text-align: right;
		max-width: 400rpx;
	}

	.arrow {
		margin-left: 8rpx;
	}
}

</style>

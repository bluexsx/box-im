<template>
	<view class="btn-bar" :class="type">
		<text v-if="icon" class="icon iconfont" :class="icon"></text>
		<text class="title">{{ title }}</text>
	</view>
</template>

<script>
export default {
	name: "btn-bar",
	props: {
		title: {
			type: String,
			required: true
		},
		icon: {
			type: String,
			required: false
		},
		type: {
			type: String,
			default: "normal"
		}
	}
}
</script>

<style lang="scss" scoped>
.btn-bar {
	height: 100rpx;
	background-color: white;
	line-height: 100rpx;
	text-align: center;
	display: flex;
	justify-content: center;
	color: $im-text-color;
	margin-bottom: 3rpx;

	&:active {
		background-color: $im-bg-active;
	}

	.icon {
		font-size: 40rpx;
		font-weight: 600;
		margin-right: 10rpx;
	}

	.title {
		font-size: $im-font-size-large;
		font-weight: 600;
	}

	&.primary {
		color: $im-color-primary
	}

	&.danger {
		color: $im-color-danger;
	}
}
</style>

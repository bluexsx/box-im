<template>
	<view class="btn-bar" :style="style">
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
		},
		color: {
			type: String,
			default: "#000"
		}
	},
	computed: {
		style() {
			let color = "#000";
			switch (this.type) {
				case 'danger':
					color = "#f14747";
					break;
				case 'primary':
					color = "#35567f";
					break;
			}
			return `color: ${color};`
		}
	}
}
</script>

<style lang="scss" scoped>
.btn-bar {
	width: 100%;
	height: 100rpx;
	margin-top: 5rpx;
	background-color: white;
	line-height: 100rpx;
	text-align: center;
	display: flex;
	justify-content: center;

	.icon {
		font-size: 40rpx;
		font-weight: 600;
		margin-right: 10rpx;
	}

	.title {
		font-size: 32rpx;
		font-weight: 600;
	}
}
</style>
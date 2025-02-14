<template>
	<view class="arrow-bar">
		<text class="icon iconfont" :class="icon" :style="{color: textColor}"></text>
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
		},
		icon: {
			type: String,
			default: ''
		}
	},
	data() {
		return {
			colors: ["#5daa31", "#c7515a", "#e03697", "#85029b",
				"#c9b455", "#326eb6"]
		}
	},
	computed:{
		textColor() {
			let hash = 0;
			for (var i = 0; i < this.title.length; i++) {
				hash += this.title.charCodeAt(i);
			}
			return this.colors[hash % this.colors.length];
		}
	}
}
</script>

<style lang="scss" scoped>
.arrow-bar {
	width: 100%;
	height: 90rpx;
	font-size: $im-font-size;
	color: $im-text-color;
	margin-top: 5rpx;
	background-color: white;
	line-height: 90rpx;
	display: flex;
	
	.icon {
		margin-left: 40rpx;
	}
	
	.title {
		flex: 1;
		margin-left: 10rpx;
	}

	.arrow {
		margin-right: 40rpx;
	}
}
</style>
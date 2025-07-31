<template>
	<view class="loading-box" :style="loadingStyle">
		<view class="rotate iconfont icon-loading" :style="icontStyle"></view>
		<slot></slot>
	</view>
</template>

<script>
export default {
	data() {
		return {}
	},
	props: {
		size: {
			type: Number,
			default: 100
		},
		iconColor: {
			type: String,
			default: ''
		},
		mask: {
			type: Boolean,
			default: true
		}
	},
	computed: {
		icontStyle() {
			let style = `font-size:${this.size}rpx;`;
			if(this.iconColor){
				style += `color: ${this.iconColor};`
			}else if(this.mask){
				style += 'color: #eee;'
			}
			return style;	
		},
		loadingStyle() {
			return this.mask ? "background: rgba(0, 0, 0, 0.3);" : "";
		}
	}
}
</script>

<style lang="scss" scoped>
.loading-box {
	width: 100%;
	height: 100%;
	position: absolute;
	left: 0;
	top: 0;
	z-index: 10000;
	display: flex;
	justify-content: center;
	align-items: center;
}

.rotate {
	animation: rotate 2s ease-in-out infinite;

}

@keyframes rotate {
	from {
		transform: rotate(0deg)
	}

	to {
		transform: rotate(360deg)
	}
}
</style>
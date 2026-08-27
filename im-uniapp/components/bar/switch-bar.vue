<template>
	<view class="switch-bar">
		<text class="title">{{ title }}</text>
		<switch class="switch" :disabled="disabled" :checked="checked" @change="onChange" style="transform:scale(0.8)">
		</switch>
	</view>
</template>

<script>
export default {
	name: "switch-bar",
	props: {
		title: {
			type: String,
			required: true
		},
		disabled: {
			type: Boolean,
			default: false
		},
		checked: {
			type: Boolean,
			default: false
		}
	},
	data() {
		return {
			value: this.checked
		}
	},
	methods: {
		onChange(e) {
			this.value = true;
			setTimeout(() => {
				this.value = false;
			}, 100)
			this.$emit('change', e);
		}
	}
}
</script>

<style lang="scss" scoped>
.switch-bar {
	height: 100rpx;
	font-size: $im-font-size;
	color: $im-text-color;
	background-color: white;
	line-height: 100rpx;
	display: flex;
	padding: 0 20rpx;
	margin-bottom: 3rpx;

	.title {
		flex: 1;
		margin-left: 20rpx;
	}
}
</style>

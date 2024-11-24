<template>
	<view class="switch-bar">
		<text class="title">{{ title }}</text>
		<switch class="switch" :checked="checked" color="#18bc37" @change="onChange"></switch>
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
			//this.value = false;

			this.$emit('change', e);
		}
	}

}
</script>

<style lang="scss" scoped>
.switch-bar {
	width: 100%;
	height: 100rpx;
	font-size: 34rpx;
	color: black;
	margin-top: 5rpx;
	background-color: white;
	line-height: 100rpx;
	display: flex;

	.title {
		flex: 1;
		margin-left: 40rpx;
	}

	.switch {
		margin-right: 40rpx;
	}
}
</style>
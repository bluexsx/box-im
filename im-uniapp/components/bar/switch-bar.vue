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
	height: 106rpx;
	font-size: $im-font-size;
	color: $im-text-color;
	background-color: #fff;
	display: flex;
	align-items: center;
	padding: 0 32rpx;

	.title {
		flex: 1;
		line-height: 1.3;
	}
}

</style>

<template>
	<scroll-view scroll-y="true" :style="{height: height}" upper-threshold="200" @scrolltolower="onScrollToBottom"
		scroll-with-animation="true">
		<view v-for="(item, idx) in showItems" :key="idx">
			<slot :item="item">
			</slot>
		</view>
	</scroll-view>
</template>

<script>
export default {
	name: "virtual-scroller",
	data() {
		return {
			page: 1,
			isInitEvent: false,
			lockTip: false
		}
	},
	props: {
		height: {
			type: String,
			default: '60vh'
		},
		items: {
			type: Array
		},
		size: {
			type: Number,
			default: 30
		}
	},
	methods: {
		onScrollToBottom(e) {
			if (this.showMaxIdx >= this.items.length) {
				this.showTip();
			} else {
				this.page++;
			}
		},
		showTip() {
			uni.showToast({
				title: "已滚动至底部",
				icon: 'none'
			});
		}
	},
	computed: {
		showMaxIdx() {
			return Math.min(this.page * this.size, this.items.length);
		},
		showItems() {
			return this.items.slice(0, this.showMaxIdx);
		}
	}
}
</script>

<style scoped></style>
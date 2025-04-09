<template>
	<view>
		<web-view :src="linkUrl"></web-view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			linkUrl: ''
		};
	},
	onLoad(options) {
		this.linkUrl = decodeURIComponent(options.url);
	}
}
</script>

<style></style>
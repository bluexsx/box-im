<template>
	<view class="chat-private-video">
		<!-- webviews实现源码在im-webview-private-rtc目录  -->
		<web-view fullscreen="false" id="chat-video-wv" :src="webviewUrl"></web-view>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js'
export default {
	data() {
		return {
			webviewUrl: "/hybrid/html/rtc-private/index.html",
			wv: ''
		}
	}
}
</script>

<style lang="scss" scoped>

</style>
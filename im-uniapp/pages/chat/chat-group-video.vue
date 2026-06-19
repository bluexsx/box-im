<template>
	<web-view class="page chat-group-video" id="chat-video-wv" :src="url"></web-view>
</template>

<script>
import UNI_APP from '@/.env.js'
export default {
	data() {
		return {
			url: "/hybrid/html/rtc-group/index.html",
			wv: '',
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-group-video {
}
</style>
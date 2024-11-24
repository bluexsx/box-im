<template>
	<web-view class="page chat-private-video" id="chat-video-wv" @message="onMessage" :src="url"></web-view>
</template>

<script>
import UNI_APP from '@/.env.js'
export default {
	data() {
		return {
			url: "",
			wv: '',
			mode: "video",
			isHost: true,
			friend: {}
		}
	},
	methods: {
		onMessage(e) {
			this.onWebviewMessage(e.detail.data[0]);
		},
		onWebviewMessage(event) {
			console.log("来自webview的消息:" + JSON.stringify(event))
			switch (event.key) {
				case "WV_READY":
					this.initWebView();
					break;
				case "WV_CLOSE":
					uni.navigateBack();
					break;
			}
		},
		sendMessageToWebView(key, message) {
			// 如果webview还没初始化好，则延迟100ms再推送
			if (!this.wv) {
				setTimeout(() => this.sendMessageToWebView(key, message), 100)
				return;
			}
			let event = {
				key: key,
				data: message
			}
			// #ifdef APP-PLUS
			this.wv.evalJS(`onEvent('${encodeURIComponent(JSON.stringify(event))}')`)
			// #endif
			// #ifdef H5
			this.wv.postMessage(event, '*');
			// #endif
		},
		initWebView() {
			// #ifdef APP-PLUS
			// APP的webview
			this.wv = this.$scope.$getAppWebview().children()[0]
			// #endif
			// #ifdef H5
			// H5的webview就是iframe
			this.wv = document.getElementById('chat-video-wv').contentWindow
			// #endif
		},
		initUrl() {
			this.url = "/hybrid/html/rtc-private/index.html";
			this.url += "?mode=" + this.mode;
			this.url += "&isHost=" + this.isHost;
			this.url += "&baseUrl=" + UNI_APP.BASE_URL;
			this.url += "&loginInfo=" + JSON.stringify(uni.getStorageSync("loginInfo"));
			this.url += "&userInfo=" + JSON.stringify(this.userStore.userInfo);
			this.url += "&friend=" + JSON.stringify(this.friend);
			this.url += "&config=" + JSON.stringify(this.configStore.webrtc);
		},
	},
	onBackPress() {
		this.sendMessageToWebView("NAV_BACK", {})
	},
	onLoad(options) {
		uni.$on('WS_RTC_PRIVATE', msg => {
			// 推送给web-view处理
			this.sendMessageToWebView("RTC_MESSAGE", msg);
		})
		// #ifdef H5
		window.onmessage = (e) => {
			this.onWebviewMessage(e.data.data.arg);
		}
		// #endif
		// 模式：视频呼叫/语音呼叫
		this.mode = options.mode;
		// 是否呼叫方
		this.isHost = JSON.parse(options.isHost);
		// 解析页面跳转时带过来的好友信息
		this.friend = JSON.parse(decodeURIComponent(options.friend));
		// 构建url
		this.initUrl();
	},
	onUnload() {
		uni.$off('WS_RTC_PRIVATE')
	}
}
</script>

<style lang="scss" scoped>
.chat-web-view {}
</style>
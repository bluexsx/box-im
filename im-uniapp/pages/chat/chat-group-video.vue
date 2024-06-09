<template>
	<view class="page chat-group-video">
		<view>
			<web-view id="chat-video-wv" @message="onMessage" :src="url"></web-view>
		</view>
	</view>
</template>

<script>
	import UNI_APP from '@/.env.js'
	export default {
		data() {
			return {
				url: "",
				wv: '',
				isHost: false,
				groupId: null,
				inviterId: null,
				userInfos: []
			}
		},
		methods: {
			onMessage(e) {
				this.onWebviewMessage(e.detail.data[0]);
			},
			onInsertMessage(msgInfo) {

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
					case "INSERT_MESSAGE":
						this.onInsertMessage(event.data);
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
				this.url = "/hybrid/html/rtc-group/index.html?";
				this.url += "baseUrl=" + UNI_APP.BASE_URL;
				this.url += "&groupId=" + this.groupId;
				this.url += "&userId=" + this.$store.state.userStore.userInfo.id;
				this.url += "&inviterId=" + this.inviterId;
				this.url += "&isHost=" + this.isHost;
				this.url += "&loginInfo=" + JSON.stringify(uni.getStorageSync("loginInfo"));
				this.url += "&userInfos=" + JSON.stringify(this.userInfos);
				console.log(this.url)
			},
		},
		onBackPress() {
			this.sendMessageToWebView("NAV_BACK", {})
		},
		onLoad(options) {
			uni.$on('WS_RTC_GROUP', msg => {
				// 推送给web-view处理
				this.sendMessageToWebView("RTC_MESSAGE", msg);
			})
			// #ifdef H5
			window.onmessage = (e) => {
				this.onWebviewMessage(e.data.data.arg);
			}
			// #endif
			// 是否发起人
			this.isHost = JSON.parse(options.isHost);
			// 发起者的用户
			this.inviterId = options.inviterId;
			// 解析页面跳转时带过来的好友信息
			this.groupId = options.groupId;
			// 邀请的用户信息
			this.userInfos = JSON.parse(decodeURIComponent(options.userInfos));

			// 构建url
			this.initUrl();
		},
		onUnload() {
			uni.$off('WS_RTC_GROUP')
		}
	}
</script>

<style lang="scss" scoped>
	.chat-group-video {
		.header {
			display: flex;
			justify-content: center;
			align-items: center;
			height: 60rpx;
			padding: 5px;
			background-color: white;
			line-height: 50px;
			font-size: 40rpx;
			font-weight: 600;
			border: #dddddd solid 1px;

			.btn-side {
				position: absolute;
				line-height: 60rpx;
				font-size: 28rpx;
				cursor: pointer;

				&.left {
					left: 30rpx;
				}

				&.right {
					right: 30rpx;
				}
			}
		}
	}
</style>
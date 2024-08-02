<template>
	<el-dialog v-dialogDrag top="5vh" title="语音通话" :close-on-click-modal="false" :close-on-press-escape="false"
			   :visible.sync="isShow" width="50%">
		<div class='rtc-group-video'>
			<div style="padding-top:30px;font-weight: 600; text-align: center;font-size: 16px;">
				多人音视频通话为付费功能，有需要请联系作者...
			</div>
			<div style="padding-top:50px; text-align: center;font-size: 16px;">
				点击下方文档了解详细信息:
			</div>
			<div style="padding-top:10px; text-align: center;font-size: 16px;">
				<a href="https://www.yuque.com/u1475064/mufu2a/vi7engzluty594s2" target="_blank">
					付费-音视频通话源码
				</a>
			</div>

		</div>
	</el-dialog>
</template>

<script>
	export default {
		name: "rtcGroupVideo",
		data() {
			return {
				isShow: false
			}
		},
		methods: {
			open() {
				this.isShow = true;
			},
			onRTCMessage(){
				//this.isShow = true;
			}
		}
	}
</script>

<style lang="scss">
	.rtc-group-video {
		height: 300px;
		background-color: #E8F2FF;
	}
</style>
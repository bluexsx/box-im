<template>
	<el-dialog v-dialogDrag top="5vh" title="语音通话" :close-on-click-modal="false" :close-on-press-escape="false"
		:visible.sync="isShow" width="50%">
		<div class='rtc-group-video'>
			<div style="padding-top:30px;font-weight: 600; text-align: center;font-size: 16px;">
				多人音视频通话属于付费功能，如有需要请联系作者购买商业版源码...
			</div>
			<div style="padding-top:50px; text-align: center;font-size: 16px;">
				点击下方文档了解详细信息:
			</div>
			<div style="padding-top:10px; text-align: center;font-size: 16px;">
				<a href="https://www.yuque.com/u1475064/imk5n2/qtezcg32q1d0dr29" target="_blank">
					盒子IM商业版付费说明
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
		onRTCMessage() {
			//this.isShow = true;
		}
	}
}
</script>

<style lang="scss" scoped>
.rtc-group-video {
	height: 300px;
	background-color: #E8F2FF;
}
</style>
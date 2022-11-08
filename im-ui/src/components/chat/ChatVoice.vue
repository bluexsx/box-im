<template>
	<el-dialog class="chat-voice" title="语言录制" :visible.sync="visible" width="35%" :before-close="handleClose">
		<div>
			录音时长:{{duration}}
		</div>

		<el-row>
			<el-button round type="primary" v-show="state=='STOP'" @click="handleStartRecord()">开始录音</el-button>
			<el-button round type="warning" v-show="state=='RUNNING'" @click="handlePauseRecord()">暂停录音</el-button>
			<el-button round type="primary" v-show="state=='PAUSE'" @click="handleResumeRecord()">继续录音</el-button>
			<el-button round type="danger" v-show="state=='RUNNING'||state=='PAUSE'" @click="handleCompleteRecord()">结束录音</el-button>
			<el-button round type="success" v-show="state=='COMPLETE'" @click="handlePlayRecord()">播放录音</el-button>
			<el-button round type="primary" v-show="state=='COMPLETE'" @click="handleRestartRecord()">重新录音</el-button>
			<el-button round type="primary" v-show="state=='COMPLETE'" @click="handleSendRecord()">立即发送</el-button>
		</el-row>

	</el-dialog>

</template>

<script>
	import Recorderx, {
		ENCODE_TYPE
	} from 'recorderx';

	export default {
		name: 'chatVoice',
		props: {
			visible: {
				type: Boolean
			}
		},
		data() {
			return {
				rc: new Recorderx(),
				state: 'STOP', //STOP、RUNNING、PAUSE、COMPLETE
				duration: 0
			}
		},
		methods: {
			handleClose() {
				this.$emit("close");
			},
			handleStartRecord() {
				this.rc.start().then(() => {
					this.$message.success("开始录音");
				}).catch(error => {
					this.$message.error("录音失败" + error.message);
				});
				console.log(this.rc)
				this.state = 'RUNNING';
			},
			handlePauseRecord() {
				this.state = 'PAUSE';
			},
			handleResumeRecord() {
				this.state = 'RUNNING';
			},
			handleCompleteRecord() {
				this.rc.pause()
				let wav = this.rc.getRecord({
					encodeTo: ENCODE_TYPE.WAV,
				});
				console.log(wav);
				this.state = 'COMPLETE';
			},
			handlePlayRecord() {

			},
			handleRestartRecord() {
				this.state = 'RUNNING';
			},
			handleSendRecord() {
				this.upload();
			},
			upload() {
				let wav = this.rc.getRecord({
					encodeTo: ENCODE_TYPE.WAV,
				});
				let name = new Date().getDate() + '.wav';
				var formData = new window.FormData()
				formData.append('file', wav, name);
				this.$http({
					url: '/file/upload',
					data: formData,
					method: 'post',
					headers: {
						'Content-Type': 'multipart/form-data'
					}
				}).then((url)=>{
					this.$message.success("上传成功");
					console.log(url);
				})
			}
		}
	}
</script>

<style>
</style>

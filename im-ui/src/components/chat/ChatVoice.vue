<template>
	<el-dialog class="chat-voice" title="语音录制" :visible.sync="visible" width="600px" :before-close="handleClose">
		<div v-show="mode=='RECORD'">
			<div class="chat-voice-tip">{{stateTip}}</div>
			<div>时长: {{state=='STOP'?0:parseInt(rc.duration)}}s</div>
		</div>
		<audio v-show="mode=='PLAY'" :src="url" controls ref="audio" @ended="handleStopAudio()"></audio>
		<el-divider content-position="center"></el-divider>
		<el-row class="chat-voice-btn-group">
			<el-button round type="primary" v-show="state=='STOP'" @click="handleStartRecord()">开始录音</el-button>
			<el-button round type="warning" v-show="state=='RUNNING'" @click="handlePauseRecord()">暂停录音</el-button>
			<el-button round type="primary" v-show="state=='PAUSE'" @click="handleResumeRecord()">继续录音</el-button>
			<el-button round type="danger" v-show="state=='RUNNING'||state=='PAUSE'" @click="handleCompleteRecord()">
				结束录音</el-button>
			<el-button round type="success" v-show="state=='COMPLETE' && mode!='PLAY'" @click="handlePlayAudio()">播放录音
			</el-button>
			<el-button round type="warning" v-show="state=='COMPLETE' && mode=='PLAY'" @click="handleStopAudio()">停止播放
			</el-button>
			<el-button round type="primary" v-show="state=='COMPLETE'" @click="handleRestartRecord()">重新录音</el-button>
			<el-button round type="primary" v-show="state=='COMPLETE'" @click="handleSendRecord()">立即发送</el-button>
		</el-row>

	</el-dialog>

</template>

<script>
	import Recorder from 'js-audio-recorder';

	export default {
		name: 'chatVoice',
		props: {
			visible: {
				type: Boolean
			}
		},
		data() {
			return {
				rc: new Recorder(),
				audio: new Audio(),
				state: 'STOP', // STOP、RUNNING、PAUSE、COMPLETE
				stateTip: "未开始",
				mode: 'RECORD', // RECORD 、PLAY
				duration: 0,
				url: ""
			}
		},
		methods: {
			handleClose() {
				// 关闭前清除数据
				this.rc.stop();
				this.audio.pause();
				this.mode = 'RECORD';
				this.state = 'STOP';
				this.stateTip = '未开始';
				this.$emit("close");
			},
			handleStartRecord() {
				this.rc.start().then((stream) => {
					this.state = 'RUNNING';
					this.stateTip = "正在录音...";
				}).catch(error => {
					this.$message.error("录音失败");
					console.log(error);
				});


			},
			handlePauseRecord() {
				this.rc.pause();
				this.state = 'PAUSE';
				this.stateTip = "已暂停录音";
			},
			handleResumeRecord() {
				this.rc.resume();
				this.state = 'RUNNING';
				this.stateTip = "正在录音...";
			},
			handleCompleteRecord() {
				this.rc.pause();
				this.state = 'COMPLETE';
				this.stateTip = "已结束录音";
			},
			handlePlayAudio() {
				let wav = this.rc.getWAVBlob();
				let url = URL.createObjectURL(wav);
				this.$refs.audio.src = url;
				this.$refs.audio.play();
				this.mode = 'PLAY';
			},
			handleStopAudio() {
				console.log(this.$refs.audio);
				this.$refs.audio.pause();
				this.mode = 'RECORD';
			},
			handleRestartRecord() {
				this.rc.destroy();
				this.rc.start();
				this.state = 'RUNNING';
				this.mode = 'RECORD';
				this.stateTip = "正在录音...";
			},
			handleSendRecord() {
				let wav = this.rc.getWAVBlob();
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
				}).then((url) => {
					let data = {
						duration: parseInt(this.rc.duration),
						url: url
					}
					this.$emit("send", data);
				})
			}
		}

	}
</script>

<style lang="scss">
	.chat-voice {

		.chat-voice-tip {
			font-size: 18px;
		}

		.chat-voice-btn-group {
			margin-bottom: 20px;
		}
	}
</style>

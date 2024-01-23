<template>
	<el-dialog class="chat-voice" title="语音录制" :visible.sync="visible" width="600px" :before-close="onClose">
		<div v-show="mode=='RECORD'">
			<div class="chat-voice-tip">{{stateTip}}</div>
			<div>时长: {{state=='STOP'?0:parseInt(rc.duration)}}s</div>
		</div>
		<audio v-show="mode=='PLAY'" :src="url" controls ref="audio" @ended="onStopAudio()"></audio>
		<el-divider content-position="center"></el-divider>
		<el-row class="chat-voice-btn-group">
			<el-button round type="primary" v-show="state=='STOP'" @click="onStartRecord()">开始录音</el-button>
			<el-button round type="warning" v-show="state=='RUNNING'" @click="onPauseRecord()">暂停录音</el-button>
			<el-button round type="primary" v-show="state=='PAUSE'" @click="onResumeRecord()">继续录音</el-button>
			<el-button round type="danger" v-show="state=='RUNNING'||state=='PAUSE'" @click="onCompleteRecord()">
				结束录音</el-button>
			<el-button round type="success" v-show="state=='COMPLETE' && mode!='PLAY'" @click="onPlayAudio()">播放录音
			</el-button>
			<el-button round type="warning" v-show="state=='COMPLETE' && mode=='PLAY'" @click="onStopAudio()">停止播放
			</el-button>
			<el-button round type="primary" v-show="state=='COMPLETE'" @click="onRestartRecord()">重新录音</el-button>
			<el-button round type="primary" v-show="state=='COMPLETE'" @click="onSendRecord()">立即发送</el-button>
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
			onClose() {
				// 关闭前清除数据
				this.rc.destroy();
				this.rc = new Recorder();
				this.audio.pause();
				this.mode = 'RECORD';
				this.state = 'STOP';
				this.stateTip = '未开始';
				this.$emit("close");
			},
			onStartRecord() {
				this.rc.start().then((stream) => {
					this.state = 'RUNNING';
					this.stateTip = "正在录音...";
				}).catch(error => {
					this.$message.error(error);
				});


			},
			onPauseRecord() {
				this.rc.pause();
				this.state = 'PAUSE';
				this.stateTip = "已暂停录音";
			},
			onResumeRecord() {
				this.rc.resume();
				this.state = 'RUNNING';
				this.stateTip = "正在录音...";
			},
			onCompleteRecord() {
				this.rc.pause();
				this.state = 'COMPLETE';
				this.stateTip = "已结束录音";
			},
			onPlayAudio() {
				let wav = this.rc.getWAVBlob();
				let url = URL.createObjectURL(wav);
				this.$refs.audio.src = url;
				this.$refs.audio.play();
				this.mode = 'PLAY';
			},
			onStopAudio() {
				this.$refs.audio.pause();
				this.mode = 'RECORD';
			},
			onRestartRecord() {
				this.rc.destroy();
				this.rc = new Recorder()
				this.rc.start();
				this.state = 'RUNNING';
				this.mode = 'RECORD';
				this.stateTip = "正在录音...";
			},
			onSendRecord() {
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
					this.onClose();
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

<template>
	<div>
		<el-dialog v-dialogDrag :title="title" top="5vh" :close-on-click-modal="false" :close-on-press-escape="false"
			:visible.sync="showRoom" width="50%" height="70%" :before-close="onQuit">
			<div class="rtc-private-video">
				<div v-show="isVideo" class="rtc-video-box">
					<div class="rtc-video-friend" v-loading="!isChating"
						element-loading-spinner="el-icon-loading" >
						<head-image class="friend-head-image" :id="friend.id" :size="80" :name="friend.nickName"
							:url="friend.headImage">
						</head-image>
						<video ref="remoteVideo" autoplay=""></video>
					</div>
					<div class="rtc-video-mine">
						<video ref="localVideo" autoplay=""></video>
					</div>
				</div>
				<div v-show="!isVideo" class="rtc-voice-box" v-loading="!isChating" element-loading-text="等待对方接听..."
					element-loading-background="rgba(0, 0, 0, 0.3)">
					<head-image class="friend-head-image" :id="friend.id" :size="200" :name="friend.nickName"
						:url="friend.headImage">
						<div class="rtc-voice-name">{{friend.nickName}}</div>
					</head-image>
				</div>
				<div class="rtc-control-bar">
					<div v-show="isWaiting" title="取消呼叫" class="icon iconfont icon-phone-reject reject"
						style="color: red;" @click="onCancel()"></div>
					<div v-show="isChating" title="挂断" class="icon iconfont icon-phone-reject reject"
						style="color: red;" @click="onHandup()"></div>
				</div>
			</div>
		</el-dialog>
		<rtc-private-acceptor v-if="!isHost&&isWaiting" ref="acceptor" :friend="friend" :mode="mode" @accept="onAccept"
			@reject="onReject"></rtc-private-acceptor>
	</div>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';
	import RtcPrivateAcceptor from './RtcPrivateAcceptor.vue';
	import ImWebRtc from '@/api/webrtc';
	import ImCamera from '@/api/camera';
	import RtcPrivateApi from '@/api/rtcPrivateApi'

	export default {
		name: 'rtcPrivateVideo',
		components: {
			HeadImage,
			RtcPrivateAcceptor
		},
		data() {
			return {
				camera: new ImCamera(), // 摄像头和麦克风
				webrtc: new ImWebRtc(), // webrtc相关
				API: new RtcPrivateApi(), // API
				audio: new Audio(), // 呼叫音频
				showRoom: false,
				friend: {},
				isHost: false, // 是否发起人
				state: "CLOSE", // CLOSE:关闭  WAITING:等待呼叫或接听 CHATING:聊天中  ERROR:出现异常
				mode: 'video', // 模式 video:视频聊 voice:语音聊天
				localStream: null, // 本地视频流
				remoteStream: null, // 对方视频流
				videoTime: 0,
				videoTimer: null,
				heartbeatTimer: null,
				candidates: [],
			}
		},
		methods: {
			open(rtcInfo) {
				this.showRoom = true;
				this.mode = rtcInfo.mode;
				this.isHost = rtcInfo.isHost;
				this.friend = rtcInfo.friend;
				if (this.isHost) {
					this.onCall();
				}
			},
			initAudio() {
				let url = require(`@/assets/audio/call.wav`);
				this.audio.src = url;
				this.audio.loop = true;
			},
			initRtc() {
				this.webrtc.init(this.configuration)
				this.webrtc.setupPeerConnection((stream) => {
					this.$refs.remoteVideo.srcObject = stream;
					this.remoteStream = stream;
				})
				// 监听候选信息
				this.webrtc.onIcecandidate((candidate) => {
					if (this.state == "CHATING") {
						// 连接已就绪,直接发送
						this.API.sendCandidate(this.friend.id, candidate);
					} else {
						// 连接未就绪,缓存起来，连接后再发送
						this.candidates.push(candidate)
					}
				})
				// 监听连接成功状态
				this.webrtc.onStateChange((state) => {
					if (state == "connected") {
						console.log("webrtc连接成功")
					} else if (state == "disconnected") {
						console.log("webrtc连接断开")
					}
				})
			},
			onCall() {
				if (!this.checkDevEnable()) {
					this.close();
				}
				// 初始化webrtc
				this.initRtc();
				// 启动心跳
				this.startHeartBeat();
				// 打开摄像头
				this.openStream().finally(() => {
					this.webrtc.setStream(this.localStream);
					this.webrtc.createOffer().then((offer) => {
						// 发起呼叫
						this.API.call(this.friend.id, this.mode, offer).then(() => {
							// 直接进入聊天状态
							this.state = "WAITING";
							// 播放呼叫铃声
							this.audio.play();
						}).catch(()=>{
							this.close();
						})
					})
				})
			},
			onAccept() {
				if (!this.checkDevEnable()) {
					this.API.failed(this.friend.id, "对方设备不支持通话")
					this.close();
					return;
				}
				// 进入房间
				this.showRoom = true;
				this.state = "CHATING";
				// 停止呼叫铃声
				this.audio.pause();
				// 初始化webrtc
				this.initRtc();
				// 打开摄像头
				this.openStream().finally(() => {
					this.webrtc.setStream(this.localStream);
					this.webrtc.createAnswer(this.offer).then((answer) => {
						this.API.accept(this.friend.id, answer);
						// 记录时长
						this.startChatTime();
						// 清理定时器
						this.waitTimer && clearTimeout(this.waitTimer);
					})
				})
			},
			onReject() {
				console.log("onReject")
				// 退出通话
				this.API.reject(this.friend.id);
				// 退出
				this.close();
			},
			onHandup() {
				this.API.handup(this.friend.id)
				this.$message.success("您已挂断,通话结束")
				this.close();
			},
			onCancel() {
				this.API.cancel(this.friend.id)
				this.$message.success("已取消呼叫,通话结束")
				this.close();
			},
			onRTCMessage(msg) {
				// 除了发起通话，如果在关闭状态就无需处理
				if (msg.type != this.$enums.MESSAGE_TYPE.RTC_CALL_VOICE &&
					msg.type != this.$enums.MESSAGE_TYPE.RTC_CALL_VIDEO &&
					this.isClose) {
					return;
				}
				// RTC信令处理
				switch (msg.type) {
					case this.$enums.MESSAGE_TYPE.RTC_CALL_VOICE:
						this.onRTCCall(msg, 'voice')
						break;
					case this.$enums.MESSAGE_TYPE.RTC_CALL_VIDEO:
						this.onRTCCall(msg, 'video')
						break;
					case this.$enums.MESSAGE_TYPE.RTC_ACCEPT:
						this.onRTCAccept(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_REJECT:
						this.onRTCReject(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_CANCEL:
						this.onRTCCancel(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_FAILED:
						this.onRTCFailed(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_HANDUP:
						this.onRTCHandup(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_CANDIDATE:
						this.onRTCCandidate(msg)
						break;
				}
			},
			onRTCCall(msg, mode) {
				this.offer = JSON.parse(msg.content);
				this.isHost = false;
				this.mode = mode;
				this.$http({
					url: `/friend/find/${msg.sendId}`,
					method: 'get'
				}).then((friend) => {
					this.friend = friend;
					this.state = "WAITING";
					this.audio.play();
					this.startHeartBeat();
					// 30s未接听自动挂掉
					this.waitTimer = setTimeout(() => {
						this.API.failed(this.friend.id,"对方无应答");
						this.$message.error("您未接听");
						this.close();
					}, 30000)
				})
			},
			onRTCAccept(msg) {
				if (msg.selfSend) {
					// 在其他设备接听
					this.$message.success("已在其他设备接听");
					this.close();
				} else {
					// 对方接受了的通话
					let offer = JSON.parse(msg.content);
					this.webrtc.setRemoteDescription(offer);
					// 状态为聊天中
					this.state = 'CHATING'
					// 停止播放语音
					this.audio.pause();
					// 发送candidate
					this.candidates.forEach((candidate) => {
						this.API.sendCandidate(this.friend.id, candidate);
					})
					
				}
			},
			onRTCReject(msg) {
				if (msg.selfSend) {
					this.$message.success("已在其他设备拒绝");
					this.close();
				} else {
					this.$message.error("对方拒绝了您的通话请求");
					this.close();
				}
			},
			onRTCFailed(msg) {
				// 呼叫失败
				this.$message.error(msg.content)
				this.close();
			},
			onRTCCancel() {
				// 对方取消通话
				this.$message.success("对方取消了呼叫");
				this.close();
			},
			onRTCHandup() {
				// 对方挂断
				this.$message.success("对方已挂断");
				this.close();
			},
			onRTCCandidate(msg) {
				let candidate = JSON.parse(msg.content);
				this.webrtc.addIceCandidate(candidate);
			},

			openStream() {
				return new Promise((resolve, reject) => {
					if (this.isVideo) {
						// 打开摄像头+麦克风
						this.camera.openVideo().then((stream) => {
							this.localStream = stream;
							this.$nextTick(() => {
								this.$refs.localVideo.srcObject = stream;
								this.$refs.localVideo.muted = true;
							})
							resolve(stream);
						}).catch((e) => {
							this.$message.error("打开摄像头失败")
							console.log("本摄像头打开失败:" + e.message)
							reject(e);
						})
					} else {
						// 打开麦克风
						this.camera.openAudio().then((stream) => {
							this.localStream = stream;
							this.$refs.localVideo.srcObject = stream;
							resolve(stream);
						}).catch((e) => {
							this.$message.error("打开麦克风失败")
							console.log("打开麦克风失败:" + e.message)
							reject(e);
						})
					}
				})
			},
			startChatTime() {
				this.videoTime = 0;
				this.videoTimer && clearInterval(this.videoTimer);
				this.videoTimer = setInterval(() => {
					this.videoTime++;
				}, 1000)
			},
			checkDevEnable() {
				// 检测摄像头
				if (!this.camera.isEnable()) {
					this.message.error("访问摄像头失败");
					return false;
				}
				// 检测webrtc
				if (!this.webrtc.isEnable()) {
					this.message.error("初始化RTC失败，原因可能是: 1.服务器缺少ssl证书 2.您的设备不支持WebRTC");
					return false;
				}
				return true;
			},
			startHeartBeat() {
				// 每15s推送一次心跳
				this.heartbeatTimer && clearInterval(this.heartbeatTimer);
				this.heartbeatTimer = setInterval(() => {
					this.API.heartbeat(this.friend.id);
				}, 15000)
			},
			close() {
				this.showRoom = false;
				this.camera.close();
				this.webrtc.close();
				this.audio.pause();
				this.videoTime = 0;
				this.videoTimer && clearInterval(this.videoTimer);
				this.heartbeatTimer && clearInterval(this.heartbeatTimer);
				this.waitTimer && clearTimeout(this.waitTimer);
				this.videoTimer = null;
				this.heartbeatTimer = null;
				this.waitTimer = null;
				this.state = 'CLOSE';
				this.candidates = [];
			},
			onQuit() {
				if (this.isChating) {
					this.onHandup()
				} else if (this.isWaiting) {
					this.onCancel();
				} else {
					this.close();
				}
			}
		},
		computed: {
			title() {
				let strTitle = `${this.modeText}通话-${this.friend.nickName}`;
				if (this.isChating) {
					strTitle += `(${this.currentTime})`;
				} else if (this.isWaiting) {
					strTitle += `(呼叫中)`;
				}
				return strTitle;
			},
			currentTime() {
				let min = Math.floor(this.videoTime / 60);
				let sec = this.videoTime % 60;
				let strTime = min < 10 ? "0" : "";
				strTime += min;
				strTime += ":"
				strTime += sec < 10 ? "0" : "";
				strTime += sec;
				return strTime;
			},
			configuration() {
				const iceServers = this.$store.state.configStore.webrtc.iceServers;
				return {
					iceServers: iceServers
				}
			},
			isVideo() {
				return this.mode == "video"
			},
			modeText() {
				return this.isVideo ? "视频" : "语音";
			},
			isChating() {
				return this.state == "CHATING";
			},
			isWaiting() {
				return this.state == "WAITING";
			},
			isClose() {
				return this.state == "CLOSE";
			}
		},
		mounted() {
			// 初始化音频文件
			this.initAudio();
		},
		created() {
			// 监听页面刷新事件
			window.addEventListener('beforeunload', () => {
				this.onQuit();
			});
		},
		beforeUnmount() {
			this.onQuit();
		}
	}
</script>

<style lang="scss">
	.rtc-private-video {
		position: relative;

		.el-loading-text {
			color: white !important;
			font-size: 16px !important;
		}

		.path {
			stroke: white !important;
		}

		.rtc-video-box {
			position: relative;
			border: #4880b9 solid 1px;
			background-color: #eeeeee;

			.rtc-video-friend {
				height: 70vh;

				.friend-head-image {
					position: absolute;
				}

				video {
					width: 100%;
					height: 100%;
					object-fit: cover;
					transform: rotateY(180deg);
				}
			}

			.rtc-video-mine {
				position: absolute;
				z-index: 99999;
				width: 25vh;
				right: 0;
				bottom: 0;
				box-shadow: 0px 0px 5px #ccc;
				background-color: #cccccc;

				video {
					width: 100%;
					object-fit: cover;
					transform: rotateY(180deg);
				}
			}
		}

		.rtc-voice-box {
			position: relative;
			display: flex;
			justify-content: center;
			border: #4880b9 solid 1px;
			width: 100%;
			height: 50vh;
			padding-top: 10vh;
			background-color: aliceblue;

			.rtc-voice-name {
				text-align: center;
				font-size: 22px;
				font-weight: 600;
			}
		}

		.rtc-control-bar {
			display: flex;
			justify-content: space-around;
			padding: 10px;

			.icon {
				font-size: 50px;
				cursor: pointer;
			}
		}
	}
</style>
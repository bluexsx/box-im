<template>
	<el-dialog v-dialogDrag :title="title" top="5vh" :close-on-click-modal="false" :close-on-press-escape="false"
		:visible="isShow" width="50%" height="70%" :before-close="handleClose">
		<div class="chat-video">
			<div v-show="rtcInfo.mode=='video'" class="chat-video-box">
				<div class="chat-video-friend" v-loading="loading" element-loading-text="等待对方接听..." 
					element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.3)">
					<head-image class="friend-head-image" :id="rtcInfo.friend.id" :size="80" :name="rtcInfo.friend.nickName"
						:url="rtcInfo.friend.headImage">
					</head-image>
					<video ref="friendVideo" autoplay=""></video>
				</div>
				<div class="chat-video-mine">
					<video ref="mineVideo" autoplay=""></video>
				</div>
			</div>
			<div v-show="rtcInfo.mode=='voice'" class="chat-voice-box" v-loading="loading" element-loading-text="等待对方接听..."
				element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.3)">
				<head-image class="friend-head-image" :id="rtcInfo.friend.id" :size="200" :name="rtcInfo.friend.nickName"
					:url="rtcInfo.friend.headImage">
					<div class="chat-voice-name">{{rtcInfo.friend.nickName}}</div>
				</head-image>
			</div>
			<div class="chat-video-controllbar">
				<div v-show="isWaiting" title="取消呼叫" class="icon iconfont icon-phone-reject reject" style="color: red;"
					@click="cancel()"></div>
				<div v-show="isAccepted" title="挂断" class="icon iconfont icon-phone-reject reject" style="color: red;"
					@click="handup()"></div>
			</div>
		</div>
	</el-dialog>

</template>

<script>
	import HeadImage from '../common/HeadImage.vue';

	export default {
		name: 'chatVideo',
		components: {
			HeadImage
		},
		data() {
			return {
				isShow: false,
				stream: null,
				audio: new Audio(),
				loading: false,
				peerConnection: null,
				videoTime: 0,
				videoTimer: null,
				candidates: [],
				configuration: {
					iceServers: []
				}
			}
		},
		methods: {
			init() {
				this.isShow = true;
				if (!this.hasUserMedia() || !this.hasRTCPeerConnection()) {
					this.$message.error("初始化失败，原因可能是: 1.未部署ssl证书 2.您的浏览器不支持WebRTC");
					this.insertMessage("设备不支持通话");
					if (!this.rtcInfo.isHost) {
						this.sendFailed("对方设备不支持通话")
					}
					return;
				}
				// 打开摄像头
				this.openCamera((stream) => {
					// 初始化webrtc连接
					this.setupPeerConnection(stream);
					if (this.rtcInfo.isHost) {
						// 发起呼叫
						this.call();
					} else {
						// 接受呼叫
						this.accept(this.rtcInfo.offer);
					}
				});
			},
			openCamera(callback) {
				navigator.getUserMedia({
					video: this.isVideo,
					audio: true
				}, (stream) => {
					console.log(this.loading)
					this.stream = stream;
					this.$refs.mineVideo.srcObject = stream;
					this.$refs.mineVideo.muted = true;
					callback(stream)
				}, (error) => {
					let devText = this.isVideo ? "摄像头" : "麦克风"
					this.$message.error(`打开${devText}失败:${error}`);
					callback()
				})
			},
			closeCamera() {
				if (this.stream) {
					this.stream.getTracks().forEach((track) => {
						track.stop();
					});
					this.$refs.mineVideo.srcObject = null;
					this.stream = null;
				}
			},
			setupPeerConnection(stream) {
				this.peerConnection = new RTCPeerConnection(this.configuration);
				this.peerConnection.ontrack = (e) => {
					this.$refs.friendVideo.srcObject = e.streams[0];
				};
				this.peerConnection.onicecandidate = (event) => {
					if (event.candidate) {
						if (this.isAccepted) {
							// 已连接,直接发送
							this.sendCandidate(event.candidate);
						} else {
							// 未连接,缓存起来，连接后再发送
							this.candidates.push(event.candidate)
						}
					}
				}
				if (stream) {
					stream.getTracks().forEach((track) => {
						this.peerConnection.addTrack(track, stream);
					});
				}
				this.peerConnection.oniceconnectionstatechange = (event) => {
					let state = event.target.iceConnectionState;
					console.log("ICE connection status changed : " + state)
					if (state == 'connected') {
						this.resetTime();
					}
				};
			},
			insertMessage(messageTip) {
				// 打开会话，防止消息无法插入
				let chat = {
					type: 'PRIVATE',
					targetId: this.rtcInfo.friend.id,
					showName: this.rtcInfo.friend.nickName,
					headImage: this.rtcInfo.friend.headImage,
				};
				this.$store.commit("openChat", chat);
				// 插入消息
				let MESSAGE_TYPE = this.$enums.MESSAGE_TYPE;
				let msgInfo = {
					type: this.rtcInfo.mode == "video" ? MESSAGE_TYPE.RT_VIDEO : MESSAGE_TYPE.RT_VOICE,
					sendId: this.rtcInfo.sendId,
					recvId: this.rtcInfo.recvId,
					content: this.isChating ? "通话时长 " + this.currentTime : messageTip,
					status: 1,
					selfSend: this.rtcInfo.isHost,
					sendTime: new Date().getTime()
				}
				this.$store.commit("insertMessage", msgInfo);
			},
			onRTCMessage(msg) {
				if (!msg.selfSend && msg.type == this.$enums.MESSAGE_TYPE.RTC_ACCEPT) {
					// 对方接受了的通话
					this.peerConnection.setRemoteDescription(new RTCSessionDescription(JSON.parse(msg.content)));
					// 关闭等待提示
					this.loading = false;
					// 状态为聊天中
					this.$store.commit("setRtcState", this.$enums.RTC_STATE.CHATING)
					// 停止播放语音
					this.audio.pause();
					// 发送candidate
					this.candidates.forEach((candidate) => {
						this.sendCandidate(candidate);
					})
				} else if (!msg.selfSend && msg.type == this.$enums.MESSAGE_TYPE.RTC_REJECT) {
					// 对方拒绝了通话
					this.$message.error("对方拒绝了您的通话请求");
					// 插入消息
					this.insertMessage("对方已拒绝")
					// 状态为空闲
					this.close();
				} else if (msg.type == this.$enums.MESSAGE_TYPE.RTC_FAILED) {
					// 呼叫失败
					this.$message.error(msg.content)
					// 插入消息
					this.insertMessage(msg.content)
					this.close();
				} else if (msg.type == this.$enums.MESSAGE_TYPE.RTC_CANDIDATE) {
					// 候选线路信息
					this.peerConnection.addIceCandidate(new RTCIceCandidate(JSON.parse(msg.content)));
				} else if (msg.type == this.$enums.MESSAGE_TYPE.RTC_HANDUP) {
					// 对方挂断
					this.$message.success("对方已挂断");
					// 插入消息
					this.insertMessage("对方已挂断")
					this.close();
				}
			},
			call() {
				let offerParam = {
					offerToRecieveAudio: 1,
					offerToRecieveVideo: this.isVideo ? 1 : 0
				}
				this.peerConnection.createOffer(offerParam).then((offer) => {
					this.peerConnection.setLocalDescription(offer);
					this.$http({
						url: `/webrtc/private/call?uid=${this.rtcInfo.friend.id}&mode=${this.rtcInfo.mode}`,
						method: 'post',
						data: JSON.stringify(offer)
					}).then(() => {
						this.loading = true;
						// 状态为聊天中
						this.audio.play();
					})
				}, (error) => {
					this.insertMessage("未接通")
					this.$message.error(error);
				});

			},
			accept(offer) {
				let offerParam = {
					offerToRecieveAudio: 1,
					offerToRecieveVideo: this.isVideo ? 1 : 0
				}
				this.peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
				this.peerConnection.createAnswer(offerParam).then((answer) => {
						this.peerConnection.setLocalDescription(answer);
						this.$http({
							url: `/webrtc/private/accept?uid=${this.rtcInfo.friend.id}`,
							method: 'post',
							data: JSON.stringify(answer)
						}).then(() => {
							// 聊天中状态
							this.$store.commit("setRtcState", this.$enums.RTC_STATE.CHATING)
						})

					},
					(error) => {
						this.$message.error(error);
					});

			},
			handup() {
				this.$http({
					url: `/webrtc/private/handup?uid=${this.rtcInfo.friend.id}`,
					method: 'post'
				})
				this.insertMessage("已挂断")
				this.close();
				this.$message.success("您已挂断,通话结束")
			},
			cancel() {
				this.$http({
					url: `/webrtc/private/cancel?uid=${this.rtcInfo.friend.id}`,
					method: 'post'
				})
				this.insertMessage("已取消")
				this.close();
				this.$message.success("已取消呼叫,通话结束")
			},
			sendFailed(reason) {
				this.$http({
					url: `/webrtc/private/failed?uid=${this.rtcInfo.friend.id}&reason=${reason}`,
					method: 'post'
				})
			},
			sendCandidate(candidate) {
				this.$http({
					url: `/webrtc/private/candidate?uid=${this.rtcInfo.friend.id}`,
					method: 'post',
					data: JSON.stringify(candidate)
				})
			},
			close() {
				this.isShow = false;
				this.closeCamera();
				this.loading = false;
				this.videoTime = 0;
				this.videoTimer && clearInterval(this.videoTimer);
				this.audio.pause();
				this.candidates = [];
				if (this.peerConnection) {
					this.peerConnection.close();
					this.peerConnection.onicecandidate = null;
					this.peerConnection.onaddstream = null;
				}
				if (this.$refs.friendVideo) {
					this.$refs.friendVideo.srcObject = null;
				}
				// 状态置为空闲
				this.$store.commit("setRtcState", this.$enums.RTC_STATE.FREE);
			},
			resetTime() {
				this.videoTime = 0;
				this.videoTimer && clearInterval(this.videoTimer);
				this.videoTimer = setInterval(() => {
					this.videoTime++;
				}, 1000)
			},
			handleClose() {
				if (this.isAccepted) {
					this.handup()
				} else if (this.isWaiting) {
					this.cancel();
				} else {
					this.close();
				}
			},
			hasUserMedia() {
				navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator
					.mozGetUserMedia ||
					navigator.msGetUserMedia;
				return !!navigator.getUserMedia;
			},
			hasRTCPeerConnection() {
				window.RTCPeerConnection = window.RTCPeerConnection || window.webkitRTCPeerConnection || window
					.mozRTCPeerConnection;
				window.RTCSessionDescription = window.RTCSessionDescription || window.webkitRTCSessionDescription || window
					.mozRTCSessionDescription;
				window.RTCIceCandidate = window.RTCIceCandidate || window.webkitRTCIceCandidate || window
					.mozRTCIceCandidate;
				return !!window.RTCPeerConnection;
			},
			initAudio() {
				let url = require(`@/assets/audio/call.wav`);
				this.audio.src = url;
				this.audio.loop = true;
			},
			initICEServers() {
				this.$http({
					url: '/webrtc/private/iceservers',
					method: 'get'
				}).then((servers) => {
					this.configuration.iceServers = servers;
				})
			}

		},
		watch: {
			rtcState: {
				handler(newState, oldState) {
					// WAIT_CALL是主动呼叫弹出，ACCEPTED是被呼叫接受后弹出
					if (newState == this.$enums.RTC_STATE.WAIT_CALL ||
						newState == this.$enums.RTC_STATE.ACCEPTED) {
						this.init();
					}
				}
			}
		},
		computed: {
			title() {
				let strTitle = `${this.modeText}通话-${this.rtcInfo.friend.nickName}`;
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
			rtcInfo() {
				return this.$store.state.userStore.rtcInfo;
			},
			rtcState() {
				return this.rtcInfo.state;
			},
			isVideo() {
				return this.rtcInfo.mode == "video"
			},
			modeText() {
				return this.isVideo ? "视频" : "语音";
			},
			isAccepted() {
				return this.rtcInfo.state == this.$enums.RTC_STATE.CHATING ||
					this.rtcInfo.state == this.$enums.RTC_STATE.ACCEPTED
			},
			isWaiting() {
				return this.rtcInfo.state == this.$enums.RTC_STATE.WAIT_CALL;
			},
			isChating() {
				return this.rtcInfo.state == this.$enums.RTC_STATE.CHATING;
			}
		},
		mounted() {
			this.initAudio();
			this.initICEServers();
		}
	}
</script>

<style lang="scss">
	.chat-video {
		position: relative;

		.el-loading-text {
			color: white !important;
			font-size: 16px !important;
		}
		
		.el-icon-loading {
			color: white !important;
			font-size: 30px !important;
		}
		
		.chat-video-box {
			position: relative;
			border: #4880b9 solid 1px;
			background-color: #eeeeee;

			.chat-video-friend {
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

			.chat-video-mine {
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

		.chat-voice-box {
			position: relative;
			display: flex;
			justify-content: center;
			border: #4880b9 solid 1px;
			width: 100%;
			height: 50vh;
			padding-top: 10vh;
			background-color: aliceblue;

			.chat-voice-name {
				text-align: center;
				font-size: 22px;
				font-weight: 600;
			}
		}

		.chat-video-controllbar {
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
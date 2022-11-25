<template>
	<el-dialog :title="title" :visible.sync="visible" width="800px" :before-close="handleClose">
		<div class="chat-video">
			<div class="chat-video-box">
				<div class="chat-video-friend" v-loading="loading" element-loading-text="等待对方接听..." element-loading-spinner="el-icon-loading"
				 element-loading-background="rgba(0, 0, 0, 0.9)">
					<video ref="friendVideo" autoplay=""></video>
				</div>
				<div class="chat-video-mine">
					<video ref="mineVideo" autoplay=""></video>
				</div>
			</div>
			<div class="chat-video-controllbar">

				<div v-show="state=='CONNECTING'" title="取消呼叫" class="icon iconfont icon-phone-reject reject" style="color: red;"
				 @click="cancel()"></div>
				<div v-show="state=='CONNECTED'" title="挂断" class="icon iconfont icon-phone-reject reject" style="color: red;"
				 @click="handup()"></div>

			</div>
		</div>
	</el-dialog>

</template>

<script>
	export default {
		name: 'chatVideo',
		props: {
			visible: {
				type: Boolean
			},
			friend: {
				type: Object
			},
			master: {
				type: Boolean
			},
			offer: {
				type: Object
			}
		},
		data() {
			return {
				stream: null,
				loading: false,
				peerConnection: null,
				state: 'NOT_CONNECTED',
				candidates: [],
				configuration: {
					iceServers: [
						{
							urls: 'stun:stun.l.google.com:19302'
						},
						{
							urls: 'turn:www.boxim.online:3478',
							credential: 'admin123',
							username: 'admin'
						},
						{
							urls: 'stun:www.boxim.online:3478',
							credential: 'admin123',
							username: 'admin'
						}
					]
				}
			}
		},
		methods: {
			init() {
				if (!this.hasUserMedia() || !this.hasRTCPeerConnection()) {
					this.$message.error("您的浏览器不支持WebRTC");
					if (!this.master) {
						this.sendFailed("对方浏览器不支持WebRTC")
					}
					return;
				}

				// 打开摄像头
				this.openCamera((stream) => {
					// 初始化webrtc连接
					this.setupPeerConnection(stream);
					if (this.master) {
						// 发起呼叫
						this.call();
					} else {
						// 接受呼叫
						this.accept(this.offer);
					}

					this.timerx && clearInterval(this.timerx);
					this.timerx = setInterval(() => {
						console.log(this.peerConnection.iceConnectionState);
					}, 3000)
				});

			},
			openCamera(callback) {
				navigator.getUserMedia({
						video: true,
						audio: true
					},
					(stream) => {
						this.stream = stream;
						this.$refs.mineVideo.srcObject = stream;
						this.$refs.mineVideo.muted = true;
						callback(stream)
					},
					(error) => {
						this.$message.error("打开摄像头失败:" + error);
						callback()
					});
			},
			closeCamera() {
				if (this.stream) {

					this.stream.getVideoTracks().forEach((track) => {
						track.stop();
					});
					this.$refs.mineVideo.srcObject = null;
					this.stream = null;
				}

			},
			setupPeerConnection(stream) {
				this.peerConnection = new RTCPeerConnection(this.configuration);
				this.peerConnection.ontrack = (e) => {
					console.log("onaddstream")
					this.$refs.friendVideo.srcObject = e.streams[0];
				};
				this.peerConnection.onicecandidate = (event) => {
					if (event.candidate) {
						if (this.state == 'CONNECTED') {
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
				this.peerConnection.oniceconnectionstatechange = function(event) {
					console.log("ICE connection status changed : " + event.target.iceConnectionState)
				};

			},
			handleMessage(msg) {
				if (msg.type == this.$enums.MESSAGE_TYPE.RTC_ACCEPT) {
					console.log("接收answer")
					console.log(msg.content)
					this.peerConnection.setRemoteDescription(new RTCSessionDescription(JSON.parse(msg.content)));
					// 关闭等待提示
					this.loading = false;
					// 状态为连接中
					this.state = 'CONNECTED';
					// 发送candidate
					this.candidates.forEach((candidate) => {
						this.sendCandidate(candidate);
					})
				}
				if (msg.type == this.$enums.MESSAGE_TYPE.RTC_REJECT) {
					this.$message.error("对方拒绝了您的视频请求");
					this.peerConnection.close();
					// 关闭等待提示
					this.loading = false;
					// 状态为未连接
					this.state = 'NOT_CONNECTED';
				}
				if (msg.type == this.$enums.MESSAGE_TYPE.RTC_FAILED) {
					this.$message.error(msg.content)
					// 关闭等待提示
					this.loading = false;
					// 状态为未连接
					this.state = 'NOT_CONNECTED';
				}
				if (msg.type == this.$enums.MESSAGE_TYPE.RTC_CANDIDATE) {
					this.peerConnection.addIceCandidate(new RTCIceCandidate(JSON.parse(msg.content)));
				}
				if (msg.type == this.$enums.MESSAGE_TYPE.RTC_HANDUP) {
					this.$message.success("对方已挂断");
					this.close();
				}
			},
			call() {
				this.peerConnection.createOffer((offer) => {
						this.peerConnection.setLocalDescription(offer);
						console.log("发送offer")
						console.log(JSON.stringify(offer))
						this.$http({
							url: `/webrtc/private/call?uid=${this.friend.id}`,
							method: 'post',
							data: JSON.stringify(offer)
						}).then(() => {
							this.loading = true;
							this.state = 'CONNECTING';
						});
					},
					(error) => {
						this.$message.error(error);
					});

			},
			accept(offer) {
				console.log("接收到offer")
				console.log(offer)
				this.peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
				this.peerConnection.createAnswer((answer) => {
						console.log("发送answer")
						console.log(JSON.stringify(answer))
						this.peerConnection.setLocalDescription(answer);
						this.$http({
							url: `/webrtc/private/accept?uid=${this.friend.id}`,
							method: 'post',
							data: JSON.stringify(answer)
						})
						this.state = 'CONNECTED';
					},
					(error) => {
						this.$message.error(error);
					});

			},
			handup() {
				this.$http({
					url: `/webrtc/private/handup?uid=${this.friend.id}`,
					method: 'post'
				})
				this.close();
				this.$message.success("已挂断视频通话")
			},
			cancel() {
				this.$http({
					url: `/webrtc/private/cancel?uid=${this.friend.id}`,
					method: 'post'
				})
				this.close();
				this.$message.success("已停止呼叫视频通话")
			},
			sendFailed(reason) {
				this.$http({
					url: `/webrtc/private/failed?uid=${this.friend.id}&reason=${reason}`,
					method: 'post'
				})
			},
			sendCandidate(candidate) {
				this.$http({
					url: `/webrtc/private/candidate?uid=${this.friend.id}`,
					method: 'post',
					data: JSON.stringify(candidate)
				})
			},
			close() {
				this.$emit("close");
				this.closeCamera();
				this.loading = false;
				this.state = 'NOT_CONNECTED';
				this.candidates = [];
				this.$store.commit("setUserState", this.$enums.USER_STATE.FREE);
				this.$refs.friendVideo.srcObject = null;
				this.peerConnection.close();
				this.peerConnection.onicecandidate = null;
				this.peerConnection.onaddstream = null;

			},
			handleClose() {
				if (this.state == 'CONNECTED') {
					this.handup()
				} else if (this.state == 'CONNECTING') {
					this.cancel();
				} else {
					this.close();
				}
			},
			hasUserMedia() {
				navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia ||
					navigator.msGetUserMedia;
				return !!navigator.getUserMedia;
			},
			hasRTCPeerConnection() {
				window.RTCPeerConnection = window.RTCPeerConnection || window.webkitRTCPeerConnection || window.mozRTCPeerConnection;
				window.RTCSessionDescription = window.RTCSessionDescription || window.webkitRTCSessionDescription || window.mozRTCSessionDescription;
				window.RTCIceCandidate = window.RTCIceCandidate || window.webkitRTCIceCandidate || window.mozRTCIceCandidate;
				return !!window.RTCPeerConnection;
			}
		},
		watch: {
			visible: {
				handler(newValue, oldValue) {
					if (newValue) {
						this.init();
						// 用户忙状态
						this.$store.commit("setUserState", this.$enums.USER_STATE.BUSY);
						console.log(this.$store.state.userStore.state)
					}
				}
			}
		},
		computed: {
			title() {
				return `视频聊天-${this.friend.nickName}`;
			}
		}
	}
</script>

<style lang="scss">
	.chat-video {

		.chat-video-box {
			position: relative;
			border: #2C3E50 solid 1px;
			background-color: #eeeeee;

			.chat-video-friend {
				height: 600px;

				video {
					width: 100%;
					height: 100%;
				}
			}

			.chat-video-mine {
				position: absolute;
				z-index: 99999;
				width: 200px;
				right: 0;
				bottom: 0;

				video {
					width: 100%;
				}
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

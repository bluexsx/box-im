<template>
	<div v-show="isShow" class="rtc-private-acceptor">
		<head-image :id="rtcInfo.friend.id" :name="rtcInfo.friend.nickName" :url="rtcInfo.friend.headImage" :size="100"></head-image>
		<div class="acceptor-text">
			{{tip}}
		</div>
		<div class="acceptor-btn-group">
			<div class="icon iconfont icon-phone-accept accept" @click="accpet()" title="接受"></div>
			<div class="icon iconfont icon-phone-reject reject" @click="reject()" title="拒绝"></div>
		</div>
	</div>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';

	export default {
		name: "videoAcceptor",
		components: {
			HeadImage
		},
		data() {
			return {
				isShow: false,
				audio: new Audio()
			}
		},
		methods: {
			accpet() {
				// 状态置为已接受
				this.$store.commit("setRtcState", this.$enums.RTC_STATE.ACCEPTED);
				// 关闭
				this.close();
			},
			reject() {
				this.$http({
					url: `/webrtc/private/reject?uid=${this.rtcInfo.friend.id}`,
					method: 'post'
				})
				// 插入消息到会话中
				this.insertMessage("已拒绝");
				// 状态置为空闲
				this.$store.commit("setRtcState", this.$enums.RTC_STATE.FREE);
				// 关闭
				this.close();
			},
			failed(reason) {
				this.$http({
					url: `/webrtc/private/failed?uid=${this.rtcInfo.friend.id}&reason=${reason}`,
					method: 'post'
				})
				// 插入消息到会话中
				this.insertMessage("未接听");
				// 状态置为空闲
				this.$store.commit("setRtcState", this.$enums.RTC_STATE.FREE);
				// 关闭
				this.close();
			},
			onRtcCall(msgInfo, friend, mode) {
				console.log("onRtcCall")
				// 只要不是空闲状态，都是繁忙
				if (this.rtcInfo.state != this.$enums.RTC_STATE.FREE) {
					// 已经在跟别人聊天了，不能让其他会话进来
					let reason = "对方忙,无法与您通话";
					this.$http({
						url: `/webrtc/private/failed?uid=${msgInfo.sendId}&reason=${reason}`,
						method: 'post'
					})
					return;
				}
				// 显示呼叫
				this.isShow = true;
				// 初始化RTC会话信息
				let rtcInfo = {
					mode: mode,
					isHost: false,
					friend: friend,
					sendId: msgInfo.sendId,
					recvId: msgInfo.recvId,
					offer: JSON.parse(msgInfo.content),
					state: this.$enums.RTC_STATE.WAIT_ACCEPT
				}
				this.$store.commit("setRtcInfo", rtcInfo);
				// 播放呼叫音频
				this.audio.play();
				// 超时未接听
				this.timer && clearTimeout(this.timer);
				this.timer = setTimeout(() => {
					this.failed("对方无应答");
				}, 30000)

			},
			onRtcCancel(msgInfo) {
				// 防止被其他用户的操作干扰
				if (msgInfo.sendId != this.rtcInfo.friend.id) {
					return;
				}
				// 取消视频通话请求
				this.$message.success("对方取消了呼叫");
				// 插入消息到会话中
				this.insertMessage("对方已取消");
				// 状态置为空闲
				this.$store.commit("setRtcState", this.$enums.RTC_STATE.FREE);
				// 关闭
				this.close();
			},
			onRtcAccept(msgInfo) {
				// 这里处理的时自己在其他设备接听了的情况
				if (msgInfo.selfSend) {
					this.$message.success("已在其他设备接听");
					// 插入消息到会话中
					this.insertMessage("已在其他设备接听")
					// 状态置为空闲
					this.$store.commit("setRtcState", this.$enums.RTC_STATE.FREE);
					// 关闭
					this.close();
				}
			},
			onRtcReject(msgInfo){
				// 我在其他终端拒绝了对方的通话
				if (msgInfo.selfSend) {
					this.$message.success("已在其他设备拒绝通话");
					// 插入消息到会话中
					this.insertMessage("已在其他设备拒绝")
					// 状态置为空闲
					this.$store.commit("setRtcState", this.$enums.RTC_STATE.FREE);
					// 关闭
					this.close();
				} 
			},
			onRTCMessage(msgInfo, friend) {
				if (msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_CALL_VOICE) {
					this.onRtcCall(msgInfo, friend, "voice");
				} else if (msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_CALL_VIDEO) {
					this.onRtcCall(msgInfo, friend, "video");
				} else if (msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_CANCEL) {
					this.onRtcCancel(msgInfo);
				} else if (msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_ACCEPT) {
					this.onRtcAccept(msgInfo);
				}else if (msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_REJECT) {
					this.onRtcReject(msgInfo);
				}
			},
			insertMessage(messageTip) {
				// 先打开会话，防止会话存在导致无法插入消息
				let chat = {
					type: 'PRIVATE',
					targetId: this.rtcInfo.friend.id,
					showName: this.rtcInfo.friend.nickName,
					headImage: this.rtcInfo.friend.headImageThumb,
				};
				this.$store.commit("openChat", chat);
				// 插入消息
				let MESSAGE_TYPE = this.$enums.MESSAGE_TYPE;
				let msgInfo = {
					type: this.rtcInfo.mode == "video" ? MESSAGE_TYPE.RT_VIDEO : MESSAGE_TYPE.RT_VOICE,
					sendId: this.rtcInfo.sendId,
					recvId: this.rtcInfo.recvId,
					content: messageTip,
					status: 1,
					selfSend: this.rtcInfo.isHost,
					sendTime: new Date().getTime()
				}
				this.$store.commit("insertMessage", msgInfo);
			},
			close() {
				this.timer && clearTimeout(this.timer);
				this.audio.pause();
				this.isShow = false;
			},
			initAudio() {
				let url = require(`@/assets/audio/call.wav`);
				this.audio.src = url;
				this.audio.loop = true;
			}
		},
		computed: {
			tip() {
				let modeText = this.mode == "video" ? "视频" : "语音"
				return `${this.rtcInfo.friend.nickName} 请求和您进行${modeText}通话...`
			},
			rtcInfo(){
				return this.$store.state.userStore.rtcInfo;
			}
		},
		mounted() {
			// 初始化语音
			this.initAudio();
		}
	}
</script>

<style scoped lang="scss">
	.rtc-private-acceptor {
		position: absolute;
		display: flex;
		flex-direction: column;
		align-items: center;
		right: 5px;
		bottom: 5px;
		width: 250px;
		height: 250px;
		padding: 20px;
		background-color: #eeeeee;
		border: #dddddd solid 5px;
		border-radius: 3%;

		.acceptor-text {
			padding: 10px;
			text-align: center;
			font-size: 16px;
		}

		.acceptor-btn-group {
			display: flex;
			justify-content: space-around;
			margin-top: 20px;
			width: 100%;

			.icon {
				font-size: 60px;
				cursor: pointer;
				border-radius: 50%;

				&.accept {
					color: green;
					animation: anim 2s ease-in infinite, vibration 2s ease-in infinite;

					@keyframes anim {
						0% {
							box-shadow: 0 1px 0 4px #ffffff;
						}

						10% {
							box-shadow: 0 1px 0 8px rgba(255, 165, 0, 1);
						}

						25% {
							box-shadow: 0 1px 0 12px rgba(255, 210, 128, 1), 0 1px 0 16px rgba(255, 201, 102, 1);
						}

						50% {
							box-shadow: 0 2px 5px 10px rgba(255, 184, 51, 1), 0 2px 5px 23px rgba(248, 248, 255, 1);
						}
					}

					@keyframes vibration {
						0% {
							transform: rotate(0deg);
						}

						25% {
							transform: rotate(20deg);
						}

						50% {
							transform: rotate(0deg);
						}

						75% {
							transform: rotate(-15deg);
						}

						100% {
							transform: rotate(0deg);
						}
					}

				}

				&.reject {
					color: red;
				}
			}
		}
	}
</style>
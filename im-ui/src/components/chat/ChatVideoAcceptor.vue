<template>
	<div class="chat-video-acceptor">

		<head-image :id="friend.id" :name="friend.nickName" :url="friend.headImage" :size="100"></head-image>

		<div class="acceptor-text">
			{{friend.nickName}} 请求和您进行视频通话...
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
		props: {
			friend: {
				type: Object
			}
		},
		data() {
			return {
				offer: {},
				audio: new Audio()
			}
		},
		methods: {
			accpet() {
				let info = {
					friend: this.friend,
					master: false,
					offer: this.offer
				}
				this.$store.commit("showChatPrivateVideoBox", info);
				this.close();
			},
			reject() {
				this.$http({
					url: `/webrtc/private/reject?uid=${this.friend.id}`,
					method: 'post'
				})
				this.close();
			},
			failed(reason) {
				this.$http({
					url: `/webrtc/private/failed?uid=${this.friend.id}&reason=${reason}`,
					method: 'post'
				})
				this.close();
			},
			onCall(msgInfo) {
				this.offer = JSON.parse(msgInfo.content);
				if (this.$store.state.userStore.state == this.$enums.USER_STATE.BUSY) {
					this.failed("对方正忙,暂时无法接听");
					return;
				}
				// 超时未接听
				this.timer && clearTimeout(this.timer);
				this.timer = setTimeout(() => {
					this.failed("对方未接听");
				}, 30000)
				this.audio.play();
			},
			onCancel() {
				// 取消视频通话请求
				this.$message.success("对方取消了呼叫");
				this.close();
			},
			handleMessage(msgInfo) {
				if (msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_CALL) {
					this.onCall(msgInfo);
				} else if (msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_CANCEL) {
					this.onCancel();
				}
			},
			close() {
				this.timer && clearTimeout(this.timer);
				this.audio.pause();
				this.$emit("close");
			},
			initAudio() {
				let url = require(`@/assets/audio/call.wav`);
				this.audio.src = url;
				this.audio.loop = true;
			}
		},
		mounted() {
			// 初始化语音
			this.initAudio();

		}
	}
</script>

<style scoped lang="scss">
	.chat-video-acceptor {
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
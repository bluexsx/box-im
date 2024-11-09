<template>
	<div class="rtc-private-acceptor">
		<head-image :id="friend.id" :name="friend.nickName" :url="friend.headImage" :size="100" :isShowUserInfo="false"></head-image>
		<div class="acceptor-text">
			{{tip}}
		</div>
		<div class="acceptor-btn-group">
			<div class="icon iconfont icon-phone-accept accept" @click="$emit('accept')" title="接受"></div>
			<div class="icon iconfont icon-phone-reject reject" @click="$emit('reject')" title="拒绝"></div>
		</div>
	</div>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';

	export default {
		name: "rtcPrivateAcceptor",
		components: {
			HeadImage
		},
		data() {
			return {}
		},
		props: {
			mode:{
			   type: String
			},
			friend:{
				type: Object
			}
		},
		computed: {
			tip() {
				let modeText = this.mode == "video" ? "视频" : "语音"
				return `${this.friend.nickName} 请求和您进行${modeText}通话...`
			}
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
		background-color: #fff;
    box-shadow: var(--im-box-shadow-dark);
		border-radius: 4px;

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

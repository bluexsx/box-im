<template>
	<view class="chat-msg-item">
		<view class="chat-msg-tip" v-show="msgInfo.type==$enums.MESSAGE_TYPE.RECALL">{{msgInfo.content}}</view>

		<view class="chat-msg-normal" v-show="msgInfo.type!=$enums.MESSAGE_TYPE.RECALL"
			:class="{'chat-msg-mine':msgInfo.selfSend}">
			<view class="avatar" @click="onShowUserInfo(msgInfo.sendId)">
				<image class="head-image" :src="headImage"></image>
			</view>

			<view class="chat-msg-content" @longpress="onShowMenu($event)">
				<view class="chat-msg-top">
					<text>{{showName}}</text>
					<chat-time :time="msgInfo.sendTime"></chat-time>
				</view>

				<view class="chat-msg-bottom">
					<rich-text class="chat-msg-text" v-if="msgInfo.type==$enums.MESSAGE_TYPE.TEXT"
						:nodes="$emo.transform(msgInfo.content)"></rich-text>
					<view class="chat-msg-image" v-if="msgInfo.type==$enums.MESSAGE_TYPE.IMAGE">
						<view class="img-load-box">
							<image class="send-image" :src="JSON.parse(msgInfo.content).thumbUrl" lazy-load="true"
								@click.stop="onShowFullImage()">
							</image>
							<loading v-show="loading"></loading>
						</view>
						<text title="发送失败" v-show="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>

					<view class="chat-msg-file" v-if="msgInfo.type==$enums.MESSAGE_TYPE.FILE">
						<view class="chat-file-box">
							<view class="chat-file-info">
								<uni-link class="chat-file-name" :text="data.name" showUnderLine="true" color="#007BFF"
									:href="data.url"></uni-link>
								<view class="chat-file-size">{{fileSize}}</view>
							</view>
							<view class="chat-file-icon iconfont icon-file"></view>
							<loading v-show="loading"></loading>
						</view>
						<text title="发送失败" v-show="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
					<!--
					<view class="chat-msg-voice" v-if="msgInfo.type==$enums.MESSAGE_TYPE.AUDIO" @click="onPlayVoice()">
						<audio controls :src="JSON.parse(msgInfo.content).url"></audio>
					</view>
					-->
				</view>

			</view>

		</view>
		<pop-menu v-show="menu.show" :menu-style="menu.style" :items="menuItems" @close="menu.show=false"
			@select="onSelectMenu"></pop-menu>
	</view>
</template>

<script>
	export default {
		name: "chat-message-item",
		props: {
			headImage: {
				type: String,
				required: true
			},
			showName: {
				type: String,
				required: true
			},
			msgInfo: {
				type: Object,
				required: true
			}
		},
		data() {
			return {
				audioPlayState: 'STOP',
				menu: {
					show: false,
					style: ""
				}
			}

		},
		methods: {
			onShowMenu(e) {
				uni.getSystemInfo({
					success: (res) => {
						let touches = e.touches[0];
						let style = "";
						/* 因 非H5端不兼容 style 属性绑定 Object ，所以拼接字符 */
						if (touches.clientY > (res.windowHeight / 2)) {
							style = `bottom:${res.windowHeight-touches.clientY}px;`;
						} else {
							style = `top:${touches.clientY}px;`;
						}
						if (touches.clientX > (res.windowWidth / 2)) {
							style += `right:${res.windowWidth-touches.clientX}px;`;
						} else {
							style += `left:${touches.clientX}px;`;
						}
						this.menu.style = style;
						// 
						this.$nextTick(() => {
							this.menu.show = true;
						});
					}
				})


			},
			onSendFail() {
				uni.showToast({
					title: "该文件已发送失败，目前不支持自动重新发送，建议手动重新发送",
					icon: "none"
				})
			},
			onPlayVoice() {
				if (!this.audio) {
					this.audio = new Audio();
				}
				this.audio.src = JSON.parse(this.msgInfo.content).url;
				this.audio.play();
				this.handlePlayVoice = 'RUNNING';
			},
			onSelectMenu(item) {
				this.$emit(item.key.toLowerCase(), this.msgInfo);
				this.menu.show = false;
			},
			onShowFullImage() {
				let imageUrl = JSON.parse(this.msgInfo.content).originUrl;
				uni.previewImage({
					urls: [imageUrl]
				})
			},
			onShowUserInfo(userId){
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + userId
				})
			}
			
		},
		computed: {
			loading() {
				return !this.isTimeout && this.msgInfo.loadStatus && this.msgInfo.loadStatus === "loading";
			},
			loadFail() {
				return this.msgInfo.loadStatus && (this.isTimeout || this.msgInfo.loadStatus === "fail");
			},
			isTimeout() {
				return (new Date().getTime() - new Date(this.msgInfo.sendTime).getTime()) > 30 * 1000;

			},
			data() {
				return JSON.parse(this.msgInfo.content)
			},
			fileSize() {
				let size = this.data.size;
				if (size > 1024 * 1024) {
					return Math.round(size / 1024 / 1024) + "M";
				}
				if (size > 1024) {
					return Math.round(size / 1024) + "KB";
				}
				return size + "B";
			},
			menuItems() {
				let items = [];
				items.push({
					key: 'DELETE',
					name: '删除',
					icon: 'trash'
				});
				if (this.msgInfo.selfSend && this.msgInfo.id > 0) {
					items.push({
						key: 'RECALL',
						name: '撤回',
						icon: 'refreshempty'
					});
				}
				if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.FILE) {
					items.push({
						key: 'DOWNLOAD',
						name: '下载并打开',
						icon: 'download'
					});
				}
				return items;
			}
		}

	}
</script>

<style scoped lang="scss">
	.chat-msg-item {
		padding: 20rpx;

		.chat-msg-tip {
			line-height: 60rpx;
			text-align: center;
		}

		.chat-msg-normal {
			position: relative;
			font-size: 0;
			margin-bottom: 15rpx;
			padding-left: 120rpx;
			min-height: 120rpx;

			.avatar {
				position: absolute;
				display: flex;
				justify-content: center;
				align-items: center;
				width: 100rpx;
				height: 100rpx;
				top: 0;
				left: 0;

				.head-image {
					width: 100%;
					height: 100%;
					border-radius: 5%;
				}
			}



			.chat-msg-content {
				display: flex;
				flex-direction: column;

				.chat-msg-top {
					display: flex;
					flex-wrap: nowrap;
					color: #333;
					font-size: 14px;
					line-height: 20px;

					text {
						margin-right: 12px;
					}
				}

				.chat-msg-bottom {
					text-align: left;

					.chat-msg-text {
						position: relative;
						line-height: 22px;
						margin-top: 10px;
						padding: 10px;
						background-color: #eeeeee;
						border-radius: 3px;
						color: #333;
						display: inline-block;
						font-size: 14px;
						word-break: break-all;
						white-space: pre-line;

						&:after {
							content: "";
							position: absolute;
							left: -10px;
							top: 13px;
							width: 0;
							height: 0;
							border-style: solid dashed dashed;
							border-color: #eeeeee transparent transparent;
							overflow: hidden;
							border-width: 10px;
						}
					}


					.chat-msg-image {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;

						.img-load-box {
							position: relative;

							.send-image {
								min-width: 200rpx;
								min-height: 150rpx;
								max-width: 400rpx;
								max-height: 300rpx;
								border: #dddddd solid 1px;
								cursor: pointer;
							}
						}


						.send-fail {
							color: #e60c0c;
							font-size: 30px;
							cursor: pointer;
							margin: 0 20px;
						}
					}

					.chat-msg-file {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;
						cursor: pointer;

						.chat-file-box {
							position: relative;
							display: flex;
							flex-wrap: nowrap;
							align-items: center;
							width: 65%;
							min-height: 80px;
							border: #dddddd solid 1px;
							border-radius: 3px;
							background-color: #eeeeee;
							padding: 10px 15px;

							.chat-file-info {
								flex: 1;
								height: 100%;
								text-align: left;
								font-size: 14px;

								.chat-file-name {
									font-size: 16px;
									font-weight: 600;
									margin-bottom: 15px;
									word-break: break-all;
								}
							}

							.chat-file-icon {
								font-size: 80rpx;
								color: #d42e07;
							}
						}

						.send-fail {
							color: #e60c0c;
							font-size: 50rpx;
							cursor: pointer;
							margin: 0 20rpx;
						}

					}

					.chat-msg-voice {
						font-size: 14px;
						cursor: pointer;

						audio {
							height: 45px;
							padding: 5px 0;
						}
					}
				}
			}


			&.chat-msg-mine {
				text-align: right;
				padding-left: 0;
				padding-right: 120rpx;

				.avatar {
					left: auto;
					right: 0;
				}

				.chat-msg-content {

					.chat-msg-top {
						flex-direction: row-reverse;

						text {
							margin-left: 12px;
							margin-right: 0;
						}
					}

					.chat-msg-bottom {
						text-align: right;

						.chat-msg-text {
							margin-left: 10px;
							background-color: #5fb878;
							color: #fff;
							display: inline-block;
							vertical-align: top;
							font-size: 14px;

							&:after {
								left: auto;
								right: -10px;
								border-top-color: #5fb878;
							}
						}

						.chat-msg-image {
							flex-direction: row-reverse;
						}

						.chat-msg-file {
							flex-direction: row-reverse;
						}
					}
				}
			}

		}
	}
</style>
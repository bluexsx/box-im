<template>
	<view class="chat-msg-item">
		<view class="chat-msg-tip"
			v-if="msgInfo.type == $enums.MESSAGE_TYPE.RECALL || msgInfo.type == $enums.MESSAGE_TYPE.TIP_TEXT">
			{{ msgInfo.content }}
		</view>
		<view class="chat-msg-tip" v-if="msgInfo.type == $enums.MESSAGE_TYPE.TIP_TIME">
			{{ $date.toTimeText(msgInfo.sendTime) }}
		</view>
		<view class="chat-msg-normal" v-if="isNormal" :class="{ 'chat-msg-mine': msgInfo.selfSend }">
			<head-image class="avatar" @longpress.prevent="$emit('longPressHead')" :id="msgInfo.sendId" :url="headImage"
				:name="showName" size="small"></head-image>
			<view class="chat-msg-content">
				<view v-if="msgInfo.groupId && !msgInfo.selfSend" class="chat-msg-top">
					<text>{{ showName }}</text>
				</view>
				<view class="chat-msg-bottom">
					<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.TEXT">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<up-parse class="chat-msg-text" :showImgMenu="false" :content="nodesText"></up-parse>
							<!-- <rich-text class="chat-msg-text" :nodes="nodesText"></rich-text> -->
						</long-press-menu>
					</view>
					<view class="chat-msg-image" v-if="msgInfo.type == $enums.MESSAGE_TYPE.IMAGE">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<view class="img-load-box">
								<image class="send-image" mode="heightFix" :src="JSON.parse(msgInfo.content).thumbUrl"
									lazy-load="true" @click.stop="onShowFullImage()">
								</image>
								<loading v-if="loading"></loading>
							</view>
						</long-press-menu>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
					<view class="chat-msg-file" v-if="msgInfo.type == $enums.MESSAGE_TYPE.FILE">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<view class="chat-file-box">
								<view class="chat-file-info">
									<uni-link class="chat-file-name" :text="data.name" showUnderLine="true"
										color="#007BFF" :href="data.url"></uni-link>
									<view class="chat-file-size">{{ fileSize }}</view>
								</view>
								<view class="chat-file-icon iconfont icon-file"></view>
								<loading v-if="loading"></loading>
							</view>
						</long-press-menu>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
					<long-press-menu v-if="msgInfo.type == $enums.MESSAGE_TYPE.AUDIO" :items="menuItems"
						@select="onSelectMenu">
						<view class="chat-msg-audio chat-msg-text" @click="onPlayAudio()">
							<text class="iconfont icon-voice-play"></text>
							<text class="chat-audio-text">{{ JSON.parse(msgInfo.content).duration + '"' }}</text>
							<text v-if="audioPlayState == 'PAUSE'" class="iconfont icon-play"></text>
							<text v-if="audioPlayState == 'PLAYING'" class="iconfont icon-pause"></text>
						</view>
					</long-press-menu>
					<long-press-menu v-if="isAction" :items="menuItems" @select="onSelectMenu">
						<view class="chat-realtime chat-msg-text" @click="$emit('call')">
							<text v-if="msgInfo.type == $enums.MESSAGE_TYPE.ACT_RT_VOICE"
								class="iconfont icon-chat-voice"></text>
							<text v-if="msgInfo.type == $enums.MESSAGE_TYPE.ACT_RT_VIDEO"
								class="iconfont icon-chat-video"></text>
							<text>{{ msgInfo.content }}</text>
						</view>
					</long-press-menu>
					<view class="chat-msg-status" v-if="!isAction">
						<text class="chat-readed" v-if="msgInfo.selfSend && !msgInfo.groupId
							&& msgInfo.status == $enums.MESSAGE_STATUS.READED">已读</text>
						<text class="chat-unread" v-if="msgInfo.selfSend && !msgInfo.groupId
							&& msgInfo.status != $enums.MESSAGE_STATUS.READED">未读</text>
					</view>
					<view class="chat-receipt" v-if="msgInfo.receipt" @click="onShowReadedBox">
						<text v-if="msgInfo.receiptOk" class="tool-icon iconfont icon-ok"></text>
						<text v-else>{{ msgInfo.readedCount }}人已读</text>
					</view>
				</view>
			</view>
		</view>
		<chat-group-readed ref="chatGroupReaded" :groupMembers="groupMembers" :msgInfo="msgInfo"></chat-group-readed>

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
		},
		groupMembers: {
			type: Array
		}
	},
	data() {
		return {
			audioPlayState: 'STOP',
			innerAudioContext: null,
			menu: {
				show: false,
				style: ""
			}
		}
	},
	methods: {
		onSendFail() {
			uni.showToast({
				title: "该文件已发送失败，目前不支持自动重新发送，建议手动重新发送",
				icon: "none"
			})
		},
		onPlayAudio() {
			// 初始化音频播放器
			if (!this.innerAudioContext) {
				this.innerAudioContext = uni.createInnerAudioContext();
				let url = JSON.parse(this.msgInfo.content).url;
				this.innerAudioContext.src = url;
				this.innerAudioContext.onEnded((e) => {
					console.log('停止')
					this.audioPlayState = "STOP"
					this.emit();
				})
				this.innerAudioContext.onError((e) => {
					this.audioPlayState = "STOP"
					console.log("播放音频出错");
					console.log(e)
					this.emit();
				});
			}
			if (this.audioPlayState == 'STOP') {
				this.innerAudioContext.play();
				this.audioPlayState = "PLAYING";
			} else if (this.audioPlayState == 'PLAYING') {
				this.innerAudioContext.pause();
				this.audioPlayState = "PAUSE"
			} else if (this.audioPlayState == 'PAUSE') {
				this.innerAudioContext.play();
				this.audioPlayState = "PLAYING"
			}
			this.emit();
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
		onShowReadedBox() {
			this.$refs.chatGroupReaded.open();
		},
		emit() {
			this.$emit("audioStateChange", this.audioPlayState, this.msgInfo);
		},
		stopPlayAudio() {
			if (this.innerAudioContext) {
				this.innerAudioContext.stop();
				this.innerAudioContext = null;
				this.audioPlayState = "STOP"
			}
		}
	},
	computed: {
		loading() {
			return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "loading";
		},
		loadFail() {
			return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "fail";
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
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.TEXT) {
				items.push({
					key: 'COPY',
					name: '复制',
					icon: 'bars'
				});
			}
			if (this.msgInfo.selfSend && this.msgInfo.id > 0) {
				items.push({
					key: 'RECALL',
					name: '撤回',
					icon: 'refreshempty'
				});
			}
			items.push({
				key: 'DELETE',
				name: '删除',
				icon: 'trash',
				color: '#e64e4e'
			});
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.FILE) {
				items.push({
					key: 'DOWNLOAD',
					name: '下载并打开',
					icon: 'download'
				});
			}
			return items;
		},
		isAction() {
			return this.$msgType.isAction(this.msgInfo.type);
		},
		isNormal() {
			const type = this.msgInfo.type;
			return this.$msgType.isNormal(type) || this.$msgType.isAction(type)
		},
		nodesText() {
			let color = this.msgInfo.selfSend ? 'white' : '';
			let text = this.$url.replaceURLWithHTMLLinks(this.msgInfo.content, color)
			return this.$emo.transform(text, 'emoji-normal')
		}
	}

}
</script>

<style scoped lang="scss">
.chat-msg-item {
	padding: 2rpx 20rpx;

	.chat-msg-tip {
		line-height: 60rpx;
		text-align: center;
		color: $im-text-color-lighter;
		font-size: $im-font-size-smaller-extra;
		padding: 10rpx;
	}

	.chat-msg-normal {
		position: relative;
		margin-bottom: 22rpx;
		padding-left: 110rpx;
		min-height: 80rpx;

		.avatar {
			position: absolute;
			top: 0;
			left: 0;
		}

		.chat-msg-content {
			text-align: left;

			.chat-msg-top {
				display: flex;
				flex-wrap: nowrap;
				color: $im-text-color-lighter;
				font-size: $im-font-size-smaller;
				line-height: $im-font-size-smaller;
				height: $im-font-size-smaller;
			}

			.chat-msg-bottom {
				display: inline-block;
				padding-right: 80rpx;
				margin-top: 5rpx;

				.chat-msg-text {
					position: relative;
					line-height: 1.6;
					margin-top: 10rpx;
					padding: 16rpx 24rpx;
					background-color: $im-bg;
					border-radius: 20rpx;
					color: $im-text-color;
					font-size: $im-font-size;
					text-align: left;
					display: block;
					word-break: break-all;
					white-space: pre-line;


					&:after {
						content: "";
						position: absolute;
						left: -20rpx;
						top: 26rpx;
						width: 6rpx;
						height: 6rpx;
						border-style: solid dashed dashed;
						border-color: $im-bg transparent transparent;
						overflow: hidden;
						border-width: 18rpx;
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
							max-width: 420rpx;
							height: 350rpx;
							cursor: pointer;
							border-radius: 4px;
						}
					}


					.send-fail {
						color: $im-color-danger;
						font-size: $im-font-size;
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
						min-height: 60px;
						border-radius: 4px;
						padding: 10px 15px;
						box-shadow: $im-box-shadow-dark;

						.chat-file-info {
							flex: 1;
							height: 100%;
							text-align: left;
							font-size: 14px;
							width: 300rpx;

							.chat-file-name {
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

				.chat-msg-audio {
					display: flex;
					align-items: center;

					.chat-audio-text {
						padding-right: 8px;
					}

					.icon-voice-play {
						font-size: 18px;
						padding-right: 8px;
					}
				}

				.chat-realtime {
					display: flex;
					align-items: center;

					.iconfont {
						font-size: 20px;
						padding-right: 8px;
					}
				}

				.chat-msg-status {
					line-height: $im-font-size-smaller-extra;
					font-size: $im-font-size-smaller-extra;
					padding-top: 2rpx;

					.chat-readed {
						display: block;
						padding-top: 2rpx;
						color: $im-text-color-lighter;
					}

					.chat-unread {
						color: $im-color-danger;
					}
				}

				.chat-receipt {
					font-size: $im-font-size-smaller;
					color: $im-text-color-lighter;
					font-weight: 600;

					.icon-ok {
						font-size: 20px;
						color: $im-color-success;
					}
				}
			}
		}


		&.chat-msg-mine {
			text-align: right;
			padding-left: 0;
			padding-right: 110rpx;

			.avatar {
				left: auto;
				right: 0;
			}

			.chat-msg-content {
				text-align: right;

				.chat-msg-bottom {
					padding-left: 80rpx;
					padding-right: 0;

					.chat-msg-text {
						margin-left: 10px;
						background-color: $im-color-primary-light-2;
						color: #fff;

						&:after {
							left: auto;
							right: -9px;
							border-top-color: $im-color-primary-light-2;
						}
					}

					.chat-msg-image {
						flex-direction: row-reverse;
					}

					.chat-msg-file {
						flex-direction: row-reverse;
					}

					.chat-msg-audio {
						flex-direction: row-reverse;

						.chat-audio-text {
							padding-right: 0;
							padding-left: 8px;
						}

						.icon-voice-play {
							transform: rotateY(180deg);
						}
					}

					.chat-realtime {
						display: flex;
						flex-direction: row-reverse;

						.iconfont {
							transform: rotateY(180deg);
						}
					}

				}
			}
		}

	}
}
</style>
<template>
	<view class="chat-msg-item">
		<view class="chat-msg-tip" v-if="msgInfo.type==$enums.MESSAGE_TYPE.RECALL">{{msgInfo.content}}</view>
		<view class="chat-msg-tip" v-if="msgInfo.type==$enums.MESSAGE_TYPE.TIP_TIME">
			{{$date.toTimeText(msgInfo.sendTime)}}
		</view>

		<view class="chat-msg-normal" v-if="msgInfo.type>=0 && msgInfo.type<10"
			:class="{'chat-msg-mine':msgInfo.selfSend}">
			<head-image class="avatar" @longpress.prevent="$emit('longPressHead')" :id="msgInfo.sendId" :url="headImage" :name="showName" :size="80"></head-image>
			<view class="chat-msg-content" @longpress="onShowMenu($event)">
				<view v-if="msgInfo.groupId && !msgInfo.selfSend" class="chat-msg-top">
					<text>{{showName}}</text>
				</view>

				<view class="chat-msg-bottom">
					<rich-text class="chat-msg-text" v-if="msgInfo.type==$enums.MESSAGE_TYPE.TEXT"
						:nodes="$emo.transform(msgInfo.content)"></rich-text>
					<view class="chat-msg-image" v-if="msgInfo.type==$enums.MESSAGE_TYPE.IMAGE">
						<view class="img-load-box">
							<image class="send-image" mode="heightFix" :src="JSON.parse(msgInfo.content).thumbUrl"
								lazy-load="true" @click.stop="onShowFullImage()">
							</image>
							<loading v-if="loading"></loading>
						</view>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
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
							<loading v-if="loading"></loading>
						</view>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
					<text class="chat-readed" v-show="msgInfo.selfSend && !msgInfo.groupId
							&& msgInfo.status==$enums.MESSAGE_STATUS.READED">已读</text>
					<text class="chat-unread" v-show="msgInfo.selfSend && !msgInfo.groupId 
							&& msgInfo.status!=$enums.MESSAGE_STATUS.READED">未读</text>

					<!--
					<view class="chat-msg-voice" v-if="msgInfo.type==$enums.MESSAGE_TYPE.AUDIO" @click="onPlayVoice()">
						<audio controls :src="JSON.parse(msgInfo.content).url"></audio>
					</view>
					-->
				</view>

			</view>

		</view>
		<pop-menu v-if="menu.show" :menu-style="menu.style" :items="menuItems" @close="menu.show=false"
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
		padding: 2rpx 20rpx;

		.chat-msg-tip {
			line-height: 60rpx;
			text-align: center;
			color: #555;
			font-size: 24rpx;
			padding: 10rpx;
		}

		.chat-msg-normal {
			position: relative;
			font-size: 0;
			margin-bottom: 15rpx;
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
					color: #333;
					font-size: 24rpx;
					line-height: 24rpx;

				}

				.chat-msg-bottom {
					display: inline-block;
					padding-right: 80rpx;

					.chat-msg-text {
						position: relative;
						line-height: 60rpx;
						margin-top: 10rpx;
						padding: 10rpx 20rpx;
						background-color: white;
						border-radius: 10rpx;
						color: #333;
						font-size: 30rpx;
						text-align: left;
						display: block;
						word-break: break-all;
						white-space: pre-line;
						box-shadow: 1px 1px 1px  #c0c0f0;

						&:after {
							content: "";
							position: absolute;
							left: -20rpx;
							top: 26rpx;
							width: 6rpx;
							height: 6rpx;
							border-style: solid dashed dashed;
							border-color: white transparent transparent;
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
								min-height: 200rpx;
								max-width: 400rpx;
								max-height: 400rpx;
								border: 8rpx solid #ebebf5;
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
							min-height: 80px;
							border: #dddddd solid 1px;
							border-radius: 10rpx;
							background-color: #eeeeee;
							padding: 10px 15px;
							box-shadow: 2px 2px 2px #c0c0c0;

							.chat-file-info {
								flex: 1;
								height: 100%;
								text-align: left;
								font-size: 14px;
								width: 300rpx;

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

					
					.chat-unread {
						font-size: 10px;
						color: #f23c0f;
						font-weight: 600;
					}
					
					.chat-readed {
						font-size: 10px;
						color: #ccc;
						font-weight: 600;
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
							background-color: #587ff0;
							color: #fff;
							box-shadow: 1px 1px 1px #ccc;

							&:after {
								left: auto;
								right: -10px;
								border-top-color: #587ff0;
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
<template>
	<div class="chat-msg-item">
		<div class="chat-msg-tip" v-show="msgInfo.type==$enums.MESSAGE_TYPE.RECALL">{{msgInfo.content}}</div>
		<div class="chat-msg-tip" v-show="msgInfo.type==$enums.MESSAGE_TYPE.TIP_TIME">
			{{$date.toTimeText(msgInfo.sendTime)}}
		</div>

		<div class="chat-msg-normal" v-show="msgInfo.type>=0 && msgInfo.type<10" :class="{'chat-msg-mine':mine}">
			<div class="head-image">
				<head-image :name="showName" :size="40" :url="headImage" :id="msgInfo.sendId"></head-image>
			</div>
			<div class="chat-msg-content">
				<div v-show="mode==1 && msgInfo.groupId && !msgInfo.selfSend" class="chat-msg-top">
					<span>{{showName}}</span>
				</div>
				<div v-show="mode==2" class="chat-msg-top">
					<span>{{showName}}</span>
					<span>{{$date.toTimeText(msgInfo.sendTime)}}</span>
				</div>
				<div class="chat-msg-bottom" @contextmenu.prevent="showRightMenu($event)">
					<span class="chat-msg-text" v-if="msgInfo.type==$enums.MESSAGE_TYPE.TEXT"
						v-html="$emo.transform(msgInfo.content)"></span>
					<div class="chat-msg-image" v-if="msgInfo.type==$enums.MESSAGE_TYPE.IMAGE">
						<div class="img-load-box" v-loading="loading" element-loading-text="上传中.."
							element-loading-background="rgba(0, 0, 0, 0.4)">
							<img class="send-image" :src="JSON.parse(msgInfo.content).thumbUrl"
								@click="showFullImageBox()" />
						</div>
						<span title="发送失败" v-show="loadFail" @click="onSendFail"
							class="send-fail el-icon-warning"></span>
					</div>
					<div class="chat-msg-file" v-if="msgInfo.type==$enums.MESSAGE_TYPE.FILE">
						<div class="chat-file-box" v-loading="loading">
							<div class="chat-file-info">
								<el-link class="chat-file-name" :underline="true" target="_blank" type="primary"
									:href="data.url">{{data.name}}</el-link>
								<div class="chat-file-size">{{fileSize}}</div>
							</div>
							<div class="chat-file-icon">
								<span type="primary" class="el-icon-document"></span>
							</div>
						</div>
						<span title="发送失败" v-show="loadFail" @click="onSendFail"
							class="send-fail el-icon-warning"></span>
					</div>
					<div class="chat-msg-voice" v-if="msgInfo.type==$enums.MESSAGE_TYPE.AUDIO"
						@click="onPlayVoice()">
						<audio controls :src="JSON.parse(msgInfo.content).url"></audio>
					</div>
					<span class="chat-readed" v-show="msgInfo.selfSend && !msgInfo.groupId 
						&& msgInfo.status==$enums.MESSAGE_STATUS.READED">已读</span>
					<span class="chat-unread" v-show="msgInfo.selfSend && !msgInfo.groupId 
						&& msgInfo.status!=$enums.MESSAGE_STATUS.READED">未读</span>
				</div>
			</div>

		</div>
		<right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="menuItems"
			@close="rightMenu.show=false" @select="onSelectMenu"></right-menu>
	</div>
</template>

<script>
	import HeadImage from "../common/HeadImage.vue";
	import RightMenu from '../common/RightMenu.vue';

	export default {
		name: "messageItem",
		components: {
			HeadImage,
			RightMenu
		},
		props: {
			mode: {
				type: Number,
				default: 1
			},
			mine: {
				type: Boolean,
				required: true
			},
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
			menu: {
				type: Boolean,
				default: true
			}
		},
		data() {
			return {
				audioPlayState: 'STOP',
				rightMenu: {
					show: false,
					pos: {
						x: 0,
						y: 0
					}
				}
			}

		},
		methods: {
			onSendFail() {
				this.$message.error("该文件已发送失败，目前不支持自动重新发送，建议手动重新发送")
			},
			showFullImageBox() {
				let imageUrl = JSON.parse(this.msgInfo.content).originUrl;
				if (imageUrl) {
					this.$store.commit('showFullImageBox', imageUrl);
				}
			},
			onPlayVoice() {
				if (!this.audio) {
					this.audio = new Audio();
				}
				this.audio.src = JSON.parse(this.msgInfo.content).url;
				this.audio.play();
				this.onPlayVoice = 'RUNNING';
			},
			showRightMenu(e) {
				this.rightMenu.pos = {
					x: e.x,
					y: e.y
				};
				this.rightMenu.show = "true";
			},
			onSelectMenu(item) {
				this.$emit(item.key.toLowerCase(), this.msgInfo);
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
					icon: 'el-icon-delete'
				});
				if (this.msgInfo.selfSend && this.msgInfo.id > 0) {
					items.push({
						key: 'RECALL',
						name: '撤回',
						icon: 'el-icon-refresh-left'
					});
				}
				return items;
			}
		}
	}
</script>

<style scoped lang="scss">
	.chat-msg-item {

		.chat-msg-tip {
			line-height: 50px;
			font-size: 14px;
		}

		.chat-msg-normal {
			position: relative;
			font-size: 0;
			padding-left: 60px;
			min-height: 50px;
			margin-top: 10px;

			.head-image {
				position: absolute;
				width: 40px;
				height: 40px;
				top: 0;
				left: 0;
			}

			.chat-msg-content {
				text-align: left;

				.send-fail {
					color: #e60c0c;
					font-size: 30px;
					cursor: pointer;
					margin: 0 20px;
				}

				.chat-msg-top {
					display: flex;
					flex-wrap: nowrap;
					color: #333;
					font-size: 14px;
					line-height: 20px;

					span {
						margin-right: 12px;
					}
				}

				.chat-msg-bottom {
					display: inline-block;
					padding-right: 80px;

					.chat-msg-text {
						display: block;
						position: relative;
						line-height: 30px;
						margin-top: 3px;
						padding: 7px;
						background-color: white;
						border-radius: 10px;
						color: black;
						display: block;
						font-size: 16px;
						text-align: left;
						white-space: pre-wrap;
						word-break: break-all;
						box-shadow: 1px 1px 1px #c0c0f0;

						&:after {
							content: "";
							position: absolute;
							left: -10px;
							top: 13px;
							width: 0;
							height: 0;
							border-style: solid dashed dashed;
							border-color: white transparent transparent;
							overflow: hidden;
							border-width: 10px;
						}
					}

					.chat-msg-image {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;

						.send-image {
							min-width: 200px;
							min-height: 150px;
							max-width: 400px;
							max-height: 300px;
							border: #dddddd solid 1px;
							border: 5px solid #ccc;
							border-radius: 6px;
							cursor: pointer;
						}

					}

					.chat-msg-file {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;
						cursor: pointer;

						.chat-file-box {
							display: flex;
							flex-wrap: nowrap;
							align-items: center;
							min-height: 80px;
							box-shadow: 5px 5px 2px #c0c0c0;
							border: #dddddd solid 1px;
							border-radius: 6px;
							background-color: #eeeeee;
							padding: 10px 15px;

							.chat-file-info {
								flex: 1;
								height: 100%;
								text-align: left;
								font-size: 14px;

								.chat-file-name {
									display: inline-block;
									min-width: 150px;
									max-width: 300px;
									font-size: 16px;
									font-weight: 600;
									margin-bottom: 15px;
									white-space: pre-wrap;
									word-break: break-all;
								}
							}

							.chat-file-icon {
								font-size: 50px;
								color: #d42e07;
							}
						}

						.send-fail {
							color: #e60c0c;
							font-size: 30px;
							cursor: pointer;
							margin: 0 20px;
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

					.chat-unread {
						font-size: 10px;
						color: #f23c0f;
						font-weight: 600;
					}
					
					.chat-readed {
						font-size: 10px;
						color: #888;
						font-weight: 600;
					}
				}
			}


			&.chat-msg-mine {
				text-align: right;
				padding-left: 0;
				padding-right: 60px;

				.head-image {
					left: auto;
					right: 0;
				}

				.chat-msg-content {
					text-align: right;

					.chat-msg-top {
						flex-direction: row-reverse;

						span {
							margin-left: 12px;
							margin-right: 0;
						}
					}

					.chat-msg-bottom {
						padding-left: 80px;
						padding-right: 0;

						.chat-msg-text {
							margin-left: 10px;
							background-color: rgb(88, 127, 240);
							color: #fff;
							vertical-align: top;
							box-shadow: 1px 1px 1px #ccc;

							&:after {
								left: auto;
								right: -10px;
								border-top-color: rgb(88, 127, 240);
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
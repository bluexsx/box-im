<template>
	<div class="chat-msg-item">
		<div class="chat-msg-tip" v-show="msgInfo.type==$enums.MESSAGE_TYPE.RECALL">{{msgInfo.content}}</div>
		<div class="chat-msg-normal" v-show="msgInfo.type!=$enums.MESSAGE_TYPE.RECALL" :class="{'chat-msg-mine':mine}">
			<div class="head-image">
				<head-image :url="headImage" :id="msgInfo.sendId"></head-image>
			</div>
			<div class="chat-msg-content">
				<div class="chat-msg-top">
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
						<span title="发送失败" v-show="loadFail" @click="handleSendFail"
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
						<span title="发送失败" v-show="loadFail" @click="handleSendFail"
							class="send-fail el-icon-warning"></span>
					</div>
					<div class="chat-msg-voice" v-if="msgInfo.type==$enums.MESSAGE_TYPE.AUDIO"
						@click="handlePlayVoice()">
						<audio controls :src="JSON.parse(msgInfo.content).url"></audio>
					</div>
				</div>
			</div>
			
		</div>
		<right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="menuItems"
			@close="rightMenu.show=false" @select="handleSelectMenu"></right-menu>
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
			handleSendFail() {
				this.$message.error("该文件已发送失败，目前不支持自动重新发送，建议手动重新发送")
			},
			showFullImageBox() {
				let imageUrl = JSON.parse(this.msgInfo.content).originUrl;
				if (imageUrl) {
					this.$store.commit('showFullImageBox', imageUrl);
				}
			},
			handlePlayVoice() {
				if (!this.audio) {
					this.audio = new Audio();
				}
				this.audio.src = JSON.parse(this.msgInfo.content).url;
				this.audio.play();
				this.handlePlayVoice = 'RUNNING';
			},
			showRightMenu(e) {
				this.rightMenu.pos = {
					x: e.x,
					y: e.y
				};
				this.rightMenu.show = "true";
			},
			handleSelectMenu(item) {
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
		},
		mounted() {
			//console.log(this.msgInfo);
		}
	}
</script>

<style scoped lang="scss">
	.chat-msg-item {

		.chat-msg-tip {
			line-height: 50px;
		}

		.chat-msg-normal {
			position: relative;
			font-size: 0;
			margin-bottom: 10px;
			padding-left: 60px;
			min-height: 68px;

			.head-image {
				position: absolute;
				width: 40px;
				height: 40px;
				top: 0;
				left: 0;
			}

			.chat-msg-content {
				text-align: left;

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
						line-height: 35px;
						margin-top: 10px;
						padding: 14px;
						background-color: rgb(235,235,245);
						border-radius: 10px;
						color: black;
						display: block;
						font-size: 17px;
						text-align: left;
						white-space: pre-wrap;
						word-break: break-all;

						&:after {
							content: "";
							position: absolute;
							left: -10px;
							top: 13px;
							width: 0;
							height: 0;
							border-style: solid dashed dashed;
							border-color: rgb(235,235,245) transparent transparent;
							overflow: hidden;
							border-width: 13px;
						}
					}

					.chat-msg-image {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;

						.send-image {
							min-width: 300px;
							min-height: 200px;
							max-width: 600px;
							max-height: 400px;
							border: #dddddd solid 1px;
							cursor: pointer;
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
							display: flex;
							flex-wrap: nowrap;
							align-items: center;
							width: 20%;
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
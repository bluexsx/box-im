<template>
	<div class="im-msg-item" :class="{'im-chat-mine':mine}">
		<div class="head-image">
			<head-image :url="headImage"></head-image>
		</div>
		<div class="im-msg-content">
			<div class="im-msg-top">
				<span>{{showName}}</span>
				<chat-time :time="msgInfo.sendTime"></chat-time>
			</div>
			<div class="im-msg-bottom">
				<span class="im-msg-text" v-if="msgInfo.type==0" >{{msgInfo.content}}</span>
				<div class="im-msg-image"   v-if="msgInfo.type==1">
					<div class="img-load-box" v-loading="loading"  
					element-loading-text="上传中.."
					element-loading-background="rgba(0, 0, 0, 0.4)">
						<img class="send-image" :src="JSON.parse(msgInfo.content).thumbUrl"/>	
					</div>
					<span title="发送失败" v-show="loadFail" @click="handleSendFail" class="send-fail el-icon-warning"></span>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
	import ChatTime from "./ChatTime.vue";
	import HeadImage from "../common/HeadImage.vue";

	export default {
		name: "messageItem",
		components: {
			ChatTime,
			HeadImage
		},
		props: {
			mine:{
				type:Boolean,
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
			}
		},
		methods:{
			handleSendFail(){
				this.$message.error("该文件已上传失败，目前不支持自动重新发送，建议手动重新发送")
			}
		},
		computed:{
			loading(){
				return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "loadding";
			},
			loadFail(){
				return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "fail";
			}
		}
	}
</script>

<style lang="scss">
	.im-msg-item {
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

		.im-msg-content {
			display: flex;
			flex-direction: column;

			.im-msg-top {
				display: flex;
				flex-wrap: nowrap;
				color: #333;
				font-size: 14px;
				line-height: 20px;

				span {
					margin-right: 12px;
				}
			}

			.im-msg-bottom {
				text-align: left;

				.im-msg-text {
					position: relative;
					line-height: 22px;
					margin-top: 10px;
					padding: 10px;
					background-color: #eeeeee;
					border-radius: 3px;
					color: #333;
					display: inline-block;
					font-size: 14px;

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
				
				.im-msg-image{
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;
		
		
					.send-image{
						min-width: 300px;
						min-height: 200px;
						max-width: 600px;
						max-height: 400px;
						border: #dddddd solid 1px;
						cursor: pointer;
					}
		
					.send-fail{
						color: red;
						font-size: 30px;
						cursor: pointer;
						margin: 0 20px;
					}
				}
			}
		}


		&.im-chat-mine {
			text-align: right;
			padding-left: 0;
			padding-right: 60px;

			.head-image {
				left: auto;
				right: 0;
			}

			.im-msg-content {

				.im-msg-top {
					flex-direction: row-reverse;

					span {
						margin-left: 12px;
						margin-right: 0;
					}
				}

				.im-msg-bottom {
					text-align: right;

					.im-msg-text {
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
					
					.im-msg-image {
						flex-direction: row-reverse;
					}
				}
			}

			.message-info {
				right: 60px !important;
				display: inline-block;
			}
		}

	}
</style>

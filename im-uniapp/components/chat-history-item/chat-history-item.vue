<template>
	<view class="chat-history-item">
		<view class="chat-message">
			<view @click.stop="onShowUserInfo">
				<head-image class="avatar" :id="message.sendId" :url="headImage" :name="showName" size="small"></head-image>
			</view>
			<view class="chat-content">
				<view class="chat-top">
					<view>{{ showName }}</view>
					<view class="chat-time">{{ $date.formatDateTime(message.sendTime,true) }}</view>
				</view>
				<view class="chat-bottom">
					<view v-if="message.type == $enums.MESSAGE_TYPE.TEXT">
						<rich-text class="chat-text" :nodes="nodesText"></rich-text>
					</view>
					<view v-else-if="message.type == $enums.MESSAGE_TYPE.IMAGE" class="chat-image" @click.stop="onShowFullImage">
						<image class="preview-image" :src="data.thumbUrl" mode="aspectFill" lazy-load="true"></image>
					</view>
					<view v-else-if="message.type == $enums.MESSAGE_TYPE.AUDIO" class="chat-text">
						[语音] {{ data.duration}} "
					</view>
					<view v-else-if="message.type == $enums.MESSAGE_TYPE.FILE" class="chat-text">
						[文件] {{data.name}}
					</view>
					<view v-else-if="message.type == $enums.MESSAGE_TYPE.ACT_RT_VOICE" class="chat-text">
						[语音通话]
					</view>
					<view v-else-if="message.type == $enums.MESSAGE_TYPE.ACT_RT_VIDEO" class="chat-text">
						[视频通话]
					</view>
					<view v-else class="chat-text">[暂不支持的消息类型]</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: "chat-history-item",
	props: {
		headImage: {
			type: String,
			required: true
		},
		showName: {
			type: String,
			required: true
		},
		message: {
			type: Object,
			required: true
		}
	},
	data() {
		return {}
	},
	methods: {
		onShowUserInfo() {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + this.message.sendId
			})
		},
		onShowFullImage() {
			const imageUrl = this.data && this.data.originUrl;
			if (!imageUrl) {
				return;
			}
			uni.previewImage({
				urls: [imageUrl]
			})
		}
	},
	computed: {
		data() {
			return JSON.parse(this.message.content)
		},
		nodesText() {
			let text = this.$str.html2Escape(this.message.content);
			text = this.$url.replaceURLWithHTMLLinks(text, '')
			return this.$emo.transform(text, 'emoji-normal')
		}
	}
}
</script>

<style scoped lang="scss">
.chat-history-item {
	padding: 15rpx 20rpx;
	margin-bottom: 3rpx;
	background: white;

	&:hover {
		background: $im-bg-active;
	}

	.chat-message {
		position: relative;
		padding-left: 110rpx;
		min-height: 80rpx;

		.avatar {
			position: absolute;
			top: 0;
			left: 0;
		}

		.chat-content {
			text-align: left;

			.chat-top {
				display: flex;
				flex-wrap: nowrap;
				color: $im-text-color-lighter;
				font-size: $im-font-size-smaller;
				line-height: $im-font-size-smaller;
				height: $im-font-size-smaller;

				.chat-time {
					flex: 1;
					text-align: right;
				}
			}

			.chat-bottom {
				display: inline-block;
				margin-top: 5rpx;
				line-height: 36rpx;

				.chat-text {
					position: relative;
					line-height: 1.6;
					margin-top: 10rpx;
					border-radius: 20rpx;
					color: $im-text-color;
					font-size: $im-font-size;
					text-align: left;
					display: inline-flex;
					word-break: break-word;
					white-space: pre-line;
					overflow: visible;
				}

				.chat-image {
					position: relative;
					margin-top: 10rpx;
					display: inline-block;
					cursor: pointer;

					.preview-image {
						max-width: 300rpx;
						max-height: 300rpx;
						border-radius: 10rpx;
					}
				}
			}
		}
	}
}
</style>

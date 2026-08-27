<template>
	<view class="page chat-history-image none-pointer-events">
		<nav-bar title="图片" back></nav-bar>
		<scroll-view v-if="messages.length>0" class="chat-message-box" scroll-y="true" lower-threshold="200"
			@scrolltolower="onScrollToBottom">
			<view v-for="[timeText, ms] of messageMap.entries()" :key="timeText">
				<view class="time-tip">{{timeText}}</view>
				<view class="chat-message-list">
					<view v-for="m in ms" :key="m.localId">
						<view class="chat-message" @longpress.prevent.stop="onLongPress(m)" @touchmove="onTouchMove"
							@touchend="onTouchEnd">
							<image class="chat-image" mode="aspectFill" :src="JSON.parse(m.content).thumbUrl"
								lazy-load="true" @click.stop="onShowFullImage(m)">
							</image>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
		<no-data-tip v-else class="tip" tip="没有数据"></no-data-tip>
		<popup-menu ref="popMenu" :items="menuItems" @select="onSelectMenuItem"></popup-menu>
	</view>
</template>

<script>
import { chatStore } from '@/store/stores.js'

const DB_BATCH_SIZE = 1000;
const MEDIA_PAGE_SIZE = 30;

export default {
	data() {
		return {
			conversation: {},
			messages: [],
			convMinSeqNo: 1,
			scannedMinSeqNo: 0,
			hasMore: true,
			loading: false,
			activeMessage: null,
			menuItems: [{
				key: 'LOCATE_MESSAGE',
				name: '在聊天中定位'
			}],
			isTouchMove: false
		}
	},
	methods: {
		onShowFullImage(m) {
			const imageUrl = JSON.parse(m.content).originUrl;
			if (!imageUrl) {
				return;
			}
			uni.previewImage({
				urls: [imageUrl]
			})
		},
		onScrollToBottom() {
			this.loadMoreMedia(MEDIA_PAGE_SIZE);
		},
		onLongPress(m) {
			if (!this.isTouchMove) {
				this.activeMessage = m;
				this.$refs.popMenu.open();
			}
		},
		onTouchMove() {
			this.isTouchMove = true;
		},
		onTouchEnd() {
			this.isTouchMove = false;
		},
		onSelectMenuItem(item) {
			if (item.key == 'LOCATE_MESSAGE') {
				const convKey = this.conversation.key;
				const id = this.activeMessage.localId;
				uni.navigateTo({
					url: `/pages/chat/chat-box?convKey=${convKey}&locateId=${id}`
				})
			}
		},
		timeText(timeStamp) {
			let dateTime = new Date(timeStamp)
			if (this.$date.isWeek(dateTime)) {
				return '本周'
			} else if (this.$date.isMonth(dateTime)) {
				return '本月'
			} else {
				return this.$date.formatDateTime(dateTime).substr(0, 7);
			}
		},
		isImageMessage(m) {
			return m.type == this.$enums.MESSAGE_TYPE.IMAGE && !m.deleted &&
				m.status != this.$enums.MESSAGE_STATUS.RECALL
		},
		async loadMoreMedia(minCount) {
			if (this.loading || !this.hasMore) {
				return;
			}
			this.loading = true;
			try {
				let added = 0;
				let cursor = this.scannedMinSeqNo - 1;
				while (added < minCount && cursor >= this.convMinSeqNo) {
					const min = Math.max(this.convMinSeqNo, cursor - DB_BATCH_SIZE + 1);
					const batch = await this.$db.findPageMessage(this.conversation.key, min, cursor);
					this.scannedMinSeqNo = min;
					const media = batch.filter((m) => this.isImageMessage(m)).reverse();
					if (media.length) {
						this.messages = this.messages.concat(media);
						added += media.length;
					}
					if (min <= this.convMinSeqNo) {
						this.hasMore = false;
						break;
					}
					cursor = min - 1;
					await new Promise((resolve) => setTimeout(resolve, 0));
				}
				if (this.scannedMinSeqNo <= this.convMinSeqNo) {
					this.hasMore = false;
				}
			} finally {
				this.loading = false;
			}
		}
	},
	computed: {
		messageMap() {
			const map = new Map();
			this.messages.forEach(m => {
				const timeText = this.timeText(m.sendTime);
				if (map.has(timeText)) {
					map.get(timeText).push(m);
				} else {
					map.set(timeText, [m]);
				}
			})
			return map;
		}
	},
	async onLoad(options) {
		this.conversation = chatStore.conversationMap.get(options.convKey);
		this.convMinSeqNo = Math.max(1, this.conversation.minSeqNo);
		this.scannedMinSeqNo = this.conversation.maxSeqNo + 1;
		this.hasMore = this.conversation.maxSeqNo >= this.convMinSeqNo;
		await this.loadMoreMedia(MEDIA_PAGE_SIZE);
	}
}
</script>

<style lang="scss" scoped>
.chat-history-image {
	display: flex;

	.chat-message-box {
		flex: 1;
		height: 100%;

		.time-tip {
			margin-top: 20rpx;
			text-align: left;
			padding: 10rpx;
			color: $im-text-color-light;
		}

		.chat-message-list {
			display: flex;
			flex-wrap: wrap;

			.chat-image {
				margin: 5rpx;
				width: 240rpx;
				height: 240rpx;
				border-radius: 10rpx;
				background: #333;
			}
		}
	}

	.tip {
		width: 100%;
		flex: 1;
	}
}
</style>

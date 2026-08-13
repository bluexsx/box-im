<template>
	<view class="page chat-history-image none-pointer-events">
		<nav-bar title="图片" back></nav-bar>
		<scroll-view v-if="messageMap.size>0" class="chat-message-box" scroll-y="true" upper-threshold="200"
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

export default {
	data() {
		return {
			conversation: {},
			messages: [],
			showMaxIdx: 30,
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
			this.showMaxIdx += 20;
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
		}
	},
	computed: {
		messageMap() {
			const map = new Map();
			const messages = this.messages.slice(0, this.showMaxIdx);
			// 按时间分组
			messages.forEach(m => {
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
		const messages = await this.$db.findMessageByConvKey(this.conversation.key);
		this.messages = messages.filter(m => m.type == this.$enums.MESSAGE_TYPE.IMAGE && !m.deleted &&
			m.status != this.$enums.MESSAGE_STATUS.RECALL).reverse();
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

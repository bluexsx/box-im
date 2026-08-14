<template>
	<view class="page chat-history-file">
		<nav-bar title="文件" back></nav-bar>
		<view class="nav-bar" id="navBar">
			<view class="nav-search">
				<uni-search-bar radius="100" v-model="searchText" placeholder="搜索文件名"
					cancelButton="none"></uni-search-bar>
			</view>
		</view>
		<view v-if="initializing" class="loading-tip">加载中...</view>
		<scroll-view v-else-if="showMessages.length>0" class="chat-message-list"
			:style="{height: scrollbarHeight+'px'}" scroll-y="true" upper-threshold="200" @scrolltolower="onScrollToBottom">
			<view v-for="(m, idx) in showMessages" :key="m.localId">
				<view class="chat-message" @longpress.prevent.stop="onLongPress(m)" @touchmove="onTouchMove"
					@touchend="onTouchEnd">
					<chat-history-item :headImage="headImage(m)" :showName="showName(m)" :message="m"
						@tap="onClickMessageItem(m)">
					</chat-history-item>
				</view>
			</view>
		</scroll-view>
		<no-data-tip v-else class="tip" :tip="noDataTip"></no-data-tip>
		<popup-menu ref="popMenu" :items="menuItems" @select="onSelectMenuItem"></popup-menu>
	</view>
</template>

<script>
import { chatStore, groupStore, userStore } from '@/store/stores.js'

export default {
	data() {
		return {
			searchText: '',
			conversation: {},
			messages: [],
			groupMemberMap: [],
			showMaxIdx: 30,
			navBarHeight: 0,
			activeMessage: null,
			isTouchMove: false,
			initializing: true
		}
	},
	methods: {
		onClickMessageItem(message) {
			uni.hideKeyboard();
			// 延迟50ms,因为必须等键盘消失再跳转，否则会影响计算聊天页面高度
			setTimeout(() => {
				const convKey = this.conversation.key;
				const id = message.localId;
				uni.navigateTo({
					url: `/pages/chat/chat-box?convKey=${convKey}&locateId=${id}`
				})
			}, 50);
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
		headImage(message) {
			if (this.isGroup) {
				const member = this.groupMemberMap.get(message.sendId);
				return member ? member.headImage : "";
			} else {
				return message.selfSend ? this.mine.headImageThumb : this.conversation.headImage
			}
		},
		showName(message) {
			if (!message) return "";
			if (this.isGroup) {
				const member = this.groupMemberMap.get(message.sendId);
				return member ? member.showNickName : "";
			} else {
				return message.selfSend ? this.mine.nickName : this.conversation.showName
			}
		}
	},
	computed: {
		mine() {
			return userStore.userInfo;
		},
		showMessages() {
			return this.messages.filter(m => {
				const fileName = JSON.parse(m.content).name.toLowerCase();
				return fileName.includes(this.searchText.toLowerCase());
			}).slice(0, this.showMaxIdx);
		},
		scrollbarHeight() {
			let h = uni.getSystemInfoSync().windowHeight;
			// 减去标题栏高度
			h -= 50;
			// 减去搜索栏高度
			h -= this.navBarHeight;
			// #ifndef H5
			// h5需要减去状态栏高度
			h -= uni.getSystemInfoSync().statusBarHeight;
			// #endif
			return h;
		},
		noDataTip() {
			return this.searchText ? `未搜索到与'${this.searchText}'相关的内容` : '没有数据';
		},
		menuItems() {
			return [{
				key: 'LOCATE_MESSAGE',
				name: '在聊天中定位'
			}]
		},
		isGroup() {
			return this.conversation.type == this.$enums.CONVERSATION_TYPE.GROUP;
		}
	},
	async onLoad(options) {
		this.conversation = chatStore.conversationMap.get(options.convKey);
		try {
			const messages = await this.$db.findMessageByConvKey(this.conversation.key);
			this.messages = messages.filter(m => this.$enums.MESSAGE_TYPE.FILE == m.type && !m.deleted &&
				m.status != this.$enums.MESSAGE_STATUS.RECALL).reverse();
			if (this.isGroup) {
				const members = groupStore.findGroup(this.conversation.targetId).members;
				this.groupMemberMap = new Map(members.map(m => [m.userId, m]));
			}
		} finally {
			this.initializing = false;
		}
	},
	mounted() {
		const query = uni.createSelectorQuery().in(this);
		query.select('#navBar').boundingClientRect(rect => {
			this.navBarHeight = Number(rect.height)
		}).exec();
	}
}
</script>

<style lang="scss" scoped>
.chat-history-file {
	position: relative;
	display: flex;
	flex-direction: column;

	.chat-message-list {
		flex: 1;
		height: 100%;
	}

	.loading-tip {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		color: $im-text-color-light;
		font-size: $im-font-size;
	}

	.tip {
		width: 100%;
		flex: 1;
	}
}
</style>

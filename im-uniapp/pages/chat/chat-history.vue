<template>
	<view class="page chat-history">
		<nav-bar title="聊天记录" back></nav-bar>
		<view class="nav-bar" id="navBar">
			<view class="nav-search">
				<uni-search-bar radius="100" v-model="searchText" placeholder="搜索聊天记录"
					cancelButton="none"></uni-search-bar>
			</view>
		</view>
		<view v-if="!searchText">
			<view class="search-tip">快速搜索聊天内容</view>
			<view class="search-tabs">
				<view class="search-tab" @click="onClickFileTab">
					<view class="search-icon iconfont icon-doc"></view>
					<view class="search-name">文件</view>
				</view>
				<view class="search-tab" @click="onClickImageTab">
					<view class="search-icon iconfont icon-image"></view>
					<view class="search-name">图片</view>
				</view>
			</view>
		</view>
		<scroll-view v-else-if="showMessages.length>0" class="chat-message-list" :style="{height: scrollbarHeight+'px'}"
			scroll-y="true" upper-threshold="200" @scrolltolower="onScrollToBottom">
			<view v-for="(m, idx) in showMessages" :key="m.localId">
				<chat-history-item :headImage="headImage(m)" :showName="showName(m)"
					:message="m" @tap="onClickMessageItem(m)">
				</chat-history-item>
			</view>
		</scroll-view>
		<no-data-tip v-else class="tip" :tip="'未搜索到与\'' + searchText + '\'相关的内容'"></no-data-tip>
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
			navBarHeight: 0
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
			}, 50)
		},
		onClickFileTab() {
			uni.navigateTo({
				url: `/pages/chat/chat-history-file?convKey=${this.conversation.key}`
			})
		},
		onClickImageTab() {
			uni.navigateTo({
				url: `/pages/chat/chat-history-image?convKey=${this.conversation.key}`
			})
		},
		onScrollToBottom() {
			this.showMaxIdx += 20;
		},
		headImage(message) {
			if (!message) return "";
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
		},
		filterInvalidMessage(localMessages) {
			// 排除已经删除或撤回的消息
			return localMessages.filter(m => !m.deleted && m.status != this.$enums.MESSAGE_STATUS.RECALL &&
				m.type != this.$enums.MESSAGE_TYPE.RECALL);
		}
	},
	computed: {
		mine() {
			return userStore.userInfo;
		},
		showMessages() {
			return this.messages.filter(m => {
				// 只有文字和文件支持检索
				if (this.$enums.MESSAGE_TYPE.TEXT == m.type) {
					return m.content.toLowerCase().includes(this.searchText.toLowerCase())
				} else if (this.$enums.MESSAGE_TYPE.FILE == m.type) {
					return JSON.parse(m.content).name.toLowerCase().includes(this.searchText.toLowerCase());
				}
				return false;
			}).reverse().slice(0, this.showMaxIdx);
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
		isGroup() {
			return this.conversation && this.conversation.type == this.$enums.CONVERSATION_TYPE.GROUP;
		}
	},
	async onLoad(options) {
		this.conversation = chatStore.conversationMap.get(options.convKey);
		const messages = await this.$db.findRecentMessagesByConvKey(this.conversation.key, 50000);
		this.messages = this.filterInvalidMessage(messages);
		if (this.isGroup) {
			const members = groupStore.findGroup(this.conversation.targetId).members;
			this.groupMemberMap = new Map(members.map(m => [m.userId, m]));
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
.chat-history {
	position: relative;
	display: flex;
	flex-direction: column;
	background: white;

	.search-tip {
		margin-top: 100rpx;
		color: $im-text-color-lighter;
		text-align: center;
		padding: 20rpx;
		font-size: $im-font-size-smaller;
	}

	.search-tabs {
		padding: 20rpx 80rpx;
		display: flex;
		justify-content: center;

		.search-tab {
			padding: 30rpx 80rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			color: $im-text-color-light;

			.search-icon {
				font-size: 50rpx;
			}

			.search-name {
				margin-top: 5rpx;
				font-size: $im-font-size-smaller;
			}
		}
	}

	.chat-message-list {
		flex: 1;
		height: 100%;
	}

	.tip {
		width: 100%;
		flex: 1;
	}
}
</style>

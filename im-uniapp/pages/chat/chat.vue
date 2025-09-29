<template>
	<view class="tab-page">
		<nav-bar search @search="onSearch()">消息</nav-bar>
		<view v-if="loading" class="chat-loading">
			<loading :size="50" :mask="false">
				<view>消息接收中...</view>
			</loading>
		</view>
		<view v-else-if="initializing" class="chat-loading">
			<loading :size="50" :mask="false">
				<view>正在初始化...</view>
			</loading>
		</view>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar focus="true" radius="100" v-model="searchText" cancelButton="none"
					placeholder="搜索"></uni-search-bar>
			</view>
		</view>
		<view class="chat-tip" v-if="!initializing && !loading && chatStore.chats.length == 0">
			<view class="tip-icon">
				<text class="iconfont icon-chat"></text>
			</view>
			<view class="tip-title">还没有聊天</view>
			<view class="tip-content">添加好友或创建群聊，开始精彩的对话吧</view>
		</view>
		<scroll-view class="scroll-bar" v-else scroll-with-animation="true" scroll-y="true">
			<view v-for="(chat, index) in chatStore.chats" :key="index">
				<long-press-menu v-if="isShowChat(chat)" :items="menu.items" @select="onSelectMenu($event, index)">
					<chat-item :chat="chat" :index="index" :active="menu.chatIdx == index"></chat-item>
				</long-press-menu>
			</view>
		</scroll-view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			showSearch: false,
			searchText: "",
			menu: {
				show: false,
				style: "",
				chatIdx: -1,
				isTouchMove: false,
				items: [{
						key: 'DELETE',
						name: '删除该聊天',
						icon: 'trash',
						color: '#e64e4e'
					},
					{
						key: 'TOP',
						name: '置顶该聊天',
						icon: 'arrow-up'
					}
				]
			}
		}
	},
	methods: {
		onSelectMenu(item, chatIdx) {
			switch (item.key) {
				case 'DELETE':
					this.removeChat(chatIdx);
					break;
				case 'TOP':
					this.moveToTop(chatIdx);
					break;
				default:
					break;
			}
			this.menu.show = false;
		},
		removeChat(chatIdx) {
			this.chatStore.removeChat(chatIdx);
		},
		moveToTop(chatIdx) {
			this.chatStore.moveTop(chatIdx);
		},
		isShowChat(chat) {
			if (chat.delete) {
				return false;
			}
			return !this.searchText || chat.showName.includes(this.searchText)
		},
		onSearch() {
			this.showSearch = !this.showSearch;
			this.searchText = "";
		},
		refreshUnreadBadge() {
			if (this.unreadCount > 0) {
				uni.setTabBarBadge({
					index: 0,
					text: this.unreadCount + ""
				})
			} else {
				uni.removeTabBarBadge({
					index: 0,
					complete: () => {}
				})
			}
		}
	},
	computed: {
		unreadCount() {
			let count = 0;
			this.chatStore.chats.forEach(chat => {
				if (!chat.isDnd && !chat.delete) {
					count += chat.unreadCount;
				}
			})
			return count;
		},
		loading() {
			return this.chatStore.loading;
		},
		initializing() {
			return !this.configStore.appInit;
		},
		showChats() {
			this.chatStore.chats.filter((chat) => !chat.delete && chat.showName && chat.showName.includes(this
				.searchText))
		}
	},
	watch: {
		unreadCount(newCount, oldCount) {
			this.refreshUnreadBadge();
		}
	},
	onShow() {
		this.refreshUnreadBadge();
	}
}
</script>

<style lang="scss">
.tab-page {
	position: relative;
	display: flex;
	flex-direction: column;

	.chat-tip {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 40rpx;
		text-align: center;
		width: 80%;
		
		.tip-icon {
			width: 120rpx;
			height: 120rpx;
			background: linear-gradient(135deg, #f8f9fa, #e9ecef);
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 40rpx;
			box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
			border: 1rpx solid $im-bg-active;
			
			.iconfont {
				font-size: 60rpx;
				color: $im-text-color-lighter;
			}
		}

		.tip-title {
			font-size: $im-font-size-large;
			color: $im-text-color;
			font-weight: 500;
			margin-bottom: 20rpx;
		}

		.tip-content {
			font-size: $im-font-size-smaller;
			color: $im-text-color-lighter;
			line-height: 1.6;
			margin-bottom: 50rpx;
		}
	}

	.chat-loading {
		display: block;
		width: 100%;
		height: 120rpx;
		background: white;
		color: $im-text-color-lighter;

		.loading-box {
			position: relative;
		}
	}

	.scroll-bar {
		flex: 1;
		height: 100%;
	}
}
</style>
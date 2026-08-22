<template>
	<view class="tab-page">
		<nav-bar search @search="onSearch()">消息</nav-bar>
		<view v-if="loading" class="chat-loading">
			<custom-loading :size="50" :mask="false">
				<view>消息接收中...</view>
			</custom-loading>
		</view>
		<view v-else-if="reconnecting" class="chat-loading">
			<custom-loading :size="50" :mask="false">
				<view>连接已断开，正在重新连接...</view>
			</custom-loading>
		</view>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar focus="true" radius="100" v-model="searchText" cancelButton="none"
					placeholder="搜索"></uni-search-bar>
			</view>
		</view>
		<view class="chat-tip" v-if="!loading && !reconnecting && showConversations.length == 0">
			<view class="tip-icon">
				<text class="iconfont icon-chat"></text>
			</view>
			<view class="tip-title">还没有聊天</view>
			<view class="tip-content">添加好友或创建群聊，开始精彩的对话吧</view>
		</view>
		<scroll-view class="scroll-bar" v-else-if="!loading && !reconnecting" scroll-with-animation="true" scroll-y="true"
			@scrolltolower="onScrollToBottom">
			<long-press-menu ref="longPressMenu" @select="onSelectChatMenu">
				<view v-for="conv in showConversations" :key="conv.key">
					<chat-item :conversation="conv" @longpress.prevent="onChatLongPress($event, conv)"></chat-item>
				</view>
			</long-press-menu>
		</scroll-view>
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>

<script>
import { chatStore, friendStore, groupStore } from '@/store/stores.js'
export default {
	data() {
		return {
			showMaxIdx: 30,
			showSearch: false,
			searchText: "",
			menuConv: null,
		}
	},
	methods: {
		onScrollToBottom() {
			// 多显示一页数据
			if (this.showMaxIdx < chatStore.conversations.length) {
				this.showMaxIdx += 30
			}
		},
		onChatLongPress(e, conv) {
			// 屏幕移动时不弹出
			if (this.isTouchMove) {
				return;
			}
			this.menuConv = conv;
			this.menuTouch = e.touches[0];
			const menuItems = this.chatMenuItems(conv);
			this.$refs.longPressMenu.open(e, menuItems);
		},
		onSelectChatMenu(item) {
			switch (item.key) {
				case 'DELETE':
					this.removeChat(this.menuConv);
					break;
				case 'DND':
					this.onDnd(this.menuConv);
					break;
				case 'TOP':
					this.onTop(this.menuConv);
					break;
				default:
					break;
			}
		},
		removeChat(conv) {
			this.$refs.modal.open({
				title: '确认删除',
				content: `确认删除'${conv.showName}'的聊天记录?`,
				success: async () => {
					if (this.isPrivate(conv) || this.isGroup(conv)) {
						const data = { chatId: conv.targetId }
						const chatTypeText = this.isPrivate(conv) ? "private" : "group";
						await this.$http({
							url: `/message/${chatTypeText}/deleteChat`,
							method: 'delete',
							data: data
						});
					}
					await chatStore.remove(conv.key);
				}
			});
		},
		onDnd(conv) {
			if (this.isPrivate(conv)) {
				this.setFriendDnd(conv, conv.targetId, !conv.isDnd)
			} else {
				this.setGroupDnd(conv, conv.targetId, !conv.isDnd)
			}
		},
		setFriendDnd(conv, friendId, isDnd) {
			const formData = {
				friendId: friendId,
				isDnd: isDnd
			}
			this.$http({
				url: '/friend/dnd',
				method: 'put',
				data: formData
			}).then(() => {
				friendStore.setDnd(friendId, isDnd)
				chatStore.setDnd(conv.key, isDnd)
			})
		},
		setGroupDnd(conv, groupId, isDnd) {
			const formData = {
				groupId: groupId,
				isDnd: isDnd
			}
			this.$http({
				url: '/group/dnd',
				method: 'put',
				data: formData
			}).then(() => {
				groupStore.setDnd(groupId, isDnd)
				chatStore.setDnd(conv.key, isDnd)
			})
		},
		onTop(conv) {
			if (this.isPrivate(conv)) {
				this.setFriendTop(conv, conv.targetId, !conv.isTop)
			} else if (this.isGroup(conv)) {
				this.setGroupTop(conv, conv.targetId, !conv.isTop)
			} else {
				chatStore.setTop(conv.key, !conv.isTop)
			}
		},
		setFriendTop(conv, friendId, isTop) {
			const formData = {
				friendId: friendId,
				isTop: isTop
			}
			this.$http({
				url: '/friend/top',
				method: 'put',
				data: formData
			}).then(() => {
				friendStore.setTop(friendId, isTop)
				chatStore.setTop(conv.key, isTop)
			})
		},
		setGroupTop(conv, groupId, isTop) {
			const formData = {
				groupId: groupId,
				isTop: isTop
			}
			this.$http({
				url: '/group/top',
				method: 'put',
				data: formData
			}).then(() => {
				groupStore.setTop(groupId, isTop)
				chatStore.setTop(conv.key, isTop)
			})
		},
		isShow(conv) {
			return !this.searchText || conv.showName.includes(this.searchText)
		},
		onSearch() {
			this.showSearch = !this.showSearch;
			this.searchText = "";
		},
		chatMenuItems(conv) {
			let items = [];
			items.push({
				key: 'DELETE',
				name: '删除该聊天',
				danger: true
			})
			items.push({
				key: 'TOP',
				name: conv.isTop ? '取消置顶' : '置顶该聊天',
			})
			if (conv.isDnd) {
				items.push({
					key: 'DND',
					name: '新消息提醒'
				})
			} else {
				items.push({
					key: 'DND',
					name: '消息免打扰'
				})
			}
			return items;
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
		},
		isPrivate(conv) {
			return this.$enums.CONVERSATION_TYPE.PRIVATE == conv.type
		},
		isGroup(conv) {
			return this.$enums.CONVERSATION_TYPE.GROUP == conv.type
		},
	},
	computed: {
		unreadCount() {
			if (!chatStore) {
				return 0;
			}
			let unreadCount = 0;
			const conversations = chatStore.conversations;
			conversations.forEach((conv) => {
				if (!conv.isDnd) {
					unreadCount += conv.unreadCount
				}
			})
			return unreadCount;
		},
		loading() {
			return chatStore && chatStore.loading;
		},
		reconnecting() {
			return getApp().$vm.reconnecting;
		},
		showConversations() {
			if (!chatStore) {
				return [];
			}
			return chatStore.conversations.filter(conv => this.isShow(conv)).slice(0, this.showMaxIdx);
		},
	},
	watch: {
		unreadCount(newCount, oldCount) {
			this.refreshUnreadBadge();
		}
	},
	onShow() {
		// 防止小程序通过home键在未登录的情况直接进入首页
		const loginInfo = uni.getStorageSync("loginInfo");
		if (!loginInfo || !loginInfo.accessToken) {
			uni.reLaunch({ url: '/pages/login/login' })
			return
		}
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

<template>
	<view class="tab-page">

		<view v-if="loading" class="chat-loading">
			<loading :size="50" :mask="false">
				<view>消息接收中...</view>
			</loading>
		</view>
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar radius="100" v-model="searchText" cancelButton="none" placeholder="搜索"></uni-search-bar>
			</view>
		</view>
		<view class="chat-tip" v-if="!loading && chatStore.chats.length==0">
			温馨提示：您现在还没有任何聊天消息，快跟您的好友发起聊天吧~
		</view>
		<scroll-view class="scroll-bar" v-else scroll-with-animation="true" scroll-y="true">
			<view v-for="(chatPos,i) in chatsPos" :key="i">
				<pop-menu v-if="isShowChat(chatStore.chats[chatPos.idx])" :items="menu.items"
					@select="onSelectMenu($event,chatPos.idx)">
					<chat-item  :chat="chatStore.chats[chatPos.idx]"
						:active="menu.chatIdx==chatPos.idx" :index="chatPos.idx"></chat-item>
				</pop-menu>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
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
			onSelectMenu(item,chatIdx) {
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
				this.$store.commit("removeChat", chatIdx);
			},
			moveToTop(chatIdx) {
				this.$store.commit("moveTop", chatIdx);
			},
			isShowChat(chat){
				if(chat.delete){
					return false;
				}
				return !this.searchText || chat.showName.includes(this.searchText)
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
			chatsPos() {
				// 计算会话的顺序
				let chatsPos = [];
				let chats = this.chatStore.chats;
				chats.forEach((chat, idx) => {
					chatsPos.push({
						idx: idx,
						sendTime: chat.lastSendTime
					})
				})
				chatsPos.sort((chatPos1, chatPos2) => {
					return chatPos2.sendTime - chatPos1.sendTime;
				});
				return chatsPos;
			},
			chatStore() {
				return this.$store.state.chatStore;
			},
			unreadCount() {
				let count = 0;
				this.chatStore.chats.forEach(chat => {
					if (!chat.delete) {
						count += chat.unreadCount;
					}
				})
				return count;
			},
			loading() {
				return this.chatStore.loadingGroupMsg || this.chatStore.loadingPrivateMsg
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

<style scoped lang="scss">
	.tab-page {
		position: relative;
		border: #dddddd solid 1px;
		display: flex;
		flex-direction: column;
		

		.nav-bar {
			padding: 2rpx 20rpx;
			display: flex;
			align-items: center;
			background-color: white;
			border-bottom: 1px solid #ddd;
			height: 110rpx;
			.nav-search {
				flex: 1;
				height: 110rpx;
			}
		
			
		}
		
		.chat-tip {
			position: absolute;
			top: 400rpx;
			padding: 50rpx;
			line-height: 50rpx;
			text-align: left;
			color: darkblue;
			font-size: 30rpx;
		}

		.chat-loading {
			display: block;
			width: 100%;
			height: 100rpx;
			background: white;
			position: relative;
			color: blue;

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
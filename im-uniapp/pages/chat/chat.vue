<template>
	<view class="tab-page">
		<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
			<view v-for="(chat,index) in $store.state.chatStore.chats" :key="index">
				<chat-item :chat="chat" :index="index" @longpress.native="onShowMenu($event,index)"></chat-item>
			</view>
		</scroll-view>
		<pop-menu v-show="menu.show" :menu-style="menu.style" :items="menu.items" @close="menu.show=false"
			@select="onSelectMenu"></pop-menu>
	</view>
</template>

<script>
	export default {
		data() {
			return {

				menu: {
					show: false,
					style: "",
					chatIdx: -1,
					items: [{
							key: 'DELETE',
							name: '删除',
							icon: 'trash'
						},
						{
							key: 'TOP',
							name: '置顶',
							icon: 'arrow-up'
						}
					]
				}
			}
		},
		methods: {
			onSelectMenu(item) {
				switch (item.key) {
					case 'DELETE':
						this.removeChat(this.menu.chatIdx); 
					case 'TOP':
						this.moveToTop(this.menu.chatIdx);
					default:
						break;
				}
			},
			onShowMenu(e,chatIdx) {
				uni.getSystemInfo({
					success: (res) => {
						let touches = e.touches[0];
						let style = "";
						/* 因 非H5端不兼容 style 属性绑定 Object ，所以拼接字符 */
						if (touches.clientY > (res.windowHeight / 2)) {
							style = `bottom:${res.windowHeight-touches.clientY}px;`;
						} else {
							style = `top:${touches.clientY}px;`;
						}
						if (touches.clientX > (res.windowWidth / 2)) {
							style += `right:${res.windowWidth-touches.clientX}px;`;
						} else {
							style += `left:${touches.clientX}px;`;
						}
						this.menu.style = style;
						this.menu.chatIdx = chatIdx;
						// 
						this.$nextTick(() => {
							this.menu.show = true;
						});
					}
				})
			},
			removeChat(chatIdx) {
				this.$store.commit("removeChat", chatIdx);
			},
			moveToTop(chatIdx) {
				this.$store.commit("moveTop", chatIdx);
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
						complete:()=>{}
					})

				}
			}
		},
		computed: {
			unreadCount() {
				let count = 0;
				this.$store.state.chatStore.chats.forEach(chat => {
					count += chat.unreadCount;
				})
				return count;
			}
		},
		watch:{
			unreadCount(newCount,oldCount){
				this.refreshUnreadBadge();
			}
		},
		onShow() {
			this.refreshUnreadBadge();
		}
	}
</script>

<style>

</style>
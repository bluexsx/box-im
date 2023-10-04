<template>
	<view class="tab-page">
		<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
			<view v-for="(chat,index) in $store.state.chatStore.chats" :key="index">
				<chat-item :chat="chat" :index="index"></chat-item>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				
			}
		},
		methods: {
			refreshUnreadBadge(){
				if(this.unreadCount>0){
					uni.setTabBarBadge({
					  index: 0,
					  text: this.unreadCount+""
					})
				}else{
					uni.removeTabBarBadge({
						index:0
					})
					
				}
			}
		},
		computed:{
			unreadCount(){
				let count = 0;
				this.$store.state.chatStore.chats.forEach(chat =>{
					count += chat.unreadCount;
				})
				return count;
			}
		},
		onLoad() {
			this.refreshUnreadBadge();
		}
	}
</script>

<style>

</style>

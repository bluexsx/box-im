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
			
		}
	}
</script>

<style>

</style>

<template>
	<el-container class="chat-page">
		<el-aside width="280px" class="chat-list-box">
			<div class="chat-list-header">
				<el-input class="search-text" placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
			</div>
			<div class="chat-list-loading"  v-if="loading" v-loading="true" element-loading-text="消息接收中..."
				element-loading-spinner="el-icon-loading" element-loading-background="#eee">
				<div class="chat-loading-box"></div>
			</div>
			<el-scrollbar class="chat-list-items">
				<div v-for="(chat,index) in chatStore.chats" :key="index">
					<chat-item v-show="chat.showName.includes(searchText)" :chat="chat" :index="index"
						@click.native="onActiveItem(index)" @delete="onDelItem(index)" @top="onTop(index)"
						:active="chat === chatStore.activeChat"></chat-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="chat-box">
			<chat-box v-if="chatStore.activeChat" :chat="chatStore.activeChat"></chat-box>
		</el-container>
	</el-container>
</template>

<script>
	import ChatItem from "../components/chat/ChatItem.vue";
	import ChatBox from "../components/chat/ChatBox.vue";

	export default {
		name: "chat",
		components: {
			ChatItem,
			ChatBox
		},
		data() {
			return {
				searchText: "",
				messageContent: "",
				group: {},
				groupMembers: []
			}
		},
		methods: {
			onActiveItem(index) {
				this.$store.commit("activeChat", index);
			},
			onDelItem(index) {
				this.$store.commit("removeChat", index);
			},
			onTop(chatIdx) {
				this.$store.commit("moveTop", chatIdx);
			},
		},
		computed: {
			chatStore() {
				return this.$store.state.chatStore;
			},
			loading(){
				return this.chatStore.loadingGroupMsg || this.chatStore.loadingPrivateMsg
			}
		}
	}
</script>

<style lang="scss">
	.chat-page {
		.chat-list-box {
			display: flex;
			flex-direction: column;
			border-right: #53a0e79c solid 1px;	
			background: white;
			width: 3rem;

			.chat-list-header {
				padding: 3px 8px;
				line-height: 50px;
				border-bottom: 1px #ddd solid;
				
				.el-input__inner {
					border-radius: 10px !important;
					background-color: #F8F8F8;
				}
				
			}
			
			.chat-list-loading{
				height: 50px;
				background-color: #eee;
				
				.chat-loading-box{
					height: 100%;
				}
			}
			
			.chat-list-items {
				flex: 1;
				background: #F8F8F8;
				margin: 0 3px;
			}
		}
	}
</style>
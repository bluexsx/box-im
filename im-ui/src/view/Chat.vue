<template>
	<el-container class="chat-page">
		<el-aside width="260px" class="chat-list-box">
			<div class="chat-list-header">
				<el-input width="200px" placeholder="搜索" v-model="searchText">
					<el-button slot="append" icon="el-icon-search"></el-button>
				</el-input>
			</div>
			<div class="chat-list-loadding"  v-if="loading" v-loading="true" element-loading-text="消息接收中..."
				element-loading-spinner="el-icon-loading" element-loading-background="#eee">
				<div class="chat-loading-box"></div>
			</div>
			<el-scrollbar class="chat-list-items">
				<div v-for="(chat,index) in chatStore.chats" :key="index">
					<chat-item v-show="chat.showName.startsWith(searchText)" :chat="chat" :index="index"
						@click.native="onActiveItem(index)" @delete="onDelItem(index)" @top="onTop(index)"
						:active="index === chatStore.activeIndex"></chat-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="chat-box">
			<chat-box v-show="activeChat.targetId>0" :chat="activeChat"></chat-box>
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
			activeChat() {
				let index = this.chatStore.activeIndex;
				let chats = this.chatStore.chats
				if (index >= 0 && chats.length > 0) {
					return chats[index];
				}
				// 当没有激活任何会话时，创建一个空会话，不然控制台会有很多报错
				let emptyChat = {
					targetId: -1,
					showName: "",
					headImage: "",
					messages: []
				}
				return emptyChat;
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
			border: #dddddd solid 1px;
			background: white;
			width: 3rem;

			.chat-list-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
			}
			
			.chat-list-loadding{
				height: 50px;
				background-color: #eee;
				
				.chat-loading-box{
					height: 100%;
				}
			}
			
			.chat-list-items {
				flex: 1;
			}
		}
	}
</style>
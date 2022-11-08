<template>
	<el-container>
		<el-aside width="250px" class="l-chat-box">
			<div class="l-chat-header">
				<el-input width="200px" placeholder="搜索" v-model="searchText">
					<el-button slot="append" icon="el-icon-search"></el-button>
				</el-input>
			</div>
			<el-scrollbar class="l-chat-list" >
				<div v-for="(chat,index) in chatStore.chats" :key="chat.type+chat.targetId">
					<chat-item :chat="chat" :index="index" @click.native="handleActiveItem(index)" @del="handleDelItem(chat,index)"
					 :active="index === chatStore.activeIndex"></chat-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="r-chat-box">
			<chat-box :chat="activeChat"></chat-box>
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
			handleActiveItem(index) {
				this.$store.commit("activeChat", index);
			},
			handleDelItem(chat, index) {
				this.$store.commit("removeChat", index);
			}
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
			}
		}
	}
</script>

<style lang="scss">
	.el-container {
		.l-chat-box {
			display: flex;
			flex-direction: column;
			border: #dddddd solid 1px;
			background: white;
			width: 3rem;

			.l-chat-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
			}
			
			.l-friend-ist{
				flex: 1;
			}
		}
	}
</style>

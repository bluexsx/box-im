<template>
	<el-drawer title="聊天历史记录" size="700px" :visible.sync="visible" direction="rtl" :before-close="onClose">
		<div class="chat-history" v-loading="loading" element-loading-text="拼命加载中">
			<el-scrollbar class="scroll-box" ref="scrollbar" id="historyScrollbar">
				<ul>
					<li v-for="(msgInfo, idx) in messages" :key="idx">
						<chat-message-item :mode="2" :mine="msgInfo.sendId == mine.id" :headImage="headImage(msgInfo)"
							:showName="showName(msgInfo)" :msgInfo="msgInfo" :menu="false">
						</chat-message-item>
					</li>
				</ul>
			</el-scrollbar>
		</div>
	</el-drawer>
</template>

<script>
import ChatMessageItem from './ChatMessageItem.vue';

export default {
	name: 'chatHistory',
	components: {
		ChatMessageItem
	},
	props: {
		visible: {
			type: Boolean
		},
		chat: {
			type: Object
		},
		friend: {
			type: Object
		},
		group: {
			type: Object
		},
		groupMembers: {
			type: Array,
		}
	},
	data() {
		return {
			page: 1,
			size: 10,
			messages: [],
			loadAll: false,
			loading: false,
			lastScrollTime: new Date()
		}
	},
	methods: {
		onClose() {
			this.page = 1;
			this.messages = [];
			this.loadAll = false;
			this.$emit('close');
		},
		onScroll() {
			let high = this.$refs.scrollbar.$refs.wrap.scrollTop; //距离顶部的距离
			let timeDiff = new Date().getTime() - this.lastScrollTime.getTime();
			if (high < 30 && timeDiff > 500) {
				this.lastScrollTime = new Date();
				this.loadMessages();

			}
		},
		loadMessages() {
			if (this.loadAll) {
				return this.$message.success("已到达顶部");
			}
			let param = {
				page: this.page++,
				size: this.size
			}
			if (this.chat.type == 'GROUP') {
				param.groupId = this.group.id;
			} else {
				param.friendId = this.friend.id;
			}
			this.loading = true;
			this.$http({
				url: this.histroyAction,
				method: 'get',
				params: param
			}).then(messages => {
				messages.forEach(m => this.messages.unshift(m));
				this.loading = false;
				if (messages.length < this.size) {
					this.loadAll = true;
				}
				this.refreshScrollPos();
			}).catch(() => {
				this.loading = false;
			})
		},
		showName(msgInfo) {
			if (this.chat.type == 'GROUP') {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.showNickName : "";
			} else {
				return msgInfo.sendId == this.mine.id ? this.mine.nickName : this.chat.showName
			}
		},
		headImage(msgInfo) {
			if (this.chat.type == 'GROUP') {
				let member = this.groupMembers.find((m) => m.userId == msgInfo.sendId);
				return member ? member.headImage : "";
			} else {
				return msgInfo.sendId == this.mine.id ? this.mine.headImageThumb : this.chat.headImage
			}
		},
		refreshScrollPos() {
			let scrollWrap = this.$refs.scrollbar.$refs.wrap;
			let scrollHeight = scrollWrap.scrollHeight;
			let scrollTop = scrollWrap.scrollTop;
			this.$nextTick(() => {
				let offsetTop = scrollWrap.scrollHeight - scrollHeight;
				scrollWrap.scrollTop = scrollTop + offsetTop;
				// 滚动条没出来，继续加载
				if (scrollWrap.scrollHeight == scrollHeight) {
					this.loadMessages();
				}
			});
		}
	},
	computed: {
		mine() {
			return this.$store.state.userStore.userInfo;
		},
		histroyAction() {
			return `/message/${this.chat.type.toLowerCase()}/history`;
		}
	},
	watch: {
		visible: {
			handler(newValue, oldValue) {
				if (newValue) {
					this.loadMessages();
					this.$nextTick(() => {
						document.getElementById('historyScrollbar').addEventListener("mousewheel", this.onScroll, true);
					});
				}
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-history {
	display: flex;
	height: 100%;

	.scroll-box {
		flex: 1;

		.el-scrollbar__thumb {
			background-color: #555555;
		}

		ul {
			padding: 20px;

			li {
				list-style-type: none;
			}
		}
	}
}
</style>

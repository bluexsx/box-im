<template>
	<div class="chat-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="chat-left">
			<head-image :url="chat.headImage" :name="chat.showName" :size="42"
				:id="chat.type == 'PRIVATE' ? chat.targetId : 0" :isShowUserInfo="false" :online="online"></head-image>
			<div v-show="!chat.isDnd && chat.unreadCount > 0" class="unread-text">{{ chat.unreadCount }}</div>
		</div>
		<div class="chat-right">
			<div class="chat-name">
				<div class="chat-name-text">
					<div>{{ chat.showName }}</div>
					<el-tag v-if="chat.type == 'GROUP'" size="mini">群</el-tag>
				</div>
				<div class="chat-time-text">{{ showTime }}</div>
			</div>
			<div class="chat-content">
				<div class="chat-at-text">{{ atText }}</div>
				<div class="chat-send-name" v-show="isShowSendName">{{ chat.sendNickName + ':&nbsp;' }}</div>
				<div class="chat-content-text"
					v-html="$emo.transform($str.html2Escape(chat.lastContent), 'emoji-small')"></div>
				<div class="icon iconfont icon-dnd" v-if="chat.isDnd"></div>
			</div>
		</div>
		<right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
	</div>

</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import RightMenu from '../common/RightMenu.vue';

export default {
	name: "chatItem",
	components: {
		HeadImage,
		RightMenu
	},
	data() {
		return {
		}
	},
	props: {
		chat: {
			type: Object
		},
		active: {
			type: Boolean
		}
	},
	methods: {
		showRightMenu(e) {
			this.$refs.rightMenu.open(e, this.menuItems);
		},
		onSelectMenu(item) {
			this.$emit(item.key.toLowerCase(), this.msgInfo);
		}
	},
	computed: {
		isShowSendName() {
			if (!this.chat.sendNickName) {
				return false;
			}
			let size = this.chat.messages.length;
			if (size == 0) {
				return false;
			}
			// 只有群聊的普通消息需要显示名称
			let lastMsg = this.chat.messages[size - 1];
			return this.$msgType.isNormal(lastMsg.type)
		},
		showTime() {
			return this.$date.toTimeText(this.chat.lastSendTime, true)
		},
		atText() {
			if (this.chat.atMe) {
				return "[有人@我]"
			} else if (this.chat.atAll) {
				return "[@全体成员]"
			}
			return "";
		},
		menuItems() {
			let items = [];
			items.push({
				key: 'TOP',
				name: '置顶'
			});
			if (this.chat.isDnd) {
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
			items.push({
				key: 'DELETE',
				name: '删除聊天',
				color: '#F56C6C'
			})
			return items;
		},
		online() {
			if (this.chat.type == 'PRIVATE') {
				let friend = this.friendStore.findFriend(this.chat.targetId);
				return friend && friend.online;
			}
			return false;
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-item {
	height: 50px;
	display: flex;
	position: relative;
	padding: 5px 10px;
	align-items: center;
	background-color: var(--im-background);
	white-space: nowrap;
	cursor: pointer;

	&:hover {
		background-color: var(--im-background-active);
	}

	&.active {
		background-color: var(--im-background-active-dark);
	}

	.chat-left {
		position: relative;
		display: flex;
		justify-content: center;
		align-items: center;

		.unread-text {
			position: absolute;
			background-color: #f56c6c;
			right: -4px;
			top: -8px;
			color: white;
			border-radius: 30px;
			padding: 1px 5px;
			font-size: 10px;
			text-align: center;
			white-space: nowrap;
			border: 1px solid #f1e5e5;
		}
	}


	.chat-right {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding-left: 10px;
		text-align: left;
		overflow: hidden;

		.chat-name {
			display: flex;
			line-height: 20px;
			height: 20px;

			.chat-name-text {
				flex: 1;
				display: flex;
				align-items: center;
				font-size: var(--im-font-size);
				white-space: nowrap;
				overflow: hidden;

				.el-tag {
					min-width: 22px;
					text-align: center;
					border-radius: 10px;
					border: 0;
					height: 16px;
				}
			}

			.chat-time-text {
				font-size: var(--im-font-size-smaller);
				text-align: right;
				color: var(--im-text-color-light);
				white-space: nowrap;
				overflow: hidden;
				padding-left: 10px;
			}
		}

		.chat-content {
			display: flex;
			line-height: 22px;

			.chat-at-text {
				color: #c70b0b;
				font-size: var(--im-font-size-smaller);
			}

			.chat-send-name {
				font-size: var(--im-font-size-small);
				color: var(--im-text-color-light);
			}

			.chat-content-text {
				flex: 1;
				white-space: nowrap;
				overflow: hidden;
				text-overflow: ellipsis;
				font-size: var(--im-font-size-small);
				color: var(--im-text-color-light);
			}

			.icon {
				color: var(--im-text-color-light);
			}
		}
	}
}
</style>

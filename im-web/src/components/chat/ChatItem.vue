<template>
	<div class="chat-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="chat-left">
			<head-image :url="conversation.headImage" :name="conversation.showName" :size="42"
				:id="isPrivate ? conversation.targetId : 0" :isShowUserInfo="false" :online="online"></head-image>
			<div v-show="!conversation.isDnd && conversation.unreadCount > 0" class="unread-text">{{
				conversation.unreadCount }}</div>
		</div>
		<div class="chat-right">
			<div class="chat-name">
				<div class="chat-name-text">
					<div>{{ conversation.showName }}</div>
					<el-tag v-if="isGroup" type="primary" size="mini">群</el-tag>
					<el-tag v-if="conversation.isTop" type="warning" size="mini">置顶</el-tag>
				</div>
				<div class="chat-time-text">{{ showTime }}</div>
			</div>
			<div class="chat-content">
				<div class="chat-at-text">{{ atText }}</div>
				<div class="chat-send-name" v-show="isShowSendName">{{ conversation.sendNickName + ':&nbsp;' }}</div>
				<div class="chat-content-text"
					v-html="$emo.transform($str.html2Escape(conversation.lastContent), 'emoji-small')"></div>
				<div class="icon iconfont icon-dnd" v-if="conversation.isDnd"></div>
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
		conversation: {
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
			this.$emit(item.key.toLowerCase(), this.conversation);
		}
	},
	computed: {
		isShowSendName() {
			return !!this.conversation.sendNickName;
		},
		showTime() {
			return this.$date.toTimeText(this.conversation.lastSendTime, true)
		},
		atText() {
			if (this.conversation.atMe) {
				return "[有人@我]"
			} else if (this.conversation.atAll) {
				return "[@全体成员]"
			}
			return "";
		},
		menuItems() {
			let items = [];
			items.push({
				key: 'TOP',
				name: this.conversation.isTop ? '取消置顶' : '置顶'
			});
			if (this.conversation.isDnd) {
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
				danger: true
			})
			return items;
		},
		online() {
			if (this.isPrivate) {
				let friend = this.friendStore.findFriend(this.conversation.targetId);
				return friend && friend.online;
			}
			return false;
		},
		isPrivate() {
			return this.$enums.CONVERSATION_TYPE.PRIVATE == this.conversation.type
		},
		isGroup() {
			return this.$enums.CONVERSATION_TYPE.GROUP == this.conversation.type
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-item {
	height: 56px;
	display: flex;
	position: relative;
	margin: 0 3px;
	padding: 5px 8px;
	align-items: center;
	background-color: var(--im-background);
	white-space: nowrap;
	cursor: pointer;
	border-radius: 10px;

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
			line-height: 26px;
			height: 26px;

			.chat-name-text {
				flex: 1;
				display: flex;
				align-items: center;
				font-size: var(--im-font-size);
				white-space: nowrap;
				overflow: hidden;
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
			line-height: 20px;
			height: 20px;
			martin-top: 3px;

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

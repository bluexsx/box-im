<template>
	<div v-show="show">
		<div class="chat-group-readed-mask" @click.self="close()">
			<div class="chat-group-readed" :style="{ 'left': pos.x + 'px', 'top': pos.y + 'px' }" @click.prevent="">
				<el-tabs type="border-card" :stretch="true">
					<el-tab-pane :label="`已读(${readedMembers.length})`">
						<virtual-scroller class="scroll-box" :items="readedMembers">
							<template v-slot="{ item }">
								<chat-group-member :member="item"></chat-group-member>
							</template>
						</virtual-scroller>
					</el-tab-pane>
					<el-tab-pane :label="`未读(${unreadMembers.length})`">
						<virtual-scroller class="scroll-box" :items="unreadMembers">
							<template v-slot="{ item }">
								<chat-group-member :member="item"></chat-group-member>
							</template>
						</virtual-scroller>
					</el-tab-pane>
				</el-tabs>
				<div v-show="message.selfSend" class="arrow-right" :style="{ 'top': pos.arrowY + 'px' }">
					<div class="arrow-right-inner">
					</div>
				</div>
				<div v-show="!message.selfSend" class="arrow-left" :style="{ 'top': pos.arrowY + 'px' }">
					<div class="arrow-left-inner">
					</div>
				</div>
			</div>
		</div>

	</div>
</template>


<script>
import VirtualScroller from '../common/VirtualScroller.vue';
import ChatGroupMember from "./ChatGroupMember.vue";

export default {
	name: "chatGroupReaded",
	components: {
		ChatGroupMember, VirtualScroller
	},
	data() {
		return {
			show: false,
			pos: {
				x: 0,
				y: 0,
				arrowY: 0
			},
			readedMembers: [],
			unreadMembers: []
		}
	},
	props: {
		group: {
			type: Object
		},
		message: {
			type: Object
		}
	},
	methods: {
		close() {
			this.show = false;
		},
		open(rect) {
			this.show = true;
			this.pos.arrowY = 200;
			// 计算窗口位置
			if (this.message.selfSend) {
				// 自己发的消息弹出在消息的左边
				this.pos.x = rect.left - 310;
			} else {
				// 别人发的消息弹窗在消息右边
				this.pos.x = rect.right + 20;
			}
			this.pos.y = rect.top + rect.height / 2 - 215;
			// 防止窗口溢出
			if (this.pos.y < 0) {
				this.pos.arrowY += this.pos.y
				this.pos.y = 0;
			}
			this.loadReadedUser()
		},
		async loadReadedUser() {
			if (!this.message.id) {
				return;
			}
			const userIds = await this.$http({
				url: "/message/group/findReadedUsers",
				method: 'get',
				params: { groupId: this.message.groupId, messageId: this.message.id }
			})
			this.readedMembers = [];
			this.unreadMembers = [];
			this.groupMembers.forEach(member => {
				// 发送者和已退群的不显示
				if (member.userId == this.message.sendId || member.quit) {
					return;
				}
				// 区分已读还是未读
				if (userIds.find(userId => member.userId == userId)) {
					this.readedMembers.push(member);
				} else {
					this.unreadMembers.push(member);
				}
			})
			// 更新已读人数
			const message = {
				localId: this.message.localId,
				readedCount: this.readedMembers.length
			}
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, this.message.groupId)
			if (this.chatStore.isActive(convKey)) {
				this.chatStore.updateMessage(convKey, message)
			}
		}
	},
	computed: {
		groupMembers() {
			return this.group.members;
		}
	},
}
</script>

<style lang="scss" scoped>
.chat-group-readed-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	width: 100%;
	height: 100%;
	z-index: 9999;
}

.chat-group-readed {
	position: fixed;
	width: 300px;

	.scroll-box {
		height: 400px;
	}

	.arrow-left {
		position: absolute;
		left: -15px;
		width: 0;
		height: 0;
		border-top: 15px solid transparent;
		border-bottom: 15px solid transparent;
		border-right: 15px solid #ccc;

		.arrow-left-inner {
			position: absolute;
			top: -12px;
			left: 3px;
			width: 0;
			height: 0;
			overflow: hidden;
			border-top: 12px solid transparent;
			border-bottom: 12px solid transparent;
			border-right: 12px solid white;
		}
	}

	.arrow-right {
		position: absolute;
		right: -15px;
		width: 0;
		height: 0;
		border-top: 15px solid transparent;
		border-bottom: 15px solid transparent;
		border-left: 15px solid #ccc;

		.arrow-right-inner {
			position: absolute;
			top: -12px;
			right: 3px;
			width: 0;
			height: 0;
			overflow: hidden;
			border-top: 12px solid transparent;
			border-bottom: 12px solid transparent;
			border-left: 12px solid white;
		}
	}
}
</style>
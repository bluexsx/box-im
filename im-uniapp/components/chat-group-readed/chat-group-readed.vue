<template>
	<uni-popup ref="popup" type="bottom">
		<view class="chat-group-readed">
			<view class="uni-padding-wrap uni-common-mt">
				<uni-segmented-control :current="current" :values="segmentItems" style-type="button" @clickItem="onClickItem" />
			</view>
			<view class="content">
				<view v-if="current === 0">
					<virtual-scroller :items="readedMembers">
						<template v-slot="{ item }">
							<view class="member-item">
								<head-image :name="item.showNickName" :online="item.online" :url="item.headImage"
									:size="90"></head-image>
								<view class="member-name">{{ item.showNickName }}</view>
							</view>
						</template>
					</virtual-scroller>
				</view>
				<view v-if="current === 1">
					<virtual-scroller :items="unreadMembers">
						<template v-slot="{ item }">
							<view class="member-item">
								<head-image :name="item.showNickName" :online="item.online" :url="item.headImage"
									:size="90"></head-image>
								<view class="member-name">{{ item.showNickName }}</view>
							</view>
						</template>
					</virtual-scroller>
				</view>
			</view>
		</view>
	</uni-popup>
</template>

<script>
export default {
	name: "chat-group-readed",
	data() {
		return {
			message: {},
			groupMembers: [],
			current: 0,
			readedMembers: [],
			unreadMembers: []
		};
	},
	methods: {
		open(message, groupMembers) {
			this.message = message;
			this.groupMembers = groupMembers;
			this.$refs.popup.open();
			this.loadReadedUser();
		},
		async loadReadedUser() {
			if (!this.message.id) {
				return;
			}
			this.readedMembers = [];
			this.unreadMembers = [];
			const userIds = await this.$http({
				url: `/message/group/findReadedUsers?groupId=${this.message.groupId}&messageId=${this.message.id}`
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
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, this.message.groupId)
			this.message.readedCount = this.readedMembers.length;
			if (this.chatStore.isActive(convKey)) {
				this.chatStore.updateMessage(convKey, this.message)
			}
		},
		onClickItem(e) {
			this.current = e.currentIndex;
		}
	},
	computed: {
		segmentItems() {
			return [
				`已读(${this.readedMembers.length })`,
				`未读(${this.unreadMembers.length })`
			];
		}
	}
}

</script>

<style lang="scss" scoped>
.chat-group-readed {
	position: relative;
	display: flex;
	flex-direction: column;
	background-color: white;
	padding: 10rpx;

	.member-item {
		height: 120rpx;
		display: flex;
		position: relative;
		padding: 0 30rpx;
		align-items: center;
		background-color: white;
		white-space: nowrap;

		.member-name {
			flex: 1;
			padding-left: 20rpx;
			font-size: 30rpx;
			font-weight: 600;
			line-height: 60rpx;
			white-space: nowrap;
			overflow: hidden;
		}
	}
}

</style>

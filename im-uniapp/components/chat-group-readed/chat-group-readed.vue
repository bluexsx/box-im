<template>
	<uni-popup ref="popup" type="bottom">
		<view class="chat-group-readed">
			<view class="uni-padding-wrap uni-common-mt">
				<uni-segmented-control :current="current" :values="items" style-type="button"
					@clickItem="onClickItem" />
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
			items: ['已读', '未读'],
			current: 0,
			readedMembers: [],
			unreadMembers: []
		};
	},
	props: {
		msgInfo: {
			type: Object,
			required: true
		},
		groupMembers: {
			type: Array
		}
	},
	methods: {
		open() {
			this.$refs.popup.open();
			this.loadReadedUser();
		},
		loadReadedUser() {
			this.readedMembers = [];
			this.unreadMembers = [];
			this.$http({
				url: `/message/group/findReadedUsers?groupId=${this.msgInfo.groupId}&messageId=${this.msgInfo.id}`,
				method: 'Get'
			}).then(userIds => {
				this.groupMembers.forEach(member => {
					// 发送者和已退群的不显示
					if (member.userId == this.msgInfo.sendId || member.quit) {
						return;
					}
					// 区分已读还是未读
					if (userIds.find(userId => member.userId == userId)) {
						this.readedMembers.push(member);
					} else {
						this.unreadMembers.push(member);
					}
				})
				this.items[0] = `已读(${this.readedMembers.length})`;
				this.items[1] = `未读(${this.unreadMembers.length})`;

				let chatInfo = {
					type: 'GROUP',
					targetId: this.msgInfo.groupId
				}
				let msgInfo = {
					id: this.msgInfo.id,
					groupId: this.msgInfo.groupId,
					readedCount: this.readedMembers.length
				}
				// 更新已读人数
				this.chatStore.updateMessage(msgInfo, chatInfo)
			})
		},
		onClickItem(e) {
			this.current = e.currentIndex;
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
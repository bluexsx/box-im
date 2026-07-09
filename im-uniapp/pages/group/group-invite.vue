<template>
	<view class="page group-invite">
		<nav-bar :title="pageTitle" back></nav-bar>
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" cancelButton="none" clearButton="none"
					placeholder="输入好友昵称搜索"></uni-search-bar>
			</view>
		</view>
		<view class="friend-items">
			<virtual-scroller height="100%" :items="showFriends">
				<template v-slot="{ item }">
					<friend-item :friend="item" :detail="false" @tap="onSwitchChecked(item)">
						<radio @click.stop="onSwitchChecked(item)" :disabled="item.disabled" :checked="item.checked" />
					</friend-item>
				</template>
			</virtual-scroller>
		</view>
		<view class="btn-bar">
			<button class="btn" type="primary" :disabled="checkedSize == 0 || loading" :loading="loading"
				@click="onSubmit()">完成</button>
		</view>
	</view>
</template>

<script>
import { chatStore, friendStore, groupStore } from '@/store/stores.js'

export default {
	data() {
		return {
			mode: 'invite',
			groupId: null,
			searchText: "",
			loading: false,
			groupMembers: [],
			friendItems: [],
			maxSelectSize: 50
		}
	},
	methods: {
		onSubmit() {
			if (this.isCreate) {
				this.onCreateGroup();
			} else {
				this.onInviteFriends();
			}
		},
		onCreateGroup() {
			const userIds = this.friendItems.filter(f => f.checked).map(f => f.id);
			if (userIds.length === 0) {
				uni.showToast({
					title: '请至少选择1位好友',
					icon: 'none'
				});
				return;
			}
			this.loading = true;
			this.$http({
				url: "/group/new",
				method: 'POST',
				data: { userIds }
			}).then(async (group) => {
				groupStore.addGroup(group);
				await groupStore.refreshMember(group.id);
				const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, group.id);
				const chatInfo = {
					type: this.$enums.CONVERSATION_TYPE.GROUP,
					targetId: group.id,
					showName: group.showGroupName,
					headImage: group.headImageThumb,
					isDnd: group.isDnd
				};
				await chatStore.openChat(chatInfo);
				await chatStore.moveTop(convKey);
				uni.redirectTo({
					url: `/pages/chat/chat-box?convKey=${convKey}`
				});
			}).finally(() => {
				this.loading = false;
			});
		},
		onInviteFriends() {
			let inviteVo = {
				groupId: this.groupId,
				friendIds: []
			}
			this.friendItems.forEach(f => {
				if (f.checked && !f.disabled) {
					inviteVo.friendIds.push(f.id);
				}
			})
			if (inviteVo.friendIds.length > 0) {
				this.loading = true;
				this.$http({
					url: "/group/invite",
					method: 'POST',
					data: inviteVo
				}).then(() => {
					groupStore.refreshMember(this.groupId)
					uni.showToast({
						title: '邀请成功',
						icon: 'none'
					})
					setTimeout(() => uni.navigateBack(), 1000);
				}).finally(() => {
					this.loading = false;
				})
			}
		},
		onSwitchChecked(friend) {
			if (friend.disabled) {
				return;
			}
			if (!friend.checked && this.checkedSize >= this.maxSelectSize) {
				uni.showToast({
					title: `最多只能选择${this.maxSelectSize}位好友`,
					icon: 'none'
				});
				return;
			}
			friend.checked = !friend.checked;
		},
		initFriendItems() {
			this.friendItems = [];
			friendStore.friends.filter(f => !f.deleted).forEach(f => {
				let item = JSON.parse(JSON.stringify(f));
				if (this.isCreate) {
					item.checked = false;
				} else {
					item.disabled = this.isGroupMember(f.id);
					item.checked = item.disabled;
				}
				this.friendItems.push(item);
			})
		},
		isGroupMember(id) {
			return this.groupMembers.some(m => m.userId == id);
		}
	},
	computed: {
		isCreate() {
			return this.mode === 'create';
		},
		pageTitle() {
			return this.isCreate ? '发起群聊' : '邀请好友进群';
		},
		checkedSize() {
			return this.friendItems.filter(f => !f.disabled && f.checked).length;
		},
		showFriends() {
			return this.friendItems.filter(f => f.nickName.includes(this.searchText))
		}
	},
	onLoad(options) {
		this.mode = options.mode === 'create' ? 'create' : 'invite';
		if (this.isCreate) {
			this.initFriendItems();
			return;
		}
		this.groupId = parseInt(options.id);
		let group = groupStore.findGroup(this.groupId);
		this.groupMembers = group.members.filter(m => !m.quit);
		this.initFriendItems();
	}
}
</script>

<style lang="scss" scoped>
.group-invite {
	position: relative;
	display: flex;
	flex-direction: column;

	.friend-items {
		position: relative;
		flex: 1;
		overflow: hidden;
	}

	.btn-bar {
		position: fixed;
		bottom: 0;
		background: $im-bg;
		padding: 30rpx;
		box-sizing: border-box;
		width: 100%;
	}
}
</style>

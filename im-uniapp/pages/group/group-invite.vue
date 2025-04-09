<template>
	<view class="page group-invite">
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" cancelButton="none"
					placeholder="输入好友昵称搜索"></uni-search-bar>
			</view>
		</view>
		<view class="friend-items">
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="friend in friendItems" :key="friend.id">
					<view v-show="!searchText || friend.nickName.includes(searchText)" class="friend-item"
						@click="onSwitchChecked(friend)"
						:class="{ checked: friend.checked, disabled: friend.disabled }">
						<head-image :name="friend.nickName" :online="friend.online"
							:url="friend.headImage"></head-image>
						<view class="friend-name">{{ friend.nickName }}</view>
					</view>
				</view>
			</scroll-view>
		</view>
		<button class="bottom-btn" type="primary" :disabled="inviteSize == 0"
			@click="onInviteFriends()">邀请({{ inviteSize }}) </button>
	</view>
</template>

<script>
export default {
	data() {
		return {
			groupId: null,
			searchText: "",
			groupMembers: [],
			friendItems: []
		}
	},
	methods: {
		onInviteFriends() {
			let inviteVo = {
				groupId: this.groupId,
				friendIds: []
			}
			this.friendItems.forEach((f) => {
				if (f.checked && !f.disabled) {
					inviteVo.friendIds.push(f.id);
				}
			})
			if (inviteVo.friendIds.length > 0) {
				this.$http({
					url: "/group/invite",
					method: 'POST',
					data: inviteVo
				}).then(() => {
					uni.showToast({
						title: "邀请成功",
						icon: 'none'
					})
					setTimeout(() => {
						// 回退并刷新
						let pages = getCurrentPages();
						let prevPage = pages[pages.length - 2];
						prevPage.$vm.loadGroupMembers();
						uni.navigateBack();
					}, 1000);

				})
			}
		},
		onShowUserInfo(userId) {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + userId
			})
		},
		onSwitchChecked(friend) {
			if (!friend.disabled) {
				friend.checked = !friend.checked;
			}
		},
		initFriendItems() {
			this.friendItems = [];
			let friends = this.friendStore.friends;
			friends.filter(f => !f.deleted).forEach((f => {
				let item = {
					id: f.id,
					headImage: f.headImage,
					nickName: f.nickName,
					online: f.online
				}
				item.disabled = this.isGroupMember(f.id);
				item.checked = item.disabled;
				this.friendItems.push(item);
			}))
		},
		loadGroupMembers(id) {
			this.$http({
				url: `/group/members/${id}`,
				method: "GET"
			}).then((members) => {
				this.groupMembers = members.filter(m => !m.quit);
				this.initFriendItems();
			})
		},

		isGroupMember(id) {
			return this.groupMembers.some(m => m.userId == id);
		}
	},
	computed: {
		inviteSize() {
			return this.friendItems.filter(f => !f.disabled && f.checked).length;
		}
	},
	onLoad(options) {
		this.groupId = options.id;
		this.loadGroupMembers(options.id);
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

		.friend-item {
			height: 120rpx;
			display: flex;
			margin-bottom: 1rpx;
			position: relative;
			padding: 0 30rpx;
			align-items: center;
			background-color: white;
			white-space: nowrap;

			&.disabled {
				background-color: $im-bg-active !important;
			}

			&.checked {
				background-color: $im-color-primary-light-9;
			}

			.friend-name {
				flex: 1;
				padding-left: 20rpx;
				font-size: 30rpx;
				font-weight: 600;
				line-height: 60rpx;
				white-space: nowrap;
				overflow: hidden;
			}
		}

		.scroll-bar {
			height: 100%;
		}
	}
}
</style>
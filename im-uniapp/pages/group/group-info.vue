<template>
	<view class="page group-info">
		<nav-bar back>群聊信息</nav-bar>
		<view v-if="!group.quit" class="group-members">
			<view class="member-items">
				<view v-for="(member, idx) in groupMembers" :key="idx">
					<view class="member-item" v-if="idx < showMaxIdx">
						<head-image :id="member.userId" :name="member.showNickName" :url="member.headImage" size="small"
							:online="member.online"></head-image>
						<view class="member-name">
							<text>{{ member.showNickName }}</text>
						</view>
					</view>
				</view>
				<view class="member-item" @click="onInviteMember()">
					<view class="tools-btn">
						<uni-icons class="icon" type="plusempty" color="#888888"></uni-icons>
					</view>
					<view class="member-name">邀请</view>
				</view>
				<view v-if="isOwner" class="member-item" @click="onRemoveMember()">
					<view class="tools-btn">
						<text class="icon iconfont icon-remove"></text>
					</view>
					<view class="member-name">移除</view>
				</view>
			</view>
			<view class="member-more" @click="onShowMoreMmeber()">{{ `查看全部群成员${groupMembers.length}人` }}></view>
		</view>
		<view class="form">
			<view class="form-item">
				<view class="label">群聊名称</view>
				<view class="value">{{group.name}}</view>
			</view>
			<view class="form-item">
				<view class="label">群主</view>
				<view class="value">{{ownerName}}</view>
			</view>
			<view class="form-item">
				<view class="label">群名备注</view>
				<view class="value">{{group.remarkGroupName}}</view>
			</view>
			<view class="form-item">
				<view class="label">我在本群的昵称</view>
				<view class="value">{{group.showNickName}}</view>
			</view>
			<view v-if="group.notice" class="form-item">
				<view class="label">群公告</view>
			</view>
			<view v-if="group.notice" class="form-item">
				<uni-notice-bar :text="group.notice" />
			</view>
			<view v-if="!group.quit" class="group-edit" @click="onEditGroup()">修改群聊资料 > </view>
		</view>
		<bar-group v-if="!group.quit">
			<btn-bar type="primary" title="发送消息" @tap="onSendMessage()"></btn-bar>
			<btn-bar v-if="!isOwner" type="danger" title="退出群聊" @tap="onQuitGroup()"></btn-bar>
			<btn-bar v-if="isOwner" type="danger" title="解散群聊" @tap="onDissolveGroup()"></btn-bar>
		</bar-group>
		<group-member-selector ref="removeSelector" :members="groupMembers" :group="group"
			@complete="onRemoveComplete"></group-member-selector>
	</view>
</template>

<script>
export default {
	data() {
		return {
			groupId: null,
			group: {},
			groupMembers: []
		}
	},
	methods: {
		onFocusSearch() {},
		onInviteMember() {
			uni.navigateTo({
				url: `/pages/group/group-invite?id=${this.groupId}`
			})
		},
		onRemoveMember() {
			// 群主不显示
			let hideIds = [this.group.ownerId];
			this.$refs.removeSelector.init([], [], hideIds);
			this.$refs.removeSelector.open();
		},
		onRemoveComplete(userIds) {
			let data = {
				groupId: this.group.id,
				userIds: userIds
			}
			this.$http({
				url: "/group/members/remove",
				method: 'DELETE',
				data: data
			}).then(() => {
				this.loadGroupMembers();
				uni.showToast({
					title: `您移除了${userIds.length}位成员`,
					icon: 'none'
				})
			})
		},
		onShowMoreMmeber() {
			uni.navigateTo({
				url: `/pages/group/group-member?id=${this.groupId}`
			})
		},
		onEditGroup() {
			uni.navigateTo({
				url: `/pages/group/group-edit?id=${this.groupId}`
			})
		},
		onSendMessage() {
			let chat = {
				type: 'GROUP',
				targetId: this.group.id,
				showName: this.group.showGroupName,
				headImage: this.group.headImage,
			};
			this.chatStore.openChat(chat);
			let chatIdx = this.chatStore.findChatIdx(chat);
			uni.navigateTo({
				url: "/pages/chat/chat-box?chatIdx=" + chatIdx
			})
		},
		onQuitGroup() {
			uni.showModal({
				title: '确认退出?',
				content: `退出群聊后将不再接受群里的消息，确认退出吗?`,
				success: (res) => {
					if (res.cancel)
						return;
					this.$http({
						url: `/group/quit/${this.groupId}`,
						method: 'DELETE'
					}).then(() => {
						uni.showToast({
							title: `您退出了群聊'${this.group.name}'`,
							icon: "none"
						})
						setTimeout(() => {
							uni.switchTab({
								url: "/pages/group/group"
							});
							this.groupStore.removeGroup(this.groupId);
							this.chatStore.removeGroupChat(this
								.groupId);
						}, 1500)
					});
				}
			});
		},
		onDissolveGroup() {
			uni.showModal({
				title: '确认解散?',
				content: `确认要解散群聊'${this.group.name}'吗?`,
				success: (res) => {
					if (res.cancel)
						return;
					this.$http({
						url: `/group/delete/${this.groupId}`,
						method: 'delete'
					}).then(() => {
						uni.showToast({
							title: `您解散了群聊'${this.group.name}'`,
							icon: "none"
						})
						setTimeout(() => {
							uni.switchTab({
								url: "/pages/group/group"
							});
							this.groupStore.removeGroup(this.groupId);
							this.chatStore.removeGroupChat(this
								.groupId);
						}, 1500)
					});
				}
			});
		},
		loadGroupInfo() {
			this.$http({
				url: `/group/find/${this.groupId}`,
				method: 'GET'
			}).then((group) => {
				this.group = group;
				// 更新聊天页面的群聊信息
				this.chatStore.updateChatFromGroup(group);
				// 更新聊天列表的群聊信息
				this.groupStore.updateGroup(group);

			});
		},
		loadGroupMembers() {
			this.$http({
				url: `/group/members/${this.groupId}`,
				method: "GET"
			}).then((members) => {
				this.groupMembers = members.filter(m => !m.quit);
			})
		}
	},
	computed: {
		ownerName() {
			let member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
			return member && member.showNickName;
		},
		isOwner() {
			return this.group.ownerId == this.userStore.userInfo.id;
		},
		showMaxIdx() {
			return this.isOwner ? 8 : 9;
		}
	},
	onLoad(options) {
		this.groupId = options.id;
		// 查询群聊信息
		this.loadGroupInfo(options.id);
		// 查询群聊成员
		this.loadGroupMembers(options.id)
	}

}
</script>

<style lang="scss">
.group-info {
	.group-members {
		padding: 30rpx;
		background: white;

		.member-items {
			display: flex;
			flex-wrap: wrap;

			.member-item {
				width: 120rpx;
				display: flex;
				flex-direction: column;
				margin: 8rpx 2rpx;
				position: relative;
				align-items: center;
				padding-right: 5px;
				white-space: nowrap;

				.member-name {
					width: 100%;
					flex: 1;
					overflow: hidden;
					text-align: center;
					white-space: nowrap;
					padding-top: 8rpx;
					font-size: $im-font-size-smaller;
				}

				.tools-btn {
					display: flex;
					justify-content: center;
					align-items: center;
					border: $im-border solid 1rpx;
					border-radius: 10%;
					width: 80rpx;
					height: 80rpx;

					.icon {
						font-size: 40rpx !important;
						color: $im-text-color-lighter !important;
					}
				}
			}
		}

		.member-more {
			padding-top: 24rpx;
			text-align: center;
			font-size: $im-font-size-small;
			color: $im-text-color-lighter;
		}
	}

	.form {
		margin-top: 20rpx;

		.form-item {
			padding: 0 40rpx;
			display: flex;
			background: white;
			align-items: center;
			margin-top: 2rpx;

			.label {
				width: 220rpx;
				line-height: 100rpx;
				font-size: $im-font-size;
				white-space: nowrap;
			}

			.value {
				flex: 1;
				text-align: right;
				line-height: 100rpx;
				color: $im-text-color-lighter;
				font-size: $im-font-size-small;
				white-space: nowrap;
				overflow: hidden;
			}
		}

		.group-edit {
			padding: 10rpx 40rpx 30rpx 40rpx;
			text-align: center;
			background: white;
			font-size: $im-font-size-small;
			color: $im-text-color-lighter;
		}
	}
}
</style>
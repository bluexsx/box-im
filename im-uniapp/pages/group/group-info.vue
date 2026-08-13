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
							{{ member.showNickName }}
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
			<view v-if="group.notice" class="notice-text">
				{{group.notice}}
			</view>
			<view v-if="!group.quit" class="group-edit" @click="onEditGroup()">修改群聊资料 > </view>
		</view>
		<bar-group v-if="!group.quit">
			<switch-bar title="消息免打扰" :checked="group.isDnd" @change="onDndChange"></switch-bar>
		</bar-group>
		<bar-group v-if="isExistHistory">
			<arrow-bar title="查找聊天记录" @tap="onChatHistory()"></arrow-bar>
		</bar-group>
		<bar-group v-if="!group.quit && isExistHistory ">
			<arrow-bar title="清空聊天记录" @tap="onCleanMessage()"></arrow-bar>
		</bar-group>
		<bar-group v-if="!group.quit">
			<btn-bar type="primary" title="发送消息" @tap="onSendMessage()"></btn-bar>
			<btn-bar v-if="!isOwner" type="danger" title="退出群聊" @tap="onQuitGroup()"></btn-bar>
			<btn-bar v-if="isOwner" type="danger" title="解散群聊" @tap="onDissolveGroup()"></btn-bar>
		</bar-group>
		<group-member-selector ref="removeSelector" :members="groupMembers" :group="group"
			@complete="onRemoveComplete"></group-member-selector>
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>

<script>
import { chatStore, groupStore, userStore } from '@/store/stores.js'

export default {
	data() {
		return {
			groupId: null,
			group: {}
		}
	},
	methods: {
		onInviteMember() {
			uni.navigateTo({
				url: `/pages/group/group-invite?id=${this.groupId}`
			})
		},
		onRemoveMember() {
			// 群主不显示
			const hideIds = [this.group.ownerId];
			this.$refs.removeSelector.init([], [], hideIds);
			this.$refs.removeSelector.open();
		},
		onRemoveComplete(userIds) {
			const data = {
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
		async onSendMessage() {
			const chatInfo = {
				convKey: this.convKey,
				type: this.$enums.CONVERSATION_TYPE.GROUP,
				targetId: this.groupId,
				showName: this.group.showGroupName,
				headImage: this.group.headImageThumb,
				isDnd: this.group.isDnd
			};
			await chatStore.openChat(chatInfo);
			await chatStore.moveTop(this.convKey)
			uni.navigateTo({
				url: "/pages/chat/chat-box?convKey=" + this.convKey
			})
		},
		onQuitGroup() {
			const convKey = this.convKey;
			const groupId = this.groupId;
			const groupName = this.group.name;
			this.$refs.modal.open({
				title: '确认退出?',
				content: `退出群聊后将不再接受群里的消息，确认退出吗?`,
				success: async () => {
					await this.$http({
						url: `/group/quit/${groupId}`,
						method: 'DELETE'
					})
					await groupStore.removeGroup(groupId);
					const data = { chatId: groupId }
					await this.$http({
						url: `/message/group/deleteChat`,
						method: 'delete',
						data: data
					});
					await chatStore.remove(convKey);
					uni.showToast({
						title: `您退出了群聊'${groupName}'`,
						icon: "none"
					})
					setTimeout(() => uni.switchTab({ url: "/pages/group/group" }), 1500)
				}
			});
		},
		onDissolveGroup() {
			const convKey = this.convKey;
			const groupId = this.groupId;
			const groupName = this.group.name;
			this.$refs.modal.open({
				title: '确认解散?',
				content: `确认要解散群聊'${groupName}'吗?`,
				success: async () => {
					await this.$http({
						url: `/group/delete/${groupId}`,
						method: 'delete'
					})
					await groupStore.removeGroup(groupId);
					uni.showToast({
						title: `您解散了群聊'${groupName}'`,
						icon: "none"
					})
					setTimeout(() => uni.switchTab({ url: "/pages/group/group" }), 1500)
				}
			});
		},
		onDndChange(e) {
			const isDnd = e.detail.value;
			const convKey = this.convKey;
			const groupId = this.group.id;
			const formData = {
				groupId: groupId,
				isDnd: isDnd
			}
			this.$http({
				url: '/group/dnd',
				method: 'put',
				data: formData
			}).then(() => {
				groupStore.setDnd(groupId, isDnd);
				chatStore.setDnd(convKey, isDnd)
			})
		},
		onChatHistory() {
			uni.navigateTo({
				url: '/pages/chat/chat-history?convKey=' + this.convKey
			})
		},
		onCleanMessage() {
			const convKey = this.convKey;
			const groupId = this.groupId;
			const groupName = this.group.name;
			this.$refs.modal.open({
				title: '清空聊天记录',
				content: `确认删除群聊'${groupName}'的聊天记录吗?`,
				confirmText: '确认',
				success: async () => {
					// 删除会话
					const data = { chatId: groupId }
					await this.$http({
						url: `/message/group/deleteChat`,
						method: 'delete',
						data: data
					});
					await chatStore.cleanMessage(convKey);
					uni.showToast({
						title: `您清空了'${groupName}'的聊天记录`,
						icon: 'none'
					})
				}
			})
		},
		loadGroupInfo() {
			this.$http({
				url: `/group/find/${this.groupId}`,
				method: 'get'
			}).then((group) => {
				this.group = group;
				// 更新聊天页面的群聊信息
				chatStore.updateFromGroup(group);
				// 更新聊天列表的群聊信息
				groupStore.updateGroup(group);
			}).catch((e) => {
				console.log(e)
				uni.navigateBack();
			});
		},
		loadGroupMembers() {
			groupStore.refreshMember(this.groupId);
		}
	},
	computed: {
		convKey() {
			return this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP, this.groupId);
		},
		isExistHistory() {
			return chatStore.conversationMap.has(this.convKey);
		},
		ownerName() {
			const member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
			return member && member.showNickName;
		},
		isOwner() {
			return this.group.ownerId == userStore.userInfo.id;
		},
		showMaxIdx() {
			return this.isOwner ? 8 : 9;
		},
		groupMembers() {
			const group = groupStore.findGroup(this.groupId)
			return group.members.filter(m => !m.quit);
		}
	},
	onLoad(options) {
		this.groupId = parseInt(options.id);
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
					text-overflow: ellipsis;
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

		.notice-text {
			padding: 0 40rpx;
			background: white;
			text-align: left;
			font-size: $im-font-size;
			color: $im-text-color-light;
			line-height: 50rpx;
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

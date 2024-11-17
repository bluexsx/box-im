<template>
	<view v-if="userStore.userInfo.type == 1" class="page group-info">
    <nav-bar back>群聊信息</nav-bar>
		<view v-if="!group.quit"  class="group-members">
			<view class="member-items">
				<view v-for="(member,idx) in  groupMembers" :key="idx">
					<view class="member-item" v-if="idx<9">
						<head-image :id="member.userId" :name="member.showNickName" :url="member.headImage" size="small"
                        :online="member.online" ></head-image>
						<view class="member-name">
							<text>{{member.showNickName}}</text>
						</view>
					</view>
				</view>
				<view class="invite-btn" @click="onInviteMember()">
					<uni-icons type="plusempty" size="20" color="#888888"></uni-icons>
				</view>
			</view>
			<view class="member-more" @click="onShowMoreMmeber()">{{`查看全部群成员${groupMembers.length}人`}}></view>
		</view>
		<view class="group-detail">

			<uni-section title="群聊名称">
				<template v-slot:right>
					<text class="detail-text">{{group.name}}</text>
				</template>
			</uni-section>
			<uni-section title="群主">
				<template v-slot:right>
					<text class="detail-text">{{ownerName}}</text>
				</template>
			</uni-section>

			<uni-section title="群名备注">
				<template v-slot:right>
					<text class="detail-text"> {{group.remarkGroupName}}</text>
				</template>
			</uni-section>
			<uni-section title="我在本群的昵称">
				<template v-slot:right>
					<text class="detail-text"> {{group.showNickName}}</text>
				</template>
			</uni-section>
			<uni-section v-if="group.notice" title="群公告">
				<uni-notice-bar :text="group.notice" />
			</uni-section>
			<view v-if="!group.quit"  class="group-edit" @click="onEditGroup()">修改群聊资料 > </view>
		</view>
		<view v-if="!group.quit"  class="bottom-btn">
			<button type="primary" @click="onSendMessage()">发消息</button>
			<button v-show="!isOwner" type="warn" @click="onQuitGroup()">退出群聊</button>
			<button v-show="isOwner" type="warn" @click="onDissolveGroup()">解散群聊</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				groupId: null,
				group:{},
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
							uni.showModal({
								title: `退出成功`,
								content: `您已退出群聊'${this.group.name}'`,
								showCancel: false,
								success: () => {
									setTimeout(()=>{
										uni.switchTab({
											url:"/pages/group/group"
										});
										this.groupStore.removeGroup(this.groupId);
										this.chatStore.removeGroupChat(this.groupId);
									},100)
								}
							})
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
							uni.showModal({
								title: `解散成功`,
								content: `群聊'${this.group.name}'已解散`,
								showCancel: false,
								success: () => {	
									setTimeout(()=>{
										uni.switchTab({
											url:"/pages/group/group"
										});
										this.groupStore.removeGroup(this.groupId);
										this.chatStore.removeGroupChat(this.groupId);
									},100)	
								}
							})
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
				console.log("loadGroupMembers")
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

<style lang="scss" scoped>
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
				}

				.invite-btn {
					display: flex;
					justify-content: center;
					align-items: center;
					width: 86rpx;
					height: 86rpx;
					margin: 10rpx;
					border: $im-border solid 2rpx;
					border-radius: 10%;
				}
			}

			.member-more {
				padding-top: 24rpx;
				text-align: center;
				font-size: $im-font-size-small;
        color: $im-text-color-lighter;
			}
		}


		.group-detail {
			margin-top: 30rpx;
			padding: 20rpx 20rpx;
			background: white;
			
			.detail-text{
				font-size: $im-font-size;
			}
			
			.group-edit {
				padding-top: 30rpx;
				text-align: center;
				font-size: $im-font-size-small;
				color: $im-text-color-lighter;
			}
		}
	}
</style>
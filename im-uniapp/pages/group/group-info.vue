<template>
	<view class="page group-info">
		<view class="group-members">
			<view class="member-items">
				<view v-for="(member,idx) in  groupMembers" :key="idx">
					<view class="member-item" v-if="idx<9" @click="onShowUserInfo(member.userId)">
						<view class="avatar">
							<image class="head-image" :src="member.headImage" mode="aspectFill"> </image>
						</view>
						<view class="text">
							<text>{{member.aliasName}}</text>
						</view>
					</view>
				</view>
				<view class="invite-btn" @click="onInviteMember()">
					<uni-icons type="plusempty" size="28" color="#888888"></uni-icons>
				</view>
			</view>
			<view class="member-more" @click="onShowMoreMmeber()">查看更多群成员 ></view>
		</view>
		<view class="group-detail">
			<uni-section title="群聊名称:" titleFontSize="12px">
				<template v-slot:right>
					<text>{{group.name}}</text>
				</template>
			</uni-section>
			<uni-section title="群主:" titleFontSize="12px">
				<template v-slot:right>
					<text>{{ownerName}}</text>
				</template>
			</uni-section>

			<uni-section title="群聊备注:" titleFontSize="12px">
				<template v-slot:right>
					<text> {{group.remark}}</text>
				</template>
			</uni-section>
			<uni-section title="我在本群的昵称:" titleFontSize="12px">
				<template v-slot:right>
					<text> {{group.aliasName}}</text>
				</template>
			</uni-section>
			<uni-section title="群公告:" titleFontSize="12px">
				<uni-notice-bar :text="group.notice" />
			</uni-section>
			<view class="group-edit" @click="onEditGroup()">修改群聊资料 > </view>
		</view>
		<view class="btn-group">
			<button class="btn" type="primary" @click="onSendMessage()">发消息</button>
			<button class="btn" v-show="!isOwner" type="warn" @click="onQuitGroup()">退出群聊</button>
			<button class="btn" v-show="isOwner" type="warn" @click="onDissolveGroup()">解散群聊</button>
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
			onShowUserInfo(userId) {
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + userId
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
					showName: this.group.remark,
					headImage: this.group.headImage,
				};
				this.$store.commit("openChat", chat);
				uni.navigateTo({
					url: "/pages/chat/chat-box?chatIdx=0"
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
										this.$store.commit("removeGroup", this.groupId);
										this.$store.commit("removeGroupChat", this.groupId);
									},100)
								}
							})
						});
					}
				});
			},
			onDissolveGroup() {
				console.log(this.group.name)
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
										this.$store.commit("removeGroup", this.groupId);
										this.$store.commit("removeGroupChat", this.groupId);
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
					this.$store.commit("updateChatFromGroup", group);
					// 更新聊天列表的群聊信息
					this.$store.commit("updateGroup", group);

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
				return member && member.aliasName;
			},
			isOwner() {
				return this.group.ownerId == this.$store.state.userStore.userInfo.id;
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
					background-color: #fafafa;
					white-space: nowrap;

					.avatar {
						display: flex;
						justify-content: center;
						align-items: center;
						width: 100rpx;
						height: 100rpx;

						.head-image {
							width: 100%;
							height: 100%;
							border-radius: 10%;
							border: #d8d8d8 solid 1px;
						}
					}

					.text {
						width: 100%;
						flex: 1;
						font-size: 14px;
						overflow: hidden;
						text-align: center;
						white-space: nowrap;
					}
				}

				.invite-btn {
					display: flex;
					justify-content: center;
					align-items: center;
					width: 100rpx;
					height: 100rpx;
					margin: 10rpx;
					border: #686868 dashed 2px;
					border-radius: 10%;
				}
			}

			.member-more {
				padding: 20rpx;
				text-align: center;
				font-size: 16px;
			}
		}


		.group-detail {
			margin-top: 30rpx;
			padding: 30rpx;
			background: white;


			.group-edit {
				padding: 20rpx;
				text-align: center;
				font-size: 16px;	
			}
		}


		.btn-group {
			margin-top: 30rpx;
			padding: 30rpx;
			background: white;


			.btn {
				margin: 10rpx;
			}
		}


	}
</style>
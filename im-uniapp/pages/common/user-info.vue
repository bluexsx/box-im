<template>
	<view class="page user-info">
		<nav-bar back>用户信息</nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<view class="content">
				<head-image :name="userInfo.nickName" :url="userInfo.headImageThumb" :size="160"
					@click="onShowFullImage()"></head-image>

				<view class="info-item">
					<view class="info-primary">
						<text class="info-username">
							{{ userInfo.nickName }}
						</text>
						<text v-show="userInfo.sex == 0" class="iconfont icon-man" color="darkblue"></text>
						<text v-show="userInfo.sex == 1" class="iconfont icon-girl" color="darkred"></text>
					</view>
					<view class="info-text">
						<text class="label-text">
							用户名:
						</text>
						<text class="content-text">
							{{ userInfo.userName }}
						</text>
					</view>
					<view class="info-text">
						<view>
							<text class="label-text">
								签名:
							</text>
							<text class="content-text">
								{{ userInfo.signature }}
							</text>
						</view>
					</view>
				</view>
			</view>
		</uni-card>
		<bar-group>
			<btn-bar v-show="isFriend" type="primary" title="发送消息" @tap="onSendMessage()">
			</btn-bar>
			<btn-bar v-show="!isFriend" type="primary" title="加为好友" @tap="onAddFriend()"></btn-bar>
			<btn-bar v-show="isFriend" type="danger" title="删除好友" @tap="onDelFriend()"></btn-bar>
		</bar-group>
	</view>
</template>

<script>
export default {
	data() {
		return {
			userInfo: {}
		}
	},
	methods: {
		onShowFullImage() {
			let imageUrl = this.userInfo.headImage;
			if (imageUrl) {
				uni.previewImage({
					urls: [imageUrl]
				})
			}
		},
		onSendMessage() {
			let chat = {
				type: 'PRIVATE',
				targetId: this.userInfo.id,
				showName: this.userInfo.nickName,
				headImage: this.userInfo.headImage,
			};
			this.chatStore.openChat(chat);
			let chatIdx = this.chatStore.findChatIdx(chat);
			uni.navigateTo({
				url: "/pages/chat/chat-box?chatIdx=" + chatIdx
			})
		},
		onAddFriend() {
			this.$http({
				url: "/friend/add?friendId=" + this.userInfo.id,
				method: "POST"
			}).then((data) => {
				let friend = {
					id: this.userInfo.id,
					nickName: this.userInfo.nickName,
					headImage: this.userInfo.headImageThumb,
					online: this.userInfo.online,
					deleted: false
				}
				this.friendStore.addFriend(friend);
				uni.showToast({
					title: '对方已成为您的好友',
					icon: 'none'
				})
			})
		},
		onDelFriend() {
			uni.showModal({
				title: "确认删除",
				content: `确认删除 '${this.userInfo.nickName}',并删除聊天记录吗?`,
				success: (res) => {
					if (res.cancel)
						return;
					this.$http({
						url: `/friend/delete/${this.userInfo.id}`,
						method: 'delete'
					}).then((data) => {
						this.friendStore.removeFriend(this.userInfo.id);
						this.chatStore.removePrivateChat(this.userInfo.id);
						uni.showToast({
							title: `与 '${this.userInfo.nickName}'的好友关系已解除`,
							icon: 'none'
						})
					})
				}
			})
		},
		updateFriendInfo() {
			if (this.isFriend) {
				// store的数据不能直接修改，深拷贝一份store的数据
				let friend = JSON.parse(JSON.stringify(this.friendInfo));
				friend.headImage = this.userInfo.headImageThumb;
				friend.nickName = this.userInfo.nickName;

				// 更新好友列表中的昵称和头像
				this.friendStore.updateFriend(friend);
				// 更新会话中的头像和昵称
				this.chatStore.updateChatFromFriend(this.userInfo);
			}
		},
		loadUserInfo(id) {
			this.$http({
				url: "/user/find/" + id,
				method: 'GET'
			}).then((user) => {
				this.userInfo = user;
				// 如果发现好友的头像和昵称改了，进行更新
				this.updateFriendInfo()

			})
		}
	},
	computed: {
		isFriend() {
			return this.friendStore.isFriend(this.userInfo.id);
		},
		friendInfo() {
			return this.friendStore.findFriend(this.userInfo.id);
		}
	},
	onLoad(options) {
		// 查询用户信息
		this.loadUserInfo(options.id);
	}
}
</script>

<style lang="scss" scoped>
.user-info {
	.content {
		height: 200rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx;

		.info-item {
			display: flex;
			align-items: flex-start;
			flex-direction: column;
			padding-left: 40rpx;
			flex: 1;

			.info-text {
				line-height: 1.5;
				//margin-bottom: 10rpx;
			}

			.label-text {
				font-size: $im-font-size-small;
				color: $im-text-color-light;

			}

			.content-text {
				font-size: $im-font-size-small;
				color: $im-text-color-light;
			}

			.info-primary {
				display: flex;
				align-items: center;
				margin-bottom: 10rpx;

				.info-username {
					font-size: $im-font-size-large;
					font-weight: 600;
				}

				.icon-man {
					color: $im-text-color;
					font-size: $im-font-size-large;
					padding-left: 10rpx;
				}

				.icon-girl {
					color: darkred;
				}
			}
		}
	}

}
</style>
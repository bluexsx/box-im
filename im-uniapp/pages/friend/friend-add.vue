<template>
	<view class="page friend-add">
		<nav-bar back>添加好友</nav-bar>
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" :focus="true" @confirm="onSearch()"
					@cancel="onCancel()" placeholder="用户名/昵称"></uni-search-bar>
			</view>
		</view>
		<view class="user-items">
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="(user) in users" :key="user.id" v-show="user.id != userStore.userInfo.id">
					<view class="user-item">
						<head-image :id="user.id" :name="user.nickName" :online="user.online"
							:url="user.headImage"></head-image>
						<view class="user-info">
							<view class="nick-name">
								<view>{{ user.nickName }}</view>
								<uni-tag v-if="user.status == 1" circle type="error" text="已注销" size="small"></uni-tag>
							</view>
							<view class="user-name">用户名:{{ `${user.userName}`}}</view>
						</view>
						<view class="user-btns">
							<button type="primary" v-show="!isFriend(user.id)" size="mini"
								@click.stop="onAddFriend(user)">加为好友</button>
							<button type="default" v-show="isFriend(user.id)" size="mini" disabled>已添加</button>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			searchText: "",
			users: []
		}
	},
	methods: {
		onCancel() {
			uni.navigateBack();
		},
		onSearch() {
			this.$http({
				url: "/user/findByName?name=" + this.searchText,
				method: "GET"
			}).then((data) => {
				this.users = data;
			})
		},
		onAddFriend(user) {
			this.$http({
				url: "/friend/add?friendId=" + user.id,
				method: "POST"
			}).then((data) => {
				let friend = {
					id: user.id,
					nickName: user.nickName,
					headImage: user.headImageThumb,
					online: user.online,
					delete: false
				}
				this.friendStore.addFriend(friend);
				uni.showToast({
					title: "添加成功，对方已成为您的好友",
					icon: "none"
				})
			})
		},
		onShowUserInfo(user) {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + user.id
			})
		},
		isFriend(userId) {
			return this.friendStore.isFriend(userId);
		}
	}
}
</script>

<style scoped lang="scss">
.friend-add {
	position: relative;
	display: flex;
	flex-direction: column;

	.user-items {
		position: relative;
		flex: 1;
		overflow: hidden;

		.user-item {
			height: 100rpx;
			display: flex;
			margin-bottom: 1rpx;
			position: relative;
			padding: 18rpx 20rpx;
			align-items: center;
			background-color: white;
			white-space: nowrap;

			.user-info {
				flex: 1;
				display: flex;
				flex-direction: column;
				padding-left: 20rpx;
				font-size: $im-font-size;
				white-space: nowrap;
				overflow: hidden;

				.nick-name {
					display: flex;
					flex: 1;
					font-size: $im-font-size-large;
					white-space: nowrap;
					overflow: hidden;
					align-items: center;

					.uni-tag {
						text-align: center;
						margin-left: 5rpx;
						padding: 1px 5px;
					}
				}

				.user-name {
					display: flex;
					font-size: $im-font-size-smaller;
					color: $im-text-color-lighter;
					padding-top: 8rpx;
				}
			}
		}

		.scroll-bar {
			height: 100%;
		}
	}

}
</style>
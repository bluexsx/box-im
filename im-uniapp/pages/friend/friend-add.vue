<template>
	<view class="page friend-add">
		<view class="search-bar">
			<uni-search-bar v-model="searchText" :focus="true" @confirm="onSearch()" @cancel="onCancel()"
				placeholder="用户名/昵称"></uni-search-bar>
		</view>
		<view class="user-items">
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="(user) in users" :key="user.id" v-show="user.id != $store.state.userStore.userInfo.id">
					<uni-list-chat :avatar="user.headImage" :title="user.userName" :note="'昵称:'+user.nickName"
						:clickable="true" @click="onShowUserInfo(user)">
						<view class="chat-custom-right">
							<button type="primary" v-show="!isFriend(user.id)" size="mini"
								@click.stop="onAddFriend(user)">加为好友</button>
							<button type="default" v-show="isFriend(user.id)" size="mini" disabled>已添加</button>
						</view>
					</uni-list-chat>
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
						headImage: user.headImage,
						online: user.online
					}
					this.$store.commit("addFriend", friend);
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
				let friends = this.$store.state.friendStore.friends;
				let friend = friends.find((f) => f.id == userId);
				return friend != undefined;
			}
		}
	}
</script>

<style scoped lang="scss">
	.friend-add {
		position: relative;
		border: #dddddd solid 1px;
		display: flex;
		flex-direction: column;

		.search-bar {
			background: white;
		}
		.user-items{
			position: relative;
			flex: 1;
			overflow: hidden;
			.scroll-bar {
				height: 100%;
			}
		}
		
	}
</style>
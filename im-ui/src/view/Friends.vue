<template>
	<el-container>
		<el-aside width="250px" class="l-friend-box">
			<el-header class="l-friend-header" height="60px">
				<div class="l-friend-search">
					<el-input width="200px" placeholder="搜索好友" v-model="searchText">
						<el-button slot="append" icon="el-icon-search"></el-button>
					</el-input>
				</div>
				<el-button plain icon="el-icon-plus" style="border: none; padding:12px; font-size: 20px;color: black;" title="添加好友" @click="onShowAddFriends"></el-button>

				<add-friends :dialogVisible="showAddFriend" @close="onCloseAddFriends" @add="onAddFriend()">
				</add-friends>
			</el-header>
			<el-main>
				<div v-for="(friendsInfo,index) in $store.state.friendsStore.friendsList" :key="friendsInfo.id">
					<friends-item v-show="friendsInfo.friendNickName.startsWith(searchText)" :friendsInfo="friendsInfo" :index="index"
					 :active="index === $store.state.friendsStore.activeIndex" @del="onDelItem(friendsInfo,index)" @click.native="onClickItem(friendsInfo,index)">
					</friends-item>
				</div>
			</el-main>
		</el-aside>
		<el-container class="r-friend-box">
			<div v-show="$store.state.friendsStore.activeIndex>=0">
				<div class="user-detail">
					<head-image class="detail-head-image" :url="activeUserInfo.headImage"></head-image>
					<div class="info-item">
						<el-descriptions title="好友信息" class="description" :column="1">
							<el-descriptions-item label="用户名">{{ activeUserInfo.userName }}
							</el-descriptions-item>
							<el-descriptions-item label="昵称">{{ activeUserInfo.nickName }}
							</el-descriptions-item>
							<el-descriptions-item label="性别">{{ activeUserInfo.sex==0?"男":"女" }}</el-descriptions-item>
							<el-descriptions-item label="签名">{{ activeUserInfo.signature }}</el-descriptions-item>
						</el-descriptions>
					</div>
				</div>
				<div class="btn-group">
					<el-button class="send-btn" @click="onSend()">发消息</el-button>
				</div>
			</div>
		</el-container>
	</el-container>
</template>

<script>
	import FriendsItem from "../components/friend/FriendsItem.vue";
	import AddFriends from "../components/friend/AddFriends.vue";
	import HeadImage from "../components/common/HeadImage.vue";

	export default {
		name: "friends",
		components: {
			FriendsItem,
			AddFriends,
			HeadImage
		},
		data() {
			return {
				searchText: "",
				showAddFriend: false,
				activeUserInfo: {}
			}
		},
		methods: {
			onShowAddFriends() {
				this.showAddFriend = true;
			},
			onCloseAddFriends() {
				this.showAddFriend = false;
			},
			onClickItem(friendsInfo, index) {
				this.$store.commit("activeFriends", index);
				this.$http({
					url: `/api/user/find/${friendsInfo.friendId}`,
					method: 'get'
				}).then((userInfo) => {
					this.activeUserInfo = userInfo;
					// 如果发现好友的头像和昵称改了，进行更新
					if (userInfo.headImageThumb != friendsInfo.friendHeadImage ||
						userInfo.nickName != friendsInfo.friendNickName) {
						this.updateFriendInfo(friendsInfo, userInfo, index)
					}
				})
			},
			onDelItem(friendsInfo, index) {
				this.$http({
					url: '/api/friends/delete',
					method: 'delete',
					params: {
						friendId: friendsInfo.friendId
					}
				}).then((data) => {
					this.$message.success("删除好友成功");
					this.$store.commit("removeFriends", index);
				})
			},
			onSend() {
				let userInfo = this.activeUserInfo;
				let chatInfo = {
					type: 'single',
					targetId: userInfo.id,
					showName: userInfo.nickName,
					headImage: userInfo.headImage,
				};
				this.$store.commit("openChat", chatInfo);
				this.$store.commit("activeChat", 0);
				this.$router.push("/home/chat");
			},
			updateFriendInfo(friendsInfo, userInfo, index) {
				friendsInfo.friendHeadImage = userInfo.headImageThumb;
				friendsInfo.friendNickName = userInfo.nickName;
				this.$http({
					url: "/api/friends/update",
					method: "put",
					data: friendsInfo
				}).then(() => {
					this.$store.commit("updateFriends", friendsInfo);
					this.$store.commit("setChatUserInfo", userInfo);
				})
			}
		}

	}
</script>

<style scoped lang="scss">
	.el-container {
		.l-friend-box {
			border: #dddddd solid 1px;
			background: #eeeeee;

			.l-friend-header {
				display: flex;
				align-items: center;
				padding: 5px;
				background-color: white;
				
				.l-friend-search{
					flex: 1;
				}
			}

			.el-main {
				padding: 0;
			}
		}


		.r-friend-box {
			.user-detail {
				width: 100%;
				display: flex;
				padding: 50px 10px 10px 50px;
				text-align: center;
				justify-content: space-around;

				.detail-head-image {
					width: 200px;
					height: 200px;
				}

				.info-item {
					width: 400px;
					height: 200px;
					background-color: #ffffff;
				}

				.description {
					padding: 20px 20px 0px 20px;

				}
			}

			.btn-group {
				text-align: left !important;
				padding-left: 100px;
			}
		}



	}
</style>

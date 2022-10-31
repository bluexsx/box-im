<template>
	<el-container>
		<el-aside width="250px" class="l-friend-box">
			<el-header class="l-friend-header" height="60px">
				<div class="l-friend-search">
					<el-input width="200px" placeholder="搜索好友" v-model="searchText">
						<el-button slot="append" icon="el-icon-search"></el-button>
					</el-input>
				</div>
				<el-button plain icon="el-icon-plus" style="border: none; padding:12px; font-size: 20px;color: black;" title="添加好友" @click="handleShowAddFriend()"></el-button>

				<add-friend :dialogVisible="showAddFriend" @close="handleCloseAddFriend">
				</add-friend>
			</el-header>
			<el-main>
				<div v-for="(friendInfo,index) in $store.state.friendStore.friends" :key="friendInfo.id">
					<friend-item v-show="friendInfo.friendNickName.startsWith(searchText)" :friendInfo="friendInfo" :index="index"
					 :active="index === $store.state.friendStore.activeIndex" @del="handleDelItem(friendInfo,index)" @click.native="handleActiveItem(friendInfo,index)">
					</friend-item>
				</div>
			</el-main>
		</el-aside>
		<el-container class="r-friend-box">
			<div v-show="$store.state.friendStore.activeIndex>=0">
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
					<el-button class="send-btn" @click="handleSendMessage()">发消息</el-button>
				</div>
			</div>
		</el-container>
	</el-container>
</template>

<script>
	import FriendItem from "../components/friend/FriendItem.vue";
	import AddFriend from "../components/friend/AddFriend.vue";
	import HeadImage from "../components/common/HeadImage.vue";

	export default {
		name: "friend",
		components: {
			FriendItem,
			AddFriend,
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
			handleShowAddFriend() {
				this.showAddFriend = true;
			},
			handleCloseAddFriend() {
				this.showAddFriend = false;
			},
			handleActiveItem(friendInfo, index) {
				this.$store.commit("activeFriend", index);
				this.$http({
					url: `/api/user/find/${friendInfo.friendId}`,
					method: 'get'
				}).then((userInfo) => {
					this.activeUserInfo = userInfo;
					// 如果发现好友的头像和昵称改了，进行更新
					if (userInfo.headImageThumb != friendInfo.friendHeadImage ||
						userInfo.nickName != friendInfo.friendNickName) {
						this.updateFriendInfo(friendInfo, userInfo, index)
					}
				})
			},
			handleDelItem(friendInfo, index) {
				this.$http({
					url: '/api/friend/delete',
					method: 'delete',
					params: {
						friendId: friendInfo.friendId
					}
				}).then((data) => {
					this.$message.success("删除好友成功");
					this.$store.commit("removeFriend", index);
				})
			},
			handleSendMessage() {
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
			updateFriendInfo(friendInfo, userInfo, index) {
				friendInfo.friendHeadImage = userInfo.headImageThumb;
				friendInfo.friendNickName = userInfo.nickName;
				this.$http({
					url: "/api/friend/update",
					method: "put",
					data: friendInfo
				}).then(() => {
					this.$store.commit("updateFriend", friendInfo);
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

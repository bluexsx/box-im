<template>
	<el-container>
		<el-aside width="250px" class="l-friend-box">
			<el-header height="60px">
				<el-row>
					<el-col :span="19">
						<el-input width="200px" placeholder="搜索好友" v-model="searchText">
							<el-button slot="append" icon="el-icon-search"></el-button>
						</el-input>
					</el-col>
					<el-col :span="1"></el-col>
					<el-col :span="3">
						<el-button plain icon="el-icon-plus" style="border: none; font-size: 20px;color: black;"
							title="添加好友" @click="onShowAddFriends"></el-button>
					</el-col>
				</el-row>
				<add-friends :dialogVisible="showAddFriend" @close="onCloseAddFriends" @add="onAddFriend()">
				</add-friends>
			</el-header>
			<el-main>
				<div v-for="(friendsInfo,index) in $store.state.friendsStore.friendsList" :key="friendsInfo.id">
					<friends-item v-show="friendsInfo.friendNickName.startsWith(searchText)" :friendsInfo="friendsInfo"
						:index="index" :active="index === $store.state.friendsStore.activeIndex"
						@del="onDelItem(friendsInfo,index)" @click.native="onClickItem(friendsInfo,index)">
					</friends-item>
				</div>
			</el-main>
		</el-aside>
		<el-container class="r-friend-box">
			<div v-show="$store.state.friendsStore.activeIndex>=0">
				<div class="user-detail">
					<div class="detail-head-image">
						<head-image :url="$store.state.friendsStore.activeUserInfo.headImage" ></head-image>
					</div>
					<div class="info-item">
						<el-descriptions title="好友信息" class="description" :column="1">
							<el-descriptions-item label="用户名">{{ $store.state.friendsStore.activeUserInfo.userName }}
							</el-descriptions-item>
							<el-descriptions-item label="昵称">{{ $store.state.friendsStore.activeUserInfo.nickName }}
							</el-descriptions-item>
							<el-descriptions-item label="备注">好基友</el-descriptions-item>
							<el-descriptions-item label="签名">世界这么大，我想去看看</el-descriptions-item>
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
	import FriendsItem from "../components/FriendsItem.vue";
	import AddFriends from "../components/AddFriends.vue";
	import HeadImage from "../components/HeadImage.vue";
	
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
				showAddFriend: false
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
				let userInfo = this.$store.state.friendsStore.activeUserInfo
				let chatInfo = {
					type: 'single',
					targetId: userInfo.id,
					showName: userInfo.nickName,
					headImage: userInfo.headImage,
				};
				this.$store.commit("openChat", chatInfo);
				this.$store.commit("activeChat", 0);
				this.$router.push("/home/chat");
			}
		}

	}
</script>

<style scoped lang="scss">
	.el-container {
		.l-friend-box {
			border: #dddddd solid 1px;
			background: #eeeeee;

			.el-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
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

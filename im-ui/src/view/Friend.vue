<template>
	<el-container class="friend-page">
		<el-aside width="260px" class="friend-list-box">
			<div class="friend-list-header">
				<el-input class="search-text" placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
				<el-button plain class="add-btn" icon="el-icon-plus" title="添加好友"
					@click="onShowAddFriend()"></el-button>
				<add-friend :dialogVisible="showAddFriend" @close="onCloseAddFriend"></add-friend>
			</div>
			<el-scrollbar class="friend-list-items">
				<div v-for="(friend,index) in $store.state.friendStore.friends" :key="index">
					<friend-item v-show="friend.nickName.startsWith(searchText)" :index="index"
						:active="friend === $store.state.friendStore.activeFriend" @chat="onSendMessage(friend)"
						@delete="onDelItem(friend,index)" @click.native="onActiveItem(friend,index)">
					</friend-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="friend-box">
			<div class="friend-header" v-show="userInfo.id">
				{{userInfo.nickName}}
			</div>
			<div v-show="userInfo.id">
				<div class="friend-detail">
					<head-image :size="200" :name="userInfo.nickName" :url="userInfo.headImage"
						@click.native="showFullImage()"></head-image>
					<div>
						<div class="info-item">
							<el-descriptions title="好友信息" class="description" :column="1">
								<el-descriptions-item label="用户名">{{ userInfo.userName }}
								</el-descriptions-item>
								<el-descriptions-item label="昵称">{{ userInfo.nickName }}
								</el-descriptions-item>
								<el-descriptions-item label="性别">{{ userInfo.sex==0?"男":"女" }}</el-descriptions-item>
								<el-descriptions-item label="签名">{{ userInfo.signature }}</el-descriptions-item>
							</el-descriptions>

						</div>
						<div class="frient-btn-group">
							<el-button v-show="isFriend" icon="el-icon-position" type="primary"
								@click="onSendMessage(userInfo)">发消息</el-button>
							<el-button v-show="!isFriend" icon="el-icon-plus" type="primary"
								@click="onAddFriend(userInfo)">加为好友</el-button>
							<el-button v-show="isFriend" icon="el-icon-delete" type="danger"
								@click="onDelItem(userInfo,activeIdx)">删除好友</el-button>
						</div>
					</div>
				</div>
				<el-divider content-position="center"></el-divider>

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
				activeIdx: -1,
				userInfo: {}
			}
		},
		methods: {
			onShowAddFriend() {
				this.showAddFriend = true;
			},
			onCloseAddFriend() {
				this.showAddFriend = false;
			},
			onActiveItem(friend, idx) {
				this.$store.commit("activeFriend", idx);
				this.activeIdx = idx
				this.loadUserInfo(friend, idx);
			},
			onDelItem(friend, idx) {
				this.$confirm(`确认要解除与 '${friend.nickName}'的好友关系吗?`, '确认解除?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/friend/delete/${friend.id}`,
						method: 'delete'
					}).then((data) => {
						this.$message.success("删除好友成功");
						this.$store.commit("removeFriend", idx);
						this.$store.commit("removePrivateChat", friend.id);
					})
				})
			},
			onAddFriend(user) {
				this.$http({
					url: "/friend/add",
					method: "post",
					params: {
						friendId: user.id
					}
				}).then((data) => {
					this.$message.success("添加成功，对方已成为您的好友");
					let friend = {
						id: user.id,
						nickName: user.nickName,
						headImage: user.headImage,
						online: user.online
					}
					this.$store.commit("addFriend", friend);
				})
			},
			onSendMessage(user) {
				let chat = {
					type: 'PRIVATE',
					targetId: user.id,
					showName: user.nickName,
					headImage: user.headImageThumb,
				};
				this.$store.commit("openChat", chat);
				this.$store.commit("activeChat", 0);
				this.$router.push("/home/chat");
			},
			showFullImage() {
				if (this.userInfo.headImage) {
					this.$store.commit('showFullImageBox', this.userInfo.headImage);
				}
			},
			updateFriendInfo(friend, user, index) {
				// store的数据不能直接修改，深拷贝一份store的数据
				friend = JSON.parse(JSON.stringify(friend));
				friend.headImage = user.headImageThumb;
				friend.nickName = user.nickName;
				this.$http({
					url: "/friend/update",
					method: "put",
					data: friend
				}).then(() => {
					this.$store.commit("updateFriend", friend);
					this.$store.commit("updateChatFromFriend", user);
				})
			},
			loadUserInfo(friend, index) {
				this.$http({
					url: `/user/find/${friend.id}`,
					method: 'get'
				}).then((user) => {
					this.userInfo = user;
					// 如果发现好友的头像和昵称改了，进行更新
					if (user.headImageThumb != friend.headImage ||
						user.nickName != friend.nickName) {
						this.updateFriendInfo(friend, user, index)
					}
				})
			}
		},
		computed: {
			friendStore() {
				return this.$store.state.friendStore;
			},
			isFriend() {
				return this.friendStore.friends.find((f) => f.id == this.userInfo.id);
			}
		}
	}
</script>

<style lang="scss">
	.friend-page {
		.friend-list-box {
			display: flex;
			flex-direction: column;
			border: #dddddd solid 1px;
			background: white;

			.friend-list-header {
				height: 50px;
				display: flex;
				align-items: center;
				padding: 3px 8px;
				border-bottom: 1px #ddd solid;
				
				.el-input__inner {
					border-radius: 10px !important;
					background-color: #F8F8F8;
				}
				
				.add-btn {
					padding: 5px !important;
					margin: 5px;
					font-size: 20px;
					color: #587FF0;
					border: #587FF0 1px solid;
					background-color: #F0F8FF;
					border-radius: 50%;
				}
			}

			.friend-list-items {
				flex: 1;
				margin: 0 3px;
				background: #F8F8F8;
			}
		}

		.friend-box {
			display: flex;
			flex-direction: column;
			border: #dddddd solid 1px;

			.friend-header {
				padding: 3px;	
				height: 50px;
				line-height: 50px;
				font-size: 20px;
				font-weight: 600;
				text-align: center;
				background-color: white;
				border-bottom: 1px #ddd solid;
			}

			.friend-detail {
				display: flex;
				padding: 50px 80px 20px 80px;
				text-align: center;
				
				
				.info-item {
					margin-left: 20px;
					background-color: #ffffff;
					border-radius: 10px ;
					border: 1px #ddd solid;
				}

				.description {
					padding: 20px 20px 0px 20px;
				}
			}

			.frient-btn-group {
				text-align: left !important;
				padding: 20px;
			}
		}
	}
</style>
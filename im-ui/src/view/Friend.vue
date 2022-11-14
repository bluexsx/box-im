<template>
	<el-container>
		<el-aside width="250px" class="l-friend-box">
			<div class="l-friend-header">
				<div class="l-friend-search">
					<el-input width="200px" placeholder="搜索好友" v-model="searchText">
						<el-button slot="append" icon="el-icon-search"></el-button>
					</el-input>
				</div>
				<el-button plain icon="el-icon-plus" style="border: none; padding:12px; font-size: 20px;color: black;" title="添加好友"
				 @click="handleShowAddFriend()"></el-button>
				<add-friend :dialogVisible="showAddFriend" @close="handleCloseAddFriend">
				</add-friend>
			</div>
			<el-scrollbar class="l-friend-list" >
				<div v-for="(friend,index) in $store.state.friendStore.friends" :key="index">
					<friend-item v-show="friend.nickName.startsWith(searchText)" :friend="friend" :index="index" :active="index === $store.state.friendStore.activeIndex"
					 @del="handleDelItem(friend,index)" @click.native="handleActiveItem(friend,index)">
					</friend-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="r-friend-box">
			<div class="r-friend-header" v-show="userInfo.id">
				{{userInfo.nickName}}
			</div>
			<div v-show="userInfo.id">
				<div class="user-detail">
					<head-image class="detail-head-image" :size="200" 
					:url="userInfo.headImage" @click.native="showFullImage()"></head-image>
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
				userInfo: {}
			}
		},
		methods: {
			handleShowAddFriend() {
				this.showAddFriend = true;
			},
			handleCloseAddFriend() {
				this.showAddFriend = false;
			},
			handleActiveItem(friend, index) {
				this.$store.commit("activeFriend", index);
				this.loadUserInfo(friend,index);
			},
			handleDelItem(friend, index) {
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
						this.$store.commit("removeFriend", index);
						this.$store.commit("removePrivateChat", friend.id);
					})
				})
			},
			handleSendMessage() {
				let user = this.userInfo;
				let chat = {
					type: 'PRIVATE',
					targetId: user.id,
					showName: user.nickName,
					headImage: user.headImage,
				};
				this.$store.commit("openChat", chat);
				this.$store.commit("activeChat", 0);
				this.$router.push("/home/chat");
			},
			showFullImage(){
				if(this.userInfo.headImage){
					this.$store.commit('showFullImageBox',this.userInfo.headImage);
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
			loadUserInfo(friend,index){
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
		computed:{
			friendStore(){
				return this.$store.state.friendStore;
			}
		},
		mounted() {
			if(this.friendStore.activeIndex>=0){
				let friend = this.friendStore.friends[this.friendStore.activeIndex];
				this.loadUserInfo(friend,this.friendStore.activeIndex);
			}
		}

	}
</script>

<style scoped lang="scss">
	.el-container {
		.l-friend-box {
			display: flex;
			flex-direction: column;
			border: #dddddd solid 1px;
			background: white;
			.l-friend-header {
				height: 50px;
				display: flex;
				align-items: center;
				padding: 5px;
				background-color: white;

				.l-friend-search {
					flex: 1;
				}
			}
			
			.l-friend-ist{
				flex: 1;
			}
		}

		.r-friend-box {
			display: flex;
			flex-direction: column;
			border: #dddddd solid 1px;
			.r-friend-header {
				width: 100%;
				height: 50px;
				padding: 5px;
				line-height: 50px;
				font-size: 20px;
				text-align: left;
				text-indent: 10px;
				font-weight: 600;
				background-color: white;
				border: #dddddd solid 1px;
			}
			
			.user-detail {
				width: 100%;
				display: flex;
				padding: 50px 10px 10px 50px;
				text-align: center;
				
				.info-item {
					width: 400px;
					height: 200px;
					margin-left: 20px;
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

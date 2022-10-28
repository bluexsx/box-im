<template>
	<el-dialog title="添加好友" :visible.sync="dialogVisible" width="350px" :before-close="onClose">
		<el-input width="200px" placeholder="搜索好友" class="input-with-select" v-model="searchText" @keyup.enter.native="onSearch()">
			<el-button slot="append" icon="el-icon-search" @click="onSearch()"></el-button>
		</el-input>
		<el-scrollbar style="height:400px">
			<div v-for="(userInfo) in users" :key="userInfo.id">
				<div class="item">
					<div class="avatar">
						<head-image :url="userInfo.headImage"></head-image>
					</div>
					<div class="add-friend-text">
						<div>{{userInfo.nickName}}</div>
						<div :class="userInfo.online ? 'online-status  online':'online-status'">{{ userInfo.online?"[在线]":"[离线]"}}</div>
					</div>
					 <el-button type="success" v-show="!isFriend(userInfo.id)" plain @click="onAddFriends(userInfo)">添加</el-button>
					 <el-button type="info" v-show="isFriend(userInfo.id)" plain disabled>已添加</el-button>
				</div>
			</div>
		</el-scrollbar>
	</el-dialog>
</template>

<script>
	import HeadImage from './HeadImage.vue'
	
	
	export default {
		name: "addFriends",
		components:{HeadImage},
		data() {
			return {
				users: [],
				searchText: ""
			}
		},
		props: {
			dialogVisible: {
				type: Boolean
			}
		},
		methods: {
			onClose() {
				this.$emit("close");
			},
			onSearch() {
				this.$http({
					url: "/api/user/findByNickName",
					method: "get",
					params: {
						nickName: this.searchText
					}
				}).then((data) => {
					this.users = data;
				})
			},
			onAddFriends(userInfo){
				this.$http({
					url: "/api/friends/add",
					method: "post",
					params: {
						friendId: userInfo.id
					}
				}).then((data) => {
					this.$store.commit("")
					this.$message.success("添加成功，对方已成为您的好友");
					let friendsInfo = {
						friendId:userInfo.id,
						friendNickName: userInfo.nickName,
						friendHeadImage: userInfo.headImage,
						online: userInfo.online
					}
					this.$store.commit("addFriends",friendsInfo);
					
				})
			},
			isFriend(userId){
				let friendList = this.$store.state.friendsStore.friendsList;
				let friend = friendList.find((f)=> f.friendId==userId);			
				return friend != undefined;
			}
		},
	
		mounted() {
			this.onSearch();
		}
	}
</script>

<style scoped lang="scss">

	.item {
		height: 80px;
		display: flex;
		position: relative;
		padding-left: 15px;
		align-items: center;
		padding-right: 25px;
		
		.add-friend-text {
			margin-left: 15px;
			line-height: 80px;
			flex: 3;
			display: flex;
			flex-direction: row;
			height: 100%;
			flex-shrink: 0;
			overflow: hidden;
		
			.online-status{
				font-size: 12px;
				font-weight: 600;
				&.online{
					color: #5fb878;
				}
			}
		}
	}
</style>

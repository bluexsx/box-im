<template>
	<el-dialog title="添加好友" :visible.sync="dialogVisible" width="30%" :before-close="handleClose">
		<el-input  placeholder="搜索好友" class="input-with-select" v-model="searchText" @keyup.enter.native="handleSearch()">
			<el-button slot="append" icon="el-icon-search" @click="handleSearch()"></el-button>
		</el-input>
		<el-scrollbar style="height:400px">
			<div v-for="(user) in users" :key="user.id" v-show="user.id != $store.state.userStore.userInfo.id">
				<div class="item">
					<div class="avatar">
						<head-image :url="user.headImage"></head-image>
					</div>
					<div class="add-friend-text">
						<div>{{user.nickName}}</div>
						<div :class="user.online ? 'online-status  online':'online-status'">{{ user.online?"[在线]":"[离线]"}}</div>
					</div>
					 <el-button type="success" v-show="!isFriend(user.id)" plain @click="handleAddFriend(user)">添加</el-button>
					 <el-button type="info" v-show="isFriend(user.id)" plain disabled>已添加</el-button>
				</div>
			</div>
		</el-scrollbar>
	</el-dialog>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue'
	
	
	export default {
		name: "addFriend",
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
			handleClose() {
				this.$emit("close");
			},
			handleSearch() {
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
			handleAddFriend(user){
				this.$http({
					url: "/api/friend/add",
					method: "post",
					params: {
						friendId: user.id
					}
				}).then((data) => {
					this.$message.success("添加成功，对方已成为您的好友");
					let friend = {
						id:user.id,
						nickName: user.nickName,
						headImage: user.headImage,
						online: user.online
					}
					this.$store.commit("addFriend",friend);
				})
			},
			isFriend(userId){
				let friends = this.$store.state.friendStore.friends;
				let friend = friends.find((f)=> f.id==userId);			
				return friend != undefined;
			}
		},
	
		mounted() {
			this.handleSearch();
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

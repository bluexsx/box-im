<template>
	<el-dialog title="添加好友" :visible.sync="dialogVisible" width="400px" :before-close="onClose"
		custom-class="add-friend-dialog">
		<el-input placeholder="输入用户名或昵称按下enter搜索，最多展示20条" class="input-with-select" v-model="searchText" size="small"
			@keyup.enter.native="onSearch()">
			<i class="el-icon-search el-input__icon" slot="suffix" @click="onSearch()"> </i>
		</el-input>
		<el-scrollbar style="height:400px">
			<div v-for="(user) in users" :key="user.id" v-show="user.id != $store.state.userStore.userInfo.id">
				<div class="item">
					<div class="avatar">
						<head-image :name="user.nickName" :url="user.headImage" :online="user.online"></head-image>
					</div>
					<div class="add-friend-text">
						<div class="text-user-name">
							<div>{{ user.userName }}</div>
							<div :class="user.online ? 'online-status  online' : 'online-status'">{{
								user.online ? "[在线]" :"[离线]"}}</div>
						</div>
						<div class="text-nick-name">
							<div>昵称:{{ user.nickName }}</div>
						</div>
					</div>
					<el-button type="success" size="mini" v-show="!isFriend(user.id)"
						@click="onAddFriend(user)">添加</el-button>
					<el-button type="info" size="mini" v-show="isFriend(user.id)" plain disabled>已添加</el-button>
				</div>
			</div>
		</el-scrollbar>
	</el-dialog>
</template>

<script>
import HeadImage from '../common/HeadImage.vue'


export default {
	name: "addFriend",
	components: { HeadImage },
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
			if (!this.searchText) {
				this.users = [];
				return;
			}
			this.$http({
				url: "/user/findByName",
				method: "get",
				params: {
					name: this.searchText
				}
			}).then((data) => {
				this.users = data;
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
		isFriend(userId) {
			let friends = this.$store.state.friendStore.friends;
			let friend = friends.find((f) => f.id == userId);
			return friend != undefined;
		}
	}
}
</script>

<style lang="scss">
.add-friend-dialog {
	.item {
		height: 65px;
		display: flex;
		position: relative;
		padding-left: 15px;
		align-items: center;
		padding-right: 25px;

		.add-friend-text {
			margin-left: 15px;
			flex: 3;
			display: flex;
			flex-direction: column;
			flex-shrink: 0;
			overflow: hidden;

			.text-user-name {
				display: flex;
				flex-direction: row;
				font-weight: 600;
				font-size: 16px;
				line-height: 25px;

				.online-status {
					font-size: 12px;
					font-weight: 600;

					&.online {
						color: #5fb878;
					}
				}
			}

			.text-nick-name {
				display: flex;
				flex-direction: row;
				font-size: 12px;
				line-height: 20px;
			}

		}
	}
}
</style>

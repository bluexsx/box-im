<template>
	<el-dialog title="邀请好友" :visible.sync="visible"  width="50%" :before-close="handleClose">
		<div class="agm-container">
			<div class="agm-l-box">
				<el-input width="200px" placeholder="搜索好友" class="input-with-select" v-model="searchText" @keyup.enter.native="handleSearch()">
					<el-button slot="append" icon="el-icon-search" @click="handleSearch()"></el-button>
				</el-input>
				<el-scrollbar style="height:400px;">
					<div v-for="(friend,index) in friends" :key="friend.id">
						<friend-item v-show="friend.nickName.startsWith(searchText)" :showDelete="false" @click.native="handleSwitchCheck(friend)"
						 :friend="friend" :index="index" :active="index === activeIndex">
							<el-checkbox :disabled="friend.disabled" @click.native.stop="" class="agm-friend-checkbox" v-model="friend.isCheck"
							 size="medium"></el-checkbox>
						</friend-item>
					</div>
				</el-scrollbar>
			</div>
			<div class="agm-r-box">
				<div class="agm-select-tip"> 已勾选{{checkCount}}位好友</div>
				<el-scrollbar style="height:400px;">
					<div v-for="(friend,index) in friends" :key="friend.id">
						<friend-item v-if="friend.isCheck && !friend.disabled" :friend="friend" :index="index" :active="false" @del="handleRemoveFriend(friend,index)">
						</friend-item>
					</div>
				</el-scrollbar>
			</div>
		</div>
		<span slot="footer" class="dialog-footer">
			<el-button @click="handleClose()">取 消</el-button>
			<el-button type="primary" @click="handleOk()">确 定</el-button>
		</span>
	</el-dialog>
</template>

<script>
	import FriendItem from '../friend/FriendItem.vue';

	export default {
		name: "addGroupMember",
		components: {
			FriendItem
		},
		data() {
			return {
				searchText: "",
				activeIndex: -1,
				friends: []
			}
		},
		methods: {
			handleClose() {
				this.$emit("close");
			},
			handleOk() {

				let inviteVO = {
					groupId: this.groupId,
					friendIds: []
				}
				this.friends.forEach((f) => {
					if (f.isCheck && !f.disabled) {
						inviteVO.friendIds.push(f.id);
					}
				})
				if (inviteVO.friendIds.length > 0) {
					this.$http({
						url: "/group/invite",
						method: 'post',
						data: inviteVO
					}).then(() => {
						this.$message.success("邀请成功");
						this.$emit("reload");
						this.$emit("close");
					})
				}
			},
			handleRemoveFriend(friend, index) {
				friend.isCheck = false;
			},
			handleSwitchCheck(friend) {
				if (!friend.disabled) {
					friend.isCheck = !friend.isCheck
				}
			}
		},
		props: {
			visible: {
				type: Boolean
			},
			groupId: {
				type: Number
			},
			members: {
				type: Array
			}
		},
		computed: {
			checkCount() {
				return this.friends.filter((f) => f.isCheck && !f.disabled).length;
			}
		},
		watch: {
			visible: function(newData, oldData) {
				if (newData) {
					this.friends = [];
					this.$store.state.friendStore.friends.forEach((f) => {
						let friend = JSON.parse(JSON.stringify(f))
						let m = this.members.filter((m) => !m.quit)
							.find((m) => m.userId == f.id);
						console.log(m);
						if (m) {
							// 好友已经在群里
							friend.disabled = true;
							friend.isCheck = true
						} else {
							friend.disabled = false;
							friend.isCheck = false;
						}
						this.friends.push(friend);
					})
				}
			}
		}

	}
</script>

<style lang="scss">
	.agm-container {
		display: flex;

		.agm-l-box {
			flex: 1;
			border: #dddddd solid 1px;

			.el-checkbox {
				display: flex;
				align-items: center;

				//修改选中框的大小
				.el-checkbox__inner {
					width: 20px;
					height: 20px;

					//修改选中框中的对勾的大小和位置
					&::after {
						height: 12px;
						left: 7px;
					}
				}

				//修改点击文字颜色不变
				.el-checkbox__input.is-checked+.el-checkbox__label {
					color: #333333;
				}

				.el-checkbox__label {
					line-height: 20px;
					padding-left: 8px;
				}
			}

			.agm-friend-checkbox {
				margin-right: 20px;


			}
		}

		.agm-r-box {
			flex: 1;
			border: #dddddd solid 1px;

			.agm-select-tip {
				text-align: left;
				height: 40px;
				line-height: 40px;
				text-indent: 5px;
			}
		}
	}
</style>

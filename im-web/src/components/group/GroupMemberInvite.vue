<template>
	<el-dialog v-dialogDrag :title="dialogTitle" :visible.sync="show" width="620px" :before-close="close">
		<div class="group-member-invite">
			<div class="left-box">
				<div class="search">
					<el-input placeholder="搜索好友" v-model="searchText" size="small">
						<i class="el-icon-search el-input__icon" slot="suffix"> </i>
					</el-input>
				</div>
				<el-scrollbar style="height:400px;">
					<div v-for="friend in friends" :key="friend.id">
						<friend-item v-show="friend.nickName.includes(searchText)" :showDelete="false"
							@click.native="onSwitchCheck(friend)" :menu="false" :friend="friend" :active="false">
							<el-checkbox :disabled="friend.disabled" @click.native.stop="" class="checkbox"
								v-model="friend.isCheck" size="medium"></el-checkbox>
						</friend-item>
					</div>
				</el-scrollbar>
			</div>
			<div class="arrow el-icon-d-arrow-right"></div>
			<div class="right-box">
				<div class="tip">已勾选{{ checkCount }}位好友</div>
				<el-scrollbar style="height:400px;">
					<div v-for="friend in friends" :key="friend.id">
						<friend-item v-if="friend.isCheck && !friend.disabled" :friend="friend" :active="false"
							@del="onRemoveFriend(friend)" :menu="false">
						</friend-item>
					</div>
				</el-scrollbar>
			</div>
		</div>
		<span slot="footer" class="dialog-footer">
			<el-button @click="close()">取 消</el-button>
			<el-button type="primary" :disabled="checkCount === 0 || loading" :loading="loading" @click="onOk()">完
				成</el-button>
		</span>
	</el-dialog>
</template>

<script>
import FriendItem from '../friend/FriendItem.vue';

export default {
	name: "groupMemberInvite",
	components: {
		FriendItem
	},
	data() {
		return {
			show: false,
			loading: false,
			mode: 'invite',
			searchText: "",
			friends: [],
			maxSelectSize: 50
		}
	},
	methods: {
		openCreate() {
			this.mode = 'create';
			this.initDialog();
		},
		open() {
			this.mode = 'invite';
			this.initDialog();
		},
		initDialog() {
			this.show = true;
			this.loading = false;
			this.searchText = "";
			this.friends = [];
			this.friendStore.friends.forEach((f) => {
				if (f.deleted) {
					return;
				}
				let friend = JSON.parse(JSON.stringify(f));
				if (this.isCreate) {
					friend.disabled = false;
					friend.isCheck = false;
				} else {
					let m = this.members.filter((m) => !m.quit).find((m) => m.userId == f.id);
					if (m) {
						friend.disabled = true;
						friend.isCheck = true;
					} else {
						friend.disabled = false;
						friend.isCheck = false;
					}
				}
				this.friends.push(friend);
			})
		},
		close() {
			this.show = false;
		},
		onOk() {
			if (this.isCreate) {
				this.onCreateGroup();
			} else {
				this.onInviteFriends();
			}
		},
		onCreateGroup() {
			const userIds = this.friends.filter(f => f.isCheck).map(f => f.id);
			if (userIds.length === 0) {
				this.$message.warning('请至少选择1位好友');
				return;
			}
			this.loading = true;
			this.$http({
				url: "/group/new",
				method: 'post',
				data: { userIds }
			}).then((group) => {
				this.$emit("success", group);
				this.close();
			}).finally(() => {
				this.loading = false;
			})
		},
		onInviteFriends() {
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
				this.loading = true;
				this.$http({
					url: "/group/invite",
					method: 'post',
					data: inviteVO
				}).then(() => {
					this.$message.success("邀请成功");
					this.$emit("reload");
					this.close();
				}).finally(() => {
					this.loading = false;
				})
			}
		},
		onRemoveFriend(friend) {
			friend.isCheck = false;
		},
		onSwitchCheck(friend) {
			if (friend.disabled) {
				return;
			}
			if (!friend.isCheck && this.checkCount >= this.maxSelectSize) {
				this.$message.warning(`最多只能选择${this.maxSelectSize}位好友`);
				return;
			}
			friend.isCheck = !friend.isCheck;
		}
	},
	props: {
		groupId: {
			type: Number
		},
		members: {
			type: Array,
			default: () => []
		}
	},
	computed: {
		isCreate() {
			return this.mode === 'create';
		},
		dialogTitle() {
			return this.isCreate ? '发起群聊' : '邀请好友进群';
		},
		checkCount() {
			return this.friends.filter((f) => f.isCheck && !f.disabled).length;
		}
	}
}
</script>

<style lang="scss" scoped>
.group-member-invite {
	display: flex;

	.left-box {
		flex: 1;
		overflow: hidden;
		border: var(--im-border);

		.search {
			height: 40px;
			display: flex;
			align-items: center;

			.el-input__inner {
				border: unset;
				border-bottom: var(--im-border);
			}
		}

		.checkbox {
			margin-right: 20px;
		}
	}

	.arrow {
		display: flex;
		align-items: center;
		font-size: 18px;
		padding: 10px;
		font-weight: 600;
		color: var(--im-color-primary);
	}

	.right-box {
		flex: 1;
		border: var(--im-border);

		.tip {
			text-align: left;
			height: 40px;
			line-height: 40px;
			text-indent: 10px;
			color: var(--im-text-color-light)
		}
	}
}
</style>

<template>
	<el-dialog v-dialogDrag title="邀请好友" :visible.sync="show" width="620px" :before-close="close">
		<div class="add-group-member">
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
							<el-checkbox :disabled="friend.disabled" @click.native.stop="" class="friend-checkbox"
								v-model="friend.isCheck" size="medium"></el-checkbox>
						</friend-item>
					</div>
				</el-scrollbar>
			</div>
			<div class="arrow el-icon-d-arrow-right"></div>
			<div class="right-box">
				<div class="tip"> 已勾选{{ checkCount }}位好友</div>
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
			<el-button type="primary" @click="onOk()">确 定</el-button>
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
			show: false,
			searchText: "",
			friends: []
		}
	},
	methods: {
		open() {
			this.show = true;
			this.friends = [];
			this.$store.state.friendStore.friends.forEach((f) => {
				if (f.deleted) {
					return;
				}
				let friend = JSON.parse(JSON.stringify(f))
				let m = this.members.filter((m) => !m.quit)
					.find((m) => m.userId == f.id);
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
		},
		close() {
			this.show = false;
		},
		onOk() {
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
					this.close()
				})
			}
		},
		onRemoveFriend(friend) {
			friend.isCheck = false;
		},
		onSwitchCheck(friend) {
			if (!friend.disabled) {
				friend.isCheck = !friend.isCheck
			}
		}
	},
	props: {
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
	}
}
</script>

<style lang="scss" scoped>
.add-group-member {
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

		.friend-checkbox {
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
<template>
	<el-dialog title="选择成员" :visible.sync="isShow" width="700px">
		<div class="group-member-selector">
			<div class="left-box">
				<el-input placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="suffix"> </i>
				</el-input>
				<virtual-scroller class="scroll-box" :items="showMembers">
					<template v-slot="{ item }">
						<group-member-item :member="item" @click.native="onClickMember(item)">
							<el-checkbox :disabled="item.locked" v-model="item.checked" @change="onChange(item)"
								@click.native.stop=""></el-checkbox>
						</group-member-item>
					</template>
				</virtual-scroller>
			</div>
			<div class="arrow el-icon-d-arrow-right"></div>
			<div class="right-box">
				<div class="select-tip"> 已勾选{{ checkedMembers.length }}位成员</div>
				<div class="checked-member-list">
					<div v-for="m in members" :key="m.userId">
						<group-member class="member-item" v-if="m.checked" :member="m"></group-member>
					</div>
				</div>
			</div>
		</div>
		<span slot="footer" class="dialog-footer">
			<el-button @click="close()">取 消</el-button>
			<el-button type="primary" @click="ok()">确 定</el-button>
		</span>
	</el-dialog>
</template>

<script>
import VirtualScroller from '../common/VirtualScroller.vue';
import GroupMemberItem from './GroupMemberItem.vue';
import GroupMember from './GroupMember.vue';

export default {
	name: "addGroupMember",
	components: {
		GroupMemberItem,
		GroupMember,
		VirtualScroller
	},
	data() {
		return {
			isShow: false,
			searchText: "",
			maxSize: -1,
			members: []
		}
	},
	props: {
		groupId: {
			type: Number
		}
	},
	methods: {
		open(maxSize, checkedIds, lockedIds) {
			this.maxSize = maxSize;
			this.isShow = true;
			this.loadGroupMembers(checkedIds, lockedIds);
		},
		loadGroupMembers(checkedIds, lockedIds) {
			this.$http({
				url: `/group/members/${this.groupId}`,
				method: 'get'
			}).then((members) => {
				members.forEach((m) => {
					// 默认选择和锁定的用户
					m.checked = checkedIds.indexOf(m.userId) >= 0;
					m.locked = lockedIds.indexOf(m.userId) >= 0;
				});
				this.members = members;
			});
		},
		onClickMember(m) {
			if (!m.locked) {
				m.checked = !m.checked;
			}
			if (this.checkedMembers.length > this.maxSize) {
				this.$message.error(`最多选择${this.maxSize}位成员`)
				m.checked = false;
			}
		},
		onChange(m) {
			if (this.checkedMembers.length > this.maxSize) {
				this.$message.error(`最多选择${this.maxSize}位成员`)
				m.checked = false;
			}
		},
		ok() {
			this.$emit("complete", this.checkedMembers);
			this.isShow = false;
		},
		close() {
			this.isShow = false;
		}
	},
	computed: {
		checkedMembers() {
			let ids = [];
			this.members.forEach((m) => {
				if (m.checked) {
					ids.push(m);
				}
			})
			return ids;
		},
		showMembers() {
			return this.members.filter((m) => !m.hide && !m.quit && m.showNickName.includes(this.searchText))
		}
	}

}
</script>

<style lang="scss">
.group-member-selector {
	display: flex;


	.left-box {
		width: 48%;
		overflow: hidden;
		border: var(--im-border);


		.scroll-box {
			height: 400px;
		}


		.el-input__inner {
			border: none;
			border-bottom: var(--im-border);
		}
	}


	.arrow {
		display: flex;
		align-items: center;
		font-size: 20px;
		padding: 10px;
		font-weight: 600;
		color: var(--im-color-primary);
	}

	.right-box {
		width: 48%;
		border: var(--im-border);

		.select-tip {
			text-align: left;
			height: 40px;
			line-height: 40px;
			text-indent: 5px;
			color: var(--im-text-color-light)
		}

		.checked-member-list {
			padding: 10px;
			display: flex;
			flex-direction: row;
			flex-wrap: wrap;

			.member-item {
				padding: 2px;
			}
		}
	}
}
</style>

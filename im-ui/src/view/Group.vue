<template>
	<el-container class="im-group-box">
		<el-aside width="250px" class="l-group-box">
			<el-header class="l-group-header" height="60px">
				<div class="l-group-search">
					<el-input width="200px" placeholder="搜索群聊" v-model="searchText">
						<el-button slot="append" icon="el-icon-search"></el-button>
					</el-input>
				</div>
				<el-button plain icon="el-icon-plus" style="border: none; padding: 12px; font-size: 20px;color: black;" title="创建群聊"
				 @click="handleCreateGroup()"></el-button>
			</el-header>
			<el-main>
				<el-main>
					<div v-for="(group,index) in groupStore.groups" :key="group.id">
						<group-item v-show="group.name.startsWith(searchText)" :group="group" :index="index" :active="index === groupStore.activeIndex"
						 @click.native="handleActiveItem(group,index)">
						</group-item>
					</div>
				</el-main>
			</el-main>
		</el-aside>
		<el-container class="r-chat-box">
			group
		</el-container>
	</el-container>
</template>


<script>
	import GroupItem from '../components/group/GroupItem';

	export default {
		name: "group",
		components: {
			GroupItem
		},
		data() {
			return {
				searchText: ""
			};
		},
		methods: {
			handleCreateGroup() {
				this.$prompt('请输入群聊名称', '创建群聊', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					inputPattern: /\S/,
					inputErrorMessage: '请输入群聊名称'
				}).then((o) => {
					this.$http({
						url: `/api/group/create?groupName=${o.value}`,
						method: 'post'
					}).then((groupInfo) => {
						console.log(groupInfo);
					})
				})
			},
			handleActiveItem(group, index) {
				this.$store.commit("activeGroup", index);
			}
		},
		computed: {
			groupStore() {
				return this.$store.state.groupStore;
			}
		}
	}
</script>

<style lang="scss">
	.im-group-box {
		.l-group-box {
			border: #dddddd solid 1px;
			background: #eeeeee;

			.l-group-header {
				display: flex;
				align-items: center;
				padding: 5px;
				background-color: white;

				.l-group-search {
					flex: 1;
				}
			}
		}
	}
</style>

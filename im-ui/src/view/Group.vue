<template>
	<el-container class="group-page">
		<el-aside width="260px" class="group-list-box">
			<div class="group-list-header">
				<div class="group-list-search">
					<el-input width="200px" placeholder="搜索群聊" v-model="searchText">
						<el-button slot="append" icon="el-icon-search"></el-button>
					</el-input>
				</div>
				<el-button plain icon="el-icon-plus" style="border: none; padding: 12px; font-size: 20px;color: black;"
					title="创建群聊" @click="onCreateGroup()"></el-button>
			</div>
			<el-scrollbar class="group-list-items">
				<div v-for="(group,index) in groupStore.groups" :key="index">
					<group-item v-show="group.remark.startsWith(searchText)" :group="group"
						:active="index === groupStore.activeIndex" @click.native="onActiveItem(group,index)">
					</group-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="group-box">
			<div class="group-header" v-show="activeGroup.id">
				{{activeGroup.remark}}({{groupMembers.length}})
			</div>
			<el-scrollbar class="group-container">
				<div v-show="activeGroup.id">
					<div class="group-info">
						<div>
							<file-upload  v-show="isOwner" class="avatar-uploader" :action="imageAction"
								:showLoading="true" :maxSize="maxSize" @success="onUploadSuccess"
								:fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp']">
								<img v-if="activeGroup.headImage" :src="activeGroup.headImage" class="avatar">
								<i v-else class="el-icon-plus avatar-uploader-icon"></i>
							</file-upload>
							<head-image  v-show="!isOwner" class="avatar" :size="200" 
								:url="activeGroup.headImage"
								:name="activeGroup.remark">
							</head-image>
							<el-button class="send-btn" icon="el-icon-chat-dot-round" type="primary" @click="onSendMessage()">发送消息</el-button>
						</div>
						<el-form class="group-form" label-width="130px" :model="activeGroup" :rules="rules"
							ref="groupForm">
							<el-form-item label="群聊名称" prop="name">
								<el-input v-model="activeGroup.name" :disabled="!isOwner" maxlength="20"></el-input>
							</el-form-item>
							<el-form-item label="群主">
								<el-input :value="ownerName" disabled></el-input>
							</el-form-item>
							<el-form-item label="备注">
								<el-input v-model="activeGroup.remark" placeholder="群聊的备注仅自己可见"
									maxlength="20"></el-input>
							</el-form-item>
							<el-form-item label="我在本群的昵称">
								<el-input v-model="activeGroup.aliasName" placeholder="" maxlength="20"></el-input>
							</el-form-item>
							<el-form-item label="群公告">
								<el-input v-model="activeGroup.notice" :disabled="!isOwner" type="textarea"
									maxlength="1024" placeholder="群主未设置"></el-input>
							</el-form-item>
							<div>
								<el-button type="success" @click="onSaveGroup()">提交</el-button>
								<el-button type="danger" v-show="!isOwner" @click="onQuit()">退出群聊</el-button>
								<el-button type="danger" v-show="isOwner" @click="onDissolve()">解散群聊</el-button>
							</div>
						</el-form>
					</div>
					<el-divider content-position="center"></el-divider>
					<el-scrollbar style="height:200px;">
						<div class="group-member-list">
							<div v-for="(member) in groupMembers" :key="member.id">
								<group-member v-show="!member.quit" class="group-member" :member="member"
									:showDel="isOwner&&member.userId!=activeGroup.ownerId"
									@del="onKick"></group-member>
							</div>
							<div class="group-invite">
								<div class="invite-member-btn" title="邀请好友进群聊" @click="onInviteMember()">
									<i class="el-icon-plus"></i>
								</div>
								<div class="invite-member-text">邀请</div>
								<add-group-member :visible="showAddGroupMember" :groupId="activeGroup.id"
									:members="groupMembers" @reload="loadGroupMembers"
									@close="onCloseAddGroupMember"></add-group-member>
							</div>
						</div>
					</el-scrollbar>
				</div>
			</el-scrollbar>
		</el-container>
	</el-container>
</template>


<script>
	import GroupItem from '../components/group/GroupItem';
	import FileUpload from '../components/common/FileUpload';
	import GroupMember from '../components/group/GroupMember.vue';
	import AddGroupMember from '../components/group/AddGroupMember.vue';
	import HeadImage from '../components/common/HeadImage.vue';
	export default {
		name: "group",
		components: {
			GroupItem,
			GroupMember,
			FileUpload,
			AddGroupMember,
			HeadImage
		},
		data() {
			return {
				searchText: "",
				maxSize: 5 * 1024 * 1024,
				activeGroup: {},
				groupMembers: [],
				showAddGroupMember: false,
				rules: {
					name: [{
						required: true,
						message: '请输入群聊名称',
						trigger: 'blur'
					}]
				}
			};
		},
		methods: {
			onCreateGroup() {
				this.$prompt('请输入群聊名称', '创建群聊', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					inputPattern: /\S/,
					inputErrorMessage: '请输入群聊名称'
				}).then((o) => {
					let userInfo = this.$store.state.userStore.userInfo;
					let data = {
						name: o.value,
						remark: o.value,
						aliasName: userInfo.name,
						ownerId: userInfo.id
					}
					this.$http({
						url: `/group/create?groupName=${o.value}`,
						method: 'post',
						data: data
					}).then((group) => {
						this.$store.commit("addGroup", group);
					})
				})
			},
			onActiveItem(group, index) {
				this.$store.commit("activeGroup", index);
				// store数据不能直接修改，所以深拷贝一份内存
				this.activeGroup = JSON.parse(JSON.stringify(group));
				// 重新加载群成员
				this.loadGroupMembers();
			},
			onInviteMember() {
				this.showAddGroupMember = true;
			},
			onCloseAddGroupMember() {
				this.showAddGroupMember = false;
			},
			onUploadSuccess(res) {
				this.activeGroup.headImage = res.data.originUrl;
				this.activeGroup.headImageThumb = res.data.thumbUrl;
			},
			onSaveGroup() {
				this.$refs['groupForm'].validate((valid) => {
					if (valid) {
						let vo = this.activeGroup;
						this.$http({
							url: "/group/modify",
							method: "put",
							data: vo
						}).then((group) => {
							this.$store.commit("updateGroup", group);
							this.$message.success("修改成功");
						})
					}
				});
			},
			onDissolve() {
				this.$confirm('确认要解散群聊吗?', '确认解散?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/delete/${this.activeGroup.id}`,
						method: 'delete'
					}).then(() => {
						this.$message.success(`群聊'${this.activeGroup.name}'已解散`);
						this.$store.commit("removeGroup", this.activeGroup.id);
						this.$store.commit("removeGroupChat", this.activeGroup.id);
						this.reset();
					});
				})

			},
			onKick(member) {
				this.$confirm(`确定将成员'${member.aliasName}'移出群聊吗？`, '确认移出?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/kick/${this.activeGroup.id}`,
						method: 'delete',
						params: {
							userId: member.userId
						}
					}).then(() => {
						this.$message.success(`已将${member.aliasName}移出群聊`);
						member.quit = true;
					});
				})

			},
			onQuit() {
				this.$confirm('退出群聊后将不再接受群里的消息，确认退出吗？', '确认退出?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/quit/${this.activeGroup.id}`,
						method: 'delete'
					}).then(() => {
						this.$store.commit("removeGroup", this.activeGroup.id);
						this.$store.commit("removeGroupChat", this.activeGroup.id);
						this.reset();
					});
				})

			},
			onSendMessage() {
				let chat = {
					type: 'GROUP',
					targetId: this.activeGroup.id,
					showName: this.activeGroup.remark,
					headImage: this.activeGroup.headImage,
				};
				this.$store.commit("openChat", chat);
				this.$store.commit("activeChat", 0);
				this.$router.push("/home/chat");
			},
			loadGroupMembers() {
				this.$http({
					url: `/group/members/${this.activeGroup.id}`,
					method: "get"
				}).then((members) => {
					this.groupMembers = members;
				})
			},
			reset(){
				this.activeGroup={};
				this.groupMembers=[];
			}
		},
		computed: {
			groupStore() {
				return this.$store.state.groupStore;
			},
			ownerName() {
				let member = this.groupMembers.find((m) => m.userId == this.activeGroup.ownerId);
				return member && member.aliasName;
			},
			isOwner() {
				return this.activeGroup.ownerId == this.$store.state.userStore.userInfo.id;
			},
			imageAction() {
				return `${process.env.VUE_APP_BASE_API}/image/upload`;
			}
		},
		mounted() {
			if (this.groupStore.activeIndex >= 0) {
				let activeGroup = this.groupStore.groups[this.groupStore.activeIndex];
				// store数据不能直接修改，所以深拷贝一份内存
				this.activeGroup = JSON.parse(JSON.stringify(activeGroup));
				// 加载群成员
				this.loadGroupMembers();
			}
		}
	}
</script>

<style lang="scss">
	.group-page {
		.group-list-box {
			display: flex;
			flex-direction: column;
			border: #dddddd solid 1px;
			background: white;

			.group-list-header {
				height: 50px;
				display: flex;
				align-items: center;
				padding: 5px;
				background-color: white;

				.group-list-search {
					flex: 1;
				}
			}

			.group-list-items {
				flex: 1;
			}
		}

		.group-box {
			display: flex;
			flex-direction: column;
			border: #dddddd solid 1px;

			.group-header {
				width: 100%;
				height: 50px;
				padding: 5px;
				line-height: 50px;
				font-size: 20px;
				font-weight: 600;
				text-align: left;
				text-indent: 10px;
				background-color: white;
				border: #dddddd solid 1px;
			}

			.group-container {
				padding: 20px;
				.group-info {
					display: flex;
					padding: 5px 20px;

					.group-form {
						flex: 1;
						padding-left: 40px;
						max-width: 700px;
					}

					.avatar-uploader {
						text-align: left;

						.el-upload {
							border: 1px dashed #d9d9d9 !important;
							border-radius: 6px;
							cursor: pointer;
							position: relative;
							overflow: hidden;
						}

						.el-upload:hover {
							border-color: #409EFF;
						}

						.avatar-uploader-icon {
							font-size: 28px;
							color: #8c939d;
							width: 200px;
							height: 200px;
							line-height: 200px;
							text-align: center;
						}

						.avatar {
							width: 200px;
							height: 200px;
							display: block;
						}
					}

					.send-btn {
						margin-top: 20px;
					}
				}

				.group-member-list {
					padding: 5px 20px;
					display: flex;
					align-items: center;
					flex-wrap: wrap;
					font-size: 16px;
					text-align: center;

					.group-member {
						margin-right: 15px;
					}

					.group-invite {
						display: flex;
						flex-direction: column;
						align-items: center;
						width: 60px;

						.invite-member-btn {
							width: 100%;
							height: 60px;
							line-height: 60px;
							border: #cccccc solid 1px;
							font-size: 25px;
							cursor: pointer;
							box-sizing: border-box;

							&:hover {
								border: #aaaaaa solid 1px;
							}
						}

						.invite-member-text {
							font-size: 16px;
							text-align: center;
							width: 100%;
							height: 30px;
							line-height: 30px;
							white-space: nowrap;
							text-overflow: ellipsis;
							overflow: hidden
						}
					}

				}
			}
		}
	}
</style>
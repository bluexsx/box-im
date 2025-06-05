<template>
	<el-container class="group-page">
		<el-aside width="260px" class="aside">
			<div class="header">
				<el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
				<el-button plain class="add-btn" icon="el-icon-plus" title="创建群聊" @click="onCreateGroup()"></el-button>
			</div>
			<el-scrollbar class="group-items">
				<div v-for="(groups, i) in groupValues" :key="i">
					<div class="letter">{{ groupKeys[i] }}</div>
					<div v-for="group in groups" :key="group.id">
						<group-item :group="group" :active="group.id == activeGroup.id"
							@click.native="onActiveItem(group)">
						</group-item>
					</div>
					<div v-if="i < groupValues.length - 1" class="divider"></div>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="container">
			<div class="header" v-show="activeGroup.id">{{ activeGroup.showGroupName }}({{ showMembers.length }})</div>
			<div class="container-box" v-show="activeGroup.id">
				<div class="group-info">
					<div>
						<file-upload v-show="isOwner" class="avatar-uploader" :action="imageAction" :showLoading="true"
							:maxSize="maxSize" @success="onUploadSuccess"
							:fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp']">
							<img v-if="activeGroup.headImage" :src="activeGroup.headImage" class="avatar">
							<i v-else class="el-icon-plus avatar-uploader-icon"></i>
						</file-upload>
						<head-image v-show="!isOwner" class="avatar" :size="160" :url="activeGroup.headImage"
							:name="activeGroup.showGroupName" radius="10%" @click.native="showFullImage()">
						</head-image>
						<el-button class="send-btn" icon="el-icon-position" type="primary" @click="onSendMessage()">发消息
						</el-button>
					</div>
					<el-form class="form" label-width="130px" :model="activeGroup" :rules="rules" size="small"
						ref="groupForm">
						<el-form-item label="群聊名称" prop="name">
							<el-input v-model="activeGroup.name" :disabled="!isOwner" maxlength="20"></el-input>
						</el-form-item>
						<el-form-item label="群主">
							<el-input :value="ownerName" disabled></el-input>
						</el-form-item>
						<el-form-item label="群名备注">
							<el-input v-model="activeGroup.remarkGroupName" :placeholder="activeGroup.name"
								maxlength="20"></el-input>
						</el-form-item>
						<el-form-item label="我在本群的昵称">
							<el-input v-model="activeGroup.remarkNickName" maxlength="20"
								:placeholder="userStore.userInfo.nickName"></el-input>
						</el-form-item>
						<el-form-item label="群公告">
							<el-input v-model="activeGroup.notice" :disabled="!isOwner" type="textarea" :rows="3"
								maxlength="1024" placeholder="群主未设置"></el-input>
						</el-form-item>
						<div>
							<el-button type="warning" @click="onInvite()">邀请</el-button>
							<el-button type="success" @click="onSaveGroup()">保存</el-button>
							<el-button type="danger" v-show="!isOwner" @click="onQuit()">退出</el-button>
							<el-button type="danger" v-show="isOwner" @click="onDissolve()">解散</el-button>
						</div>
					</el-form>
				</div>
				<el-divider content-position="center"></el-divider>
				<el-scrollbar ref="scrollbar" :style="'height: ' + scrollHeight + 'px'">
					<div class="member-items">
						<div class="member-tools">
							<div class="tool-btn" title="邀请好友进群聊" @click="onInvite()">
								<i class="el-icon-plus"></i>
							</div>
							<div class="tool-text">邀请</div>
							<add-group-member ref="addGroupMember" :groupId="activeGroup.id" :members="groupMembers"
								@reload="loadGroupMembers"></add-group-member>
						</div>
						<div class="member-tools" v-if="isOwner">
							<div class="tool-btn" title="选择成员移出群聊" @click="onRemove()">
								<i class="el-icon-minus"></i>
							</div>
							<div class="tool-text">移除</div>
							<group-member-selector ref="removeSelector" title="选择成员进行移除" :group="activeGroup"
								@complete="onRemoveComplete"></group-member-selector>
						</div>
						<div v-for="(member, idx) in showMembers" :key="member.id">
							<group-member v-if="idx < showMaxIdx" class="member-item" :member="member"></group-member>
						</div>
					</div>
				</el-scrollbar>
			</div>
		</el-container>
	</el-container>
</template>


<script>
import GroupItem from '../components/group/GroupItem';
import FileUpload from '../components/common/FileUpload';
import GroupMember from '../components/group/GroupMember.vue';
import AddGroupMember from '../components/group/AddGroupMember.vue';
import GroupMemberSelector from '../components/group/GroupMemberSelector.vue';
import HeadImage from '../components/common/HeadImage.vue';
import { pinyin } from 'pinyin-pro';

export default {
	name: "group",
	components: {
		GroupItem,
		GroupMember,
		FileUpload,
		AddGroupMember,
		GroupMemberSelector,
		HeadImage
	},
	data() {
		return {
			searchText: "",
			maxSize: 5 * 1024 * 1024,
			activeGroup: {},
			groupMembers: [],
			showAddGroupMember: false,
			showMaxIdx: 150,
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
			}).then(o => {
				let data = {
					name: o.value
				}
				this.$http({
					url: `/group/create?groupName=${o.value}`,
					method: 'post',
					data: data
				}).then((group) => {
					this.groupStore.addGroup(group);
					this.onActiveItem(group)
					this.$message.success('创建成功');
				})
			})
		},
		onActiveItem(group) {
			this.showMaxIdx = 150;
			// store数据不能直接修改，所以深拷贝一份内存
			this.activeGroup = JSON.parse(JSON.stringify(group));
			// 重新加载群成员
			this.groupMembers = [];
			this.loadGroupMembers();
		},
		onInvite() {
			this.$refs.addGroupMember.open();
		},
		onRemove() {
			// 群主不显示
			let hideIds = [this.activeGroup.ownerId];
			this.$refs.removeSelector.open(50, [], [], hideIds);
		},
		onRemoveComplete(members) {
			let userIds = members.map(m => m.userId);
			let data = {
				groupId: this.activeGroup.id,
				userIds: userIds
			}
			this.$http({
				url: "/group/members/remove",
				method: 'delete',
				data: data
			}).then(() => {
				this.loadGroupMembers();
				this.$message.success(`您移除了${userIds.length}位成员`);
			})
		},
		onUploadSuccess(data) {
			this.activeGroup.headImage = data.originUrl;
			this.activeGroup.headImageThumb = data.thumbUrl;
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
						this.groupStore.updateGroup(group);
						this.$message.success("修改成功");
					})
				}
			});
		},
		onDissolve() {
			this.$confirm(`确认要解散'${this.activeGroup.name}'吗?`, '确认解散?', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				this.$http({
					url: `/group/delete/${this.activeGroup.id}`,
					method: 'delete'
				}).then(() => {
					this.$message.success(`群聊'${this.activeGroup.name}'已解散`);
					this.groupStore.removeGroup(this.activeGroup.id);
					this.reset();
				});
			})
		},
		onQuit() {
			this.$confirm(`确认退出'${this.activeGroup.showGroupName}',并清空聊天记录吗？`, '确认退出?', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				this.$http({
					url: `/group/quit/${this.activeGroup.id}`,
					method: 'delete'
				}).then(() => {
					this.$message.success(`您已退出'${this.activeGroup.name}'`);
					this.groupStore.removeGroup(this.activeGroup.id);
					this.chatStore.removeGroupChat(this.activeGroup.id);
					this.reset();
				});
			})
		},
		onSendMessage() {
			let chat = {
				type: 'GROUP',
				targetId: this.activeGroup.id,
				showName: this.activeGroup.showGroupName,
				headImage: this.activeGroup.headImage,
			};
			this.chatStore.openChat(chat);
			this.chatStore.setActiveChat(0);
			this.$router.push("/home/chat");
		},
		onScroll(e) {
			const scrollbar = e.target;
			// 滚到底部
			if (scrollbar.scrollTop + scrollbar.clientHeight >= scrollbar.scrollHeight - 30) {
				if (this.showMaxIdx < this.showMembers.length) {
					this.showMaxIdx += 50;
				}
			}
		},
		showFullImage() {
			if (this.activeGroup.headImage) {
				this.$eventBus.$emit("openFullImage", this.activeGroup.headImage);
			}
		},
		loadGroupMembers() {
			this.$http({
				url: `/group/members/${this.activeGroup.id}`,
				method: "get"
			}).then((members) => {
				this.groupMembers = members;
			})
		},
		reset() {
			this.activeGroup = {};
			this.groupMembers = [];
		},
		firstLetter(strText) {
			// 使用pinyin-pro库将中文转换为拼音
			let pinyinOptions = {
				toneType: 'none', // 无声调
				type: 'normal' // 普通拼音
			};
			let pyText = pinyin(strText, pinyinOptions);
			return pyText[0];
		},
		isEnglish(character) {
			return /^[A-Za-z]+$/.test(character);
		}
	},
	computed: {
		ownerName() {
			let member = this.groupMembers.find(m => m.userId == this.activeGroup.ownerId);
			return member && member.showNickName;
		},
		isOwner() {
			return this.activeGroup.ownerId == this.userStore.userInfo.id;
		},
		imageAction() {
			return `/image/upload`;
		},
		groupMap() {
			// 按首字母分组
			let map = new Map();
			this.groupStore.groups.forEach((g) => {
				if (g.quit || (this.searchText && !g.showGroupName.includes(this.searchText))) {
					return;
				}
				let letter = this.firstLetter(g.showGroupName).toUpperCase();
				// 非英文一律为#组
				if (!this.isEnglish(letter)) {
					letter = "#"
				}
				if (map.has(letter)) {
					map.get(letter).push(g);
				} else {
					map.set(letter, [g]);
				}
			})
			// 排序
			let arrayObj = Array.from(map);
			arrayObj.sort((a, b) => {
				// #组在最后面
				if (a[0] == '#' || b[0] == '#') {
					return b[0].localeCompare(a[0])
				}
				return a[0].localeCompare(b[0])
			})
			map = new Map(arrayObj.map(i => [i[0], i[1]]));
			return map;
		},
		groupKeys() {
			return Array.from(this.groupMap.keys());
		},
		groupValues() {
			return Array.from(this.groupMap.values());
		},
		showMembers() {
			return this.groupMembers.filter((m) => !m.quit)
		},
		scrollHeight() {
			return Math.min(300, 80 + this.showMembers.length / 10 * 80);
		}
	},
	mounted() {
		let scrollWrap = this.$refs.scrollbar.$el.querySelector('.el-scrollbar__wrap');
		scrollWrap.addEventListener('scroll', this.onScroll);
	}
}
</script>

<style lang="scss" scoped>
.group-page {
	.aside {
		display: flex;
		flex-direction: column;
		background: var(--im-background);

		.header {
			height: 50px;
			display: flex;
			align-items: center;
			padding: 0 8px;

			.add-btn {
				padding: 5px !important;
				margin: 5px;
				font-size: 16px;
				border-radius: 50%;
			}
		}

		.group-items {
			flex: 1;

			.letter {
				text-align: left;
				font-size: var(--im-larger-size-larger);
				padding: 5px 15px;
				color: var(--im-text-color-light);
			}
		}
	}

	.container {
		display: flex;
		flex-direction: column;

		.header {
			display: flex;
			justify-content: space-between;
			padding: 0 12px;
			line-height: 50px;
			font-size: var(--im-font-size-larger);
			border-bottom: var(--im-border);
		}

		.el-divider--horizontal {
			margin: 16px 0;
		}

		.container-box {
			overflow: auto;
			padding: 20px;
			flex: 1;

			.group-info {
				display: flex;
				padding: 5px 20px;

				.form {
					flex: 1;
					padding-left: 40px;
					max-width: 700px;
				}

				.avatar-uploader {
					--width: 160px;
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
						width: var(--width);
						height: var(--width);
						line-height: var(--width);
						text-align: center;
					}

					.avatar {
						width: var(--width);
						height: var(--width);
						display: block;
					}
				}

				.send-btn {
					margin-top: 12px;
				}
			}

			.member-items {
				padding: 0 12px;
				display: flex;
				align-items: center;
				flex-wrap: wrap;
				text-align: center;

				.member-item {
					margin-right: 5px;
				}

				.member-tools {
					display: flex;
					flex-direction: column;
					align-items: center;
					width: 60px;

					.tool-btn {
						width: 38px;
						height: 38px;
						line-height: 38px;
						border: var(--im-border);
						font-size: 14px;
						cursor: pointer;
						box-sizing: border-box;

						&:hover {
							border: #aaaaaa solid 1px;
						}
					}

					.tool-text {
						font-size: var(--im-font-size-smaller);
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
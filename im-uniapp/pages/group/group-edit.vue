<template>
	<view v-if="$store.state.userStore.userInfo.type == 1" class="page group-edit">
		<uni-forms ref="form" :modelValue="group" :rules="rules" validate-trigger="bind" label-position="top"
			label-width="100%">
			<uni-forms-item label="群聊头像:" name="headImage">
				<image-upload v-show="isOwner" :onSuccess="onUnloadImageSuccess">
					<image :src="group.headImage" class="group-image"></image>
				</image-upload>
				<head-image  v-show="!isOwner" :name="group.showGroupName" 
					:url="group.headImage" :size="200"></head-image>
			</uni-forms-item>
			<uni-forms-item label="群聊名称:" name="name" :required="true">
				<uni-easyinput type="text" v-model="group.name" :disabled="!isOwner" placeholder="请输入群聊名称" />
			</uni-forms-item>
			<uni-forms-item label="群聊备注:" name="remarkGroupName">
				<uni-easyinput v-model="group.remarkGroupName" type="text" :placeholder="group.name" />
			</uni-forms-item>
			<uni-forms-item label="我在本群的昵称:" name="remarkNickName">
				<uni-easyinput v-model="group.remarkNickName" type="text" :placeholder="$store.state.userStore.userInfo.nickName" />
			</uni-forms-item>
			<uni-forms-item label="群公告:" name="notice">
				<uni-easyinput type="textarea" v-model="group.notice" :disabled="!isOwner" placeholder="请输入群公告" />
			</uni-forms-item>
		</uni-forms>
		<button type="primary" @click="submit()">提交</button>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				group: {},
				rules: {
					name: {
						rules: [{
							required: true,
							errorMessage: '请输入群聊名称',
						}]
					}

				}
			}
		},

		methods: {
			submit() {
				if (this.group.id) {
					this.modifyGroup();
				} else {
					this.createNewGroup();
				}
			},
			onUnloadImageSuccess(file, res) {
				this.group.headImage = res.data.originUrl;
				this.group.headImageThumb = res.data.thumbUrl;
			},
			modifyGroup() {
				this.$http({
					url: "/group/modify",
					method: "PUT",
					data: this.group
				}).then((group) => {
					this.$store.commit("updateGroup", group);
					uni.showToast({
						title: "修改群聊信息成功",
						icon: 'none'
					});
					setTimeout(() => {
						let pages = getCurrentPages();
						let prevPage = pages[pages.length - 2];
						prevPage.$vm.loadGroupInfo();
						uni.navigateBack();
					}, 1000);

				})
			},
			createNewGroup() {
				this.$http({
					url: "/group/create",
					method: 'POST',
					data: this.group
				}).then((group) => {
					this.$store.commit("addGroup", group);
					uni.showToast({
						title: `群聊创建成功，快邀请小伙伴进群吧`,
						icon: 'none',
						duration: 1500
					});
					setTimeout(() => {
						uni.navigateTo({
							url: "/pages/group/group-info?id=" + group.id
						});
					}, 1500)

				})
			},
			loadGroupInfo(id) {
				this.$http({
					url: `/group/find/${id}`,
					method: 'GET'
				}).then((group) => {
					this.group = group;
					// 更新聊天页面的群聊信息
					this.$store.commit("updateChatFromGroup", group);
					// 更新聊天列表的群聊信息
					this.$store.commit("updateGroup", group);

				});
			},
			initNewGroup() {
				let userInfo = this.$store.state.userStore.userInfo;
				this.group = {
					name: `${userInfo.userName}创建的群聊`,
					headImage: userInfo.headImage,
					headImageThumb: userInfo.headImageThumb,
					ownerId: this.$store.state.userStore.userInfo.id
				}
			}
		},
		computed: {
			isOwner() {
				return this.$store.state.userStore.userInfo.id == this.group.ownerId
			}
		},
		onLoad(options) {
			if (options.id) {
				// 修改群聊
				this.loadGroupInfo(options.id);
			} else {
				// 创建群聊
				this.initNewGroup();
			}

		}
	}
</script>

<style lang="scss" scoped>
	.group-edit {
		padding: 20rpx;

		.group-image {
			width: 200rpx;
			height: 200rpx;
			border: 1px solid #ccc;
			border-radius: 5%;
		}
	}
</style>
<template>
	<view class="page group-edit">
		<uni-forms ref="form" :modelValue="group" :rules="rules" validate-trigger="bind" label-position="top"
			label-width="100%">
			<uni-forms-item label="群聊头像:" name="headImage">
				<image-upload :disabled="!isOwner" :onSuccess="onUnloadImageSuccess">
					<image :src="group.headImage" class="head-image"></image>
				</image-upload>
			</uni-forms-item>
			<uni-forms-item label="群聊名称:" name="name" :required="true">
				<uni-easyinput type="text" v-model="group.name" :disabled="!isOwner" placeholder="请输入群聊名称" />
			</uni-forms-item>
			<uni-forms-item label="群聊备注:" name="remark">
				<uni-easyinput v-model="group.remark" type="text" placeholder="请输入群聊备注" />
			</uni-forms-item>
			<uni-forms-item label="我在本群的昵称:" name="email">
				<uni-easyinput v-model="group.aliasName" type="text" placeholder="请输入群聊昵称" />
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
				console.log(res)
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
					setTimeout(()=>{
						uni.navigateBack();
					},1000);
					
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
					setTimeout(()=>{
						uni.navigateTo({
							url: "/pages/group/group-info?id="+group.id
						});
					},1500)
					
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
					aliasName: userInfo.nickName,
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

		.head-image {
			width: 200rpx;
			height: 200rpx;
		}
	}
</style>
<template>
	<view  class="page group-edit">
		<nav-bar back>修改群资料</nav-bar>
		<view class="form">
			<view class="form-item">
				<view class="label">群聊头像</view>
				<view class="value"></view>
				<image-upload v-if="isOwner" :onSuccess="onUnloadImageSuccess">
					<image :src="group.headImageThumb" class="group-image"></image>
				</image-upload>
				<head-image v-else class="group-image" :name="group.showGroupName" :url="group.headImageThumb"
					:size="120"></head-image>
			</view>
			<view class="form-item">
				<view class="label">群聊名称</view>
				<input class="input" :class="isOwner?'':'disable'" maxlength="20"  v-model="group.name" :disabled="!isOwner" placeholder="请输入群聊名称"/>
			</view>
			<view class="form-item">
				<view class="label">群聊备注</view>
				<input class="input" maxlength="20"  v-model="group.remarkGroupName"  :placeholder="group.name"/>
			</view>
			<view class="form-item">
				<view class="label">我在本群的昵称</view>
				<input class="input" maxlength="20"  v-model="group.remarkNickName"  :placeholder="userStore.userInfo.nickName"/>
			</view>
			<view class="form-item">
				<view class="label">群公告</view>
				<textarea class="notice" :class="isOwner?'':'disable'" maxlength="512" :disabled="!isOwner" v-model="group.notice" :placeholder="isOwner?'请输入群公告':''"></textarea>
			</view>
		</view>	
		<button class="bottom-btn" type="primary" @click="submit()">提交</button>
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
				this.groupStore.updateGroup(group);
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
				this.groupStore.addGroup(group);
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
				this.chatStore.updateChatFromGroup(group);
				// 更新聊天列表的群聊信息
				this.groupStore.updateGroup(group);

			});
		},
		initNewGroup() {
			let userInfo = this.userStore.userInfo;
			this.group = {
				name: `${userInfo.userName}创建的群聊`,
				headImage: userInfo.headImage,
				headImageThumb: userInfo.headImageThumb,
				ownerId: this.userStore.userInfo.id
			}
		}
	},
	computed: {
		isOwner() {
			return this.userStore.userInfo.id == this.group.ownerId
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

	.form {
		margin-top: 20rpx;
	
		.form-item {
			padding: 0 40rpx;
			display: flex;
			background: white;
			align-items: center;
			margin-bottom: 2rpx;
	
			.label {
				width: 220rpx;
				line-height: 100rpx;
				font-size: $im-font-size;
				white-space: nowrap;
			}
			
			.value{
				flex: 1;
			}
			
			.input {
				flex: 1;
				text-align: right;
				line-height: 100rpx;
				font-size: $im-font-size-small;
			}
			
			.disable {
				color: $im-text-color-lighter;
			}
			
			.notice {
				flex: 1;
				font-size: $im-font-size-small;
				max-height: 200rpx;
				padding: 14rpx 0;
			}
			
			.group-image {
				width: 120rpx;
				height: 120rpx;
				border-radius: 5%;
				border: 1px solid #ccc;
			}
		}
	}
}

</style>

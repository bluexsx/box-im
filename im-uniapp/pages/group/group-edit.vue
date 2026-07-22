<template>
	<view class="page group-edit">
		<nav-bar back>修改群资料</nav-bar>
		<view class="form">
			<view class="form-item">
				<view class="label">群聊头像</view>
				<view class="value"></view>
				<image-upload v-if="isOwner" :isPermanent="true" :thumbSize="20" :onSuccess="onUnloadImageSuccess">
					<image :src="group.headImageThumb" class="group-image"></image>
				</image-upload>
				<head-image v-else class="group-image" :name="group.showGroupName" :url="group.headImageThumb"
					:size="120" @click="onShowFullImage()"></head-image>
			</view>
			<view class="form-item">
				<view class="label">群聊名称</view>
				<input class="input" :class="isOwner?'':'disable'" maxlength="20" v-model="group.name"
					:disabled="!isOwner" placeholder="请输入群聊名称" />
			</view>
			<view class="form-item">
				<view class="label">群聊备注</view>
				<input class="input" maxlength="20" v-model="group.remarkGroupName" :placeholder="group.name" />
			</view>
			<view class="form-item">
				<view class="label">我在本群的昵称</view>
				<input class="input" maxlength="20" v-model="group.remarkNickName"
					:placeholder="userStore.userInfo.nickName" />
			</view>
			<view class="form-item notice-item">
				<view class="label">群公告</view>
				<textarea class="notice" :class="isOwner?'':'disable'" maxlength="512" :disabled="!isOwner"
					v-model="group.notice" auto-height :placeholder="isOwner?'请输入群公告':''"></textarea>
			</view>
		</view>
		<button class="bottom-btn" type="primary" @click="modifyGroup()">提交</button>
	</view>
</template>

<script>
import { chatStore, groupStore, userStore } from '@/store/stores.js'

export default {
	data() {
		return {
			userStore,
			group: {}
		}
	},
	methods: {
		onUnloadImageSuccess(file, res) {
			this.group.headImage = res.data.originUrl;
			this.group.headImageThumb = res.data.thumbUrl;
		},
		onShowFullImage() {
			let imageUrl = this.group.headImage;
			if (imageUrl) {
				uni.previewImage({
					urls: [imageUrl]
				})
			}
		},
		modifyGroup() {
			this.$http({
				url: "/group/modify",
				method: "PUT",
				data: this.group
			}).then((group) => {
				groupStore.updateGroup(group);
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
		loadGroupInfo(id) {
			this.$http({
				url: `/group/find/${id}`,
				method: 'GET'
			}).then((group) => {
				this.group = group;
				chatStore.updateFromGroup(group);
				groupStore.updateGroup(group);
			});
		}
	},
	computed: {
		isOwner() {
			return userStore.userInfo.id == this.group.ownerId
		}
	},
	onLoad(options) {
		if (!options.id) {
			uni.navigateBack();
			return;
		}
		this.loadGroupInfo(options.id);
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

			.value {
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
				min-height: 320rpx;
				max-height: 800rpx;
				height: 320rpx;
				padding: 14rpx 0;
			}

			.group-image {
				width: 120rpx;
				height: 120rpx;
				border-radius: 50%;
				border: 1px solid #ccc;
			}
		}

		.notice-item {
			flex-direction: column;
			align-items: stretch;
			padding-top: 16rpx;
			padding-bottom: 16rpx;

			.label {
				width: 100%;
				line-height: 56rpx;
			}

			.notice {
				width: 100%;
			}
		}
	}
}
</style>

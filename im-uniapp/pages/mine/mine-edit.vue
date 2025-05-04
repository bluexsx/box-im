<template>
	<view class="page mine-edit">
		<nav-bar back>修改我的信息</nav-bar>
		<view class="form">
			<view class="form-item">
				<view class="label">头像</view>
				<image-upload class="value" :isPermanent="true" :onSuccess="onUnloadImageSuccess">
					<image :src="userInfo.headImageThumb" class="head-image"></image>
				</image-upload>
			</view>
			<view class="form-item">
				<view class="label">用户名</view>
				<view class="value">{{userInfo.userName}}</view>
			</view>
			<view class="form-item">
				<view class="label">昵称</view>
				<input class="input" maxlength="20" v-model="userInfo.nickName" placeholder="请输入您的昵称" />
			</view>
			<view class="form-item">
				<view class="label">性别</view>
				<radio-group class="radio-group" @change="onSexChange">
					<radio class="radio" :value="0" :checked="userInfo.sex==0">男</radio>
					<radio class="radio" :value="1" :checked="userInfo.sex==1">女</radio>
				</radio-group>
			</view>
			<view class="form-item">
				<view class="label">个性签名</view>
				<textarea class="signature" maxlength="64" auto-height v-model="userInfo.signature"
					:style="{'text-align': signTextAlign}" @linechange="onLineChange"
					placeholder="编辑个性签名,展示我的独特态度"></textarea>
			</view>
		</view>
		<button type="primary" class="bottom-btn" @click="onSubmit()">提交</button>
	</view>
</template>

<script>
export default {
	data() {
		return {
			signTextAlign: 'right',
			userInfo: {}
		}
	},
	methods: {
		onSexChange(e) {
			this.userInfo.sex = e.detail.value;
		},
		onUnloadImageSuccess(file, res) {
			this.userInfo.headImage = res.data.originUrl;
			this.userInfo.headImageThumb = res.data.thumbUrl;
		},
		onSubmit() {
			this.$http({
				url: "/user/update",
				method: "PUT",
				data: this.userInfo
			}).then(() => {
				this.userStore.setUserInfo(this.userInfo);
				uni.showToast({
					title: "修改成功",
					icon: 'none'
				});
			})
		},
		onLineChange(e) {
			this.signTextAlign = e.detail.lineCount > 1 ? "left" : "right";
		}
	},
	onLoad() {
		// 深拷贝一份数据
		let mine = this.userStore.userInfo;
		this.userInfo = JSON.parse(JSON.stringify(mine));
	}
}
</script>

<style scoped lang="scss">
.mine-edit {

	.form {
		margin-top: 20rpx;

		.form-item {
			padding: 0 40rpx;
			display: flex;
			background: white;
			align-items: center;
			margin-bottom: 2rpx;

			.label {
				width: 200rpx;
				line-height: 100rpx;
				font-size: $im-font-size;
			}

			.value {
				flex: 1;
				text-align: right;
				line-height: 100rpx;
				color: $im-text-color-lighter;
				font-size: $im-font-size-small;
			}



			.radio-group {
				flex: 1;
				text-align: right;

				.radio {
					margin-left: 50rpx;
				}
			}

			.input {
				flex: 1;
				text-align: right;
				line-height: 100rpx;
				font-size: $im-font-size-small;
			}

			.signature {
				flex: 1;
				font-size: $im-font-size-small;
				max-height: 160rpx;
				padding: 14rpx 0;
				text-align: right;
			}

			.head-image {
				width: 120rpx;
				height: 120rpx;
				border-radius: 50%;
				border: 1px solid #ccc;
			}
		}
	}
}
</style>
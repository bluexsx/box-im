<template>
	<view class="login">
		<!-- 主要内容区域 -->
		<view class="content">
			<!-- Logo和标题区域 -->
			<view class="header">
				<view class="title">欢迎登录</view>
				<view class="subtitle">登录您的账号，开启聊天之旅</view>
			</view>

			<!-- 表单区域 -->
			<view class="form-container">
				<view class="form">
					<view class="form-item" :class="{'focused': userNameFocused}">
						<view class="icon-wrapper">
							<view class="icon iconfont icon-username"></view>
						</view>
						<input class="input" type="text" v-model="dataForm.userName" placeholder="用户名"
							@focus="userNameFocused = true" @blur="userNameFocused = false" />
					</view>
					<view class="form-item" :class="{'focused': passwordFocused}">
						<view class="icon-wrapper">
							<view class="icon iconfont icon-pwd"></view>
						</view>
						<input class="input" :type="isShowPwd?'text':'password'" v-model="dataForm.password"
							placeholder="密码" @focus="passwordFocused = true" @blur="passwordFocused = false" />
						<view class="icon-suffix iconfont" :class="isShowPwd?'icon-pwd-show':'icon-pwd-hide'"
							@click="onSwitchShowPwd"></view>
					</view>
				</view>

				<!-- 登录按钮 -->
				<button class="submit-btn" @click="submit" type="primary">
					<view class="btn-content">
						<text class="btn-text">立即登录</text>
					</view>
				</button>

				<!-- 底部导航 -->
				<view class="nav-tool-bar">
					<view class="nav-register">
						<navigator url="/pages/register/register" class="register-link">
							<text class="register-text">没有账号？</text>
							<text class="register-highlight">立即注册</text>
						</navigator>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js';
export default {
	data() {
		return {
			isShowPwd: false,
			userNameFocused: false,
			passwordFocused: false,
			dataForm: {
				terminal: 1, // APP终端
				userName: '',
				password: ''
			}
		}
	},
	methods: {
		submit() {
			if (!this.dataForm.userName) {
				return uni.showToast({
					title: "请输入您的账号",
					icon: "none"
				})
			}
			if (!this.dataForm.password) {
				return uni.showToast({
					title: "请输入您的密码",
					icon: "none"
				})
			}
			this.$http({
				url: '/login',
				data: this.dataForm,
				method: 'POST'
			}).then(loginInfo => {
				console.log("登录成功,自动跳转到聊天页面...")
				uni.setStorageSync("userName", this.dataForm.userName);
				uni.setStorageSync("password", this.dataForm.password);
				uni.setStorageSync("isAgree", this.isAgree);
				uni.setStorageSync("loginInfo", loginInfo);
				// 调用App.vue的初始化方法
				getApp().$vm.init()
				// 跳转到聊天页面   
				uni.switchTab({
					url: "/pages/chat/chat"
				})
			}).catch((e) => {
				console.log("登录失败：", e);
			})
			// 清理数据，防止缓存未失效
			getApp().$vm.unloadStore();
		},
		onSwitchShowPwd() {
			this.isShowPwd = !this.isShowPwd;
		},
	},
	onLoad() {
		this.dataForm.userName = uni.getStorageSync("userName");
		this.dataForm.password = uni.getStorageSync("password");
	}
}
</script>

<style lang="scss" scoped>
.login {
	position: relative;
	display: flex;
	flex-direction: column;
	overflow: hidden;

	// 主要内容区域
	.content {
		position: relative;
		display: flex;
		flex-direction: column;
		margin-top: 120rpx;
		// #ifdef APP-PLUS
		margin-top: calc(120rpx + var(--status-bar-height));
		// #endif
		padding: 0 60rpx;
	}

	// 头部区域
	.header {
		text-align: center;
		padding: 80rpx 0;

		.title {
			color: $im-color-primary;
			font-size: 48rpx;
			font-weight: 700;
			margin-bottom: 20rpx;
			letter-spacing: 2rpx;
		}

		.subtitle {
			color: $im-text-color-light;
			font-size: 28rpx;
			opacity: 0.8;
		}
	}

	// 表单容器
	.form-container {
		display: flex;
		flex-direction: column;
	}

	// 表单样式
	.form {
		margin-bottom: 20rpx;

		.form-item {
			position: relative;
			display: flex;
			align-items: center;
			padding: 0 30rpx;
			height: 100rpx;
			margin: 24rpx 0;
			border-radius: 25rpx;
			background: rgba(255, 255, 255, 0.9);
			border: 2rpx solid transparent;
			box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
			transition: all 0.3s ease;

			&.focused {
				border-color: $im-color-primary;
				box-shadow: 0 8rpx 32rpx rgba($im-color-primary, 0.15);
				transform: translateY(-2rpx);
			}

			.icon-wrapper {
				display: flex;
				align-items: center;
				justify-content: center;
				width: 60rpx;
				height: 60rpx;
				margin-right: 30rpx;
				border-radius: 50%;
				background: $im-bg-active;
				transition: all 0.3s ease;

				.icon {
					font-size: 32rpx;
					color: $im-color-primary;
					font-weight: bold;
				}
			}

			&.focused .icon-wrapper {
				transform: scale(1.1);
			}

			.input {
				flex: 1;
				font-size: 32rpx;
				color: #333;
				background: transparent;
				border: none;
				outline: none;

				&::placeholder {
					color: $im-text-color-light;
					font-size: 30rpx;
				}
			}

			.icon-suffix {
				font-size: 36rpx;
				padding: 10rpx;
			}
		}
	}

	// 登录按钮
	.submit-btn {
		height: 100rpx;
		border-radius: 50rpx;
		border: none;
		box-shadow: 0 8rpx 32rpx rgba($im-color-primary, 0.3);
		transition: all 0.3s ease;
		overflow: hidden;
		position: relative;
		width: 100%;

		&:active {
			transform: translateY(2rpx);
			box-shadow: 0 4rpx 16rpx rgba($im-color-primary, 0.4);

			&::before {
				left: 100%;
			}
		}

		.btn-content {
			display: flex;
			align-items: center;
			justify-content: center;
			height: 100%;
			color: white;
			font-size: $im-font-size-large;
			font-weight: 600;

			.btn-text {
				margin-right: 10rpx;
			}
		}

		&:active .btn-icon {
			transform: translateX(4rpx);
		}
	}

	// 底部导航
	.nav-tool-bar {
		padding: 40rpx 0 60rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;

		.nav-register {
			.register-link {
				display: flex;
				align-items: center;
				text-decoration: none;

				.register-text {
					color: $im-text-color-light;
					font-size: $im-font-size-small;
					margin-right: 8rpx;
				}

				.register-highlight {
					color: $im-color-primary;
					font-size: $im-font-size-small;
					font-weight: 600;
				}
			}
		}
	}
}
</style>
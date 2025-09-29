<template>
	<view class="register">
		<!-- 主要内容区域 -->
		<view class="content">
			<!-- Logo和标题区域 -->
			<view class="header" @longpress.prevent="onLongPressHeader()">
				<view class="title"> 用户注册 </view>
				<view class="subtitle">创建您的账号，开始精彩旅程</view>
			</view>

			<!-- 表单区域 -->
			<view class="form-container">
				<view class="form">
					<!-- 用户名输入 -->
					<view class="form-item" :class="{'focused': userNameFocused}">
						<view class="icon-wrapper">
							<view class="icon iconfont icon-username"></view>
						</view>
						<input class="input" type="text" v-model="dataForm.userName" placeholder="请填写用户名"
							@focus="userNameFocused = true" @blur="userNameFocused = false" />
					</view>
					
					<!-- 用户名输入 -->
					<view class="form-item" :class="{'focused': nickNameFocused}">
						<view class="icon-wrapper">
							<view class="icon iconfont icon-username"></view>
						</view>
						<input class="input" type="text" v-model="dataForm.nickName" placeholder="请填写昵称"
							@focus="nickNameFocused = true" @blur="nickNameFocused = false" />
					</view>

					<!-- 密码输入 -->
					<view class="form-item" :class="{'focused': passwordFocused}">
						<view class="icon-wrapper">
							<view class="icon iconfont icon-pwd"></view>
						</view>
						<input class="input" :type="isShowPwd?'text':'password'" v-model="dataForm.password"
							placeholder="请设置密码" @focus="passwordFocused = true" @blur="passwordFocused = false" />
						<view class="icon-suffix iconfont" :class="isShowPwd?'icon-pwd-show':'icon-pwd-hide'"
							@click="onSwitchShowPwd"></view>
					</view>

					<!-- 确认密码输入 -->
					<view class="form-item" :class="{'focused': confirmPasswordFocused}">
						<view class="icon-wrapper">
							<view class="icon iconfont icon-pwd"></view>
						</view>
						<input class="input" :type="isShowConfirmPwd?'text':'password'"
							v-model="dataForm.confirmPassword" placeholder="确认密码" @focus="confirmPasswordFocused = true"
							@blur="confirmPasswordFocused = false" />
						<view class="icon-suffix iconfont" :class="isShowConfirmPwd?'icon-pwd-show':'icon-pwd-hide'"
							@click="onSwitchShowConfirmPwd"></view>
					</view>

					<!-- 注册按钮 -->
					<button class="submit-btn" @click="submit" type="primary">
						<view class="btn-content">
							<text class="btn-text">注册并登录</text>
						</view>
					</button>

					<!-- 底部导航 -->
					<view class="nav-tool-bar">
						<view class="nav-login">
							<navigator url="/pages/login/login" class="login-link">
								<text class="login-text">已有账号？</text>
								<text class="login-highlight">立即登录</text>
							</navigator>
						</view>
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
			isShowConfirmPwd: false,
			userNameFocused: false,
			nickNameFocused: false,
			passwordFocused: false,
			confirmPasswordFocused: false,
			dataForm: {
				userName: '',
				nickName: '',
				password: '',
				confirmPassword: ''
			}
		}
	},
	methods: {
		onSwitchShowPwd() {
			this.isShowPwd = !this.isShowPwd;
		},
		onSwitchShowConfirmPwd() {
			this.isShowConfirmPwd = !this.isShowConfirmPwd;
		},
		submit() {
			if (!this.dataForm.userName) {
				return uni.showToast({
					title: '请输入用户名',
					icon: 'none'
				});
			}
			if (!this.dataForm.password) {
				return uni.showToast({
					title: '请输入密码',
					icon: 'none'
				});
			}
			if (!this.dataForm.confirmPassword) {
				return uni.showToast({
					title: '请输入确认密码',
					icon: 'none'
				});
			}
			if (this.dataForm.confirmPassword != this.dataForm.password) {
				return uni.showToast({
					title: '两次密码输入不一致',
					icon: 'none'
				});
			}
			this.$http({
				url: '/register',
				data: this.dataForm,
				method: 'POST'
			}).then(() => {
				let appName = uni.getSystemInfoSync().appName
				uni.showToast({
					title: `注册成功,您已成为${appName}的用户`,
					icon: 'none'
				})
				this.login();
			})
		},
		login() {
			const loginForm = {
				terminal: this.$enums.TERMINAL_TYPE.APP,
				userName: this.dataForm.userName,
				password: this.dataForm.password
			}
			this.$http({
				url: '/login',
				data: loginForm,
				method: 'POST'
			}).then((loginInfo) => {
				console.log("登录成功,自动跳转到聊天页面...")
				uni.setStorageSync("userName", loginForm.userName);
				uni.setStorageSync("password", loginForm.password);
				uni.setStorageSync("loginInfo", loginInfo);
				// 调用App.vue的初始化方法
				getApp().init()
				// 跳转到聊天页面   
				uni.switchTab({
					url: "/pages/chat/chat"
				})
			})
			// 清理数据，防止缓存未失效
			getApp().$vm.unloadStore();
		}
	}
}
</script>

<style lang="scss" scoped>
.register {
	display: flex;
	flex-direction: column;
	position: relative;
	overflow: hidden;

	// 主要内容区域
	.content {
		flex: 1;
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
		height: 830rpx;
		display: flex;
		flex-direction: column;
		justify-content: center;

		.form-item {
			position: relative;
			display: flex;
			align-items: center;
			padding: 0 30rpx;
			height: 100rpx;
			margin: 14rpx 0;
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

	// 注册按钮
	.submit-btn {
		height: 100rpx;
		border-radius: 50rpx;
		border: none;
		box-shadow: 0 8rpx 32rpx rgba($im-color-primary, 0.3);
		transition: all 0.3s ease;
		overflow: hidden;
		position: relative;
		width: 100%;
		margin-top: 50rpx;

		&:active {
			transform: translateY(2rpx);
			box-shadow: 0 4rpx 16rpx rgba($im-color-primary, 0.4);
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
	}

	// 底部导航
	.nav-tool-bar {
		padding: 40rpx 0 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		.nav-login {
			.login-link {
				display: flex;
				align-items: center;
				text-decoration: none;

				.login-text {
					color: $im-text-color-light;
					font-size: $im-font-size-small;
					margin-right: 8rpx;
				}

				.login-highlight {
					color: $im-color-primary;
					font-size: $im-font-size-small;
					font-weight: 600;
					transition: all 0.3s ease;

					&:active {
						opacity: 0.7;
					}
				}
			}
		}
	}
}
</style>
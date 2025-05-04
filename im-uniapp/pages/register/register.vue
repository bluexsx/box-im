<template>
	<view class="register">
		<view class="title">欢迎注册</view>
		<view class="form">
			<uni-forms  ref="form" :modelValue="dataForm" :rules="rules" validate-trigger="bind" label-width="80px">
				<uni-forms-item name="userName" label="用户名">
					<uni-easyinput type="text" v-model="dataForm.userName" placeholder="用户名" />
				</uni-forms-item>
				<uni-forms-item name="nickName" label="昵称">
					<uni-easyinput type="text" v-model="dataForm.nickName" placeholder="昵称" />
				</uni-forms-item>
				<uni-forms-item name="password" label="密码">
					<uni-easyinput type="password" v-model="dataForm.password" placeholder="密码" />
				</uni-forms-item>
				<uni-forms-item name="corfirmPassword" label="确认密码">
					<uni-easyinput type="password" v-model="dataForm.corfirmPassword" placeholder="确认密码" />
				</uni-forms-item>
				<button class="btn-submit" @click="submit" type="primary">注册并登录</button>
			</uni-forms>
		</view>
		<navigator class="nav-login" url="/pages/login/login">
			返回登录页面
		</navigator>
	</view>
</template>

<script>
export default {
	data() {
		return {
			dataForm: {
				userName: '',
				nickName: '',
				password: '',
				corfirmPassword: ''
			},
			rules: {
				userName: {
					rules: [{
						required: true,
						errorMessage: '请输入用户名',
					}]
				},
				nickName: {
					rules: [{
						required: true,
						errorMessage: '请输入昵称',
					}]
				},
				password: {
					rules: [{
						required: true,
						errorMessage: '请输入密码',
					}]
				},
				corfirmPassword: {
					rules: [{
						required: true,
						errorMessage: '请输入确认密码',
					}, {
						validateFunction: (rule, value, data, callback) => {
							if (data.password != value) {
								callback('两次密码输入不一致')
							}
							return true;
						}
					}]
				}
			}
		}
	},
	methods: {
		submit() {
			this.$refs.form.validate().then(() => {
				this.$http({
					url: '/register',
					data: this.dataForm,
					method: 'POST'
				}).then(() => {
					uni.showToast({
						title: "注册成功,您已成为盒子IM的用户",
						icon: 'none'
					})
					this.login();
				})
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
		}
	}
}
</script>

<style lang="scss" scoped>
.register {
	.title {
		padding-top: 250rpx;
		padding-bottom: 50rpx;
		color: $im-color-primary;
		text-align: center;
		font-size: 24px;
		font-weight: 600;
	}

	.form {
		padding: 50rpx;

		.btn-submit {
			margin-top: 80rpx;
			border-radius: 50rpx;
		}
	}

	.nav-login {
		position: fixed;
		width: 100%;
		bottom: 100rpx;
		color: $im-color-primary;
		text-align: center;
		font-size: 32rpx;
	}
}
</style>
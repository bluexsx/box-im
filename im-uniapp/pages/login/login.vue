<template>
	<view class="login-form">
		<view class="login-title">欢迎登录</view>
		<uni-forms style="margin-top: 100px;" :modelValue="loginForm" :rules="rules" validate-trigger="bind">
			<uni-forms-item name="userName">
				<uni-easyinput type="text" v-model="loginForm.userName" prefix-icon="person"  placeholder="用户名" />
			</uni-forms-item>
			<uni-forms-item name="password">
				<uni-easyinput type="password" v-model="loginForm.password" prefix-icon="locked" placeholder="密码" />
			</uni-forms-item>
			<button @click="submit" type="primary">登录</button>
		</uni-forms>

	</view>
</template>

<script>
	export default {
		data() {
			return {
				loginForm: {
					terminal: 1, // APP终端
					userName: '',
					password: ''
				},
				rules: {
					userName: {
						rules: [{
							required: true,
							errorMessage: '请输入用户名',
						}]
					},
					password: {
						rules: [{
							required: true,
							errorMessage: '请输入密码',
						}]
					}
				}
			}
		},
		methods: {
			submit() {
				this.$http({
					url: '/login',
					data: this.loginForm,
					method: 'POST'
				}).then(loginInfo => {
					console.log("登录成功,自动跳转到聊天页面...")
					uni.setStorageSync("userName", this.loginForm.userName);
					uni.setStorageSync("password", this.loginForm.password);
					loginInfo.expireTime = new Date().getTime() + loginInfo.refreshTokenExpiresIn*1000;
					uni.setStorageSync("loginInfo", loginInfo);
					// 调用App.vue的初始化方法
					getApp().init()
					// 跳转到聊天页面   
					uni.switchTab({
						url: "/pages/chat/chat"
					})
				})
			}
		},
		
		onLoad() {
			this.loginForm.userName = uni.getStorageSync("userName");
			this.loginForm.password = uni.getStorageSync("password");
		}
	}
</script>

<style lang="scss" scoped>
	.login-form {
		margin: 50rpx;

		.login-title {
			margin-top: 100rpx;
			margin-bottom: 100rpx;
			color: royalblue;
			text-align: center;
			font-size: 60rpx;
			font-weight: 600;
		}
	}
</style>
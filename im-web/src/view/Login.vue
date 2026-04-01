<template>
	<div class="login-view">
		<div class="auth-bg">
			<div class="auth-bg__base"></div>
			<div class="auth-bg__glows"></div>
			<div class="auth-bg__panels"></div>
			<div class="auth-bg__dots"></div>
		</div>
		<div class="content">
			<el-form class="form" :model="loginForm" status-icon :rules="rules" ref="loginForm"
				@keyup.enter.native="submitForm('loginForm')">
				<div class="form-header">
					<h1 class="form-welcome">欢迎登录</h1>
					<p class="form-welcome-subtitle">一站式即时通讯解决方案</p>
				</div>
				<div class="form-body">
					<el-form-item prop="terminal" v-show="false">
						<el-input type="terminal" v-model="loginForm.terminal" autocomplete="off"></el-input>
					</el-form-item>
					<el-form-item prop="userName">
						<el-input type="userName" v-model="loginForm.userName" autocomplete="off" placeholder="用户名"
							prefix-icon="el-icon-user"></el-input>
					</el-form-item>
					<el-form-item prop="password">
						<el-input type="password" v-model="loginForm.password" autocomplete="off" placeholder="密码"
							prefix-icon="el-icon-lock"></el-input>
					</el-form-item>
					<el-form-item class="nav-tool-bar">
						<el-checkbox v-model="isAutoLogin">自动登录</el-checkbox>
					</el-form-item>
					<el-button class="submit-btn" type="primary" @click="submitForm('loginForm')">登录</el-button>
				</div>
				<div class="footer-links">
					<router-link class="link" to="/register">没有账号，前往注册</router-link>
				</div>
			</el-form>
		</div>
		<icp></icp>
	</div>
</template>

<script>
import Icp from '../components/common/Icp.vue'
export default {
	name: "login",
	components: {
		Icp
	},
	data() {
		var checkUsername = (rule, value, callback) => {
			if (!value) {
				return callback(new Error('请输入用户名'));
			}
			callback();
		};
		var checkPassword = (rule, value, callback) => {
			if (value === '') {
				callback(new Error('请输入密码'));
			}
			callback();

		};
		return {
			loginForm: {
				terminal: this.$enums.TERMINAL_TYPE.WEB,
				userName: '',
				password: ''
			},
			isAutoLogin: true,
			rules: {
				userName: [{
					validator: checkUsername,
					trigger: 'blur'
				}],
				password: [{
					validator: checkPassword,
					trigger: 'blur'
				}]
			}
		};
	},
	methods: {
		submitForm(formName) {
			this.$refs[formName].validate((valid) => {
				if (valid) {
					this.$http({
						url: "/login",
						method: 'post',
						data: this.loginForm
					})
						.then((data) => {
							// 保存密码到cookie(不安全)
							this.setCookie('username', this.loginForm.userName);
							this.setCookie('password', this.loginForm.password);
							// 保存登录信息到本地，兼容新版逻辑
							localStorage.setItem('isAutoLogin', this.isAutoLogin);
							localStorage.setItem('username', this.loginForm.userName);
							localStorage.setItem('password', this.loginForm.password);
							// 保存token
							sessionStorage.setItem("accessToken", data.accessToken);
							sessionStorage.setItem("refreshToken", data.refreshToken);
							this.$message.success("登录成功");
							this.$router.push("/home/chat");
						})

				}
			});
		},
		getCookie(name) {
			let reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
			let arr = document.cookie.match(reg)
			if (arr) {
				return unescape(arr[2]);
			}
			return '';
		},
		setCookie(name, value) {
			document.cookie = name + "=" + escape(value);
		}

	},
	mounted() {
		// 兼容旧版本 cookie 数据
		this.loginForm.userName = this.getCookie("username");
		this.loginForm.password = this.getCookie("password");
		// 加载本地自动登录配置
		if (localStorage.getItem("isAutoLogin") != null) {
			this.isAutoLogin = JSON.parse(localStorage.getItem("isAutoLogin"));
			this.loginForm.userName = localStorage.getItem("username") || this.loginForm.userName;
			this.loginForm.password = localStorage.getItem("password") || this.loginForm.password;
			if (this.isAutoLogin && this.loginForm.userName && this.loginForm.password) {
				this.submitForm('loginForm');
			}
		}
	}
}
</script>

<style scoped lang="scss">
.login-view {
	position: relative;
	width: 100%;
	height: 100%;
	box-sizing: border-box;
	display: flex;
	background: #fff;
	overflow: hidden;

	.auth-bg {
		position: absolute;
		inset: 0;
		pointer-events: none;
	}

	.auth-bg__base,
	.auth-bg__glows,
	.auth-bg__panels,
	.auth-bg__dots {
		position: absolute;
		inset: 0;
		pointer-events: none;
	}

	.auth-bg__base {
		background: linear-gradient(135deg,
				#d4e2f8 0%,
				#dce8fa 25%,
				#e6eefb 50%,
				#eef2fc 75%,
				#f8f9fd 100%);
	}

	.auth-bg__glows {
		background-image:
			radial-gradient(ellipse 70% 50% at 85% 15%, rgba(220, 235, 255, 0.6) 0%, transparent 55%),
			radial-gradient(ellipse 60% 45% at 10% 75%, rgba(230, 240, 255, 0.5) 0%, transparent 50%),
			radial-gradient(ellipse 50% 40% at 50% 50%, rgba(255, 255, 255, 0.4) 0%, transparent 45%);
	}

	.auth-bg__dots {
		opacity: 0.4;
		background-image: radial-gradient(circle, rgba(200, 218, 248, 0.35) 1px, transparent 1px);
		background-size: 24px 24px;
	}

	.auth-bg__panels {
		opacity: 0.5;
		background-image:
			repeating-linear-gradient(115deg,
				transparent 0,
				transparent 60px,
				rgba(190, 210, 245, 0.12) 60px,
				rgba(190, 210, 245, 0.12) 65px),
			repeating-linear-gradient(25deg,
				transparent 0,
				transparent 90px,
				rgba(200, 218, 248, 0.08) 90px,
				rgba(200, 218, 248, 0.08) 95px);
	}

	.content {
		position: relative;
		display: flex;
		justify-content: center;
		align-items: center;
		width: 100%;
		z-index: 1;
	}

	.form {
		width: 380px;
		height: 460px;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		padding: 32px 36px;
		background: rgba(255, 255, 255, 0.88);
		backdrop-filter: blur(24px);
		-webkit-backdrop-filter: blur(24px);
		border-radius: 28px;
		border: 1px solid rgba(255, 255, 255, 0.95);
		box-shadow: 0 20px 50px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.04);
		overflow: visible;
	}

	.form-header {
		flex-shrink: 0;
		text-align: left;
	}

	.form-welcome {
		margin: 0 0 6px;
		font-size: 26px;
		font-weight: 700;
		letter-spacing: 0.5px;
		line-height: 1.3;
		color: var(--im-color-primary);
	}

	.form-welcome-subtitle {
		margin: 0;
		font-size: var(--im-font-size);
		color: var(--im-text-color-light);
		line-height: 1.5;
	}

	.form-body {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
		min-height: 0;
	}

	.form-body ::v-deep .el-form-item {
		margin-bottom: 20px;

		.el-input__inner {
			height: 52px;
			border-radius: 50px;
			border: 1px solid rgba(0, 0, 0, 0.06);
			background: #fff;
			transition: border-color 0.2s ease, box-shadow 0.2s ease;
			padding-left: 48px;
			font-size: var(--im-font-size-large);

			&:focus {
				border-color: rgba(0, 0, 0, 0.1);
				box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.8);
			}

			&::placeholder {
				color: var(--im-text-color-lighter);
				font-size: var(--im-font-size);
			}
		}

		.el-input__prefix {
			left: 18px;
		}

		.el-input__prefix .el-input__icon {
			color: var(--im-color-primary-light-3);
			font-size: 18px;
		}

		.el-input__suffix {
			display: none;
		}
	}

	.submit-btn {
		width: 100%;
		height: 52px;
		margin-top: 20px;
		border-radius: 50px;
		border: none;
		color: #fff;
		font-size: var(--im-font-size-larger);
		font-weight: 600;
		letter-spacing: 2px;
		transition: transform 0.2s ease, box-shadow 0.2s ease;

		&:hover {
			transform: translateY(-1px);
		}
	}

	.footer-links {
		flex-shrink: 0;
		display: flex;
		justify-content: flex-end;
		align-items: center;
		margin-top: 20px;
		padding: 0 4px;

		.link {
			text-decoration: none;
			color: var(--im-text-color-light);
			font-size: var(--im-font-size-small);
			transition: color 0.2s ease;

			&:hover {
				color: var(--im-color-primary);
			}
		}
	}
}
</style>
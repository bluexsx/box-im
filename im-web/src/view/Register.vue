<template>
	<el-container class="register">
		<div class="auth-bg">
			<div class="auth-bg__base"></div>
			<div class="auth-bg__glows"></div>
			<div class="auth-bg__panels"></div>
			<div class="auth-bg__dots"></div>
		</div>
		<div class="content">
			<el-form :model="registerForm" status-icon :rules="rules" ref="registerForm" class="form">
				<div class="form-header">
					<h1 class="form-title">注册账号</h1>
					<p class="form-subtitle">完善信息，开启专属即时通讯体验</p>
				</div>
				<div class="form-body">
					<el-form-item prop="userName">
						<el-input v-model="registerForm.userName" type="userName" autocomplete="off" placeholder="用户名"
							maxlength="20" prefix-icon="el-icon-user"></el-input>
					</el-form-item>
					<el-form-item prop="password">
						<el-input v-model="registerForm.password" type="password" autocomplete="off" placeholder="密码"
							maxlength="20" prefix-icon="el-icon-lock"></el-input>
					</el-form-item>
					<el-form-item prop="confirmPassword">
						<el-input v-model="registerForm.confirmPassword" type="password" autocomplete="off"
							placeholder="确认密码" maxlength="20" prefix-icon="el-icon-lock"></el-input>
					</el-form-item>
					<el-form-item>
						<el-button class="submit-btn" type="primary" @click="submitForm('registerForm')">注册</el-button>
					</el-form-item>
				</div>
				<div class="footer-links">
					<router-link class="link" to="/login">已有账号，前往登录</router-link>
				</div>
			</el-form>
		</div>
		<icp></icp>
	</el-container>
</template>

<script>
import Icp from '../components/common/Icp.vue'
export default {
	name: "register",
	components: {
		Icp
	},
	data() {
		var checkUserName = (rule, value, callback) => {
			if (!value) {
				return callback(new Error('请输入用户名'));
			}
			callback();
		};
		var checkPassword = (rule, value, callback) => {
			if (value === '') {
				return callback(new Error('请输入密码'));
			}
			callback();
		};

		var checkConfirmPassword = (rule, value, callback) => {
			if (value === '') {
				return callback(new Error('请输入密码'));
			}
			if (value != this.registerForm.password) {
				return callback(new Error('两次密码输入不一致'));
			}
			callback();
		};


		return {
			registerForm: {
				userName: '',
				password: '',
				confirmPassword: ''
			},
			rules: {
				userName: [{
					validator: checkUserName,
					trigger: 'blur'
				}],
				password: [{
					validator: checkPassword,
					trigger: 'blur'
				}],
				confirmPassword: [{
					validator: checkConfirmPassword,
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
						url: "/register",
						method: 'post',
						data: this.registerForm
					})
						.then(() => {
							// 注册成功后自动登录
							return this.$http({
								url: "/login",
								method: 'post',
								data: {
									terminal: this.$enums.TERMINAL_TYPE.WEB,
									userName: this.registerForm.userName,
									password: this.registerForm.password
								}
							})
						})
						.then((data) => {
							// 保存登录信息到本地，兼容新版逻辑
							localStorage.setItem('isAutoLogin', true);
							localStorage.setItem('username', this.registerForm.userName);
							localStorage.setItem('password', this.registerForm.password);
							sessionStorage.setItem("accessToken", data.accessToken);
							sessionStorage.setItem("refreshToken", data.refreshToken);
							this.$message.success(`注册成功，欢迎使用${process.env.VUE_APP_NAME}`);
							this.$router.push("/home/chat");
						})
				}
			});
		},
	}
}
</script>

<style scoped lang="scss">
.register {
	position: relative;
	display: flex;
	justify-content: center;
	align-items: center;
	width: 100%;
	height: 100%;
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
		z-index: 1;
		width: 380px;
		min-height: 460px;
		padding: 32px 36px;
		background: rgba(255, 255, 255, 0.88);
		backdrop-filter: blur(24px);
		-webkit-backdrop-filter: blur(24px);
		border-radius: 28px;
		border: 1px solid rgba(255, 255, 255, 0.95);
		box-shadow: 0 20px 50px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.04);
		overflow-y: auto;
		display: flex;
		flex-direction: column;
	}

	.form {
		display: flex;
		flex-direction: column;
		flex: 1;
	}

	.form-header {
		flex-shrink: 0;
		text-align: left;
		margin-bottom: 20px;
	}

	.form-title {
		margin: 0 0 6px;
		font-size: 26px;
		font-weight: 700;
		letter-spacing: 0.5px;
		line-height: 1.3;
		color: var(--im-color-primary);
	}

	.form-subtitle {
		margin: 0;
		font-size: var(--im-font-size);
		color: var(--im-text-color-light);
		line-height: 1.5;
	}

	.form-body {
		display: flex;
		flex-direction: column;
		justify-content: center;
		flex: 1;
		min-height: 280px;

		::v-deep .el-form-item {
			margin-bottom: 18px;

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

				.el-input__icon {
					color: var(--im-color-primary-light-3);
					font-size: 18px;
				}
			}
		}
	}

	.submit-btn {
		width: 100%;
		height: 52px;
		margin-top: 8px;
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
		margin-top: 10px;
		padding: 0 4px;
	}

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
</style>

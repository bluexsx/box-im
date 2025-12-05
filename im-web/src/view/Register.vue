<template>
	<el-container class="register-view">
		<div class="decoration decoration-1"></div>
		<div class="decoration decoration-2"></div>
		<div class="decoration decoration-3"></div>
		<el-form :model="registerForm" status-icon :rules="rules" ref="registerForm" label-width="80px" class="content">
			<div class="title">
				<img class="logo" src="../../public/logo.png" />
				<div>欢迎成为盒子IM的用户</div>
			</div>
			<el-form-item label="用户名" prop="userName">
				<el-input type="userName" v-model="registerForm.userName" autocomplete="off" placeholder="用户名(登录使用)"
					maxlength="20"></el-input>
			</el-form-item>
			<el-form-item label="昵称" prop="nickName">
				<el-input type="nickName" v-model="registerForm.nickName" autocomplete="off" placeholder="昵称"
					maxlength="20"></el-input>
			</el-form-item>
			<el-form-item label="密码" prop="password">
				<el-input type="password" v-model="registerForm.password" autocomplete="off" placeholder="密码"
					maxlength="20"></el-input>
			</el-form-item>
			<el-form-item label="确认密码" prop="confirmPassword">
				<el-input type="password" v-model="registerForm.confirmPassword" autocomplete="off" placeholder="确认密码"
					maxlength="20"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="submitForm('registerForm')">注册</el-button>
				<el-button @click="resetForm('registerForm')">清空</el-button>
			</el-form-item>
			<div class="to-login">
				<router-link to="/login">已有账号,前往登录</router-link>
			</div>
		</el-form>
		<icp></icp>
	</el-container>
</template>

<script>
import Icp from '../components/common/Icp.vue'
export default {
	name: "login",
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
		var checkNickName = (rule, value, callback) => {
			if (!value) {
				return callback(new Error('请输入昵称'));
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
				nickName: '',
				password: '',
				confirmPassword: ''
			},
			rules: {
				userName: [{
					validator: checkUserName,
					trigger: 'blur'
				}],
				nickName: [{
					validator: checkNickName,
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
						.then((data) => {
							this.$message.success("注册成功!");
						})
				}
			});
		},
		resetForm(formName) {
			this.$refs[formName].resetFields();
		}
	}
}
</script>

<style scoped lang="scss">
.register-view {
	position: fixed;
	display: flex;
	justify-content: space-around;
	align-items: center;
	width: 100%;
	height: 100%;
	background: linear-gradient(15deg, var(--im-color-primary-light-9) 0%, var(--im-color-primary-light-4) 100%);

	/* 装饰性元素 */
	.decoration {
		position: absolute;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.2);
	}

	.decoration-1 {
		width: 150px;
		height: 150px;
		background: rgba(255, 255, 255, 0.2);
		top: -150px;
		right: 0px;
		animation: float 16s infinite ease-in-out;
	}

	.decoration-2 {
		width: 200px;
		height: 200px;
		background: rgba(255, 255, 255, 0.18);
		bottom: -100px;
		left: -50px;
		animation: float 12s infinite ease-in-out;
	}

	.decoration-3 {
		width: 100px;
		height: 100px;
		background: rgba(255, 255, 255, 0.15);
		top: 50%;
		right: 50px;
		animation: float 8s infinite ease-in-out;
	}

	@keyframes float {

		0%,
		100% {
			transform: translateY(0) translateX(0);
		}

		25% {
			transform: translateY(-60px) translateX(30px);
		}

		50% {
			transform: translateY(30px) translateX(-45px);
		}

		75% {
			transform: translateY(-30px) translateX(-30px);
		}
	}

	.content {
		width: 400px;
		height: 450px;
		padding: 20px;
		border-radius: 3px;
		overflow: hidden;
		border-radius: 3%;
		background: rgba(255, 255, 255, 0.95);

		.title {
			display: flex;
			justify-content: center;
			align-items: center;
			line-height: 50px;
			margin: 20px 0 30px 0;
			font-size: 22px;
			font-weight: 600;
			letter-spacing: 2px;
			text-align: center;
			text-transform: uppercase;

			.logo {
				width: 30px;
				height: 30px;
				margin-right: 10px;
			}
		}

		.to-login {
			display: flex;
			flex-direction: row-reverse;
			line-height: 40px;
			text-align: left;
			padding-left: 20px;
		}
	}
}
</style>

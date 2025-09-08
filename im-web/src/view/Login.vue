<template>
	<div class="login-view">
		<div class="decoration decoration-1"></div>
		<div class="decoration decoration-2"></div>
		<div class="decoration decoration-3"></div>
		<div class="content">
			<el-form class="form" :model="loginForm" status-icon :rules="rules" ref="loginForm"
				@keyup.enter.native="submitForm('loginForm')">
				<div class="title">
					<img class="logo" src="../../public/logo.png" />
					<div>登录盒子IM</div>
				</div>
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
				<el-form-item>
					<el-button type="primary" @click="submitForm('loginForm')">登录</el-button>
					<el-button @click="resetForm('loginForm')">清空</el-button>
				</el-form-item>
				<div class="register">
					<router-link to="/register">没有账号,前往注册</router-link>
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
							// 保存token
							sessionStorage.setItem("accessToken", data.accessToken);
							sessionStorage.setItem("refreshToken", data.refreshToken);
							this.$message.success("登录成功");
							this.$router.push("/home/chat");
						})

				}
			});
		},
		resetForm(formName) {
			this.$refs[formName].resetFields();
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
		this.loginForm.userName = this.getCookie("username");
		// cookie存密码并不安全，暂时是为了方便
		this.loginForm.password = this.getCookie("password");
	}
}
</script>

<style scoped lang="scss">
.login-view {
	width: 100%;
	height: 100%;
	box-sizing: border-box;
	background: linear-gradient(135deg,
			#87adeb 0%,
			#8287ec 25%,
			#87adeb 50%,
			#898ee3 75%,
			#87adeb 100%);
	
		/* 装饰性元素 */
	.decoration {
		position: absolute;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.2);
	}

	.decoration-1 {
		width: 450px;
		height: 450px;
		background: linear-gradient(45deg, rgba(255, 255, 255, 0.3) 0%, rgba(200, 220, 240, 0.4) 100%);
		top: -250px;
		right: -100px;
		animation: float 16s infinite ease-in-out;
	}

	.decoration-2 {
		width: 400px;
		height: 400px;
		background: linear-gradient(135deg, rgba(200, 220, 240, 0.4) 0%, rgba(255, 255, 255, 0.3) 100%);
		bottom: -200px;
		left: -100px;
		animation: float 12s infinite ease-in-out;
	}

	.decoration-3 {
		width: 300px;
		height: 300px;
		background: linear-gradient(45deg, rgba(161, 196, 253, 0.3) 0%, rgba(194, 233, 251, 0.4) 100%);
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
		position: relative;
		display: flex;
		justify-content: space-around;
		align-items: center;
		padding: 10%;

		.form {
			width: 360px;
			height: 380px;
			padding: 30px;
			box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
			background: rgba(255, 255, 255, 0.85);
			backdrop-filter: blur(8px);
			border-radius: 3%;
			overflow: hidden;

			.title {
				display: flex;
				justify-content: center;
				align-items: center;
				line-height: 50px;
				margin: 30px 0 40px 0;
				font-size: 22px;
				font-weight: 600;
				letter-spacing: 2px;
				text-transform: uppercase;
				text-align: center;

				.logo {
					width: 30px;
					height: 30px;
					margin-right: 10px;
				}
			}

			.register {
				display: flex;
				flex-direction: row-reverse;
				line-height: 40px;
				text-align: left;
				padding-left: 20px;
			}
		}
	}
}
</style>
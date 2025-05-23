<template>
	<div class="login-view">
		<div class="content">
			<el-form class="form" :model="loginForm" status-icon :rules="rules" ref="loginForm" label-width="60px"
				@keyup.enter.native="submitForm('loginForm')">
				<div class="title">
					<img class="logo" src="../../public/logo.png" />
					<div>登录盒子IM</div>
				</div>
				<el-form-item label="终端" prop="userName" v-show="false">
					<el-input type="terminal" v-model="loginForm.terminal" autocomplete="off"></el-input>
				</el-form-item>
				<el-form-item label="用户名" prop="userName">
					<el-input type="userName" v-model="loginForm.userName" autocomplete="off"
						placeholder="用户名"></el-input>
				</el-form-item>
				<el-form-item label="密码" prop="password">
					<el-input type="password" v-model="loginForm.password" autocomplete="off"
						placeholder="密码"></el-input>
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
	background: #E8F2FF;
	background-size: cover;
	box-sizing: border-box;


	.content {
		position: relative;
		display: flex;
		justify-content: space-around;
		align-items: center;
		padding: 10%;

		.form {
			height: 340px;
			width: 400px;
			padding: 30px;
			background: white;
			opacity: 0.9;
			box-shadow: 0px 0px 1px #ccc;
			border-radius: 3%;
			overflow: hidden;
			border: 1px solid #ccc;

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
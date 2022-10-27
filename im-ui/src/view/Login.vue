<template>
	<div class="login-view">
		
			
			
			<el-form :model="loginForm" status-icon :rules="rules" ref="loginForm" label-width="60px" class="web-ruleForm">
				<div class="login-brand">欢迎登陆fly-chat</div>
				<el-form-item label="用户名" prop="username">
					<el-input type="username" v-model="loginForm.username" autocomplete="off"></el-input>

				</el-form-item>
				<el-form-item label="密码" prop="password">
					<el-input type="password" v-model="loginForm.password" autocomplete="off"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="submitForm('loginForm')">登陆</el-button>
					<el-button @click="resetForm('loginForm')">清空</el-button>
				</el-form-item>
				<div class="register">
					<router-link to="/register">没有账号,前往注册</router-link>
				</div>
				
			</el-form>
			
	</div>
</template>

<script>
	export default {
		name: "login",
		data() {
			var checkUsername = (rule, value, callback) => {
				console.log("checkUsername");
				if (!value) {
					return callback(new Error('请输入用户名'));
				}
				callback();
			};
			var checkPassword = (rule, value, callback) => {
				console.log("checkPassword");
				if (value === '') {
					callback(new Error('请输入密码'));
				}
				callback();

			};
			return {
				loginForm: {
					username: '',
					password: ''
				},
				rules: {
					username: [{
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
								url: "/api/login",
								method: 'post',
								params: this.loginForm
							})
							.then((data) => {
								this.$message.success("登陆成功");
								this.$router.push("/home/chat");
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
	.login-view {
		position: relative;
		display: flex;
		justify-content: space-around;
		width: 100%;
		height: 100%;
		background:  linear-gradient(#65807a, #182e3c); 
		background-size: cover;

		
		.web-ruleForm {
			height: 340px;
			padding: 20px;
			margin-top: 150px ;
			background: rgba(255,255,255,.75);
			box-shadow: 0px 0px  1px #ccc;
			border-radius: 5px;
			overflow: hidden;
	
			
			.login-brand {
				line-height: 50px;
				margin: 30px 0 40px 0;
				font-size: 22px;
				font-weight: 600;
				letter-spacing: 2px;
				text-transform: uppercase;
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

	
</style>

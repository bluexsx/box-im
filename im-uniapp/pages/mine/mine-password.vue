<template>
	<view class="page mine-password">
		<nav-bar back>修改密码</nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<uni-forms ref="form" :modelValue="formData" label-position="top" label-width="100%">
				<uni-forms-item label="原密码" name="oldPassword">
					<uni-easyinput type="password" v-model="formData.oldPassword" maxlength="20"/>
				</uni-forms-item>
				<uni-forms-item label="新密码" name="newPassword">
					<uni-easyinput type="password" v-model="formData.newPassword" maxlength="20"/>
				</uni-forms-item>
				<uni-forms-item label="确认密码" name="confirmPassword">
					<uni-easyinput type="password" v-model="formData.confirmPassword" maxlength="20"/>
				</uni-forms-item>
			</uni-forms>
		</uni-card>
		<button class="bottom-btn" type="primary" @click="onSubmit()">提交</button>
	</view>
</template>

<script>
export default {
	data() {
		return {
			formData: {
				oldPassword: "",
				newPassword: "",
				confirmPassword: ""
			},
			rules: {
				oldPassword: {
					rules: [{
						required: true,
						errorMessage: '请输入原密码',
					}]
				},
				newPassword: {
					rules: [{
						required: true,
						errorMessage: '请输入新密码',
					}, {
						validateFunction: function (rule, value, data, callback) {
							if (data.confirmPassword != data.newPassword) {
								callback("两次输入的密码不一致");
							}
							if (data.newPassword == data.oldPassword) {
								callback("新密码不能和原密码一致");
							}
							return true;
						}
					}]
				},
				confirmPassword: {
					rules: [{
						required: true,
						errorMessage: '请输入确认密码',
					}, {
						validateFunction: function (rule, value, data, callback) {
							if (data.confirmPassword != data.newPassword) {
								callback("两次输入的密码不一致");
							}

							return true;
						}
					}]
				}

			}
		}
	},
	methods: {
		onSubmit() {
			this.$refs.form.validate().then(res => {
				this.$http({
					url: "/modifyPwd",
					method: "PUT",
					data: this.formData
				}).then((res) => {
					uni.showToast({
						title: "修改密码成功",
						icon: 'none'
					})
					setTimeout(() => {
						uni.navigateBack();
					}, 1000);
				})
			}).catch(err => {
				console.log('表单错误信息：', err);
			})

		}
	},
	onReady() {
		// 需要在onReady中设置规则
		this.$refs.form.setRules(this.rules)
	}

}
</script>

<style scoped lang="scss"></style>
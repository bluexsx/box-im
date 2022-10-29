<template>
	<el-dialog class="setting" title="设置" :visible.sync="visible" width="30%" :before-close="handleClose">
		<el-form :model="userInfo" label-width="80px" :rules="rules" ref="settingForm">
			<el-form-item label="头像">
				<el-upload class="avatar-uploader" action="/api/image/upload" :show-file-list="false" :on-success="handleAvatarSuccess"
				 :before-upload="beforeAvatarUpload" accept="image/jpeg, image/png, image/jpg">
					<img v-if="userInfo.headImage" :src="userInfo.headImage" class="avatar">
					<i v-else class="el-icon-plus avatar-uploader-icon"></i>
				</el-upload>
			</el-form-item>
			<el-form-item label="用户名">
				<el-input disabled v-model="userInfo.userName" autocomplete="off"></el-input>
			</el-form-item>
			<el-form-item prop="nickName" label="昵称">
				<el-input v-model="userInfo.nickName" autocomplete="off"></el-input>
			</el-form-item>
			<el-form-item label="性别">
				<el-radio-group v-model="userInfo.sex">
					<el-radio :label="0">男</el-radio>
					<el-radio :label="1">女</el-radio>
				</el-radio-group>
			</el-form-item>
			<el-form-item label="个性签名">
				<el-input type="textarea" v-model="userInfo.signature"></el-input>
			</el-form-item>
		</el-form>

		<span slot="footer" class="dialog-footer">
			<el-button @click="handleClose()">取 消</el-button>
			<el-button type="primary" @click="handleSubmit()">确 定</el-button>
		</span>
	</el-dialog>
</template>

<script>
	import HeadImage from "../HeadImage.vue";

	export default {
		name: "setting",
		components: {
			HeadImage
		},
		data() {
			return {
				userInfo: {

				},
				rules: {
					nickName: [{
						required: true,
						message: '请输入昵称',
						trigger: 'blur'
					}]
				}
			}
		},
		methods: {

			handleClose() {
				this.visible = false;
				this.$emit("close");
			},
			handleSubmit() {
				this.$refs['settingForm'].validate((valid) => {
					if (!valid) {
						return false;
					}
					this.$http({
						url: "/api/user/update",
						method: "put",
						data: this.userInfo
					}).then(()=>{
						this.$store.commit("setUserInfo",this.userInfo);
						this.visible = false;
						this.$emit("close");
						this.$message.success("修改成功");
					})	


				});
			},
			handleAvatarSuccess(res, file) {
				console.log(res);
				this.loading.close();
				if (res.code == 200) {
					this.userInfo.headImage = res.data.originUrl;
					this.userInfo.headImageThumb = res.data.thumbUrl;
				} else {
					this.$message.error(res.message);
				}

			},
			beforeAvatarUpload(file) {
				const limitSize = file.size * 1024 * 1024;
				if (file.size > limitSize) {
					this.$message.error('上传头像图片大小不能超过 5MB!');
					return false
				}
				this.loading = this.$loading({
					lock: true,
					text: '正在上传...',
					spinner: 'el-icon-loading',
					background: 'rgba(0, 0, 0, 0.7)'
				});

				return true;
			}
		},
		props: {
			visible: {
				type: Boolean
			}
		},
		mounted() {
			this.userInfo = this.$store.state.userStore.userInfo;
			console.log(this.userInfo)
		}

	}
</script>

<style lang="scss">
	.setting {
		.avatar-uploader {

			.el-upload {
				border: 1px dashed #d9d9d9 !important;
				border-radius: 6px;
				cursor: pointer;
				position: relative;
				overflow: hidden;
			}

			.el-upload:hover {
				border-color: #409EFF;
			}

			.avatar-uploader-icon {
				font-size: 28px;
				color: #8c939d;
				width: 178px;
				height: 178px;
				line-height: 178px;
				text-align: center;
			}

			.avatar {
				width: 178px;
				height: 178px;
				display: block;
			}
		}
	}
</style>

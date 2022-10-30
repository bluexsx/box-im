<template>
	<el-dialog class="setting" title="设置" :visible.sync="visible" width="30%" :before-close="handleClose">
		<el-form :model="userInfo" label-width="80px" :rules="rules" ref="settingForm">
			<el-form-item label="头像">
				<file-upload  class="avatar-uploader"
				action="/api/image/upload" 
				:showLoading="true"
				:maxSize="maxSize"  
				@success="handleUploadSuccess"
				
				:fileTypes="['image/jpeg', 'image/png', 'image/jpg']">
					<img v-if="userInfo.headImage" :src="userInfo.headImage" class="avatar">
					<i v-else class="el-icon-plus avatar-uploader-icon"></i>
				</file-upload>
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
	import HeadImage from "../common/HeadImage.vue";
	import FileUpload from "../common/FileUpload.vue";
	
	export default {
		name: "setting",
		components: {
			HeadImage,
			FileUpload
		},
		data() {
			return {
				userInfo: {

				},
				maxSize: 5*1024*1024,
				action: "/api/image/upload",
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
						this.$emit("close");
						this.$message.success("修改成功");
					})	
				});
			},
			handleUploadSuccess(res, file) {
				this.userInfo.headImage = res.data.originUrl;
				this.userInfo.headImageThumb = res.data.thumbUrl;
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

<style lang="scss" >
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

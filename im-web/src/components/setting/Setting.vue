<template>
	<el-dialog v-dialogDrag class="setting" title="设置" :visible.sync="visible" width="420px" :before-close="onClose">
		<el-form :model="userInfo" label-width="80px" :rules="rules" ref="settingForm" size="small">
			<el-form-item label="头像" style="margin-bottom: 0 !important;">
				<file-upload class="avatar-uploader" :action="imageAction" :showLoading="true" :maxSize="maxSize"
					:isPermanent="true" @success="onUploadSuccess"
					:fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp']">
					<img v-if="userInfo.headImage" :src="userInfo.headImage" class="avatar">
					<i v-else class="el-icon-plus avatar-uploader-icon"></i>
				</file-upload>
			</el-form-item>
			<el-form-item label="用户名">
				<el-input disabled v-model="userInfo.userName" autocomplete="off" size="small"></el-input>
			</el-form-item>
			<el-form-item prop="nickName" label="昵称">
				<el-input v-model="userInfo.nickName" autocomplete="off" size="small" maxlength="20"></el-input>
			</el-form-item>
			<el-form-item label="性别">
				<el-radio-group v-model="userInfo.sex">
					<el-radio :label="0">男</el-radio>
					<el-radio :label="1">女</el-radio>
				</el-radio-group>
			</el-form-item>
			<el-form-item label="个性签名">
				<el-input type="textarea" v-model="userInfo.signature" :rows="3" maxlength="64"></el-input>
			</el-form-item>
		</el-form>

		<span slot="footer" class="dialog-footer">
			<el-button @click="onClose()">取 消</el-button>
			<el-button type="primary" @click="onSubmit()">确 定</el-button>
		</span>
	</el-dialog>
</template>

<script>
import FileUpload from "../common/FileUpload.vue";

export default {
	name: "setting",
	components: {
		FileUpload
	},
	data() {
		return {
			userInfo: {},
			maxSize: 5 * 1024 * 1024,
			action: "/image/upload",
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
		onClose() {
			this.$emit("close");
		},
		onSubmit() {
			this.$refs['settingForm'].validate((valid) => {
				if (!valid) {
					return false;
				}
				this.$http({
					url: "/user/update",
					method: "put",
					data: this.userInfo
				}).then(() => {
					this.userStore.setUserInfo(this.userInfo);
					this.$emit("close");
					this.$message.success("修改成功");
				})
			});
		},
		onUploadSuccess(data, file) {
			this.userInfo.headImage = data.originUrl;
			this.userInfo.headImageThumb = data.thumbUrl;
		}
	},
	props: {
		visible: {
			type: Boolean
		}
	},
	computed: {
		imageAction() {
			return `/image/upload`;
		}
	},
	watch: {
		visible: function () {
			// 深拷贝
			let mine = this.userStore.userInfo;
			this.userInfo = JSON.parse(JSON.stringify(mine));
		}
	}
}
</script>

<style lang="scss" scoped>
.setting {
	.el-form {
		padding: 10px 0 0 10px;
	}

	.avatar-uploader {
		--width: 112px;

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
			font-size: 24px;
			color: #8c939d;
			width: var(--width);
			height: var(--width);
			line-height: var(--width);
			text-align: center;
		}

		.avatar {
			width: var(--width);
			height: var(--width);
			display: block;
		}
	}
}
</style>
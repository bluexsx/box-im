<template>
	<el-upload  :action="action" 
	 :accept="fileTypes==null?'':fileTypes.join(',')" 
	 :show-file-list="false" 
	 :on-success="handleSuccess"
	 :on-error="handleError"
	 :disabled="disabled"
	 :before-upload="beforeUpload">
		<slot></slot>
	</el-upload>
</template>

<script>
	export default {
		name: "fileUpload",
		data() {
			return {
				loading: null
			}
		},
		props: {
			action: {
				type: String,
				required: true
			},
			fileTypes: {
				type: Array,
				default: null
			},
			maxSize: {
				type: Number,
				default: null
			},
			showLoading: {
				type: Boolean,
				default: false
			},
			disabled: {
				type: Boolean,
				default: false
			}
		},
		methods: {
			handleSuccess(res, file) {
				this.loading && this.loading.close();
				if (res.code == 200) {
					this.$emit("success", res, file);
				}else{
					this.$message.error(res.message);
					this.$emit("fail", res, file);
				}
			},
			handleError(err,file){
				this.$emit("fail", err, file);
			},
			beforeUpload(file) {
				// 校验文件类型
				if(this.fileTypes && this.fileTypes.length > 0){
					let fileType = file.type;
					let t = this.fileTypes.find((t) => t.toLowerCase() === fileType);
					if (t === undefined) {
						this.$message.error(`文件格式错误，请上传以下格式的文件：${this.fileTypes.join("、")}`);
						return false;
					}
				}
				// 校验大小
				if (this.maxSize && file.size > this.maxSize) {
					this.$message.error(`文件大小不能超过 ${this.fileSizeStr}!`);
					return false;
				}
				// 展示加载条
				if (this.showLoading) {
					this.loading = this.$loading({
						lock: true,
						text: '正在上传...',
						spinner: 'el-icon-loading',
						background: 'rgba(0, 0, 0, 0.7)'
					});
				}
				this.$emit("before", file);
				return true;
			}
		},
		computed: {
			fileSizeStr() {
				if (this.maxSize > 1024 * 1024) {
					return Math.round(this.maxSize / 1024 / 1024) + "M";
				}
				if (this.maxSize > 1024) {
					return Math.round(this.maxSize / 1024) + "KB";
				}
				return this.maxSize + "B";
			}
		}
	}
</script>

<style>
</style>

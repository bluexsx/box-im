<template>
	<view @click="selectAndUpload()">
		<slot></slot>
	</view>
</template>

<script>
	export default {
		name: "image-upload",
		data() {
			return {
				uploadHeaders: {
					"accessToken": uni.getStorageSync('loginInfo').accessToken
				}
			}
		},
		props: {
			maxSize: {
				type: Number,
				default: null
			},
			onBefore: {
				type: Function,
				default: null
			},
			onSuccess: {
				type: Function,
				default: null
			},
			onError: {
				type: Function,
				default: null
			}
		},
		methods: {
			selectAndUpload() {
				console.log("selectAndUpload");
				uni.chooseImage({
					count: 9, //最多可以选择的图片张数，默认9
					sourceType: ['album'], //album 从相册选图，camera 使用相机，默认二者都有。如需直接开相机或直接选相册，请只使用一个选项
					sizeType: ['original'], //original 原图，compressed 压缩图，默认二者都有
					success: (res) => {
						res.tempFiles.forEach((file) => {
							console.log("选择文件");
							// 校验大小
							if (this.maxSize && file.size > this.maxSize) {
								this.$message.error(`文件大小不能超过 ${this.fileSizeStr}!`);
								this.$emit("fail", file);
								return;
							}

							if (!this.onBefore || this.onBefore(file)) {
								// 调用上传图片的接口
								this.uploadImage(file);
							}
						})
					}
				})
			},
			uploadImage(file) {
				console.log("上传文件")
				uni.uploadFile({
					url: process.env.BASE_URL + '/image/upload',
					header: {
						accessToken: uni.getStorageSync("loginInfo").accessToken
					},
					filePath: file.path, // 要上传文件资源的路径
					name: 'file',
					success: (res) => {
						let data = JSON.parse(res.data);
						if(data.code != 200){
							this.onError && this.onError(file, data);
						}else{
							console.log("上传成功")
							this.onSuccess && this.onSuccess(file, data);
						}
					},
					fail: (err) => {
						console.log("上传失败")
						console.log(this.onError)
						this.onError && this.onError(file, err);
					}
				})
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
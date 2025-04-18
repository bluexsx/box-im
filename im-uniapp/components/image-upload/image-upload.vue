<template>
	<view @click="selectAndUpload()">
		<slot></slot>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js'

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
		maxCount: {
			type: Number,
			default: 1
		},
		maxSize: {
			type: Number,
			default: 5 * 1024 * 1024
		},
		sourceType: {
			type: String,
			default: 'album'
		},
		isPermanent: {
			type: Boolean,
			default: false
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
			uni.chooseImage({
				count: this.maxCount, //最多可以选择的图片张数，默认9
				sourceType: [this.sourceType], //album 从相册选图，camera 使用相机，默认二者都有。如需直接开相机或直接选相册，请只使用一个选项
				sizeType: ['original'], //original 原图，compressed 压缩图，默认二者都有
				success: (res) => {
					res.tempFiles.forEach((file) => {
						console.log("文件:", file)
						if (!this.onBefore || this.onBefore(file)) {
							// 调用上传图片的接口
							this.uploadImage(file);
						}
					})
				}
			})
		},
		uploadImage(file) {
			uni.uploadFile({
				url: UNI_APP.BASE_URL + '/image/upload?isPermanent=' + this.isPermanent,
				header: {
					accessToken: uni.getStorageSync("loginInfo").accessToken
				},
				filePath: file.path, // 要上传文件资源的路径
				name: 'file',
				success: (res) => {
					let data = JSON.parse(res.data);
					if (data.code != 200) {
						uni.showToast({
							icon: "none",
							title: data.message,
						})
						this.onError && this.onError(file, data);
					} else {
						this.onSuccess && this.onSuccess(file, data);
					}
				},
				fail: (err) => {
					console.log(err);
					this.onError && this.onError(file, err);
				}
			})
		}
	}
}
</script>

<style></style>
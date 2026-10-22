<template>
	<view @click="selectAndUpload()">
		<slot></slot>
	</view>
</template>

<script>
import { uploadRequest } from '@/common/request'

export default {
	name: "image-upload",
	data() {
		return {}
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
		thumbSize: {
			type: Number,
			default: 50
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
				success: async (res) => {
					for (const file of res.tempFiles) {
						if (!this.onBefore || await this.onBefore(file)) {
							this.uploadImage(file);
						}
					}
				}
			})
		},
		async uploadImage(file) {
			try {
				const data = await uploadRequest({
					url: `/image/upload?isPermanent=${this.isPermanent}&thumbSize=${this.thumbSize}`,
					filePath: file.path,
					name: 'file'
				})
				this.onSuccess && this.onSuccess(file, data)
			} catch (err) {
				console.log(err)
				this.onError && this.onError(file, err)
			}
		}
	}
}
</script>

<style></style>
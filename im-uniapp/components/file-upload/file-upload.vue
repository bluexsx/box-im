<template>
	<view>
		<lsj-upload ref="lsjUpload" :height="'100%'" :option="option" @uploadEnd="onUploadEnd" @change="onChange"
			:size="maxSize" :instantly="true">
			<slot></slot>
		</lsj-upload>
	</view>
</template>

<script>
import UNI_APP from '@/.env.js';

export default {
	name: "file-upload",
	data() {
		return {
			fileMap: new Map(),
			option: {
				url: UNI_APP.BASE_URL + '/file/upload',
				name: 'file'
			}
		}
	},
	props: {
		maxSize: {
			type: Number,
			default: 10
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
		show() {
			this.$refs.lsjUpload.show();
		},
		hide() {
			this.$refs.lsjUpload.hide();
		},
		onUploadEnd(item) {
			let file = this.fileMap.get(item.path);
			if (item.type == 'fail') {
				this.onError(file)
				return;
			}
			let res = JSON.parse(item.responseText);
			if (res.code == 200) {
				// 上传成功
				this.onOk(file, res);
			} else {
				// 上传失败
				this.onError(file, res);
			}
		},
		onChange(files) {
			if (!files.size) {
				return;
			}
			files.forEach((file, name) => {
				if (!this.fileMap.has(file.path)) {
					this.onBefore && this.onBefore(file)
					this.fileMap.set(file.path, file);
				}
			})
		},
		onOk(file, res) {
			this.fileMap.delete(file.path);
			this.$refs.lsjUpload.clear(file.name);
			this.onSuccess && this.onSuccess(file, res);
		},
		onFailed(file, res) {
			this.fileMap.delete(file.path);
			this.$refs.lsjUpload.clear(file.name);
			this.onError && this.onError(file, res);
		}
	}

}
</script>

<style></style>
<template>
	<view class="file-upload-trigger">
		<!-- #ifdef APP-PLUS -->
		<view class="file-upload-slot" @click="onAppSelect">
			<slot></slot>
		</view>
		<view class="file-picker-host" :renderState="renderState" :change:renderState="fileRender.onRenderState"
			:pickSignal="pickSignal" :change:pickSignal="fileRender.onPick" v-html="inputHtml" />
		<!-- #endif -->
		<!-- #ifndef APP-PLUS -->
		<view class="file-upload-btn" @click="selectAndUpload">
			<slot></slot>
		</view>
		<!-- #endif -->
	</view>
</template>

<script>
import UNI_APP from '@/.env.js'
export default {
	name: 'file-upload',
	props: {
		maxCount: { type: Number, default: 9 },
		maxSize: { type: Number, default: 10 },
		onBefore: { type: Function, default: null },
		onSuccess: { type: Function, default: null },
		onError: { type: Function, default: null }
	},
	// #ifdef APP-PLUS
	data() {
		return {
			inputHtml: '<input type="file" id="im-file-input" />',
			renderState: { count: 9, maxSize: 10, ready: false },
			pickSignal: 0
		}
	},
	created() {
		this.renderState = { count: this.maxCount, maxSize: this.maxSize, ready: false }
	},
	mounted() {
		this.$nextTick(() => {
			this.renderState = {
				count: this.maxCount,
				maxSize: this.maxSize,
				ready: true,
				t: Date.now()
			}
		})
	},
	// #endif
	methods: {
		showToast(msg) {
			uni.showToast({ title: msg, icon: 'none' })
		},
		// #ifdef APP-PLUS
		onAppSelect() {
			this.pickSignal = Date.now()
		},
		// #endif
		selectAndUpload() {
			// #ifdef MP-WEIXIN
			wx.chooseMessageFile({
				count: this.maxCount,
				type: 'all',
				success: (res) => this.handleFiles(res.tempFiles)
			})
			// #endif
			// #ifdef H5 || APP-HARMONY
			uni.chooseFile({
				count: this.maxCount,
				type: 'all',
				success: (res) => this.handleFiles(res.tempFiles)
			})
			// #endif
		},
		async handleFiles(list) {
			for (const item of list) {
				await this.processFile(this.normalizeFile(item))
			}
		},
		normalizeFile(raw) {
			const path = raw.path || raw.tempFilePath || raw.name
			return {
				...raw,
				path,
				name: raw.name || path.substring(path.lastIndexOf('/') + 1),
				size: raw.size
			}
		},
		getUploadHeader() {
			const header = {}
			const loginInfo = uni.getStorageSync('loginInfo')
			if (loginInfo) {
				header.accessToken = loginInfo.accessToken
			}
			return header
		},
		async processFile(file) {
			if (file.size > this.maxSize * 1024 * 1024) {
				uni.showToast({
					title: `文件大小不得大于${this.maxSize}M`,
					icon: 'none'
				})
				return
			}
			if (this.onBefore && await this.onBefore(file) === false) {
				return
			}
			await this.doUpload(file, file.path)
		},
		doUpload(file, filePath) {
			return new Promise((resolve) => {
				uni.uploadFile({
					url: UNI_APP.BASE_URL + '/file/upload',
					filePath,
					name: 'file',
					header: this.getUploadHeader(),
					success: async (res) => {
						try {
							const data = JSON.parse(res.data)
							if (data.code != 200) {
								uni.showToast({ icon: 'none', title: data.message })
								if (this.onError) {
									await this.onError(file, data)
								}
							} else if (this.onSuccess) {
								await this.onSuccess(file, data)
							}
						} catch (e) {
							if (this.onError) {
								await this.onError(file, e)
							}
						}
						resolve()
					},
					fail: async (err) => {
						if (this.onError) {
							await this.onError(file, err)
						}
						resolve()
					}
				})
			})
		},
		// #ifdef APP-PLUS
		async onNativeFiles(list) {
			for (const item of list) {
				const file = { name: item.name, size: item.size, path: item.name }
				if (this.onBefore && await this.onBefore(file) === false) {
					continue
				}
				try {
					const filePath = await this.base64ToPath(item.base64, item.name)
					file.path = filePath
					await this.doUpload(file, filePath)
				} catch (e) {
					if (this.onError) {
						await this.onError(file, e)
					}
				}
			}
		},
		base64ToPath(base64, fileName) {
			return new Promise((resolve, reject) => {
				const docDir = plus.io.convertLocalFileSystemURL('_doc/')
				plus.io.resolveLocalFileSystemURL(docDir, (dirEntry) => {
					dirEntry.getFile(fileName, { create: true }, (fileEntry) => {
						fileEntry.createWriter((writer) => {
							const chunkSize = 10 * 1024 * 1024
							let offset = 0
							const writeNext = () => {
								if (offset >= base64.length) {
									resolve(`_doc/${fileName}`)
									return
								}
								const chunk = base64.substring(offset, offset + chunkSize)
								offset += chunk.length
								writer.onwrite = writeNext
								writer.onerror = reject
								writer.writeAsBinary(chunk)
							}
							writeNext()
						}, reject)
					}, reject)
				}, reject)
			})
		}
		// #endif
	}
}

</script>

<!-- #ifdef APP-PLUS -->
<script module="fileRender" lang="renderjs">
const INPUT_ID = 'im-file-input'
export default {
	methods: {
		onRenderState(state) {
			if (!state || !state.ready) return
			this.bindInput(state)
		},
		bindInput(state, retry) {
			const input = document.getElementById(INPUT_ID)
			if (!input) {
				if (retry !== false) {
					setTimeout(() => this.bindInput(state, false), 50)
				}
				return
			}
			input.multiple = state.count > 1
			input.onchange = (e) => {
				const maxBytes = state.maxSize * 1024 * 1024
				const files = Array.from(e.target.files || [])
				if (files.length > state.count) {
					this.$ownerInstance.callMethod('showToast', `最多选择${state.count}个文件`)
					e.target.value = ''
					return
				}
				Promise.all(files.map((file) => this.readFile(file, maxBytes)))
					.then((list) => {
						const valid = list.filter(Boolean)
						if (valid.length) {
							this.$ownerInstance.callMethod('onNativeFiles', valid)
						}
					})
				e.target.value = ''
			}
		},
		onPick(signal) {
			if (!signal) return
			const input = document.getElementById(INPUT_ID)
			if (input) {
				input.click()
			}
		},
		readFile(file, maxBytes) {
			return new Promise((resolve) => {
				if (file.size > maxBytes) {
					this.$ownerInstance.callMethod('showToast', `文件大小请勿超过${Math.round(maxBytes / 1024 / 1024)}M`)
					return resolve(null)
				}
				const reader = new FileReader()
				reader.onload = (ev) => {
					const base64 = ev.target.result.split(',')[1]
					resolve({ name: file.name, size: file.size, base64 })
				}
				reader.onerror = () => resolve(null)
				reader.readAsDataURL(file)
			})
		}
	}
}

</script>
<!-- #endif -->

<style scoped>
.file-upload-trigger {
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.file-upload-btn,
.file-upload-slot {
	display: flex;
	flex-direction: column;
	align-items: center;
	position: relative;
	z-index: 3;
}

/* #ifdef APP-PLUS */
.file-picker-host {
	position: absolute;
	inset: 0;
	z-index: 2;
	opacity: 0;
	overflow: hidden;
	pointer-events: none;
}

/* #endif */

</style>

<!-- #ifdef APP-PLUS -->
<style>
.file-picker-host #im-file-input {
	position: absolute;
	inset: 0;
	width: 100%;
	height: 100%;
	opacity: 0;
}

</style>
<!-- #endif -->

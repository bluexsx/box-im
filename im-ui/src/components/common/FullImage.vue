<template>
	<el-dialog width="30%" :visible.sync="visible"   :before-close="handleClose">
		<img class="full-img" :src="url" />
	</el-dialog>
</template>

<script>
	export default {
		name: "fullImage",
		data() {
			return {
			}
		},
		methods: {
			handleClose() {
				this.$emit("close");
			}
		},
		props: {
			visible: {
				type: Boolean
			},
			url: {
				type: String
			}
		}
	}
</script>

<style>
	.full-img {
		width: 100%;
		height: 100%;
	}
</style>

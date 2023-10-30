<template>
	<el-dialog width="75%" top="30px" :visible.sync="visible" :before-close="handleClose" :modal="true">
		<div class="image-box">
			<el-image :src="url" :fit="fit"></el-image>
		</div>

	</el-dialog>
</template>

<script>
	export default {
		name: "fullImage",
		data() {
			return {
				fit: 'cover'
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
	.image-box {
		display: flex;
		align-items: center;
		justify-content: center;


	}
</style>
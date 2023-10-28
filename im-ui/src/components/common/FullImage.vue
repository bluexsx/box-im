<template>
	<el-dialog  width="60%" :visible.sync="visible"   :before-close="handleClose" :modal="true">
		<div class="image-box">
			<img class="full-img" :src="url" />
		</div>
		
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
	.image-box{
		display: flex;
		align-items: center;
		justify-content: center;
		
		
	}
	
</style>

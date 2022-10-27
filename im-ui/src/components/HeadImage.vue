<template>
	<div  class='img-box'>
		<img src="../assets/default_head.png" style="width: 100%;height: 100%;cursor: pointer;" />
	</div>
</template>

<script>
	export default {
		name: "headImage",
		data() {
			return {}
		},
		props: {
			size: {
				type: Number,
				default: 50
			},
			url:{
				type: String,
				default: '../assets/default_head.png'
			}
		}
	}
</script>

<style scoped>
	.img-box {
		width: 100%;
		height: 100%;
		display: inline-block;
		border-radius: 3px;
		background-color: #c0c4cc;
		overflow: hidden;
	}
</style>
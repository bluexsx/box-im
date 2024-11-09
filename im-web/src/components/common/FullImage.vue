<template>
	<div class="full-image" v-show="visible" :before-close="onClose" :modal="true">
		<div class="mask"></div>
		<div class="image-box">
			<img :src="url"/>
		</div>
		<div class="close" @click="onClose"><i class="el-icon-close"></i></div>
	</div>
</template>

<script>
	export default {
		name: "fullImage",
		data() {
			return {
				fit: 'contain'
			}
		},
		methods: {
			onClose() {
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

<style lang="scss">
	.full-image{
		position: fixed;
		width: 100%;
		height: 100%;
    left: 0;
    top: 0;
    bottom: 0;
    right: 0;

		.mask{
			position: fixed;
			width: 100%;
			height: 100%;
			background: black;
			opacity: 0.5;

		}

		.image-box {
			position: relative;
			width: 100%;
			height: 100%;

			img{
				position: absolute;
          left: 50%;
          top: 50%;
          transform: translate(-50%, -50%);
          max-height: 100%;
          max-width: 100%;
			}
		}

		.close{
			position: fixed;
			top: 10px;
			right: 10px;
			color: white;
			font-size: 25px;
			cursor: pointer;
		}
	}


</style>

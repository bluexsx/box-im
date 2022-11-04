<template>
	<div class="head-image">
		<img :src="url" :style="{width: size+'px',height: size+'px',cursor: 'pointer'}" />
		<slot></slot>
	</div>
</template>

<script>
	export default {
		name: "headImage",
		data() {
			return {}
		},
		methods: {},
		props: {
			size: {
				type: Number,
				default: 50
			},
			url: {
				type: String
			}
		}
	}
</script>

<style scoped lang="scss">
	.head-image {
		position: relative;
		img {
			position: relative;
			overflow: hidden;
			border-radius: 5%;
		}

		img:before {
			content: '';
			display: block;
			width: 100%;
			height: 100%;
			background: url('../../assets/default_head.png') no-repeat 0 0;
			background-size: 100%;
		}
	}
</style>

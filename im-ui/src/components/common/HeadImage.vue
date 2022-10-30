<template>
	<div  class='img-box'>
		<img :src="url" 
		 style="width: 100%;height: 100%;cursor: pointer;" />
	</div>
</template>

<script>
	export default {
		name: "headImage",
		data() {
			return {
			}
		},
		methods:{
		},
		props: {
			size: {
				type: Number,
				default: 50
			},
			url:{
				type: String
			}
		}
	}
</script>

<style scoped lang="scss">
	.img-box {
		width: 100%;
		height: 100%;
		display: inline-block;
		border-radius: 3px;
		background-color: #c0c4cc;
		overflow: hidden;
		
		img {
		  position: relative;
		}
		
		img:before { 
		 
		    content: '';
		    display: block;
		    width: 100%;
		    height: 100%;
		    background:url('../../assets/default_head.png') no-repeat 0 0;
		    background-size: 100%;
			
	
		}
	}
</style>
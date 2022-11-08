<template>
	<div class="emotion-mask" @click="$emit('emotion','')">
		<div class="emotion-box" :style="{'left':x+'px','top':y+'px'}">
			<el-scrollbar style="height:200px">
				<div class="emotion-item-list">
					<div class="emotion-item" v-for="(emoText, i) in $emo.emoTextList" :key="i" @click="clickHandler(emoText)" v-html="$emo.textToImg(emoText)">
					</div>
				</div>
			</el-scrollbar>
		</div>
	</div>
</template>

<script>
	export default {
		name: "emotion",
		props: {
			pos: {
				type: Object
			}
		},
		data() {
			return {}
		},
		methods: {
			clickHandler(emoText) {
				let emotion = `#${emoText};`
				this.$emit('emotion', emotion)
			}
		},
		computed: {
			x() {
				return this.pos.x - 200;
			},
			y() {
				return this.pos.y - 230;
			}
		}
	}
</script>
<style scoped lang="scss">
	.emotion-mask {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		width: 100%;
		height: 100%;
	}
	
	.emotion-box {
		position: fixed;
		width: 400px;
		box-sizing: border-box;
		padding: 5px;
		border: 1px solid #b4b4b4;
		border-radius: 5px;
		background-color: #f5f5f5;
		box-shadow: 0px 0px  10px #ccc;	
		.emotion-item-list {
			display: flex;
			flex-wrap: wrap;

			.emotion-item {
				width: 40px;
				height: 40px;
				text-align: center;
				cursor: pointer;
			}
		}

		&:after {
		    content: "";
		    position: absolute;
		    left: 185px;
		    bottom: -30px;
		    width: 0;
		    height: 0;
		    border-style: solid dashed dashed;
		    border-color: #f5f5f5 transparent transparent;
		    overflow: hidden;
		    border-width: 15px;
		}
	}
</style>

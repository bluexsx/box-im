<template>
	<div  v-show="show" @click="close()">
		<div class="emotion-box" :style="{'left':x+'px','top':y+'px'}">
			<el-scrollbar style="height: 220px">
				<div class="emotion-item-list">
					<div class="emotion-item" v-for="(emoText, i) in $emo.emoTextList" :key="i"
						@click="onClickEmo(emoText)" v-html="$emo.textToImg(emoText)">
					</div>
				</div>
			</el-scrollbar>
		</div>
	</div>
</template>

<script>
	export default {
		name: "emotion",
		data() {
			return {
				show: false,
				pos: {
					x: 0,
					y: 0
				}
			}
		},
		methods: {
			onClickEmo(emoText) {
				let emotion = `#${emoText};`
				this.$emit('emotion', emotion)
			},
			open(pos) {
				this.pos = pos;
				this.show = true;
			},
			close() {
				this.show = false;
			}
		},
		computed: {
			x() {
				return this.pos.x - 22;
			},
			y() {
				return this.pos.y - 234;
			}
		}
	}
</script>
<style scoped lang="scss">
	.emotion-box {
		position: fixed;
		width: 372px;
		box-sizing: border-box;
		padding: 5px;
		//border-radius: 5px;
		background-color: #fff;
		box-shadow: var(--im-box-shadow);

		.emotion-item-list {
			display: flex;
			flex-wrap: wrap;

			.emotion-item {
				text-align: center;
				cursor: pointer;
				padding: 2px;

			}
		}

		//&:after {
		//	content: "";
		//	position: absolute;
		//	left: 185px;
		//	bottom: -30px;
		//	width: 0;
		//	height: 0;
		//	border-style: solid dashed dashed;
		//	border-color: #f5f5f5 transparent transparent;
		//	overflow: hidden;
		//	border-width: 15px;
		//}
	}
</style>

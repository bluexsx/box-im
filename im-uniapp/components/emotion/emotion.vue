<template>
	<view class="emotion"  @click="$emit('emotion','')">
		<scroll-view class="scroll-box" scroll-y="true" height="400">
			<view class="emotion-item-list">
				<view class="emotion-item" v-for="(emoText, i) in $emo.emoTextList" :key="i" @click="selectEmo(emoText)" v-html="$emo.textToImg(emoText)">
				</view>
			</view>
		</scroll-view>
	</view>

</template>

<script>
	export default {
		name: "emotion",
		data() {
			return {}
		},
		methods: {
			selectEmo(emoText) {
				let emotion = `#${emoText};`
				this.$emit('emotion', emotion)
			}
		}
	}
</script>
<style  lang="scss" scoped>

	.emotion {

		.scroll-box{
			height: 400rpx;
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
		}
		
	}
</style>

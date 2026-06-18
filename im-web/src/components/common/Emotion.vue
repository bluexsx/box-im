<template>
	<div v-show="show" @click="close()">
		<div class="emotion-box" :style="{ 'left': x + 'px', 'top': y + 'px' }" @click.stop>
			<el-scrollbar class="emotion-scroll">
				<div class="emotion-content">
					<div v-if="recentEmojiList.length" class="emotion-group">
						<div class="emotion-group-title">最近使用</div>
						<div class="emotion-items">
							<div class="emotion-item" v-for="(emoText, i) in recentEmojiList"
								:key="'recent-' + emoText + '-' + i" @click="onClickEmo(emoText)"
								v-html="$emo.textToImg(emoText, 'emoji-large')">
							</div>
						</div>
					</div>
					<div class="emotion-group">
						<div v-if="recentEmojiList.length" class="emotion-group-title">全部表情</div>
						<div class="emotion-items">
							<div class="emotion-item" v-for="(emoText, i) in $emo.emoTextList" :key="'default-' + i"
								@click="onClickEmo(emoText)" v-html="$emo.textToImg(emoText, 'emoji-large')">
							</div>
						</div>
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
			},
			recentEmojiList: []
		}
	},
	methods: {
		onClickEmo(emoText) {
			this.$db.addRecentEmoji(emoText);
			let emotion = `#${emoText};`
			this.$emit('emotion', emotion)
		},
		open(pos) {
			this.pos = pos;
			this.loadRecentEmojis();
			this.show = true;
		},
		close() {
			this.show = false;
		},
		loadRecentEmojis() {
			return this.$db.findRecentEmojis().then((list) => {
				this.recentEmojiList = list;
			});
		}
	},
	computed: {
		x() {
			return this.pos.x - 22;
		},
		y() {
			return this.pos.y - 254;
		}
	}
}
</script>
<style scoped lang="scss">
.emotion-box {
	position: fixed;
	width: 400px;
	box-sizing: border-box;
	padding: 8px;
	background-color: #fff;
	box-shadow: var(--im-box-shadow);

	.emotion-scroll {
		height: 240px;
	}

	.emotion-content {
		padding: 8px 10px;
	}

	.emotion-group {
		&:not(:last-child) {
			margin-bottom: 10px;
		}

		.emotion-group-title {
			font-size: 11px;
			color: var(--im-text-color-light);
			margin-bottom: 8px;
			padding: 0 4px;
			text-align: left;
		}
	}

	.emotion-items {
		display: grid;
		grid-template-columns: repeat(8, 1fr);
		gap: 5px;

		.emotion-item {
			display: flex;
			justify-content: center;
			align-items: center;
			text-align: center;
			cursor: pointer;
			padding: 2px;
		}
	}
}
</style>

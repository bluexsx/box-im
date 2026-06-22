<template>
	<div v-show="show" @click="close()">
		<div class="emotion-box" :style="{ 'left': x + 'px', 'top': y + 'px' }" @click.stop>
			<el-scrollbar class="emotion-scroll">
				<div class="emotion-content">
					<div v-if="recentEmojiList.length" class="emotion-group">
						<div class="emotion-group-title">最近使用</div>
						<div class="emotion-items">
							<div class="emotion-item" v-for="(emoText, i) in recentEmojiList"
								:key="'recent-' + emoText + '-' + i" :title="emoText" @click="onClickEmo(emoText)">
								<img :src="emojiUrl(emoText)" :alt="emoText" :title="emoText" class="emoji-large">
							</div>
						</div>
					</div>
					<div class="emotion-group">
						<div v-if="recentEmojiList.length" class="emotion-group-title">全部表情</div>
						<div class="emotion-items">
							<div class="emotion-item" v-for="(emoText, i) in $emo.emoTextList" :key="'default-' + i"
								:title="emoText" @click="onClickEmo(emoText)">
								<img :src="emojiUrl(emoText)" :alt="emoText" :title="emoText" class="emoji-large">
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
			this.loadRecentEmojis();
			let emotion = this.$emo.formatEmoji(emoText);
			this.$emit('emotion', emotion);
			this.close();
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
				this.recentEmojiList = this.$emo.filterRecentEmojis(list);
			});
		},
		emojiUrl(emoText) {
			return this.$emo.textToUrl(this.$emo.formatEmoji(emoText));
		}
	},
	computed: {
		x() {
			return this.pos.x - 22;
		},
		y() {
			return this.pos.y - 284;
		}
	}
}
</script>
<style scoped lang="scss">
.emotion-box {
	position: fixed;
	width: 480px;
	box-sizing: border-box;
	padding: 8px;
	background-color: #fff;
	box-shadow: var(--im-box-shadow-lighter);
	border-radius: 6px;

	.emotion-scroll {
		height: 270px;
	}

	.emotion-content {
		padding: 8px 4px;
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
		grid-template-columns: repeat(10, 1fr);
		padding: 4px 4px 0;
		gap: 5px;

		.emotion-item {
			display: flex;
			justify-content: center;
			align-items: center;
			text-align: center;
			cursor: pointer;
			margin: 5px;
		}
	}
}
</style>

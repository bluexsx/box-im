<template>
	<el-scrollbar v-show="show && showMembers.length" ref="scrollBox" class="chat-at-box"
		:style="{ 'left': pos.x + 'px', 'top': pos.y - 300 + 'px' }">
		<div v-for="(member, idx) in showMembers" :key="member.id">
			<chat-group-member :member="member" :height="40" :active='activeIdx == idx'
				@click.native="onSelectMember(member)"></chat-group-member>
		</div>
	</el-scrollbar>
</template>

<script>
import ChatGroupMember from "./ChatGroupMember.vue";
export default {
	name: "chatAtBox",
	components: {
		ChatGroupMember
	},
	props: {
		searchText: {
			type: String,
			default: ""
		},
		ownerId: {
			type: Number,
		},
		members: {
			type: Array
		}
	},
	data() {
		return {
			show: false,
			pos: {
				x: 0,
				y: 0
			},
			activeIdx: 0,
			showMembers: []
		};
	},
	methods: {
		init() {
			this.$refs.scrollBox.wrap.scrollTop = 0;
			this.showMembers = [];
			let userId = this.$store.state.userStore.userInfo.id;
			let name = "全体成员";
			if (this.ownerId == userId && name.startsWith(this.searchText)) {
				this.showMembers.push({
					userId: -1,
					showNickName: name
				})
			}
			this.members.forEach((m) => {
				// 只显示100条
				if (this.showMembers.length > 100) {
					return;
				}
				if (m.userId != userId && !m.quit && m.showNickName.startsWith(this.searchText)) {
					this.showMembers.push(m);
				}
			})
			this.activeIdx = this.showMembers.length > 0 ? 0 : -1;
		},
		open(pos) {
			this.show = true;
			this.pos = pos;
			this.init();
		},
		close() {
			this.show = false;
		},
		moveUp() {
			if (this.activeIdx > 0) {
				this.activeIdx--;
				this.scrollToActive()
			}
		},
		moveDown() {
			if (this.activeIdx < this.showMembers.length - 1) {
				this.activeIdx++;
				this.scrollToActive()
			}
		},
		select() {
			if (this.activeIdx >= 0) {
				this.onSelectMember(this.showMembers[this.activeIdx])
			}
			this.close();
		},
		scrollToActive() {
			if (this.activeIdx * 40 - this.$refs.scrollBox.wrap.clientHeight > this.$refs.scrollBox.wrap.scrollTop) {
				this.$refs.scrollBox.wrap.scrollTop += 140;
				if (this.$refs.scrollBox.wrap.scrollTop > this.$refs.scrollBox.wrap.scrollHeight) {
					this.$refs.scrollBox.wrap.scrollTop = this.$refs.scrollBox.wrap.scrollHeight
				}
			}
			if (this.activeIdx * 40 < this.$refs.scrollBox.wrap.scrollTop) {
				this.$refs.scrollBox.wrap.scrollTop -= 140;
				if (this.$refs.scrollBox.wrap.scrollTop < 0) {
					this.$refs.scrollBox.wrap.scrollTop = 0;
				}
			}
		},
		onSelectMember(member) {
			this.$emit("select", member);
			this.show = false;
		}
	},
	computed: {
		isOwner() {
			return this.$store.state.userStore.userInfo.id == this.ownerId;
		}
	},
	watch: {
		searchText: {
			handler(newText, oldText) {
				this.init();
			}
		}
	}

}
</script>

<style scoped lang="scss">
.chat-at-box {
	position: fixed;
	width: 200px;
	height: 300px;
	background-color: #fff;
	box-shadow: var(--im-box-shadow);
}
</style>
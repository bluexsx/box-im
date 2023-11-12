<template>
	<el-scrollbar v-show="show" ref="scrollBox" class="group-member-choose"
		:style="{'left':pos.x+'px','top':pos.y-300+'px'}">
		<div v-for="(member,idx) in showMembers" :key="member.id">
			<div class="member-item" :class="idx==activeIdx?'active':''" @click="onSelectMember(member)">
				<div class="member-avatar">
					<head-image :size="30" :name="member.aliasName" :url="member.headImage"> </head-image>
				</div>
				<div class="member-name">
					<div>{{member.aliasName}}</div>
				</div>
			</div>
		</div>
	</el-scrollbar>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';

	export default {
		name: "chatAtBox",
		components: {
			HeadImage
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
						aliasName: name
					})
				}
				this.members.forEach((m) => {
					if (m.userId != userId && m.aliasName.startsWith(this.searchText)) {
						this.showMembers.push(m);
					}
				})
				this.activeIdx = this.showMembers.length > 0 ? 0: -1;
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
				if (this.activeIdx * 35 - this.$refs.scrollBox.wrap.clientHeight > this.$refs.scrollBox.wrap.scrollTop) {
					this.$refs.scrollBox.wrap.scrollTop += 140;
					if (this.$refs.scrollBox.wrap.scrollTop > this.$refs.scrollBox.wrap.scrollHeight) {
						this.$refs.scrollBox.wrap.scrollTop = this.$refs.scrollBox.wrap.scrollHeight
					}
				}
				if (this.activeIdx * 35 < this.$refs.scrollBox.wrap.scrollTop) {
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
	.group-member-choose {
		position: fixed;
		width: 200px;
		height: 300px;
		border: 1px solid #b4b4b4;
		border-radius: 5px;
		background-color: #f5f5f5;
		box-shadow: 0px 0px 10px #ccc;

		.member-item {
			display: flex;
			height: 35px;
			margin-bottom: 1px;
			position: relative;
			padding-left: 10px;
			align-items: center;
			padding-right: 5px;
			background-color: #fafafa;
			white-space: nowrap;
			box-sizing: border-box;

			&:hover {
				background-color: #eeeeee;
			}

			&.active {
				background-color: #eeeeee;
			}


			.member-avatar {
				width: 30px;
				height: 30px;
			}

			.member-name {
				padding-left: 10px;
				height: 100%;
				text-align: left;
				line-height: 40px;
				white-space: nowrap;
				overflow: hidden;
				font-size: 14px;
				font-weight: 600;
			}
		}
	}
</style>
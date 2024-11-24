<template>
	<div class="friend-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="friend-avatar">
			<head-image :size="42" :name="friend.nickName" :url="friend.headImage" :online="friend.online">
			</head-image>
		</div>
		<div class="friend-info">
			<div class="friend-name">{{ friend.nickName }}</div>
			<div class="friend-online">
				<i class="el-icon-monitor online" v-show="friend.onlineWeb" title="电脑设备在线">
					<span class="online-icon"></span>
				</i>
				<i class="el-icon-mobile-phone online" v-show="friend.onlineApp" title="移动设备在线">
					<span class="online-icon"></span>
				</i>
			</div>
		</div>
		<right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items"
			@close="rightMenu.show = false" @select="onSelectMenu"></right-menu>
		<slot></slot>
	</div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import RightMenu from "../common/RightMenu.vue";

export default {
	name: "frinedItem",
	components: {
		HeadImage,
		RightMenu
	},
	data() {
		return {
			rightMenu: {
				show: false,
				pos: {
					x: 0,
					y: 0
				},
				items: [{
					key: 'CHAT',
					name: '发送消息',
					icon: 'el-icon-chat-dot-round'
				}, {
					key: 'DELETE',
					name: '删除好友',
					icon: 'el-icon-delete'
				}]
			}
		}
	},
	methods: {
		showRightMenu(e) {
			this.rightMenu.pos = {
				x: e.x,
				y: e.y
			};
			this.rightMenu.show = "true";
		},
		onSelectMenu(item) {
			this.$emit(item.key.toLowerCase(), this.msgInfo);
		}
	},
	computed: {
		friend() {
			return this.$store.state.friendStore.friends[this.index];
		}
	},
	props: {
		active: {
			type: Boolean
		},
		index: {
			type: Number
		},
		menu: {
			type: Boolean,
			default: true
		}
	}

}
</script>

<style scope lang="scss">
.friend-item {
	height: 50px;
	display: flex;
	position: relative;
	padding: 5px 10px;
	align-items: center;
	white-space: nowrap;
	cursor: pointer;

	&:hover {
		background-color: var(--im-background-active);
	}

	&.active {
		background-color: var(--im-background-active-dark);
	}

	.friend-avatar {
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.friend-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding-left: 10px;
		text-align: left;

		.friend-name {
			font-size: var(--im-font-size);
			white-space: nowrap;
			overflow: hidden;
		}

		.friend-online {
			.online {
				font-weight: bold;
				padding-right: 2px;
				font-size: 16px;
				position: relative;
			}

			.online-icon {
				position: absolute;
				right: 0;
				bottom: 0;
				width: 6px;
				height: 6px;
				background: limegreen;
				border-radius: 50%;
				border: 1px solid white;
			}
		}
	}
}
</style>

<template>
	<div class="friend-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="friend-avatar">
			<head-image :url="friend.headImage"> </head-image>
		</div>
		<div class="friend-info">
			<div class="friend-name">{{ friend.nickName}}</div>
			<div class="friend-online" :class="friend.online ? 'online':''">{{ friend.online?"[在线]":"[离线]"}}</div>
		</div>
		<right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items"
			@close="rightMenu.show=false" @select="handleSelectMenu"></right-menu>
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
			handleSelectMenu(item) {
				this.$emit(item.key.toLowerCase(), this.msgInfo);
			}
		},
		props: {
			friend: {
				type: Object
			},
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
		height: 65px;
		display: flex;
		margin-bottom: 1px;
		position: relative;
		padding-left: 10px;
		align-items: center;
		padding-right: 5px;
		background-color: #fafafa;
		white-space: nowrap;

		&:hover {
			background-color: #eeeeee;
		}

		&.active {
			background-color: #dddddd;
		}

		.friend-avatar {
			display: flex;
			justify-content: center;
			align-items: center;
			width: 50px;
			height: 50px;
		}

		.friend-info {
			flex: 1;
			display: flex;
			flex-direction: column;
			padding-left: 10px;
			text-align: left;

			.friend-name {
				font-size: 16px;
				font-weight: 600;
				line-height: 30px;
				white-space: nowrap;
				overflow: hidden;
			}

			.friend-online {
				font-size: 12px;

				&.online {
					color: #5fb878;
				}
			}
		}
	}
</style>
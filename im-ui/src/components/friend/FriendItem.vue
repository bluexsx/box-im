<template>
	<div class="friend-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="friend-avatar">
			<head-image :size="45" :name="friend.nickName" :url="friend.headImage" :online="friend.online">
			</head-image>
		</div>
		<div class="friend-info">
			<div class="friend-name">{{ friend.nickName}}</div>
			<div class="friend-online">
				<el-image v-show="friend.onlineWeb" class="online" :src="require('@/assets/image/online_web.png')"
					title="电脑设备在线" />
				<el-image v-show="friend.onlineApp" class="online" :src="require('@/assets/image/online_app.png')"
					title="移动设备在线" />
			</div>
		</div>
		<right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items"
			@close="rightMenu.show=false" @select="onSelectMenu"></right-menu>
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
		computed:{
			friend(){
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
		margin-bottom: 1px;
		position: relative;
		padding: 5px;
		padding-left: 10px;
		align-items: center;
		background-color: #fafafa;
		white-space: nowrap;
		cursor: pointer;

		&:hover {
			background-color: #F8FAFF;
		}
			
		&.active {
			background-color: #F4F9FF;
		}
			
		.friend-avatar {
			display: flex;
			justify-content: center;
			align-items: center;
			width: 45px;
			height: 45px;
		}

		.friend-info {
			flex: 1;
			display: flex;
			flex-direction: column;
			padding-left: 10px;
			text-align: left;

			.friend-name {
				font-size: 15px;
				font-weight: 600;
				line-height: 30px;
				white-space: nowrap;
				overflow: hidden;
			}

			.friend-online {
				.online {
					padding-right: 2px;
					width: 15px;
					height: 15px;
				
				}
			}
		}
	}
</style>
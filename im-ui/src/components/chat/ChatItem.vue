<template>
	<div class="chat-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="chat-left">
			<head-image :url="chat.headImage" :size="50" :id="chat.type=='PRIVATE'?chat.targetId:0"></head-image>
			<div v-show="chat.unreadCount>0" class="unread-text">{{chat.unreadCount}}</div>
		</div>
		<div class="chat-right">
			<div class="chat-name">
				{{ chat.showName}}
			</div>
			<div class="chat-content">
				<div class="chat-content-text" v-html="$emo.transform(chat.lastContent)"></div>
				<div class="chat-time">{{showTime}}</div>
			</div>
		</div>
		<right-menu v-show="rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items"
			@close="rightMenu.show=false" @select="handleSelectMenu"></right-menu>
	</div>

</template>

<script>
	import HeadImage from '../common/HeadImage.vue';
	import RightMenu from '../common/RightMenu.vue';
	
	export default {
		name: "chatItem",
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
						key: 'TOP',
						name: '置顶',
						icon: 'el-icon-top'
					}, {
						key: 'DELETE',
						name: '删除',
						icon: 'el-icon-delete'
					}]
				}
			}
		},
		props: {
			chat: {
				type: Object
			},
			active: {
				type: Boolean
			},
			index: {
				type: Number
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
		computed: {
			showTime() {
				return this.$date.toTimeText(this.chat.lastSendTime, true)
			}
		}
	}
</script>

<style lang="scss">
	.chat-item {
		height: 65px;
		display: flex;
		margin-bottom: 1px;
		position: relative;
		padding-left: 10px;
		align-items: center;
		padding-right: 5px;
		background-color: #fafafa;
		white-space: nowrap;
		color: black;
		cursor: pointer;

		&:hover {
			background-color: #eeeeee;
		}

		&.active {
			background-color: #e8e8f0;
		}

		.chat-left {
			position: relative;
			display: flex;
			width: 50px;
			height: 50px;

			.unread-text {
				position: absolute;
				background-color: #f56c6c;
				right: -5px;
				top: -5px;
				color: white;
				border-radius: 30px;
				padding: 1px 5px;
				font-size: 10px;
				text-align: center;
				white-space: nowrap;
				border: 1px solid #f1e5e5;
			}
		}


		.chat-right {
			flex: 1;
			display: flex;
			flex-direction: column;
			padding-left: 10px;
			text-align: left;
			overflow: hidden;
			.chat-name {
				font-size: 15px;
				font-weight: 600;
				line-height: 30px;
				white-space: nowrap;
				overflow: hidden;
			}

			.chat-content {
				display: flex;
				line-height: 30px;
				.chat-content-text {
					flex:1;
					font-size: 14px;
					white-space: nowrap;
					overflow: hidden;
					text-overflow: ellipsis;
					img {
						width: 30px !important;
						height: 30px !important;
					}
				}

				.chat-time {
					font-size: 13px;
					text-align: right;
					color: #888888;
					white-space: nowrap;
					overflow: hidden;
				}
			}
		}
	}
</style>
<template>
	<div class="chat-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="chat-left">
			<head-image :url="chat.headImage" :name="chat.showName" :size="45"
				:id="chat.type=='PRIVATE'?chat.targetId:0"></head-image>
			<div v-show="chat.unreadCount>0" class="unread-text">{{chat.unreadCount}}</div>
		</div>
		<div class="chat-right">
			<div class="chat-name">
				<div class="chat-name-text">{{chat.showName}}</div>
				<div class="chat-time-text">{{showTime}}</div>
			</div>
			<div class="chat-content">
				<div class="chat-at-text">{{atText}}</div>
				<div class="chat-send-name" v-show="chat.sendNickName">{{chat.sendNickName+':&nbsp;'}}</div>
				<div class="chat-content-text" v-html="$emo.transform(chat.lastContent)"></div>
			</div>
		</div>
		<right-menu v-show="rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items" @close="rightMenu.show=false"
			@select="onSelectMenu"></right-menu>
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
			onSelectMenu(item) {
				this.$emit(item.key.toLowerCase(), this.msgInfo);
			}
		},
		computed: {
			showTime() {
				return this.$date.toTimeText(this.chat.lastSendTime, true)
			},
			atText() {
				if (this.chat.atMe) {
					return "[有人@我]"
				} else if (this.chat.atAll) {
					return "[@全体成员]"
				}
				return "";
			}
		}
	}
</script>

<style lang="scss">
	.chat-item {
		height: 50px;
		display: flex;
		margin-bottom: 1px;
		position: relative;
		padding: 5px;
		padding-left: 10px;
		align-items: center;
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
			width: 45px;
			height: 45x;

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
				display: flex;
				line-height: 25px;
				height: 25px;
				.chat-name-text {
					flex: 1;
					font-size: 15px;
					font-weight: 600;
					white-space: nowrap;
					overflow: hidden;
				}
				
				
				.chat-time-text{
					font-size: 13px;
					text-align: right;
					color: #888888;
					white-space: nowrap;
					overflow: hidden;
					padding-left: 10px;
				}
			}

			.chat-content {
				display: flex;
				line-height: 22px;
				
				.chat-at-text {
					color: #c70b0b;
					font-size: 12px;
				}
				
				.chat-send-name{
					font-size: 13px;
				}
					

				.chat-content-text {
					flex: 1;
					white-space: nowrap;
					overflow: hidden;
					text-overflow: ellipsis;
					font-size: 13px;	
					img {
						width: 20px !important;
						height: 20px !important;
						vertical-align: bottom;
					}
				}

			}
		}
	}
</style>
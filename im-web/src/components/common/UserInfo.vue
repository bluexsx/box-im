<template>
	<div v-if="show" class="user-info" :style="{ left: pos.x + 'px', top: pos.y + 'px' }" @click.stop>
		<div class="user-info-box">
			<div class="avatar">
				<head-image :name="user.nickName" :url="user.headImageThumb" :size="60" :online="user.online"
					@click.native="showFullImage()" radius="10%"> </head-image>
			</div>
			<div class="info-card">
				<div class="header">
					<div class="nick-name">{{ user.nickName }}</div>
					<div v-if="user.sex == 0" class="icon iconfont icon-man" style="color: darkblue;"></div>
					<div v-if="user.sex == 1" class="icon iconfont icon-girl" style="color: darkred;"></div>
				</div>
				<div class="info-item">
					用户名: {{ user.userName }}
				</div>
				<div class="info-item">
					个性签名: {{ user.signature }}
				</div>
			</div>
		</div>
		<el-divider content-position="center"></el-divider>
		<div class="btn-group">
			<el-button v-if="isFriend" type="primary" @click="onSendMessage()">发消息</el-button>
			<el-button v-else type="primary" @click="onAddFriend()">加为好友</el-button>
		</div>
	</div>


</template>

<script>
import HeadImage from './HeadImage.vue'

export default {
	name: "userInfo",
	components: {
		HeadImage
	},
	data() {
		return {
			show: false,
			user: {},
			pos: {
				x: 0,
				y: 0
			}
		}
	},
	methods: {
		open(user, pos) {
			this.show = true;
			this.user = user;
			let w = document.documentElement.clientWidth;
			let h = document.documentElement.clientHeight;
			this.pos.x = Math.min(pos.x, w - 350);
			this.pos.y = Math.min(pos.y, h - 200);
		},
		close() {
			this.show = false;
		},
		onSendMessage() {
			let user = this.user;
			let chat = {
				type: 'PRIVATE',
				targetId: user.id,
				showName: user.nickName,
				headImage: user.headImage,
			};
			this.chatStore.openChat(chat);
			this.chatStore.setActiveChat(0);
			if (this.$route.path != "/home/chat") {
				this.$router.push("/home/chat");
			}
			this.show = false;
		},
		onAddFriend() {
			this.$http({
				url: "/friend/add",
				method: "post",
				params: {
					friendId: this.user.id
				}
			}).then(() => {
				this.$message.success("添加成功，对方已成为您的好友");
				let friend = {
					id: this.user.id,
					nickName: this.user.nickName,
					headImage: this.user.headImageThumb,
					online: this.user.online,
					deleted: false
				}
				this.friendStore.addFriend(friend);
			})
		},
		showFullImage() {
			if (this.user.headImage) {
				this.$eventBus.$emit("openFullImage", this.user.headImage);
			}
		}
	},
	computed: {
		isFriend() {
			return this.friendStore.isFriend(this.user.id);
		}
	}
}
</script>

<style lang="scss" scoped>
.user-info-mask {
	background-color: rgba($color: #f4f4f4, $alpha: 0);
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
}

.user-info {
	position: absolute;
	width: 300px;
	background-color: white;
	box-shadow: var(--im-box-shadow);
	border-radius: 4px;
	padding: 15px;

	.user-info-box {
		display: flex;
		align-items: center;

		.info-card {
			flex: 1;
			padding-left: 10px;

			.header {
				display: flex;
				align-items: center;

				.nick-name {
					font-size: var(--im-font-size-large);
					font-weight: 600;
				}

				.icon {
					margin-left: 3px;
					font-size: var(--im-font-size);
				}
			}

			.info-item {
				font-size: var(--im-font-size);
				margin-top: 5px;
				word-break: break-all;
			}
		}
	}

	.el-divider--horizontal {
		margin: 18px 0;
	}

	.btn-group {
		text-align: center;
	}
}
</style>
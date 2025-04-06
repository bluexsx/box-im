<template>
	<div class="user-info-mask" @click="$emit('close')">
		<div class="user-info" :style="{ left: pos.x + 'px', top: pos.y + 'px' }" @click.stop>
			<div class="user-info-box">
				<div class="avatar">
					<head-image :name="user.nickName" :url="user.headImageThumb" :size="70" :online="user.online"
						radius="10%" @click.native="showFullImage()"> </head-image>
				</div>
				<div>
					<el-descriptions :column="1" :title="user.nickName" class="user-info-items">
						<el-descriptions-item label="用户名">{{ user.userName }}
						</el-descriptions-item>
						<el-descriptions-item label="签名">{{ user.signature }}
						</el-descriptions-item>
					</el-descriptions>
				</div>
			</div>
			<el-divider content-position="center"></el-divider>
			<div class="user-btn-group">
				<el-button v-show="isFriend" type="primary" @click="onSendMessage()">发消息</el-button>
				<el-button v-show="!isFriend" type="primary" @click="onAddFriend()">加为好友</el-button>
			</div>
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

		}
	},
	props: {
		user: {
			type: Object
		},
		pos: {
			type: Object
		}
	},
	methods: {
		onSendMessage() {
			let user = this.user;
			let chat = {
				type: 'PRIVATE',
				targetId: user.id,
				showName: user.nickName,
				headImage: user.headImage,
			};
			this.$store.commit("openChat", chat);
			this.$store.commit("activeChat", 0);
			if (this.$route.path != "/home/chat") {
				this.$router.push("/home/chat");
			}
			this.$emit("close");
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
				this.$store.commit("addFriend", friend);
			})
		},
		showFullImage() {
			if (this.user.headImage) {
				this.$store.commit("showFullImageBox", this.user.headImage);
			}
		}
	},
	computed: {
		isFriend() {
			return this.$store.getters.isFriend(this.user.id);
		}
	}
}
</script>

<style lang="scss">
.user-info-mask {
	background-color: rgba($color: #000000, $alpha: 0);
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

		.user-info-items {
			margin-left: 10px;
			white-space: nowrap;
			overflow: hidden;

			.el-descriptions__header {
				margin-bottom: 5px;
			}

			.el-descriptions__title {
				font-size: 18px;
			}

			.el-descriptions-item__cell {
				padding-bottom: 1px;
			}
		}
	}

	.el-divider--horizontal {
		margin: 18px 0;
	}

	.user-btn-group {
		text-align: center;
	}
}
</style>

<template>
	<el-container class="friend-page">
		<el-aside width="260px" class="aside">
			<div class="header">
				<el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
				<el-button plain class="add-btn" icon="el-icon-plus" title="添加好友"
					@click="onShowAddFriend()"></el-button>
				<add-friend :dialogVisible="showAddFriend" @close="onCloseAddFriend"></add-friend>
			</div>
			<el-scrollbar class="friend-items">
				<div v-for="(friends, i) in friendValues" :key="i">
					<div class="letter">{{ friendKeys[i] }}</div>
					<div v-for="(friend) in friends" :key="friend.id">
						<friend-item :friend="friend" :active="friend.id === activeFriend.id"
							@chat="onSendMessage(friend)" @delete="onDelFriend(friend)"
							@click.native="onActiveItem(friend)">
						</friend-item>
					</div>
					<div v-if="i < friendValues.length - 1" class="divider"></div>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="container">
			<div class="header" v-show="userInfo.id">
				{{ userInfo.nickName }}
			</div>
			<div v-show="userInfo.id">
				<div class="friend-info">
					<head-image :size="160" :name="userInfo.nickName" :url="userInfo.headImage" radius="10%"
						@click.native="showFullImage()"></head-image>
					<div>
						<div class="info-item">
							<el-descriptions title="好友信息" class="description" :column="1">
								<el-descriptions-item label="用户名">{{ userInfo.userName }}
								</el-descriptions-item>
								<el-descriptions-item label="昵称">{{ userInfo.nickName }}
								</el-descriptions-item>
								<el-descriptions-item label="性别">{{ userInfo.sex == 0 ? "男" : "女"
								}}</el-descriptions-item>
								<el-descriptions-item label="签名">{{ userInfo.signature }}</el-descriptions-item>
							</el-descriptions>
						</div>
						<div class="btn-group">
							<el-button v-show="isFriend" icon="el-icon-position" type="primary"
								@click="onSendMessage(userInfo)">发消息</el-button>
							<el-button v-show="!isFriend" icon="el-icon-plus" type="primary"
								@click="onAddFriend(userInfo)">加为好友</el-button>
							<el-button v-show="isFriend" icon="el-icon-delete" type="danger"
								@click="onDelFriend(userInfo)">删除好友</el-button>
						</div>
					</div>
				</div>
			</div>
		</el-container>
	</el-container>
</template>

<script>
import FriendItem from "../components/friend/FriendItem.vue";
import AddFriend from "../components/friend/AddFriend.vue";
import HeadImage from "../components/common/HeadImage.vue";
import { pinyin } from 'pinyin-pro';

export default {
	name: "friend",
	components: {
		FriendItem,
		AddFriend,
		HeadImage

	},
	data() {
		return {
			searchText: "",
			showAddFriend: false,
			userInfo: {},
			activeFriend: {}
		}
	},
	methods: {
		onShowAddFriend() {
			this.showAddFriend = true;
		},
		onCloseAddFriend() {
			this.showAddFriend = false;
		},
		onActiveItem(friend) {
			this.activeFriend = friend;
			this.loadUserInfo(friend.id);
		},
		onDelFriend(friend) {
			this.$confirm(`确认删除'${friend.nickName}',并清空聊天记录吗?`, '确认解除?', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				this.$http({
					url: `/friend/delete/${friend.id}`,
					method: 'delete'
				}).then(() => {
					this.$message.success("删除好友成功");
					this.$store.commit("removeFriend", friend.id);
					this.$store.commit("removePrivateChat", friend.id);
				})
			})
		},
		onAddFriend(user) {
			this.$http({
				url: "/friend/add",
				method: "post",
				params: {
					friendId: user.id
				}
			}).then(() => {
				this.$message.success("添加成功，对方已成为您的好友");
				let friend = {
					id: user.id,
					nickName: user.nickName,
					headImage: user.headImage,
					online: user.online
				}
				this.$store.commit("addFriend", friend);
			})
		},
		onSendMessage(user) {
			let chat = {
				type: 'PRIVATE',
				targetId: user.id,
				showName: user.nickName,
				headImage: user.headImageThumb,
			};
			this.$store.commit("openChat", chat);
			this.$store.commit("activeChat", 0);
			this.$router.push("/home/chat");
		},
		showFullImage() {
			if (this.userInfo.headImage) {
				this.$store.commit('showFullImageBox', this.userInfo.headImage);
			}
		},
		updateFriendInfo() {
			if (this.isFriend) {
				// store的数据不能直接修改，深拷贝一份store的数据
				let friend = JSON.parse(JSON.stringify(this.activeFriend));
				friend.headImage = this.userInfo.headImageThumb;
				friend.nickName = this.userInfo.nickName;
				this.$store.commit("updateChatFromFriend", friend);
				this.$store.commit("updateFriend", friend);
			}
		},
		loadUserInfo(id) {
			// 获取好友用户信息
			this.$http({
				url: `/user/find/${id}`,
				method: 'GET'
			}).then((userInfo) => {
				this.userInfo = userInfo;
				this.updateFriendInfo();
			})
		},
		firstLetter(strText) {
			// 使用pinyin-pro库将中文转换为拼音
			let pinyinOptions = {
				toneType: 'none', // 无声调
				type: 'normal' // 普通拼音
			};
			let pyText = pinyin(strText, pinyinOptions);
			return pyText[0];
		},
		isEnglish(character) {
			return /^[A-Za-z]+$/.test(character);
		}
	},
	computed: {
		friendStore() {
			return this.$store.state.friendStore;
		},
		isFriend() {
			return this.$store.getters.isFriend(this.userInfo.id);
		},
		friendMap() {
			// 按首字母分组
			let map = new Map();
			this.friendStore.friends.forEach((f) => {
				if (f.deleted || (this.searchText && !f.nickName.includes(this.searchText))) {
					return;
				}
				let letter = this.firstLetter(f.nickName).toUpperCase();
				// 非英文一律为#组
				if (!this.isEnglish(letter)) {
					letter = "#"
				}
				if (f.online) {
					letter = '在线'
				}
				if (map.has(letter)) {
					map.get(letter).push(f);
				} else {
					map.set(letter, [f]);
				}
			})
			// 排序
			let arrayObj = Array.from(map);
			arrayObj.sort((a, b) => {
				// #组在最后面
				if (a[0] == '#' || b[0] == '#') {
					return b[0].localeCompare(a[0])
				}
				return a[0].localeCompare(b[0])
			})
			map = new Map(arrayObj.map(i => [i[0], i[1]]));
			return map;
		},
		friendKeys() {
			return Array.from(this.friendMap.keys());
		},
		friendValues() {
			return Array.from(this.friendMap.values());
		}
	}
}
</script>

<style lang="scss" scoped>
.friend-page {
	.aside {
		display: flex;
		flex-direction: column;
		background: var(--im-background);

		.header {
			height: 50px;
			display: flex;
			align-items: center;
			padding: 0 8px;

			.add-btn {
				padding: 5px !important;
				margin: 5px;
				font-size: 16px;
				border-radius: 50%;
			}
		}

		.friend-items {
			flex: 1;

			.letter {
				text-align: left;
				font-size: var(--im-larger-size-larger);
				padding: 5px 15px;
				color: var(--im-text-color-light);
			}
		}
	}

	.container {
		display: flex;
		flex-direction: column;

		.header {
			height: 50px;
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 0 12px;
			font-size: var(--im-font-size-larger);
			border-bottom: var(--im-border);
			box-sizing: border-box;
		}

		.friend-info {
			display: flex;
			padding: 50px 80px 20px 80px;
			text-align: center;

			.info-item {
				margin-left: 20px;
				background-color: #ffffff;
				border: 1px #ddd solid;
			}

			.description {
				padding: 20px 20px 0 20px;
			}
		}

		.btn-group {
			text-align: left !important;
			padding: 20px;
		}
	}
}
</style>
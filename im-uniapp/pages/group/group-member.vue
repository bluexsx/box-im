<template>
	<view class="page group-member">
		<nav-bar back>群成员</nav-bar>
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" cancelButton="none"
					placeholder="输入昵称搜索"></uni-search-bar>
			</view>
		</view>
		<view class="member-items">
			<virtual-scroller class="scroll-bar" :items="showMembers">
				<template v-slot="{ item }">
					<view class="member-item" @click="onShowUserInfo(item.userId)">
						<head-image :name="item.showNickName" :online="item.online" :url="item.headImage"></head-image>
						<view class="member-name">{{ item.showNickName }}
							<uni-tag v-if="item.userId == group.ownerId" text="群主" size="small" circle type="error"></uni-tag>
							<uni-tag v-if="item.userId == userStore.userInfo.id" text="我" size="small" circle></uni-tag>
						</view>
					</view>
				</template>
			</virtual-scroller>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			isModify: false,
			searchText: "",
			group: {},
			members: []
		}
	},
	methods: {
		onShowUserInfo(userId) {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + userId
			})
		},
		loadGroupInfo(id) {
			this.$http({
				url: `/group/find/${id}`,
				method: 'GET'
			}).then((group) => {
				this.group = group;
			});
		},
		loadGroupMembers(id) {
			this.$http({
				url: `/group/members/${id}`,
				method: "GET"
			}).then((members) => {
				this.members = members;
			})
		},
		isSelf(userId) {
			return this.userStore.userInfo.id == userId
		}
	},
	computed: {
		isOwner() {
			return this.userStore.userInfo.id == this.group.ownerId;
		},
		showMembers() {
			return this.members.filter(m => !m.quit && m.showNickName.includes(this.searchText))
		}
	},
	onLoad(options) {
		this.loadGroupInfo(options.id);
		this.loadGroupMembers(options.id);
	},
	onUnload() {
		if (this.isModify) {
			// 刷新页面
			let pages = getCurrentPages();
			let prevPage = pages[pages.length - 2];
			prevPage.$vm.loadGroupMembers();
		}
	}
}
</script>

<style scoped lang="scss">
.group-member {
	position: relative;
	display: flex;
	flex-direction: column;

	.member-items {
		position: relative;
		flex: 1;
		overflow: hidden;

		.member-item {
			height: 120rpx;
			display: flex;
			margin-bottom: 1rpx;
			position: relative;
			padding: 0 30rpx;
			align-items: center;
			background-color: white;
			white-space: nowrap;

			.member-name {
				display: flex;
				align-items: center;
				flex: 1;
				padding-left: 20rpx;
				font-size: $im-font-size;
				line-height: $im-font-size * 2;
				white-space: nowrap;
				overflow: hidden;

				.uni-tag {
					margin-left: 5rpx;
				}
			}
		}

		.scroll-bar {
			height: 100%;
		}
	}
}
</style>
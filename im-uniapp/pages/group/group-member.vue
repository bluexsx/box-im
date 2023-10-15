<template>
	<view class="page group-member">
		<view class="search-bar">
			<uni-search-bar v-model="searchText" cancelButton="none" placeholder="输入成员昵称搜索"></uni-search-bar>
		</view>
		<view class="member-items">
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="(member,idx) in groupMembers"
					v-show="!searchText || member.aliasName.startsWith(searchText)" :key="idx">
					<uni-list-chat :avatar="member.headImage" :title="member.aliasName" :clickable="true"
						@click="onShowUserInfo(member.userId)">
						<view class="chat-custom-right">
							<button type="warn" v-show="isOwner && !isSelf(member.userId)" size="mini"
								@click.stop="onKickOut(member,idx)">移出群聊</button>
						</view>
					</uni-list-chat>
				</view>
			</scroll-view>
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
				groupMembers: []
			}
		},
		methods: {
			onShowUserInfo(userId) {
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + userId
				})
			},
			onKickOut(member, idx) {
				uni.showModal({
					title: '确认移出?',
					content: `确定将成员'${member.aliasName}'移出群聊吗？`,
					success: (res) => {
						if(res.cancel) 
							return;
						this.$http({
							url: `/group/kick/${this.group.id}?userId=${member.userId}`,
							method: 'DELETE'
						}).then(() => {
							uni.showToast({
								title: `已将${member.aliasName}移出群聊`,
								icon: 'none'
							})
							this.groupMembers.splice(idx, 1);
							this.isModify = true;
						});
					}
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
					this.groupMembers = members.filter(m => !m.quit);
				})
			},
			isSelf(userId) {
				return this.$store.state.userStore.userInfo.id == userId
			}
		},
		computed: {
			isOwner() {
				return this.$store.state.userStore.userInfo.id == this.group.ownerId;
			}
		},
		onLoad(options) {
			this.loadGroupInfo(options.id);
			this.loadGroupMembers(options.id);
		},
		onUnload() {
			if(this.isModify){
				// 刷新页面
				let pages = getCurrentPages();
				let prevPage = pages[pages.length - 2];
				prevPage.$vm.loadGroupMembers();
			}
		}
	}
</script>

<style scoped lang="scss">
	.group-invite{
		position: relative;
		border: #dddddd solid 1px;
		display: flex;
		flex-direction: column;
		
		.member-items{
			position: relative;
			flex: 1;
			overflow: hidden;
			.scroll-bar {
				height: 100%;
			}
		}
	}
</style>
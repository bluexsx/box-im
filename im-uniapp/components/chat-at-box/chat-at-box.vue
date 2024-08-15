<template>
	<uni-popup ref="popup" type="bottom" @change="onChange">
		<view class="chat-at-box">
			<view class="chat-at-top">
				<view class="chat-at-tip"> 选择要提醒的人</view>
				<button class="chat-at-btn" type="warn" size="mini" @click="onClean()">清空 </button>
				<button class="chat-at-btn" type="primary" size="mini" @click="onOk()">确定({{atUserIds.length}})
				</button>
			</view>
			<scroll-view v-show="atUserIds.length>0" scroll-x="true" scroll-left="120">
				<view class="at-user-items">
					<view v-for="m in showMembers" v-show="m.checked" class="at-user-item">
						<head-image :name="m.showNickName" :url="m.headImage" :size="60"></head-image>
					</view>
				</view>
			</scroll-view>
			<view class="search-bar">
				<uni-search-bar v-model="searchText" cancelButton="none" radius="100" placeholder="搜索"></uni-search-bar>
			</view>
			<view class="member-items">
				<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
					<view v-for="m in showMembers" v-show="m.showNickName.includes(searchText)"
						:key="m.userId">
						<view class="member-item" @click="onSwitchChecked(m)">
							<head-image :name="m.showNickName" :online="m.online" :url="m.headImage"
								:size="90"></head-image>
							<view class="member-name">{{ m.showNickName}}</view>
							<view class="member-checked">
								<radio :checked="m.checked" @click.stop="onSwitchChecked(m)" />
							</view>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>
	</uni-popup>
</template>

<script>
	export default {
		name: "chat-at-box",
		props: {
			ownerId: {
				type: Number,
			},
			members: {
				type: Array
			}
		},
		data() {
			return {
				searchText: "",
				showMembers:[]
			};
		},
		methods: {
			init(atUserIds) {
				this.showMembers = [];
				let userId = this.userStore.userInfo.id;
				if(this.ownerId == userId){
					this.showMembers.push({
						userId:-1,
						showNickName: "全体成员"
					})
				}
				this.members.forEach((m) => {
					if(m.userId != userId){
						m.checked = atUserIds.indexOf(m.userId) >= 0;
						this.showMembers.push(m);
					}
				});
			},
			open() {
				this.$refs.popup.open();
			},
			onSwitchChecked(member) {
				member.checked = !member.checked;
			},
			onClean() {
				this.showMembers.forEach((m) => {
					m.checked = false;
				})
			},
			onOk() {
				this.$refs.popup.close();
			},
			onChange(e) {
				if (!e.show) {
					this.$emit("complete", this.atUserIds)
				}
			}
		},
		computed: {
			atUserIds() {
				let ids = [];
				this.showMembers.forEach((m) => {
					if (m.checked) {
						ids.push(m.userId);
					}
				})
				return ids;
			}
		}
	}
</script>


<style lang="scss" scoped>
	.chat-at-box {
		position: relative;
		border: #dddddd solid 1rpx;
		display: flex;
		flex-direction: column;
		background-color: white;
		padding: 10rpx;
		border-radius: 15rpx;

		.chat-at-top {
			display: flex;
			align-items: center;
			height: 70rpx;
			padding: 10rpx;

			.chat-at-tip {
				flex: 1;
			}

			.chat-at-btn {
				margin-left: 10rpx;
			}
		}


		.at-user-items {
			display: flex;
			align-items: center;
			height: 90rpx;

			.at-user-item {
				padding: 3rpx;
			}
		}



		.member-items {
			position: relative;
			flex: 1;
			overflow: hidden;

			.member-item {
				height: 120rpx;
				display: flex;
				position: relative;
				padding: 0 30rpx;
				align-items: center;
				background-color: white;
				white-space: nowrap;

				.member-name {
					flex: 1;
					padding-left: 20rpx;
					font-size: 30rpx;
					font-weight: 600;
					line-height: 60rpx;
					white-space: nowrap;
					overflow: hidden;
				}
			}

			.scroll-bar {
				height: 800rpx;
			}
		}
	}
</style>
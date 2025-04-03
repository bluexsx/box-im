<template>
	<uni-popup ref="popup" type="bottom">
		<view class="chat-group-member-choose">
			<view class="top-bar">
				<view class="top-tip">选择成员</view>
				<button class="top-btn" type="warn" size="mini" @click="onClean()">清空 </button>
				<button class="top-btn" type="primary" size="mini" @click="onOk()">确定({{ checkedIds.length }})
				</button>
			</view>
			<scroll-view v-show="checkedIds.length > 0" scroll-x="true" scroll-left="120">
				<view class="checked-users">
					<view v-for="m in checkedMembers" class="user-item" :key="m.userId">
						<head-image :name="m.showNickName" :url="m.headImage" :size="60"></head-image>
					</view>
				</view>
			</scroll-view>
			<view class="search-bar">
				<uni-search-bar v-model="searchText" cancelButton="none" placeholder="搜索"></uni-search-bar>
			</view>
			<view class="member-items">
				<virtual-scroller class="scroll-bar" :items="showMembers">
					<template v-slot="{ item }">
						<view class="member-item" @click="onSwitchChecked(item)">
							<head-image :name="item.showNickName" :online="item.online" :url="item.headImage"
								:size="90"></head-image>
							<view class="member-name">{{ item.showNickName }}
							</view>
							<view class="member-checked">
								<radio :checked="item.checked" :disabled="item.locked"
									@click.stop="onSwitchChecked(item)" />
							</view>
						</view>
					</template>
				</virtual-scroller>
			</view>
		</view>
	</uni-popup>
</template>


<script>
export default {
	name: "chat-group-member-choose",
	props: {
		members: {
			type: Array
		},
		maxSize: {
			type: Number,
			default: -1
		}
	},
	data() {
		return {
			searchText: "",
		};
	},
	methods: {
		init(checkedIds, lockedIds) {
			this.members.forEach((m) => {
				m.checked = checkedIds.indexOf(m.userId) >= 0;
				m.locked = lockedIds.indexOf(m.userId) >= 0;
			});
		},
		open() {
			this.$refs.popup.open();
		},
		onSwitchChecked(m) {
			if (!m.locked) {
				m.checked = !m.checked;
			}
			// 达到选择上限
			if (this.maxSize > 0 && this.checkedIds.length > this.maxSize) {
				m.checked = false;
				uni.showToast({
					title: `最多选择${this.maxSize}位用户`,
					icon: "none"
				})
			}
		},
		onClean() {
			this.members.forEach((m) => {
				if (!m.locked) {
					m.checked = false;
				}
			})
		},
		onOk() {
			this.$refs.popup.close();
			this.$emit("complete", this.checkedIds)
		},
		isChecked(m) {
			return this.checkedIds.indexOf(m.userId) >= 0;
		}
	},
	computed: {
		checkedIds() {
			return this.members.filter((m) => m.checked).map(m => m.userId)
		},
		checkedMembers() {
			return this.members.filter((m) => m.checked);
		},
		showMembers() {
			return this.members.filter(m => !m.quit && m.showNickName.includes(this.searchText))
		}
	}
}
</script>


<style lang="scss" scoped>
.chat-group-member-choose {
	position: relative;
	display: flex;
	flex-direction: column;
	background-color: white;
	padding: 10rpx;
	border-radius: 15rpx;

	.top-bar {
		display: flex;
		align-items: center;
		height: 70rpx;
		padding: 10rpx 30rpx;

		.top-tip {
			flex: 1;
		}

		.top-btn {
			margin-left: 10rpx;
		}
	}

	.checked-users {
		display: flex;
		align-items: center;
		height: 90rpx;
		padding: 0 30rpx;

		.user-item {
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
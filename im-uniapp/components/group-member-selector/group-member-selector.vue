<template>
	<uni-popup ref="popup" type="bottom">
		<view class="chat-group-member-choose">
			<view class="top-bar">
				<view class="top-tip">选择成员</view>
				<button class="top-btn" type="warn" size="mini" @click="onClean()">清空 </button>
				<button class="top--btn" type="primary" size="mini" @click="onOk()">确定({{checkedIds.length}})
				</button>
			</view>
			<scroll-view v-show="checkedIds.length>0" scroll-x="true" scroll-left="120">
				<view class="checked-users">
					<view v-for="m in members" v-show="m.checked" class="user-item">
						<head-image :name="m.aliasName" :url="m.headImage" :size="60"></head-image>
					</view>
				</view>
			</scroll-view>
			<view class="search-bar">
				<uni-search-bar v-model="searchText" cancelButton="none" placeholder="搜索"></uni-search-bar>
			</view>
			<view class="member-items">
				<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
					<view v-for="m in members"  v-show="!m.quit && m.aliasName.startsWith(searchText)" :key="m.userId">
						<view class="member-item" @click="onSwitchChecked(m)">
							<head-image :name="m.aliasName" :online="m.online" :url="m.headImage"
								:size="90"></head-image>
							<view class="member-name">{{ m.aliasName}}</view>
							<view class="member-checked">
								<radio :checked="m.checked" :disabled="m.locked" @click.stop="onSwitchChecked(m)" />
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
		name: "chat-group-member-choose",
		props: {
			members: {
				type: Array
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
				if(!m.locked){
					m.checked = !m.checked;
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
				let ids = [];
				this.members.forEach((m) => {
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
	.chat-group-member-choose {
		position: relative;
		border: #dddddd solid 1rpx;
		display: flex;
		flex-direction: column;
		background-color: white;
		padding: 10rpx;
		border-radius: 15rpx;

		.top-bar {
			display: flex;
			align-items: center;
			height: 70rpx;
			padding: 10rpx;

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
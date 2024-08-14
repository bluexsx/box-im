<template>
	<uni-popup ref="popup" type="center">
		<uni-popup-dialog mode="base"  :duration="2000" title="是否加入通话?" confirmText="加入"
			@confirm="onOk">
			<div class="group-rtc-join">
				<div class="host-info">
					<div>发起人</div>
					<head-image :name="rtcInfo.host.nickName" :url="rtcInfo.host.headImage" :size="80"></head-image>
				</div>
				<div class="user-info">
					<div>{{rtcInfo.userInfos.length+'人正在通话中'}}</div>
					<scroll-view scroll-x="true" scroll-left="120">
						<view class="user-list">
							<view v-for="user in rtcInfo.userInfos" class="user-item">
								<head-image :name="user.nickName" :url="user.headImage" :size="80"></head-image>
							</view>
						</view>
					</scroll-view>
				</div>
			</div>
		</uni-popup-dialog>
	</uni-popup>
</template>

<script>
	export default {
		data() {
			return {
				userStore: this.useUserStore(),
				rtcInfo: {}
			}
		},
		props: {
			groupId: {
				type: Number
			}
		},
		methods: {
			open(rtcInfo) {
				this.rtcInfo = rtcInfo;
				this.$refs.popup.open();
			},
			onOk() {
				let users = this.rtcInfo.userInfos;
				let mine = this.userStore.userInfo;
				// 加入自己的信息
				if(!users.find((user)=>user.id==mine.id)){
					users.push({
						id: mine.id,
						nickName: mine.nickName,
						headImage: mine.headImageThumb,
						isCamera: false,
						isMicroPhone: true
					})
				}
				const userInfos = encodeURIComponent(JSON.stringify(users));
				uni.navigateTo({
					url: `/pages/chat/chat-group-video?groupId=${this.groupId}&isHost=false
						&inviterId=${mine.id}&userInfos=${userInfos}`
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.group-rtc-join {
		width: 100%;

		.host-info {
			font-size: 16px;
			padding: 10px;
		}

		.user-info {
			font-size: 16px;
			padding: 10px;
		}

		.user-list {
			display: flex;
			align-items: center;
			height: 90rpx;

			.user-item {
				padding: 3rpx;
			}
		}
	}
</style>
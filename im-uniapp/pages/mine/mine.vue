<template>
	<view class="page mine">
		<nav-bar>我的</nav-bar>
		<view class="user-info" @click="onModifyInfo()">
			<head-image :name="userInfo.nickName" :url="userInfo.headImage" :size="160"></head-image>
			<view class="user-info-area">
				<view class="primary-info">
					<view class="nick-name">{{userInfo.nickName}}
						<text v-show="userInfo.sex == 0" class="iconfont icon-man" color="darkblue"></text>
						<text v-show="userInfo.sex == 1" class="iconfont icon-girl" color="darkred"></text>
					</view>
				</view>
				<view class="user-name">
					<text class="label-text">用户名:</text>
					<text class="content-text"> {{ userInfo.userName }}</text>
				</view>
				<view class="signature-text">
					<text v-if="userInfo.signature"> {{ userInfo.signature }}</text>
				</view>
			</view>
			<view class="btn-wrap">
				<nav-arrow class="info-arrow"></nav-arrow>
			</view>
		</view>
		<bar-group>
			<arrow-bar title="修改密码" icon="icon-modify-pwd" icon-color="#5daa31"
				@tap="onModifyPassword()"></arrow-bar>
		</bar-group>
		<bar-group>
			<btn-bar title="退出登录" type="danger" @tap="onQuit()"></btn-bar>
		</bar-group>
		<popup-modal ref="modal"></popup-modal>
	</view>
</template>

<script>
import { userStore } from '@/store/stores.js'

export default {
	data() {
		return {}
	},
	methods: {
		onModifyInfo() {
			uni.navigateTo({
				url: "/pages/mine/mine-edit"
			})
		},
		onModifyPassword() {
			uni.navigateTo({
				url: "/pages/mine/mine-password"
			})
		},
		onQuit() {
			this.$refs.modal.open({
				title: '确认退出?',
				success: (res) => {
					getApp().$vm.exit()
				}
			});
		}
	},
	computed: {
		userInfo() {
			return userStore.userInfo;
		}
	}
}
</script>

<style scoped lang="scss">
.mine {
	.user-info {
		background: white;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx;
		overflow: hidden;
		margin-bottom: 20rpx;

		.user-info-area {
			display: flex;
			align-items: flex-start;
			flex-direction: column;
			padding-left: 40rpx;
			flex: 1;

			.primary-info {
				display: flex;
				align-items: center;
				margin-bottom: 10rpx;
				gap: 10rpx;

				.nick-name {
					font-size: $im-font-size-large;
					font-weight: 600;
				}

				.iconfont {
					font-size: $im-font-size;
					opacity: 0.8;
					margin-left: 5rpx;
				}

				.icon-man {
					color: $im-color-primary;
				}

				.icon-girl {
					color: $im-color-danger;
				}
			}

			.user-name {
				line-height: 1.5;
				font-size: $im-font-size;
				color: $im-text-color-light;
				margin-bottom: 10rpx;
			}

			.content-text {
				margin-left: 10rpx;
				word-break: break-all;
				overflow: hidden;
			}

			.signature-text {
				color: $im-text-color-lighter;
				word-break: break-all;
				overflow-y: auto;
				overflow-x: hidden;
				line-height: 1.5;
				font-size: $im-font-size-small;
				white-space: pre-wrap;
				max-height: 300rpx;
			}
		}

		.btn-wrap {
			display: flex;
			flex-direction: column;
			width: 50rpx;
			position: relative;

			.info-arrow {
				margin-top: 30rpx;
			}
		}
	}
}
</style>

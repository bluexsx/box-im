<template>
	<view class="page mine">
		<nav-bar>我的</nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<view class="content" @click="onModifyInfo()">
				<head-image :name="userInfo.nickName" :url="userInfo.headImage" :size="160"></head-image>
				<view class="info-item">
					<view class="info-primary">
						<text class="nick-name">
							{{ userInfo.nickName }}
						</text>
						<text v-show="userInfo.sex == 0" class="iconfont icon-man" color="darkblue"></text>
						<text v-show="userInfo.sex == 1" class="iconfont icon-girl" color="darkred"></text>
					</view>
					<view class="info-text">
						<text class="label-text">用户名:</text>
						<text class="content-text"> {{ userInfo.userName }} </text>
					</view>
					<view class="info-text">
						<view>
							<text class="label-text">签名:</text>
							<text class="content-text"> {{ userInfo.signature }}</text>
						</view>
					</view>
				</view>
				<nav-arrow class="info-arrow"></nav-arrow>
			</view>
		</uni-card>
		<bar-group>
			<arrow-bar title="修改密码" icon="icon-modify-pwd" @tap="onModifyPassword()"></arrow-bar>
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
	.content {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 10rpx;
		padding-right: 30rpx;
		overflow: hidden;

		.info-item {
			display: flex;
			align-items: flex-start;
			flex-direction: column;
			padding-left: 40rpx;
			flex: 1;

			.info-text {
				line-height: 1.5;
				//margin-bottom: 10rpx;
			}

			.label-text {
				font-size: $im-font-size-small;
				color: $im-text-color-light;
			}

			.content-text {
				font-size: $im-font-size-small;
				color: $im-text-color-light;
				margin-left: 10rpx;
				word-break: break-all;
				overflow: hidden;
			}

			.info-primary {
				display: flex;
				align-items: center;
				margin-bottom: 10rpx;

				.nick-name {
					font-size: $im-font-size-large;
					font-weight: 600;
				}

				.icon-man {
					color: $im-text-color;
					font-size: $im-font-size-large;
					padding-left: 10rpx;
				}

				.icon-girl {
					color: darkred;
				}
			}
		}

		.info-arrow {
			width: 50rpx;
			position: relative;
			left: 30rpx;
		}
	}
}

</style>

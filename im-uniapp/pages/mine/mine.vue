<template>
	<view class="page mine">
		<nav-bar>我的</nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<view class="content" @click="onModifyInfo()">
				<head-image :name="userInfo.nickName" :url="userInfo.headImage" :size="160"></head-image>
				<view class="info-item">
					<view class="info-primary">
						<text class="info-username">
							{{ userInfo.userName }}
						</text>
						<text v-show="userInfo.sex == 0" class="iconfont icon-man" color="darkblue"></text>
						<text v-show="userInfo.sex == 1" class="iconfont icon-girl" color="darkred"></text>
					</view>
					<view class="info-text">
						<text class="label-text">
							昵称:
						</text>
						<text class="content-text">
							{{ userInfo.nickName }}
						</text>
					</view>
					<view class="info-text">
						<view>
							<text class="label-text">
								签名:
							</text>
							<text class="content-text">
								{{ userInfo.signature }}
							</text>
						</view>
					</view>
				</view>
			</view>
		</uni-card>
		<bar-group>
			<arrow-bar title="修改密码" @tap="onModifyPassword()"></arrow-bar>
		</bar-group>
		<bar-group>
			<btn-bar title="退出登录" type="danger" @tap="onQuit()"></btn-bar>
		</bar-group>
	</view>
</template>

<script>
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
			uni.showModal({
				title: '确认退出?',
				success: (res) => {
					if (res.confirm) {
						console.log(getApp())
						getApp().$vm.exit()
					}
				}
			});
		}
	},
	computed: {
		userInfo() {
			return this.userStore.userInfo;
		}
	}


}
</script>

<style scoped lang="scss">
.mine {
	.content {
		//height: 200rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx;

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
			}

			.info-primary {
				display: flex;
				align-items: center;
				margin-bottom: 10rpx;

				.info-username {
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
			font-size: 30rpx;
			position: relative;
			left: 30rpx;
		}
	}

}
</style>

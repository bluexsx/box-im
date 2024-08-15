<template>
	<view class="page mine">
		<view class="content" @click="onModifyInfo()">
			<head-image :name="userInfo.nickName"  
				:url="userInfo.headImage"
				:size="160"></head-image>
			<view class="info-item">
				<view class="info-primary">
					<text class="info-username">
						{{userInfo.userName}}
					</text>
					<text v-show="userInfo.sex==0" class="iconfont icon-man" 
						color="darkblue"></text>
					<text v-show="userInfo.sex==1" class="iconfont icon-girl"
						color="darkred"></text>
				</view>
				<text>
					昵称 ：{{userInfo.nickName}}
				</text>
				<text>
					签名 ：{{userInfo.signature}}
				</text>
			</view>
			<view class="info-arrow">></view>
		</view>
		<view class="line"></view>
		<view class="btn-group">
			<button class="btn" type="primary" @click="onModifyPassword()">修改密码</button>
			<button class="btn" type="warn" @click="onQuit()">退出</button>
		</view>
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
							getApp().exit()
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
			height: 200rpx;
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

				.info-primary {
					display: flex;
					align-items: center;
					.info-username {
						font-size: 40rpx;
						font-weight: 600;
					}
					
					.icon-man {
						color: darkblue;
					}
					
					.icon-girl {
						color: darkred;
					}
				}
			}

			.info-arrow {
				width: 50rpx;
				font-size: 30rpx;
				font-weight: 600;
			}
		}

		.line {
			margin: 20rpx;
			border-bottom: 1px solid #aaaaaa;
		}

		.btn-group {
			margin: 100rpx;

			.btn {
				margin-top: 20rpx;
			}

		}
	}
</style>
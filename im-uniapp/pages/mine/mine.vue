<template>
	<view class="page mine">
		<view class="content" @click="onModifyInfo()">
			<view class="avatar">
				<image class="head-image" :src="userInfo.headImage" lazy-load="true" mode="aspectFill"></image>
			</view>
			<view class="info-item">
				<view class="info-primary">
					<text class="info-username">
						{{userInfo.userName}}
					</text>
					<uni-icons v-show="userInfo.sex==0" class="sex-boy" type="person-filled" size="20"
						color="darkblue"></uni-icons>
					<uni-icons v-show="userInfo.sex==1" class="sex-girl" type="person-filled" size="20"
						color="darkred"></uni-icons>
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
							this.$exit();
						}
					}
				});
			}
		},
		computed: {
			userInfo() {
				return this.$store.state.userStore.userInfo;
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

			.avatar {
				display: flex;
				justify-content: center;
				align-items: center;
				width: 160rpx;
				height: 160rpx;

				.head-image {
					width: 100%;
					height: 100%;
					border-radius: 10%;
				}
			}

			.info-item {
				display: flex;
				align-items: flex-start;
				flex-direction: column;
				padding-left: 40rpx;
				flex: 1;

				.info-primary {
					display: flex;

					.info-username {
						font-size: 40rpx;
						font-weight: 600;
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
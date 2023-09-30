<template>
	<view class="user-info">
		<view class="content">
			<view class="avatar">
				<image class="head-image" :src="userInfo.headImage" lazy-load="true"  mode="aspectFill"></image>
			</view>
			<view class="info-item">
				<view class="info-primary">
					<text class="info-username">
						{{userInfo.userName}}
					</text>
					<uni-icons v-show="userInfo.sex==0" class="sex-boy" type="person-filled" size="20" color="darkblue"  ></uni-icons>
					<uni-icons v-show="userInfo.sex==1" class="sex-girl" type="person-filled" size="20" color="darkred"  ></uni-icons>
				</view>
				<text >
					昵称 ：{{userInfo.nickName}}
				</text>
				<text class="person-wx-name">
					签名 ：{{userInfo.signature}}
				</text>
			</view>
		</view>
		<view class="line"></view>
		<view class="btn-group">
			<button  v-show="isFriend" type="primary">发消息</button>
			<button  v-show="!isFriend" type="primary">加为好友</button>
		</view>
	</view>
</template>

<script>
	export default{
		data(){
			return {
				userInfo:{}
			}
		},
		computed: {
			isFriend(){
				let friends = this.$store.state.friendStore.friends;
				let friend = friends.find((f) => f.id == this.userInfo.id);
				return friend != undefined;
			}
		},
		onLoad(options) {
			// 查询好友信息
			console.log(options.id)
			const id = options.id;
			this.$http({
				url:"/user/find/"+id
			}).then((userInfo)=>{
				this.userInfo = userInfo;
			})
		}
	}
	
	
</script>

<style lang="scss" scoped>
	.user-info{
		.content{
			height: 200rpx;
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 20rpx;
			.avatar{
				display: flex;
				justify-content: center;
				align-items: center;
				width: 160rpx;
				height: 160rpx;
				
				.head-image{
					width: 100%;
					height: 100%;
					border-radius: 10%;
				}
			}
			
			.info-item{
				display: flex;
				align-items: flex-start;
				flex-direction: column;
				padding-left: 40rpx;
				flex: 1;
				.info-primary{
					
					display: flex;
					.info-username{
						font-size: 40rpx;
						font-weight: 600;
					}
				}
				
			}
		}
		
		.line{
			margin: 20rpx;
			border-bottom: 1px	solid #aaaaaa;
		}
		.btn-group{
			margin: 100rpx;
		}
	}
</style>
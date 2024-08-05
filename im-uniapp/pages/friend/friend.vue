<template>
	<view class="tab-page friend">
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar @focus="onFocusSearch" cancelButton="none" placeholder="点击搜索好友" ></uni-search-bar>
			</view>
			<view  class="nav-add" @click="onAddNewFriends()">
				<uni-icons type="personadd" size="30"></uni-icons>
			</view>
		</view>
		<view class="friend-tip" v-if="friends.length==0">
			温馨提示：您现在还没有任何好友，快点击右上方'+'按钮添加好友吧~
		</view>
		<view class="friend-items" v-else>
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<!-- 先展示在线好友-->
				<view v-for="(friend,index) in friends" :key="index">
					<friend-item v-if="!friend.delete&&friend.online" :friend="friend"></friend-item>
				</view>
				<!-- 再展示离线好友-->
				<view v-for="(friend,index) in friends" :key="index">
					<friend-item v-if="!friend.delete&&!friend.online" :friend="friend"></friend-item>
				</view>
			</scroll-view>
		</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {

			}
		},
		methods: {
			onFocusSearch() {
				uni.navigateTo({
					url: "/pages/friend/friend-search"
				})
			},
			onAddNewFriends(){
				uni.navigateTo({
					url: "/pages/friend/friend-add"
				})
			}
		},
		computed:{
			friends(){
				return this.$store.state.friendStore.friends;
			}
		}
	}
</script>

<style lang="scss" scoped>
	.friend {
		position: relative;
		border: #dddddd solid 1px;
		display: flex;
		flex-direction: column;
		
		.friend-tip{
			position: absolute;
			top: 400rpx;
			padding: 50rpx ;
			text-align: center;
			line-height: 50rpx;
			text-align: left;
			color: darkblue;
			font-size: 30rpx;
		}
		
		.nav-bar {
			margin: 5rpx;
			display: flex;
			align-items: center;
			background-color: white;
			.nav-search{
				flex:1;
			}
			
			.nav-add {
				line-height: 56px;
				cursor: pointer;
			}
		}

		.friend-items {
			flex: 1;
			padding: 0;
			border: #dddddd solid 1px;
			overflow: hidden;
			position: relative;
			
			.scroll-bar {
				height: 100%;
			}
		}
	}
</style>
<template>
	<view class="tab-page friend">
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar @focus="onFocusSearch" cancelButton="none" placeholder="点击搜索好友" ></uni-search-bar>
			</view>
			<view  class="nav-add">
				<uni-icons type="personadd" size="30"></uni-icons>
			</view>
		</view>
		<view class="friend-items">
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="(friend,index) in $store.state.friendStore.friends" :key="index">
					<friend-item :friend="friend" :index="index"></friend-item>
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
			}
		},
		onNavigationBarButtonTap(e) {
			if (e.index == 1) {
				uni.navigateTo({
					url: "/pages/friend/friend-search"
				})
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
<template>
	<view class="page friend-search" >
		<view>
		  	<uni-search-bar  v-model="searchText"  :focus="true"  @cancel="onCancel()" placeholder="输入好友昵称搜索"></uni-search-bar>
		</view>
		<view class="friend-items">
			<scroll-view  class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="(friend,index) in $store.state.friendStore.friends" v-show="searchText && friend.nickName.startsWith(searchText)"  :key="index">
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
				searchText:""
			}
		},
		methods: {
			onCancel(){
				uni.navigateBack();
			}
		}
	}
</script>

<style scoped lang="scss">
	.friend-search {
		position: relative;
		border: #dddddd solid 1px;
		display: flex;
		flex-direction: column;

		.search-bar {
			background: white;
		}
		.friend-items{
			position: relative;
			flex: 1;
			overflow: hidden;
			.scroll-bar {
				height: 100%;
			}
		}
		
	}
</style>
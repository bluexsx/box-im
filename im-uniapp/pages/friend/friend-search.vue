<template>
	<view>
		<view>
		  	<uni-search-bar  v-model="searchText"  :focus="true"  @cancel="onCancel()" placeholder="输入好友昵称搜索"></uni-search-bar>
		</view>
		<scroll-view  class="scroll-bar" scroll-with-animation="true" scroll-y="true">
			<view v-for="(friend,index) in $store.state.friendStore.friends" v-show="searchText && friend.nickName.startsWith(searchText)"  :key="index">
				<friend-item :friend="friend" :index="index"></friend-item>
			</view>
		</scroll-view>
		
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
			
		},
		onNavigationBarButtonTap(e) {
			if(e.index==0){
				uni.navigateBack();
			}
		},
		onNavigationBarSearchInputChanged(e){
			this.searchText = e.text;
		}
	}
</script>

<style>

</style>

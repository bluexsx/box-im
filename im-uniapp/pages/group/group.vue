<template>
	<view class="tab-page group">
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar @focus="onFocusSearch" cancelButton="none" placeholder="点击搜索群聊"></uni-search-bar>
			</view>
			<view class="nav-add" @click="onCreateNewGroup()">
				<uni-icons type="personadd" size="30"></uni-icons>
			</view>
		</view>
		<view class="group-items">
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="group in $store.state.groupStore.groups" :key="group.id">
					<group-item :group="group"></group-item>
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
			onFocusSearch() {},
			onCreateNewGroup() {
				uni.navigateTo({
					url: "/pages/group/group-edit"
				})
			}
		}
		
	}
</script>

<style lang="scss" scoped>
	.group {
		position: relative;
		border: #dddddd solid 1px;
		display: flex;
		flex-direction: column;

		.nav-bar {
			margin: 5rpx;
			display: flex;
			align-items: center;
			background-color: white;

			.nav-search {
				flex: 1;
			}

			.nav-add {
				line-height: 56px;
				cursor: pointer;
			}
		}

		.group-items {
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
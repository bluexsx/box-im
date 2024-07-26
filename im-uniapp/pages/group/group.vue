<template>
	<view  class="tab-page group">
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText"  cancelButton="none" radius="100" placeholder="点击搜索群聊"></uni-search-bar>
			</view>
			<view class="nav-add" @click="onCreateNewGroup()">
				<uni-icons type="personadd" size="35"></uni-icons>
			</view>
		</view>
		<view class="group-tip" v-if="$store.state.groupStore.groups.length==0">
			温馨提示：您现在还没有加入任何群聊，点击右上方'+'按钮可以创建群聊哦~
		</view>
		<view class="group-items" v-else>
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="group in $store.state.groupStore.groups" :key="group.id">
					<group-item v-if="!group.quit&&group.remark.includes(searchText)" :group="group"></group-item>
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
			onFocusSearch() {
				
			},
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
			padding: 2rpx 10rpx;
			display: flex;
			align-items: center;
			background-color: white;

			.nav-search {
				flex: 1;
			}

			.nav-add {
				cursor: pointer;
			}
		}

		.group-tip{
			position: absolute;
			top: 400rpx;
			padding: 50rpx;
			text-align: left;
			line-height: 50rpx;
			color: darkblue;
			font-size: 30rpx;
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
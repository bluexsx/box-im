<template>
	<view class="tab-page group">
		<nav-bar add search @add="onCreateNewGroup" @search="showSearch = !showSearch">群聊</nav-bar>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" cancelButton="none" radius="100"
					placeholder="点击搜索群聊"></uni-search-bar>
			</view>
		</view>
		<view class="group-tip" v-if="!hasGroups">
			<view class="tip-icon">
				<text class="iconfont icon-create-group"></text>
			</view>
			<view class="tip-title">还没有群聊</view>
			<view class="tip-content">创建或加入群聊，与朋友们一起畅聊吧</view>
			<button type="primary" @click="onCreateNewGroup">创建群聊</button>
		</view>
		<view class="group-items" v-else>
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="group in groupStore.groups" :key="group.id">
					<group-item v-if="!group.quit && group.showGroupName.includes(searchText)"
						:group="group"></group-item>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			showSearch: false,
			searchText: ""
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
	},
	computed: {
		hasGroups() {
			return this.groupStore.groups.some((g) => !g.quit);
		}
	}
}
</script>

<style lang="scss" scoped>
.group {
	position: relative;
	display: flex;
	flex-direction: column;

	.group-tip {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 60rpx 40rpx;
		text-align: center;
		width: 80%;
		max-width: 500rpx;

		.tip-icon {
			width: 120rpx;
			height: 120rpx;
			background: linear-gradient(135deg, #f8f9fa, #e9ecef);
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 40rpx;
			box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
			border: 2rpx solid $im-bg-active;

			.iconfont {
				font-size: 56rpx;
				color: #6c757d;
				opacity: 0.8;
			}
		}

		.tip-title {
			font-size: $im-font-size-large;
			color: $im-text-color;
			font-weight: 500;
			margin-bottom: 20rpx;
		}

		.tip-content {
			font-size: $im-font-size-smaller;
			color: $im-text-color-lighter;
			line-height: 1.6;
			margin-bottom: 50rpx;
		}
	}

	.group-items {
		flex: 1;
		padding: 0;
		overflow: hidden;
		position: relative;

		.scroll-bar {
			height: 100%;
		}
	}
}
</style>
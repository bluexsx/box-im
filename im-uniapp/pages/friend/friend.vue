<template>

	<view class="tab-page friend">
		<nav-bar add search @add="onAddNewFriends" @search="showSearch = !showSearch">好友</nav-bar>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" cancelButton="none"
					placeholder="点击搜索好友"></uni-search-bar>
			</view>
		</view>
		<view class="friend-tip" v-if="!hasFriends">
			<view class="tip-icon">
				<text class="iconfont icon-add-friend"></text>
			</view>
			<view class="tip-title">还没有好友</view>
			<view class="tip-content">添加好友，开始精彩的聊天之旅</view>
			<button type="primary" @click="onAddNewFriends">添加好友</button>
		</view>
		<view class="friend-items" v-else>
			<up-index-list :index-list="friendIdx" :sticky="false" :custom-nav-height="customNavHeight">
				<template v-for="(friends, i) in friendGroups">
					<up-index-item>
						<up-index-anchor :text="friendIdx[i] == '*' ? '在线' : friendIdx[i]"></up-index-anchor>
						<view v-for="(friend, idx) in friends" :key="idx">
							<friend-item :friend="friend"></friend-item>
						</view>
					</up-index-item>
				</template>
			</up-index-list>
		</view>
	</view>
</template>

<script>
import { pinyin } from 'pinyin-pro';

export default {
	data() {
		return {
			showSearch: false,
			searchText: ''
		}
	},
	methods: {
		onAddNewFriends() {
			uni.navigateTo({
				url: "/pages/friend/friend-add"
			})
		},
		firstLetter(strText) {
			// 使用pinyin-pro库将中文转换为拼音
			let pinyinOptions = {
				toneType: 'none', // 无声调
				type: 'normal' // 普通拼音
			};
			let pyText = pinyin(strText, pinyinOptions);
			return pyText[0];
		},
		isEnglish(character) {
			return /^[A-Za-z]+$/.test(character);
		}
	},
	computed: {
		friendGroupMap() {
			// 按首字母分组
			let groupMap = new Map();
			this.friendStore.friends.forEach((f) => {
				if (f.deleted || (this.searchText && !f.nickName.includes(this.searchText))) {
					return;
				}
				let letter = this.firstLetter(f.nickName).toUpperCase();
				// 非英文一律为#组
				if (!this.isEnglish(letter)) {
					letter = "#"
				}
				if (f.online) {
					letter = '*'
				}
				if (groupMap.has(letter)) {
					groupMap.get(letter).push(f);
				} else {
					groupMap.set(letter, [f]);
				}
			})
			// 排序
			let arrayObj = Array.from(groupMap);
			arrayObj.sort((a, b) => {
				// #组在最后面
				if (a[0] == '#' || b[0] == '#') {
					return b[0].localeCompare(a[0])
				}
				return a[0].localeCompare(b[0])
			})
			groupMap = new Map(arrayObj.map(i => [i[0], i[1]]));
			return groupMap;
		},
		friendIdx() {
			return Array.from(this.friendGroupMap.keys());
		},
		friendGroups() {
			return Array.from(this.friendGroupMap.values());
		},
		hasFriends() {
			return this.friendStore.friends.some(f => !f.deleted);
		},
		customNavHeight() {
			let h = 50;
			// #ifdef APP-PLUS
			h += uni.getSystemInfoSync().statusBarHeight;
			// #endif
			console.log("customNavHeight:", h)
			return h;
		}
	}
}
</script>

<style lang="scss" scoped>
.friend {
	position: relative;
	display: flex;
	flex-direction: column;

	:deep(.u-index-anchor) {
		height: 60rpx !important;
		background-color: unset !important;
		border-bottom: none !important;
	}

	:deep(.u-index-anchor__text) {
		color: $im-text-color !important;
	}

	.friend-tip {
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

	.friend-items {
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
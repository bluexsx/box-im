<template>
	<view class="tab-page friend">
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" cancelButton="none" placeholder="点击搜索好友"></uni-search-bar>
			</view>
			<view class="nav-add" @click="onAddNewFriends()">
				<uni-icons type="personadd" size="35"></uni-icons>
			</view>
		</view>
		<view class="friend-tip" v-if="friends.length==0">
			温馨提示：您现在还没有任何好友，快点击右上方'+'按钮添加好友吧~
		</view>
		<view class="friend-items" v-else>
			<up-index-list :index-list="friendIdx" >
				<template v-for="(friends,i) in friendGroups">
					<up-index-item>
						<up-index-anchor :text="friendIdx[i]=='*'?'在线':friendIdx[i]"
							bgColor="#f2f3fd"></up-index-anchor>
						<view v-for="(friend,idx) in friends" :key="idx">
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
				friendStore: this.useFriendStore(),
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
			friends() {
				return this.friendStore.friends;
			},
			friendGroupMap(){
				// 按首字母分组
				let groupMap = new Map();
				this.friends.forEach((f) => {
					if (f.delete) {
						return;
					}
					if(this.searchText && !f.nickName.includes(this.searchText)){
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
			friendIdx(){
				return Array.from(this.friendGroupMap.keys());
			},
			friendGroups(){
				return Array.from(this.friendGroupMap.values());
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

		.friend-tip {
			position: absolute;
			top: 400rpx;
			padding: 50rpx;
			text-align: center;
			line-height: 50rpx;
			text-align: left;
			color: darkblue;
			font-size: 30rpx;
		}

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
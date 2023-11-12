<template>
	<view class="head-image" @click="showUserInfo($event)" :title="name">
		<image class="avatar-image" v-if="url" :src="url" 
		:style="avatarImageStyle"  lazy-load="true"  mode="aspectFill"/>
		<view class="avatar-text" v-if="!url" :style="avatarTextStyle">
			{{name.substring(0,1).toUpperCase()}}
		</view>
		<view v-if="online" class="online" title="用户当前在线">
		</view>
	</view>
</template>

<script>
	export default {
		name: "head-image",
		data() {
			return {
				colors: ["#7dd24b", "#c7515a", "#db68ef", "#15d29b", "#85029b",
					"#c9b455", "#fb2609", "#bda818", "#af0831", "#326eb6"
				]
			}
		},
		props: {
			id: {
				type: Number
			},
			size: {
				type: Number,
				default: 20
			},
			url: {
				type: String
			},
			name: {
				type: String,
				default: "?"
			},
			online: {
				type: Boolean,
				default: false
			}
		},
		methods: {
			showUserInfo(e) {
				if (this.id && this.id > 0) {
					uni.navigateTo({
						url: "/pages/common/user-info?id="+this.id
					})
				}
			}
		},
		computed: {
			avatarImageStyle() {
				return `width:${this.size}rpx; height:${this.size}rpx;`
			},
			avatarTextStyle() {
				return `width: ${this.size}rpx;height:${this.size}rpx;
				color:${this.textColor};font-size:${this.size*0.6}rpx;`
			},
			textColor() {
				let hash = 0;
				for (var i = 0; i < this.name.length; i++) {
					hash += this.name.charCodeAt(i);
				}
				return this.colors[hash % this.colors.length];
			}
		}
	}
</script>

<style scoped lang="scss">
	.head-image {
		position: relative;
		cursor: pointer;

		.avatar-image {
			position: relative;
			overflow: hidden;
			border-radius: 10%;
			border: 1px solid #ccc;
			vertical-align: bottom;	
		}

		.avatar-text {
			background-color: #f2f2f2;
			/* 默认背景色 */
			border-radius: 50%;
			/* 圆角效果 */
			display: flex;
			align-items: center;
			justify-content: center;
			border: 1px solid #ccc;
		}

		.online {
			position: absolute;
			right: -10%;
			bottom: 0;
			width: 24rpx;
			height: 24rpx;
			background: limegreen;
			border-radius: 50%;
			border: 6rpx solid white;
		}
	}
</style>
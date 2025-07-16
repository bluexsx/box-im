<template>
	<view class="head-image  none-pointer-events" @click="showUserInfo($event)" :title="name">
		<image class="avatar-image" v-if="url" :src="url" :style="avatarImageStyle" lazy-load="true"
			mode="aspectFill" />
		<view class="avatar-text" v-if="!url" :style="avatarTextStyle">
			{{ name?.substring(0, 1).toUpperCase() }}
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
			colors: ["#5daa31", "#c7515a", "#e03697", "#85029b",
				"#c9b455", "#326eb6"
			]
		}
	},
	props: {
		id: {
			type: Number
		},
		size: {
			type: [Number, String],
			default: 'default'
		},
		url: {
			type: String
		},
		name: {
			type: String,
			default: null
		},
		radius: {
			type: String,
			default: "50%"
		},
		online: {
			type: Boolean,
			default: false
		},
	},
	methods: {
		showUserInfo(e) {
			if (this.id && this.id > 0) {
				uni.navigateTo({
					url: "/pages/common/user-info?id=" + this.id
				})
			}
		}
	},
	computed: {
		_size() {
			if (typeof this.size === 'number') {
				return this.size;
			} else if (typeof this.size === 'string') {
				return {
					'default': 96,
					'small': 84,
					'smaller': 72,
					'mini': 60,
					'minier': 48,
					'lage': 108,
					'lager': 120,
				} [this.size]
			}
		},
		avatarImageStyle() {
			return `width:${this._size}rpx;
					height:${this._size}rpx;`
		},
		avatarTextStyle() {
			return `width: ${this._size}rpx;
					height:${this._size}rpx;
					background: linear-gradient(145deg,#ffffff20 25%,#00000060),${this.textColor};
					font-size:${this._size * 0.45}rpx;
					border-radius: ${this.radius};
					`
		},
		textColor() {
			if (!this.name) {
				return '#fff';
			}
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
		border-radius: 50%;
		vertical-align: bottom;
	}

	.avatar-text {
		color: white;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
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
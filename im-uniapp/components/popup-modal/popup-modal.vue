<template>
	<uni-popup ref="popup" type="center">
		<view class="popup-modal">
			<view class="body">
				<view v-if="title" class="title">{{title}}</view>
				<view v-if="content" class="content">{{content}}</view>
				<slot></slot>
			</view>
			<view class="footer" :class="{ 'footer--single': !showCancel }">
				<view v-if="showCancel" class="btn btn-cancel" @click="onCancel">{{cancelText}}</view>
				<view class="btn btn-confirm" @click="onConfirm">{{confirmText}}</view>
			</view>
		</view>
	</uni-popup>
</template>

<script>
export default {
	name: "pop-modal",
	props: {
		autoClose: {
			type: Boolean,
			default: true
		}
	},
	data() {
		return {
			title: '',
			content: '',
			confirmText: '',
			cancelText: '',
			showCancel: true,
			success: null,
			cancel: null
		};
	},
	methods: {
		open(param) {
			this.title = param.title;
			this.content = param.content;
			this.confirmText = param.hasOwnProperty('confirmText') ? param.confirmText : '确定';
			this.cancelText = param.hasOwnProperty('cancelText') ? param.cancelText : '取消';
			this.showCancel = param.hasOwnProperty('showCancel') ? param.showCancel : true;
			this.success = param.success;
			this.cancel = param.cancel;
			this.$refs.popup.open();
		},
		close() {
			this.$refs.popup.close();
		},
		onCancel() {
			this.cancel && this.cancel();
			this.close();
		},
		onConfirm() {
			this.success && this.success();
			if (this.autoClose) {
				this.close();
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.popup-modal {
	width: 560rpx;
	background: #fff;
	border-radius: 24rpx;
	overflow: hidden;
	box-shadow: $im-box-shadow-light;
	.body {
		display: flex;
		flex-direction: column;
		align-items: stretch;
		gap: 20rpx;
		padding: 48rpx 40rpx 36rpx;
		.title {
			text-align: center;
			font-size: $im-font-size-large;
			font-weight: 600;
			color: $im-text-color;
			line-height: 1.4;
		}
		.content {
			text-align: center;
			font-size: $im-font-size-small;
			color: $im-text-color-light;
			line-height: 1.6;
			word-break: break-word;
		}
	}
	.footer {
		display: flex;
		align-items: stretch;
		border-top: 1rpx solid $im-border;
		.btn {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 96rpx;
			font-size: $im-font-size;
			line-height: 1;
			&:active {
				background: $im-bg-active;
			}
		}
		.btn-cancel {
			color: $im-text-color-light;
			border-right: 1rpx solid $im-border;
		}
		.btn-confirm {
			color: $im-color-primary;
			font-weight: 600;
		}
		&--single .btn-confirm {
			width: 100%;
		}
	}
}
</style>

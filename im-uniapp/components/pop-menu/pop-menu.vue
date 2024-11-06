<template>
	<view>
		<view @longpress.stop="onLongPress($event)" @touchmove="onTouchMove" @touchend="onTouchEnd">
			<slot></slot>
		</view>
		<view v-if="isShowMenu" class="pop-menu" @touchstart="onClose()" @contextmenu.prevent=""></view>
		<view v-if="isShowMenu" class="menu" :style="menuStyle">
			<view class="menu-item"  v-for="(item) in items" :key="item.key"  @click.prevent="onSelectMenu(item)">
				<uni-icons class="menu-icon" :type="item.icon" :style="itemStyle(item)" size="22"></uni-icons>
				<text :style="itemStyle(item)"> {{item.name}}</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "pop-menu",
		data() {
			return {
				isShowMenu : false,
				isTouchMove: false,
				style : ""
			}
		},
		props: {
			items: {
				type: Array
			}
		},
		methods: {
			onLongPress(e){
				if(this.isTouchMove){
					// 屏幕移动时不弹出
					return;
				}
				uni.getSystemInfo({
					success: (res) => {
						let touches = e.touches[0];
						let style = "";
						/* 因 非H5端不兼容 style 属性绑定 Object ，所以拼接字符 */
						if (touches.clientY > (res.windowHeight / 2)) {
							style = `bottom:${res.windowHeight-touches.clientY}px;`;
						} else {
							style = `top:${touches.clientY}px;`;
						}
						if (touches.clientX > (res.windowWidth / 2)) {
							style += `right:${res.windowWidth-touches.clientX}px;`;
						} else {
							style += `left:${touches.clientX}px;`;
						}
						this.menuStyle = style;
						//
						this.$nextTick(() => {
							this.isShowMenu = true;
						});
					}
				})
			},
			onTouchMove(){
				this.onClose();
				this.isTouchMove = true;
			},
			onTouchEnd(){
				this.isTouchMove = false;
			},
			onSelectMenu(item) {
				this.$emit("select", item);
				this.isShowMenu = false;
			},
			onClose() {
				this.isShowMenu = false;
			},
			itemStyle(item){
				if(item.color){
					return `color:${item.color};`
				}
				return `color:#4f76e6;`;
			}
		}
	}
</script>

<style lang="scss" scoped>
	.pop-menu {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		width: 100%;
		height: 100%;
		background-color: #333;
		z-index: 999;
		opacity: 0.5;

	}
	
	.menu {
		position: fixed;
		border: 1px solid #b4b4b4;
		border-radius: 7px;
		overflow: hidden;
		background-color: #f5f6ff;
		z-index: 1000;
		.menu-item {
			height: 25px;
			min-width: 150rpx;
			line-height: 25px;
			font-size: 18px;
			display: flex;
			padding: 10px;
			justify-content: center;
			border-bottom: 1px solid #d0d0d8;
			
			.menu-icon {
				margin-right: 10rpx;
			}
		}
	}
</style>
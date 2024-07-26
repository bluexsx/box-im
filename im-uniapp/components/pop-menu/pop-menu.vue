<template>
	<view>
		<view class="pop-menu" @tap="onClose()" @contextmenu.prevent=""></view>
		<view class="menu" :style="menuStyle">
			<view class="menu-item"  v-for="(item) in items" :key="item.key"  @click.prevent="onSelectMenu(item)">
				<uni-icons :type="item.icon" :style="itemStyle(item)" size="22"></uni-icons>
				<text :style="itemStyle(item)"> {{item.name}}</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "pop-menu",
		data() {
			return {}
		},
		props: {
			menuStyle: {
				type: String
			},
			items: {
				type: Array
			}
		},
		methods: {
			onSelectMenu(item) {
				this.$emit("select", item);
			},
			onClose() {
				this.$emit("close");
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
		z-index: 99;
		opacity: 0.5;

	}
	
	.menu {
		position: fixed;
		border: 1px solid #b4b4b4;
		border-radius: 7px;
		overflow: hidden;
		background-color: #f5f6ff;
		z-index: 100;
		.menu-item {
			height: 25px;
			min-width: 150rpx;
			line-height: 25px;
			font-size: 18px;
			display: flex;
			padding: 10px;
			justify-content: center;
			border-bottom: 1px solid #d0d0d8;
		}
	}

	
</style>
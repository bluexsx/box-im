<template>
	<view class="pop-menu" @tap="onClose()" @contextmenu.prevent="">
		<view class="menu" :style="menuStyle">
			<view class="menu-item" v-for="(item) in items" :key="item.key"  @click.prevent="onSelectMenu(item)">
				<uni-icons :type="item.icon" size="22"></uni-icons>
				<text> {{item.name}}</text>
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
		z-index: 9999;
	}

	.menu {
		position: fixed;
		border: 1px solid #b4b4b4;
		border-radius: 7px;
		overflow: hidden;
		box-shadow: 0px 0px 10px #ccc;
		background-color: #eeeeee;

		.menu-item {
			height: 25px;
			line-height: 25px;
			font-size: 18px;
			display: flex;
			padding: 10px;
			align-items: center;
			border-bottom: 1px solid #d0d0d8;
		}
	}
</style>
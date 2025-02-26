<template>
	<div class="right-menu-mask" @click.stop="close()" @contextmenu.prevent="close()">
		<div class="right-menu" :style="{ 'left': pos.x + 'px', 'top': pos.y + 'px' }">
			<el-menu text-color="#333333">
				<el-menu-item v-for="(item) in items" :key="item.key" :title="item.name"
					@click.native.stop="onSelectMenu(item)">
					<!--					<span :class="item.icon"></span>-->
					<span>{{ item.name }}</span>
				</el-menu-item>
			</el-menu>
		</div>
	</div>
</template>

<script>
export default {
	name: "rightMenu",
	data() {
		return {}
	},
	props: {
		pos: {
			type: Object
		},
		items: {
			type: Array
		}
	},
	methods: {
		close() {
			this.$emit("close");
		},
		onSelectMenu(item) {
			this.$emit("select", item);
			this.close();
		}
	}
}
</script>

<style lang="scss">
.right-menu-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	width: 100%;
	height: 100%;
	z-index: 9999;
}

.right-menu {
	position: fixed;
	border-radius: 8px;
	overflow: hidden;
	box-shadow: var(--im-box-shadow-light);

	.el-menu {
		border-radius: 4px;
		overflow: hidden;

		.el-menu-item {
			height: 36px;
			line-height: 36px;
			min-width: 100px;
			text-align: left;
			padding: 0 0 0 20px;

			&:hover {
				background-color: var(--im-background-active);
			}
		}
	}
}
</style>

<template>
	<div class="right-menu-mask" @click="close()" @contextmenu.prevent="close()">
		<div class="right-menu"  :style="{'left':pos.x+'px','top':pos.y+'px'}">
			<el-menu background-color="#f5f5f5" text-color="#333333">
				<el-menu-item v-for="(item) in items" :key="item.key"  :title="item.name" @click="handleSelectMenu(item)">
					<i :class="item.icon"></i>
					<span>{{item.name}}</span>
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
			items:{
				type: Array
			}
		},
		methods:{
			close(){
				this.$emit("close");
			},
			handleSelectMenu(item){
				this.$emit("select",item);
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
		
		.el-menu-item {
			height: 40px;
			line-height: 40px;
			
			i {
				color: #333333;
			}
		}
	}
</style>

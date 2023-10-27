<template>
	<div class="group-item" :class="active ? 'active' : ''">
		<div class="group-avatar">
			<head-image :url="group.headImage"> </head-image>
		</div>
		<div class="group-name">
			<div>{{group.remark}}</div>
		</div>
	</div>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';

	export default {
		name: "groupItem",
		components: {
			HeadImage
		},
		data() {
			return {}
		},
		props: {
			group: {
				type: Object
			},
			active: {
				type: Boolean
			}
		}

	}
</script>

<style lang="scss" >
	.group-item {
		height: 65px;
		display: flex;
		margin-bottom: 1px;
		position: relative;
		padding-left: 10px;
		align-items: center;
		padding-right: 5px;
		background-color: #fafafa;
		white-space: nowrap;
		&:hover {
			background-color: #eeeeee;
		}
	
		&.active {
			background-color: #dddddd;
		}
	
		.group-avatar {
			width: 50px;
			height: 50px;
		}
	
		.group-name {
			padding-left: 10px;
			height: 100%;
			text-align: left;
			line-height: 65px;
			white-space: nowrap;
			overflow: hidden;
			font-size: 16px;
			font-weight: 600;
		}
	}
</style>

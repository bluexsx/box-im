<template>
	<div class="group-item" :class="active ? 'active' : ''">
		<div class="group-avatar">
			<head-image :size="45" :name="group.showGroupName" :url="group.headImage"> </head-image>
		</div>
		<div class="group-name">
			<div>{{group.showGroupName}}</div>
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
		height: 50px;
		display: flex;
		margin-bottom: 1px;
		position: relative;
		padding: 5px 10px;
		align-items: center;
		background-color: white;
		white-space: nowrap;
		cursor: pointer;
		
		&:hover {
			background-color: #F8FAFF;
		}
	
		&.active {
			background-color: #F4F9FF;
		}
	
		.group-avatar {
			width: 45px;
			height: 45px;
		}
	
		.group-name {
			padding-left: 10px;
			height: 100%;
			text-align: left;
			line-height: 50px;
			white-space: nowrap;
			overflow: hidden;
			font-size: 15px;
			font-weight: 600;
		}
	}
</style>

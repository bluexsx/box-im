<template>
	<div class="group-item" :class="active ? 'active' : ''">
		<div class="group-avatar">
			<head-image :size="42" :name="group.showGroupName" :url="group.headImage"> </head-image>
		</div>
		<div class="group-name">
			<div>{{ group.showGroupName }}</div>
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

<style lang="scss" scoped>
.group-item {
	height: 50px;
	display: flex;
	position: relative;
	padding: 5px 10px;
	align-items: center;
	white-space: nowrap;
	cursor: pointer;

	&:hover {
		background-color: var(--im-background-active);
	}

	&.active {
		background-color: var(--im-background-active-dark);
	}

	.group-name {
		padding-left: 10px;
		height: 100%;
		text-align: left;
		line-height: 50px;
		white-space: nowrap;
		overflow: hidden;
		font-size: var(--im-font-size);
	}
}
</style>

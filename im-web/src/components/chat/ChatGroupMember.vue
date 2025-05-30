<template>
	<div class="chat-group-member" :class="active ? 'active' : ''" :style="{ 'height': height + 'px' }">
		<div class="member-avatar">
			<head-image :size="headImageSize" :name="member.showNickName" :url="member.headImage"> </head-image>
		</div>
		<div class="name" :style="{ 'line-height': height + 'px' }">
			<div>{{ member.showNickName }}</div>
		</div>
	</div>
</template>

<script>
import HeadImage from "../common/HeadImage.vue";
export default {
	name: "groupMember",
	components: { HeadImage },
	data() {
		return {};
	},
	props: {
		member: {
			type: Object,
			required: true
		},
		height: {
			type: Number,
			default: 50
		},
		active: {
			type: Boolean,
			default: false
		}
	},
	computed: {
		headImageSize() {
			return Math.ceil(this.height * 0.75)
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-group-member {
	display: flex;
	position: relative;
	padding: 0 5px;
	align-items: center;
	white-space: nowrap;
	box-sizing: border-box;

	&.active {
		background: #E1EAF7;
	}

	.name {
		padding-left: 10px;
		height: 100%;
		text-align: left;
		white-space: nowrap;
		overflow: hidden;
		font-size: var(--im-font-size);
	}
}
</style>
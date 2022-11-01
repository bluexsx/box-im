<template>
	<div class="group-member">
		<head-image :url="member.headImage" :size="60" class=""></head-image>
		<div class="member-name">{{member.aliasName}}121212121212</div>
	</div>
</template>

<script>
	import HeadImage from "../common/HeadImage.vue";
	
	export default{
		name: "groupMember",
		components:{HeadImage},
		data(){
			return {};
		},
		props:{
			member:{
				type: Object,
				required: true
			},
			showDel:{
				type: Boolean,
				default: true
			}
		}
	}
</script>

<style lang="scss">
	.group-member{
		display: flex;
		flex-direction: column;
		align-items: center;
		width: 60px;
		.member-name {
			font-size: 16px;
			text-align: center;
			width: 100%;
			height: 30px;
			line-height: 30px;
			white-space: nowrap;
			text-overflow:ellipsis; 
			overflow:hidden
		}
	}
</style>

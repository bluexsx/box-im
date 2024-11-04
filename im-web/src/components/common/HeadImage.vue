<template>
	<div class="head-image" @click="showUserInfo($event)">
		<img class="avatar-image" v-show="url" :src="url" 
			:style="avatarImageStyle" loading="lazy" />
		<div class="avatar-text" v-show="!url" :style="avatarTextStyle">
			{{name.substring(0,2).toUpperCase()}}</div>
		<div v-show="online" class="online" title="用户当前在线"></div>
		<slot></slot>
	</div>
</template>

<script>

	export default {
		name: "headImage",
		data() {
			return {
				colors: ["#5daa31", "#c7515a", "#e03697",  "#85029b",
					"#c9b455",  "#326eb6"]

			}
		},
		props: {
			id:{
				type: Number
			},
			size: {
				type: Number,
				default: 50
			},
			width: {
				type: Number
			},
			height: {
				type: Number
			},
			radius:{
				type: String,
				default: "50%"
			},
			url: {
				type: String
			},
			name:{
				type: String,
				default: "?"
			},
			online:{
				type: Boolean,
				default:false
			}
		},
		methods:{
			showUserInfo(e){
				if(this.id && this.id>0){
					this.$http({
						url: `/user/find/${this.id}`,
						method: 'get'
					}).then((user) => {
						this.$store.commit("setUserInfoBoxPos",e);
						this.$store.commit("showUserInfoBox",user);
					})
				}
			}
		},
		computed:{
			avatarImageStyle() {
				let w = this.width ? this.width : this.size;
				let h = this.height ? this.height : this.size;
				return `width:${w}px; height:${h}px;
					border-radius: ${this.radius};`
			},
			avatarTextStyle() {
				let w = this.width ? this.width : this.size;
				let h = this.height ? this.height : this.size;
				return `width: ${w}px;height:${h}px;
					background-color:${this.textColor};
					font-size:${w*0.35}px;
					border-radius: ${this.radius};`
			},
			textColor(){
				let hash = 0;
				for (var i = 0; i< this.name.length; i++) {
					hash += this.name.charCodeAt(i);
				}
				return this.colors[hash%this.colors.length];
			}
		}	
	}
</script>

<style scoped lang="scss">
	.head-image {
		position: relative;
		cursor: pointer;
		.avatar-image {
			position: relative;
			overflow: hidden;
			display: block;
		}
		
		.avatar-text{
			color: white;
			display: flex;
			align-items: center;
			justify-content: center;
			border: 1px solid #ccc;
		} 
		
		.online{
			position: absolute;
			right: -5px;
			bottom: 0;
			width: 12px;
			height: 12px;
			background: limegreen;
			border-radius: 50%;
			border: 3px solid white;
		}
	}
</style>

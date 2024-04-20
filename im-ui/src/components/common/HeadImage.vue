<template>
	<div class="head-image" @click="showUserInfo($event)">
		<img class="avatar-image" v-show="url" :src="url" 
			:style="avatarImageStyle" loading="lazy" />
		<div class="avatar-text" v-show="!url" :style="avatarTextStyle">
			{{name.substring(0,1).toUpperCase()}}</div>
		<div v-show="online" class="online" title="用户当前在线"></div>
		<slot></slot>
	</div>
</template>

<script>

	export default {
		name: "headImage",
		data() {
			return {
				colors:["#7dd24b","#c7515a","#db68ef","#15d29b","#85029b",
				"#c9b455","#fb2609","#bda818","#af0831","#326eb6"]

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
			avatarImageStyle(){
				return `width:${this.size}px; height:${this.size}px;`
			},
			avatarTextStyle(){
				return `width: ${this.size}px;height:${this.size}px;
				color:${this.textColor};font-size:${this.size*0.6}px;`
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
			border-radius: 10%;
		}
		
		.avatar-text{
			background-color: #f2f2f2; /* 默认背景色 */
			border-radius: 10%; /* 圆角效果 */
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

<template>
	<div class="video-acceptor">
		<div>
			<head-image :size="120" :url="this.friend.headImage" :id="this.friend.id"></head-image>
		</div>
		<div>
			{{friend.nickName}} 请求和您进行视频通话...
		</div>
		<div class="video-acceptor-btn-group">
		  <div  class="icon iconfont icon-phone-accept accept" @click="accpet()"></div>
		  <div  class="icon iconfont icon-phone-reject reject" @click="reject()"></div>
		</div>
	</div>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';
	
	export default {
		name: "videoAcceptor",
		components:{HeadImage},
		props: {
			friend:{
				type: Object
			}
		},
		data(){
			return {
				offer:{}
			}
		},
		methods:{
			accpet(){
				let info ={
					friend: this.friend,
					master: false,
					offer: this.offer
				}
				this.$store.commit("showChatPrivateVideoBox",info);
				this.close();
			},
			reject(){
				this.$http({
					url: `/webrtc/private/reject?uid=${this.friend.id}`,
					method: 'post'
				})
				this.close();
			},
			failed(reason){
				this.$http({
					url: `/webrtc/private/failed?uid=${this.friend.id}&reason=${reason}`,
					method: 'post'
				})
				this.close();
			},
			onCall(msgInfo){
				console.log("onCall")
				this.offer = JSON.parse(msgInfo.content);
				if(this.$store.state.userStore.state == this.$enums.USER_STATE.BUSY){
					this.failed("对方正忙,暂时无法接听");
					return;
				}
				// 超时未接听
				this.timer && clearTimeout(this.timer);
				this.timer = setTimeout(()=>{
					this.failed("对方未接听");
				},30000)
			},
			onCancel(){
				this.$message.success("对方取消了呼叫");
				this.close();
			},
			handleMessage(msgInfo){
				if(msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_CALL){
					this.onCall(msgInfo);
				}else if(msgInfo.type == this.$enums.MESSAGE_TYPE.RTC_CANCEL){
					this.onCancel();
				}
			},
			close(){
				this.timer && clearTimeout(this.timer);
				this.$emit("close");
			}
		}
	}
	
</script>

<style scoped lang="scss">
	.video-acceptor {
		position: absolute;
		right: 1px;
		bottom: 1px;
		width: 250px;
		height: 250px;
		padding: 20px;
		text-align: center;
		background-color: #eeeeee;
		border: #dddddd solid 1px;
		
		.video-acceptor-btn-group {
			display: flex;
			justify-content: space-around;
			margin-top: 20px;
			
			.icon {
				font-size: 50px;
				cursor: pointer;
				&.accept {
					color: green;
				}
				&.reject {
					color: red;
				}
			}
			
			
			
			
			
			
		}
	}
</style>

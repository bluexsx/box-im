<template>
	<el-container>
		<el-aside width="80px" class="navi-bar">
			<div class="user-head-image" @click="onClickHeadImage">
				<head-image :url="$store.state.userStore.userInfo.headImageThumb" > </head-image>
			</div>
			
			<el-menu background-color="#333333" text-color="#ddd" style="margin-top: 30px;" >
				<el-menu-item title="聊天">
					<router-link v-bind:to="'/home/chat'">
						<span class="el-icon-chat-dot-round"></span>
					</router-link>
				</el-menu-item >
				<el-menu-item  title="好友" >
					<router-link v-bind:to="'/home/friend'">
						<span class="el-icon-user"></span>
					</router-link>
				</el-menu-item>
				<el-menu-item  title="群聊" >
					<router-link v-bind:to="'/home/group'">
						<span class="el-icon-s-check"></span>
					</router-link>
				</el-menu-item>
				
				<el-menu-item title="设置" @click="onClickSetting()" >
						<span class="el-icon-setting"></span>
				</el-menu-item>
			</el-menu>
			<div class="exit-box" @click="handleExit()" title="退出">
				<span class="el-icon-circle-close"></span>
			</div>
		</el-aside>
		<el-main class="content-box">
			<router-view></router-view>
		</el-main>
		<setting :visible="showSettingDialog" @close="onCloseSetting()"></setting>
	</el-container>
</template>

<script>
	import HeadImage from '../components/common/HeadImage.vue';
	import Setting from '../components/setting/Setting.vue';
	
	export default {
		components:{HeadImage,Setting},
		data(){
			return {
				showSettingDialog: false
			}
		},
		methods: {
			init(userInfo){
				this.$store.commit("setUserInfo", userInfo);
				this.$store.commit("initStore");
				console.log("socket");
				this.$wsApi.createWebSocket("ws://localhost:8878/im",this.$store);
				this.$wsApi.onopen(()=>{
					console.log("pullUnreadMessage")
					this.pullUnreadMessage();
				});
				this.$wsApi.onmessage((e)=>{
					console.log(e);
					if(e.cmd == 2){
						// 异地登录，强制下线
						this.$message.error("您已在其他地方登陆，将被强制下线");
						setTimeout(()=>{
							location.href="/";
						},1000)
						
					}
					else if(e.cmd==3){
						// 插入私聊消息
						this.handlePrivateMessage(e.data);
					}else if(e.cmd == 4){
						// 插入群聊消息
						this.handleGroupMessage(e.data);
					}
				})
			},
			pullUnreadMessage(){
				// 拉取未读私聊消息
				this.$http({
					url: "/api/message/private/pullUnreadMessage",
					method: 'post'
				});
				// 拉取未读群聊消息
				this.$http({
					url: "/api/message/group/pullUnreadMessage",
					method: 'post'
				});
			},
			handlePrivateMessage(msg){
				// 好友列表存在好友信息，直接插入私聊消息
				let friend = this.$store.state.friendStore.friends.find((f)=>f.id==msg.sendId);
				if(friend){
					this.insertPrivateMessage(friend,msg);
					return;
				}
				// 好友列表不存在好友信息，则发请求获取好友信息
				this.$http({
					url: `/api/friend/find/${msg.sendId}`,
					method: 'get'
				}).then((friend)=>{
					this.insertPrivateMessage(friend,msg);
					this.$store.commit("addFriend",friend);
				})
				
				
			},
			insertPrivateMessage(friend,msg){
				let chatInfo = {
					type: 'PRIVATE',
					targetId: friend.id,
					showName: friend.nickName,
					headImage: friend.headImage
				};
				// 打开会话
				this.$store.commit("openChat",chatInfo);
				// 插入消息
				this.$store.commit("insertMessage",msg);
			},
			handleGroupMessage(msg){
				// 群聊缓存存在，直接插入群聊消息
				let group = this.$store.state.groupStore.groups.find((g)=>g.id==msg.groupId);
				if(group){
					this.insertGroupMessage(group,msg);
					return;
				}
				// 群聊缓存存在，直接插入群聊消息
				this.$http({
					url: `/api/group/find/${msg.groupId}`,
					method: 'get'
				}).then((group)=>{
					this.insertGroupMessage(group,msg);
					this.$store.commit("addGroup",group);
				})
			},
			insertGroupMessage(group,msg){
				let chatInfo = {
					type: 'GROUP',
					targetId: group.id,
					showName: group.remark,
					headImage: group.headImageThumb
				};
				// 打开会话
				this.$store.commit("openChat",chatInfo);
				// 插入消息
				this.$store.commit("insertMessage",msg);
			},
			handleExit(){
				this.$http({
					url: "/api/logout",
					method: 'get'
				}).then(()=>{
					this.$wsApi.closeWebSocket();
					location.href="/";
				})
			},
			onClickHeadImage(){
				this.$message.success(JSON.stringify(this.$store.state.userStore.userInfo));
			},
			onClickSetting(){
				this.showSettingDialog = true;
			},
			onCloseSetting(){
				this.showSettingDialog = false;
			}
		},
		mounted() {
			this.$http({
				url: "/api/user/self",
				methods: 'get'
			}).then((userInfo) => {
				this.init(userInfo);
			})
		},
		unmounted(){
			this.$wsApi.closeWebSocket();
		}
	}
</script>

<style scoped lang="scss">
	.navi-bar {
		background: #333333;
		padding: 10px;
		padding-top: 50px;
		
		.user-head-image{
			position: relative;
			width: 50px;
			height: 50px;
		}
		
		.el-menu {
			border: none;
			flex: 1;
			.el-menu-item {
				margin-top: 20px;
				.router-link-exact-active span{
					color: white !important;
				}
				
				span {
					font-size: 24px !important;
					color: #aaaaaa;
					
					&:hover{
						color: white !important;
					}
				}
			}
		}
		
		.exit-box {
			position: absolute;
			width: 60px;
			bottom: 40px;	
			color: #aaaaaa;
			font-size: 24px;
			text-align: center;
			cursor: pointer;
			&:hover{
				color: white !important;
			}
		}
	}

	.content-box {
		padding: 0;
		background-color: #E9EEF3;
		color: #333;
		text-align: center;
		
	}

</style>

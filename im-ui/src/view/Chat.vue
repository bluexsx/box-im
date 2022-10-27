<template>
	<el-container>
		<el-aside width="250px" class="l-chat-box">
			<el-header height="60px">
				<el-row>
					<el-input width="200px" placeholder="搜索" v-model="searchText">
						<el-button slot="append" icon="el-icon-search"></el-button>
					</el-input>
				</el-row>
			</el-header>
			<el-main>
				<div v-for="(chat,index) in $store.state.chatStore.chats" :key="chat.targetId">
					<chat-item :chat="chat" :index="index" @click.native="onClickItem(index)"  @del="onDelItem(chat,index)"
						:active="index === $store.state.chatStore.activeIndex"></chat-item>
				</div>
			</el-main>
		</el-aside>
		<el-container class="r-chat-box">
			<el-header height="60px">
				{{titleName}}
			</el-header>
			<el-main class="im-chat-main" id="chatScrollBox">
					<div class="im-chat-box">
						<ul>
							<li v-for="item in messages" :key="item.id"
								:class="{ 'im-chat-mine': item.sendUserId == $store.state.userStore.userInfo.id }">
								<div class="head-image">
									<head-image  :url="headImage" ></head-image>
								</div>
								<div class="im-msg-content">
									<div class="im-msg-top">
										<span>{{showName(item)}}</span>
										<chat-time :time="item.sendTime"></chat-time>
									</div>
									<div class="im-msg-bottom">
										<span class="im-msg-text">{{item.content}}</span>
									</div>
								</div>
							</li>
						</ul>
					
				</div>
			</el-main>
			<el-footer height="150px" class="im-chat-footer">
				<textarea v-model="messageContent" ref="sendBox" class="textarea" @keyup.enter="onSendMessage()"></textarea>
				<div class="im-chat-send">
					<el-button type="primary" @click="onSendMessage()">发送</el-button>
				</div>
			</el-footer>
		</el-container>
	</el-container>
</template>

<script>
	import ChatItem from "../components/ChatItem.vue";
	import ChatTime from "../components/ChatTime.vue";
	import HeadImage from "../components/HeadImage.vue";

	export default {
		name: "chat",
		components: {
			ChatItem,
			ChatTime,
			HeadImage
		},
		data() {
			return {
				searchText: "",
				messageContent: ""
			}
		},
		methods: {
			onClickItem(index) {
				this.$store.commit("activeChat", index);
			},
			onSendMessage() {
				let msgInfo = {
					recvUserId: this.$store.state.chatStore.chats[this.$store.state.chatStore.activeIndex].targetId,
					content: this.messageContent,
					type: 0
				}
				this.$http({
					url: '/api/message/single/send',
					method: 'post',
					data: msgInfo
				}).then((data) => {
					this.$message.success("发送成功");
					this.messageContent = "";
					msgInfo.sendTime = new Date().getTime();
					msgInfo.sendUserId = this.$store.state.userStore.userInfo.id;
					msgInfo.selfSend = true;
					this.$store.commit("insertMessage", msgInfo);
					console.log(this.$refs.sendBox)
					// 保持输入框焦点
					this.$refs.sendBox.focus();
					// 滚动到底部
					this.$nextTick(() => {
					  const div = document.getElementById("chatScrollBox");
					  div.scrollTop = div.scrollHeight;	
					  
					});
					
				})
			},
			onDelItem(chat,index){
				this.$store.commit("removeChat",index);
			},
			showName(item) {
				if (item.sendUserId == this.$store.state.userStore.userInfo.id) {
					return this.$store.state.userStore.userInfo.nickName;
				} else {
					let index = this.$store.state.chatStore.activeIndex;
					let chats = this.$store.state.chatStore.chats
					return chats[index].showName;
				}
			}
		},
		computed: {
			messages() {
				let index = this.$store.state.chatStore.activeIndex;
				let chats = this.$store.state.chatStore.chats
				if (index >= 0 && chats.length > 0) {
					return chats[index].messages;
				}
				return [];
			},
			titleName(){
				let index = this.$store.state.chatStore.activeIndex;
				let chats = this.$store.state.chatStore.chats
				if(index>=0 && chats.length > 0){
					let chats = this.$store.state.chatStore.chats;
					return chats[index].showName;
				}
				return "";
			},
			headImage(){
				let index = this.$store.state.chatStore.activeIndex;
				let chats = this.$store.state.chatStore.chats
				if(index>=0 && chats.length > 0){
					let chats = this.$store.state.chatStore.chats;
					return chats[index].headImage;
				}
				return "";
			}
		}
	}
</script>

<style scoped lang="scss">
	.el-container {
		
		.l-chat-box {
			border: #dddddd solid 1px;
			background: #eeeeee;
			width: 3rem;
			.el-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
			}
			
			.el-main {
				padding: 0
			}
		}

		.r-chat-box {
			background: white;
			border: #dddddd solid 1px;
			.el-header {
				padding: 5px;
				background-color: white;
				line-height: 50px;
			}

			.im-chat-main {
				padding: 0;
				border: #dddddd solid 1px;
				.im-chat-box {
					ul {
						padding: 10px;

						li {
							position: relative;
							font-size: 0;
							margin-bottom: 10px;
							padding-left: 60px;
							min-height: 68px;


							.head-image {
								position: absolute;
								width: 40px;
								height: 40px;
								top: 0;
								left: 0;
							}

							.im-msg-content {
								display: flex;
								flex-direction: column;

								.im-msg-top {
									display: flex;
									flex-wrap: nowrap;
									color: #333;
									font-size: 14px;
									line-height: 20px;

									span {
										margin-right: 12px;
									}
								}

								.im-msg-bottom {
									text-align: left;

									.im-msg-text {
										position: relative;
										line-height: 22px;
										margin-top: 10px;
										padding: 10px;
										background-color: #eeeeee;
										border-radius: 3px;
										color: #333;
										display: inline-block;
										font-size: 14px;

										&:after {
											content: "";
											position: absolute;
											left: -10px;
											top: 13px;
											width: 0;
											height: 0;
											border-style: solid dashed dashed;
											border-color: #eeeeee transparent transparent;
											overflow: hidden;
											border-width: 10px;
										}
									}
								}
							}
						}
					}

					.im-chat-mine {
						text-align: right;
						padding-left: 0;
						padding-right: 60px;

						.head-image {
							left: auto;
							right: 0;
						}

						.im-msg-content {

							.im-msg-top {
								flex-direction: row-reverse;

								span {
									margin-left: 12px;
									margin-right: 0;
								}
							}

							.im-msg-bottom {
								text-align: right;

								.im-msg-text {
									margin-left: 10px;
									background-color: #5fb878;
									color: #fff;
									display: inline-block;
									vertical-align: top;
									font-size: 14px;

									&:after {
										left: auto;
										right: -10px;
										border-top-color: #5fb878;
									}
								}
							}
						}

						.message-info {
							right: 60px !important;
							display: inline-block;
						}
					}
				}
			}
		
		
		
			.im-chat-footer {
				display: flex;
				flex-direction: column;
				padding: 0;
					
				textarea {
					box-sizing: border-box;
					padding: 5px;
					width: 100%;
					flex: 1;
					resize: none;
					background-color: #f8f8f8 !important;
					outline-color: rgba(83, 160, 231, 0.61);
				}
				
				.im-chat-send {
					text-align: right;
					padding: 7px;
				}
			} 
		}

	}
</style>

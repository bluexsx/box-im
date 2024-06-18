<template>
	<div>
		<el-dialog v-dialogDrag top="5vh" :close-on-click-modal="false" :close-on-press-escape="false"
			:visible.sync="showRoom" :fullscreen="isFullScreen" :width="width+'px'" :before-close="onQuit">
			<div class='rtc-group-video'>
				<div class="video-box" ref="videos" :style="`width:${width}px;`">
					<div class="video-list">
						<div :style="videoStyle" v-for="user in userInfos" :key="user.id">
							<local-video v-if="user.id==mine.id" ref="localVideo" :userInfo="mine" :width="vw"
								:height="vh"></local-video>
							<remote-video v-if="user.id!=mine.id" :ref="'remoteVideo'+user.id" :userInfo="user"
								:groupId="groupId" :width="vw" :height="vh"></remote-video>
						</div>
					</div>
				</div>
				<div class="control-bar">
					<div class="icon-box" v-show="isReady && isMicroPhone">
						<div class="icon iconfont icon-microphone-on icon-front" @click="onSwitchMicroPhone()"></div>
						<div class="icon-text">麦克风已开</div>
					</div>
					<div class="icon-box" v-show="isReady && !isMicroPhone">
						<div class="icon iconfont icon-microphone-off icon-back" @click="onSwitchMicroPhone()"></div>
						<div class="icon-text">麦克风已关</div>
					</div>
					<div class="icon-box" v-show="isReady && isSpeaker">
						<div class="icon iconfont icon-speaker-on icon-front" @click="onSwitchSpeaker()"></div>
						<div class="icon-text">扬声器已开</div>
					</div>
					<div class="icon-box" v-show="isReady && !isSpeaker">
						<div class="icon iconfont icon-speaker-off icon-back" @click="onSwitchSpeaker()"></div>
						<div class="icon-text">扬声器已关</div>
					</div>
					<div class="icon-box" v-show="isReady && isCamera">
						<div class="icon iconfont icon-camera-on icon-front" @click="onSwitchCamera()"></div>
						<div class="icon-text">摄像头已开</div>
					</div>
					<div class="icon-box" v-show="isReady && !isCamera">
						<div class="icon iconfont icon-camera-off icon-back" @click="onSwitchCamera()"></div>
						<div class="icon-text">摄像头已关</div>
					</div>
					<div class="icon-box" v-show="isReady">
						<i class="icon iconfont icon-invite-rtc icon-back" @click="onInviteMmeber()"></i>
						<div class="icon-text">邀请</div>
					</div>
					<div class="icon-box">
						<div class="icon iconfont icon-quit" @click="onQuit()"></div>
						<div class="icon-text">退出</div>
					</div>
				</div>
			</div>
			<template slot="title">
				<div class="header">
					<span class="title">
						{{title}}
					</span>
					<i class="icon el-icon-full-screen" @click="onSwitchFullScreen()"></i>
				</div>
			</template>
		</el-dialog>
		<rtc-group-acceptor v-if="isWaiting" ref="acceptor" :groupId="groupId" :userInfos="userInfos"
			:inviterId="inviterId" @accept="onAccept" @reject="onReject"></rtc-group-acceptor>
		<group-member-selector ref="rtcSel" :groupId="groupId" @complete="onInviteOk"></group-member-selector>
	</div>
</template>

<script>
	import RemoteVideo from './RemoteVideo.vue';
	import LocalVideo from './LocalVideo.vue';
	import RtcGroupAcceptor from './RtcGroupAcceptor'
	import ImWebRtc from '@/api/webrtc';
	import ImCamera from '@/api/camera';
	import RtcGroupApi from '@/api/rtcGroupApi'
	import GroupMemberSelector from '@/components/group/GroupMemberSelector'

	export default {
		name: "rtcGroupVideo",
		components: {
			LocalVideo,
			RemoteVideo,
			RtcGroupAcceptor,
			GroupMemberSelector
		},
		data() {
			return {
				camera: new ImCamera(), // 摄像头和麦克风
				webrtc: new ImWebRtc(), // webrtc相关
				API: new RtcGroupApi(),
				audio: new Audio(), // 呼叫音频
				showRoom: false, // 是否显示聊天房间
				groupId: null, // 群id
				isHost: false, // 我是否发起人
				inviterId: null, // 发起邀请的用户i的
				userInfos: [], // 用户列表，
				stream: null, // 本地视频流
				isMicroPhone: true, // 是否开启麦克风
				isSpeaker: true, // 是否开启扬声器
				isCamera: false, // 是否开启视频
				chatTime: 0, // 聊天时长
				waitTimer: null, // 接听等待定时器
				chatTimer: null, // 聊天计时器
				heartbeatTimer: null, // 心跳定时器
				tipTimer: null, // 提示语定时器
				lastTipTime: null, // 上一次提示时间
				state: "INIT", // INIT:初始状态  WAITING:等待呼叫或接听 CHATING:聊天中  CLOSE:关闭 ERROR:出现异常
				isFullScreen: false, // 是否全屏
				width: 800, // 窗口宽度
				vw: 200, // 单个视频宽度
				vh: 200 // 但是视频高度
			}
		},
		methods: {
			open(rtcInfo) {
				this.showRoom = true;
				this.groupId = rtcInfo.groupId;
				this.isHost = rtcInfo.isHost;
				this.inviterId = rtcInfo.inviterId;
				this.userInfos = rtcInfo.userInfos;
				this.init();
				if (this.isHost) {
					// 作为发起人，直接发起呼叫
					this.onSetup();
				} else if (this.mine.id == this.inviterId) {
					// 自己邀请自己，视作主动加入
					this.onJoin();
				}

			},
			init() {
				// 初始化呼叫音频
				this.initAudio();
				// 启动心跳
				this.startHeartBeat();
			},
			initAudio() {
				let url = require(`@/assets/audio/call.wav`);
				this.audio.src = url;
				this.audio.loop = true;
			},
			onSwitchFullScreen() {
				this.isFullScreen = !this.isFullScreen;
				this.refreshVideoWH();
			},
			onInviteMmeber() {
				let ids = [];
				this.userInfos.forEach((user) => {
					ids.push(user.id);
				})
				this.$refs.rtcSel.open(this.maxChannel, ids, ids);
			},
			onInviteOk(members) {
				// 发起邀请
				let userInfos = [];
				members.forEach(m => {
					if (!this.isExist(m.userId)) {
						userInfos.push({
							id: m.userId,
							nickName: m.aliasName,
							headImage: m.headImage,
							isCamera: false,
							isMicroPhone: true
						})
					}
				})
				if (userInfos.length > 0) {
					this.API.invite(this.groupId, userInfos).then(() => {
						this.appendUser(userInfos);
					});
				}
			},
			onSwitchMicroPhone() {
				this.isMicroPhone = !this.isMicroPhone;
				this.$refs.localVideo[0].setMicroPhone(this.isMicroPhone);
				// 通知其他人我开/关了麦克风
				this.API.device(this.groupId, this.isCamera, this.isMicroPhone);
				console.log("麦克风:" + this.isMicroPhone)
			},
			onSwitchCamera() {
				this.isCamera = !this.isCamera;
				this.mine.isCamera = this.isCamera;
				// 重新打开设备
				this.openStream().then(() => {
					// 切换传输的视频流
					this.userInfos.forEach((user) => {
						if (user.id != this.mine.id) {
							const refName = 'remoteVideo' + user.id;
							// 开关摄像头需要重新连接
							this.$refs[refName][0].reconnect(this.stream);
						}
					})
					// 通知其他人我开/关了摄像头
					this.API.device(this.groupId, this.isCamera, this.isMicroPhone);
					console.log("摄像头:" + this.isCamera);
				})
			},
			onSwitchSpeaker() {
				this.isSpeaker = !this.isSpeaker;
				// 切换传输的视频流
				this.userInfos.forEach((user) => {
					if (user.id != this.mine.id) {
						const refName = 'remoteVideo' + user.id;
						this.$refs[refName][0].setMute(!this.isSpeaker);
					}
				})
				console.log("扬声器切换:" + this.isSpeaker);
			},
			onSetup() {
				if (!this.checkDevEnable()) {
					this.close();
				}
				// 打开摄像头
				this.openStream().finally(() => {
					// 不管是否成功都进行初始化
					this.initUserVideo()
					// 发起呼叫
					this.API.setup(this.groupId, this.userInfos).then(() => {
						// 准备状态
						this.state = "READY";
						// 播放呼叫铃声
						this.audio.play();
					}).catch(() => {
						this.close();
					})
				})
			},
			onJoin() {
				if (!this.checkDevEnable()) {
					this.close();
				}
				// 打开摄像头
				this.openStream().finally(() => {
					// 不管是否成功都进行初始化
					this.initUserVideo();
					// 发起加入请求
					this.API.join(this.groupId).then(() => {
						// 聊天状态
						this.state = "CHATING";
					}).catch((e) => {
						this.close();
					});
				})
			},
			onAccept() {
				if (!this.checkDevEnable()) {
					this.API.failed(this.groupId, "设备不支持通话")
					this.close();
					return;
				}
				// 进入房间
				this.showRoom = true;
				this.state = "CHATING";
				// 停止呼叫铃声
				this.audio.pause();
				// 打开摄像头
				this.openStream().finally(() => {
					// 不管是否成功都进行初始化
					this.initUserVideo();
					// 加入通话
					this.API.accept(this.groupId).then(() => {
						// 聊天时长
						this.startChatTime();
						// 清理定时器
						this.waitTimer && clearTimeout(this.waitTimer);
					}).catch(() => {
						this.close();
					})
				})
			},
			onReject() {
				// 退出通话
				this.API.reject(this.groupId);
				// 退出
				this.close();
			},
			onQuit() {
				if (this.isClose) {
					return;
				}
				if (this.isHost && !this.isChating) {
					// 终止呼叫
					console.log("onQuit")
					this.API.cancel(this.groupId);
				} else if (!this.isHost && this.state == "WAITING") {
					// 拒绝接听
					this.API.reject(this.groupId);
				} else if (this.isChating) {
					// 退出聊天
					console.log("强制退出聊天");
					this.API.quit(this.groupId);
				}
				// 退出
				this.close();
			},
			onCancel() {
				console.log("onCancel")
				// 取消通话
				this.API.cancel(this.groupId);
				// 退出
				this.close();
			},
			onRTCMessage(msg) {
				// 除了发起通话，如果在关闭状态就无需处理
				if (msg.type != this.$enums.MESSAGE_TYPE.RTC_GROUP_SETUP &&
					this.state == 'CLOSE') {
					return;
				}
				// RTC信令处理
				switch (msg.type) {
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_SETUP:
						this.onRTCSetup(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_ACCEPT:
						this.onRTCAccept(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_REJECT:
						this.onRTCReject(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_JOIN:
						this.onRTCJoin(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_FAILED:
						this.onRTCFailed(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_CANCEL:
						this.onRTCCancel(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_QUIT:
						this.onRTCQuit(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_INVITE:
						this.onRTCInvite(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_OFFER:
						this.onRTCOffer(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_ANSWER:
						this.onRTCAnswer(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_CANDIDATE:
						this.onRTCCandidate(msg)
						break;
					case this.$enums.MESSAGE_TYPE.RTC_GROUP_DEVICE:
						this.onRTCDevice(msg)
						break;
				}
			},
			onRTCSetup(msg) {
				// 有人呼叫我
				this.isHost = false;
				this.userInfos = JSON.parse(msg.content);
				this.inviterId = msg.sendId;
				this.groupId = msg.groupId;
				// 初始化
				this.init();
				// 等待接听
				this.state = "WAITING";
				// 播放呼叫语音
				this.audio.play();
				// 30s未接听自动挂掉
				this.waitTimer = setTimeout(() => {
					this.API.reject(this.groupId);
					this.close("您未接听");
				}, 30000)


			},
			onRTCAccept(msg) {
				if (msg.selfSend) {
					// 我在其他终端接受了的通话
					this.close("已在其他设备接听");
					return;
				}
				// 有用户进入了通话
				const remoteUserId = msg.sendId;
				if (this.isChating) {
					// 与用户发起视频连接
					const refName = 'remoteVideo' + remoteUserId;
					this.$refs[refName][0].connect();
				} else if (this.isHost) {
					// 与用户发起视频连接
					const refName = 'remoteVideo' + remoteUserId;
					this.$refs[refName][0].connect();
					// 只有有人进来就进入聊天状态
					this.state = 'CHATING';
					// 停止呼叫铃声
					this.audio.pause();
					// 开始计时
					this.startChatTime();
				}
			},
			onRTCReject(msg) {
				if (msg.selfSend) {
					// 我在其他终端拒绝了的通话
					this.close("已在其他设备拒绝");
					return;
				}
				// 有人拒绝了通话
				const remoteUserId = msg.sendId;
				// 移除用户
				this.removeUser(remoteUserId, "未进入通话")
			},
			onRTCFailed(msg) {
				// 有人无法加入通话
				const remoteUserId = msg.sendId;
				const failedInfo = JSON.parse(msg.content);
				let nickNames = []
				failedInfo.userIds.forEach((userId) => {
					nickNames.push(`'${this.userInfos.find(u => u.id == userId).nickName}'`);
				})
				let tip = nickNames.join(",") + failedInfo.reason;
				this.$message.warning(tip)
				// 移除用户
				failedInfo.userIds.forEach((userId) => this.removeUser(userId));
			},
			onRTCJoin(msg) {
				// 有用户进入了通话
				const remoteUserId = msg.sendId;
				let userInfo = JSON.parse(msg.content);
				if (!this.isExist(remoteUserId)) {
					this.userInfos.push(userInfo);
				}
				this.$nextTick(() => {
					// 初始化视频窗口
					this.initUserVideo();
					// 与用户发起视频连接
					const refName = 'remoteVideo' + remoteUserId;
					this.$refs[refName][0].connect();
				})
				// 只有有人进来就进入聊天状态
				this.state = 'CHATING';
				// 停止呼叫铃声
				this.audio.pause();
				// 开始聊天计时
				this.startChatTime();
			},
			onRTCOffer(msg) {
				// 有用户向往我发起视频连接
				const remoteUserId = msg.sendId;
				const offer = JSON.parse(msg.content)
				// 回复对方
				const refName = 'remoteVideo' + remoteUserId;
				this.$refs[refName][0].onOffer(offer);
			},
			onRTCAnswer(msg) {
				// 对方同意了我的视频连接请求
				const remoteUserId = msg.sendId;
				const answer = JSON.parse(msg.content);
				// 设置answer信息
				const refName = 'remoteVideo' + remoteUserId;
				this.$refs[refName][0].onAnswer(answer);
			},
			onRTCCancel() {
				// 通话取消，直接退出
				this.close("通话已取消");
			},
			onRTCQuit(msg) {
				// 有人退出了通话
				const remoteUserId = msg.sendId;
				// 移除该用户
				this.removeUser(remoteUserId, "退出通话");
			},
			onRTCInvite(msg) {
				// 有用户被邀请进来
				let userInfos = JSON.parse(msg.content);
				// 加入列表
				this.appendUser(userInfos);
			},
			onRTCCandidate(msg) {
				// 对方同意了我的视频连接请求
				const remoteUserId = msg.sendId;
				const candidate = JSON.parse(msg.content);
				// 设置answer信息
				const refName = 'remoteVideo' + remoteUserId;
				this.$refs[refName][0].setCandidate(candidate);
			},
			onRTCDevice(msg) {
				// 对方进行了设备操作
				const remoteUserId = msg.sendId;
				const devInfo = JSON.parse(msg.content);
				// 记录摄像头/麦克风开关标志
				let userInfo = this.userInfos.find(u => u.id == remoteUserId);
				userInfo.isCamera = devInfo.isCamera;
				userInfo.isMicroPhone = devInfo.isMicroPhone;
			},
			checkDevEnable() {
				// 检测摄像头
				if (!this.camera.isEnable()) {
					this.message.error("访问摄像头失败");
					return false;
				}
				// 检测webrtc
				if (!this.webrtc.isEnable()) {
					this.message.error("初始化RTC失败，原因可能是: 1.服务器缺少ssl证书 2.您的设备不支持WebRTC");
					return false;
				}
				return true;
			},
			openStream() {
				return new Promise((resolve, reject) => {
					if (this.isCamera) {
						// 打开摄像头+麦克风
						this.camera.openVideo().then((stream) => {
							console.log("摄像头打开成功")
							this.stream = stream;
							// 显示本地视频
							this.$refs.localVideo[0].open(stream);
							resolve(stream);
						}).catch((e) => {
							this.$message.error("打开摄像头失败")
							console.log("本地摄像头打开失败:" + e.message)
							reject(e);
						})
					} else {
						// 打开麦克风
						this.camera.openAudio().then((stream) => {
							console.log("麦克风打开成功")
							this.stream = stream;
							// 显示本地视频
							this.$refs.localVideo[0].open(stream);
							resolve(stream);
						}).catch((e) => {
							this.$message.error("打开麦克风失败")
							console.log("本地摄像头打开失败:" + e.message)
							reject(e);
						})
					}
				})
			},
			initUserVideo() {
				// 初始化视频窗口
				this.userInfos.forEach(user => {
					if (user.id != this.mine.id) {
						const refName = 'remoteVideo' + user.id
						// 防止重复初始化
						if (!this.$refs[refName][0].isInit) {
							this.$refs[refName][0].init(this.stream)
						}
					}
				})
			},
			removeUser(userId, tip) {
				if (!this.isExist(userId)) {
					return;
				}
				// 销毁资源
				const refName = 'remoteVideo' + userId;
				if (this.$refs[refName]) {
					this.$refs[refName][0].close();
				}
				// 提示语
				const idx = this.userInfos.findIndex(user => user.id == userId);
				// 移除用户
				const userInfo = this.userInfos.splice(idx, 1)[0];
				if (this.isHost && tip) {
					this.$message.warning(`'${userInfo.nickName}'${tip}`);
				}
				// 如果只剩下自己了，则自己也退出
				if (this.userInfos.length <= 1) {
					this.onQuit();
				}
			},
			appendUser(userInfos) {
				userInfos.forEach(user => {
					if (!this.isExist(user.id)) {
						this.userInfos.push(user);
						console.log(`'${user.nickName}'加入通话`);
					}
				})
				// 初始化视频窗口
				this.$nextTick(() => {
					this.initUserVideo();
				})
			},
			isExist(userId) {
				return !!this.userInfos.find(u => u.id == userId);
			},
			refreshVideoWH() {
				let extendH = this.isFullScreen ? 150 : 250;
				let w = window.innerWidth;
				let h = window.innerHeight - extendH;
				let row = Math.ceil(Math.sqrt(this.userInfos.length));
				let col = Math.ceil(this.userInfos.length / row);
				let vw = w / row;
				let vh = h / col;
				let size = Math.min(vw, vh);
				if (vw > vh * 1.2) {
					this.vw = 1.2 * size;
					this.vh = size;
				} else if (vh > vw * 1.2) {
					this.vh = 1.2 * size;
					this.vw = size;
				} else {
					this.vh = size;
					this.vw = size;
				}
				this.width = this.vw * row;
			},
			startChatTime() {
				if (!this.chatTimer) {
					this.chatTime = 0;
					this.chatTimer = setInterval(() => {
						this.chatTime++;
					}, 1000)
				}
			},
			startHeartBeat() {
				// 每15s推送一次心跳
				this.heartbeatTimer && clearInterval(this.heartbeatTimer);
				this.heartbeatTimer = setInterval(() => {
					this.API.heartbeat(this.groupId);
				}, 15000)
			},
			close(tip) {
				// 弹出关闭提示语
				tip && this.$message.success(tip);
				// 停止呼叫语音
				this.audio.pause();
				// 清理定时器
				this.waitTimer && clearTimeout(this.waitTimer);
				this.chatTimer && clearInterval(this.chatTimer);
				this.heartbeatTimer && clearInterval(this.heartbeatTimer);
				// 重置配置
				this.isMicroPhone = true,
					this.isSpeaker = true,
					this.isCamera = false,
					this.chatTime = 0,
					// 关闭状态
					this.state = "CLOSE";
				// 关闭摄像头等资源
				this.camera.close();
				// 关闭连接
				this.userInfos.forEach((user) => {
					if (user.id != this.mine.id) {
						const refName = 'remoteVideo' + user.id
						if (this.$refs[refName]) {
							this.$refs[refName][0].close();
						}
					}
				})
				// 关闭 
				this.showRoom = false;
				// 数据重置
				this.userInfos = [];
				this.groupId = null;
				this.isHost = false;
				this.inviterId = null;
				this.isMicroPhone = true;
				this.isSpeaker = true;
				this.isCamera = false;
				this.chatTime = 0;
				this.waitTimer = null;
				this.chatTimer = null;
				this.heartbeatTimer = null;
			}
		},
		computed: {
			mine() {
				let userId = this.$store.state.userStore.userInfo.id;
				return this.userInfos.find(user => user.id == userId);
			},
			isChating() {
				return this.state == "CHATING";
			},
			isReady() {
				return this.state == "CHATING" || this.state == "READY";
			},
			isWaiting() {
				return this.state == "WAITING";
			},
			isClose() {
				return this.state == "CLOSE";
			},
			videoStyle() {
				return `width:${this.vw}px;height:${this.vh}px;`
			},
			userCount() {
				return this.userInfos.length;
			},
			chatTimeString() {
				let min = Math.floor(this.chatTime / 60);
				let sec = this.chatTime % 60;
				let strTime = min < 10 ? "0" : "";
				strTime += min;
				strTime += ":";
				strTime += sec < 10 ? "0" : "";
				strTime += sec;
				return strTime;
			},
			title() {
				let title = "语音通话";
				if (this.isChating) {
					title += `(${this.chatTimeString})`
				}
				return title;
			},
			maxChannel() {
				return this.$store.state.configStore.webrtc.maxChannel;
			}
		},
		watch: {
			userCount: {
				handler(newCount, oldCount) {
					this.refreshVideoWH();
				}
			}
		},
		created() {
			// 监听页面刷新事件
			window.addEventListener('beforeunload', () => {
				this.onQuit();
			});

		},
		beforeUnmount() {
			// 
			console.log("beforeUnmount")
			this.onQuit();
		}
	}
</script>

<style lang="scss">
	.header {
		position: relative;
		color: white;

		.icon {
			position: absolute;
			right: 30px;
			line-height: 20px;
			cursor: pointer;
		}
	}

	.el-dialog__body {
		padding: 0 !important;
	}

	.rtc-group-video {
		display: flex;
		flex-direction: column;
		align-items: center;
		overflow: hidden;

		.video-box {
			position: relative;
			background-color: white;

			.video-list {
				display: flex;
				flex-wrap: wrap;
				position: relative;
			}
		}

		.control-bar {
			display: flex;
			justify-content: center;
			align-items: center;
			width: 100%;
			height: 100px;
			background-color: #E8F2FF;

			.icon-box {
				display: flex;
				flex-direction: column;
				align-items: center;
				margin: 0 25px;

				.icon {
					border-radius: 50%;
					padding: 10px;
					font-size: 25px;
					cursor: pointer;
				}

				.icon-front {
					color: black;
					background-color: white;
				}

				.icon-back {
					color: white;
					background-color: black;
				}

				.icon-quit {
					color: white;
					background-color: red;
				}

				.icon-text {
					color: black;
					font-size: 14px;
					margin-top: 10px;
				}

			}


		}
	}
</style>
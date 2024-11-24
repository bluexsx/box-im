let wsurl = "";
let accessToken = "";
let messageCallBack = null;
let closeCallBack = null;
let connectCallBack = null;
let isConnect = false; //连接标识 避免重复连接
let rec = null;
let isInit = false;
let lastConnectTime = new Date(); // 最后一次连接时间

let init = () => {
	// 防止重复初始化
	if (isInit) {
		return;
	}
	isInit = true;
	uni.onSocketOpen((res) => {
		console.log("WebSocket连接已打开");
		isConnect = true;
		// 发送登录命令
		let loginInfo = {
			cmd: 0,
			data: {
				accessToken: accessToken
			}
		};
		uni.sendSocketMessage({
			data: JSON.stringify(loginInfo)
		});
	})

	uni.onSocketMessage((res) => {
		let sendInfo = JSON.parse(res.data)
		if (sendInfo.cmd == 0) {
			heartCheck.start()
			connectCallBack && connectCallBack();
			console.log('WebSocket登录成功')
		} else if (sendInfo.cmd == 1) {
			// 重新开启心跳定时
			heartCheck.reset();
		} else {
			// 其他消息转发出去
			console.log("接收到消息", sendInfo);
			messageCallBack && messageCallBack(sendInfo.cmd, sendInfo.data)
		}
	})

	uni.onSocketClose((res) => {
		console.log('WebSocket连接关闭')
		isConnect = false;
		closeCallBack && closeCallBack(res);
	})

	uni.onSocketError((e) => {
		console.log(e)
		isConnect = false;
		// APP 应用切出超过一定时间(约1分钟)会触发报错，此处回调给应用进行重连
		closeCallBack && closeCallBack({ code: 1006 });
	})
};

let connect = (url, token) => {
	wsurl = url;
	accessToken = token;
	if (isConnect) {
		return;
	}
	lastConnectTime = new Date();
	uni.connectSocket({
		url: wsurl,
		success: (res) => {
			console.log("websocket连接成功");
		},
		fail: (e) => {
			console.log(e);
			console.log("websocket连接失败，10s后重连");
			setTimeout(() => {
				connect();
			}, 10000)
		}
	});
}

//定义重连函数
let reconnect = (wsurl, accessToken) => {
	console.log("尝试重新连接");
	if (isConnect) {
		//如果已经连上就不在重连了
		return;
	}
	// 延迟10秒重连  避免过多次过频繁请求重连
	let timeDiff = new Date().getTime() - lastConnectTime.getTime()
	let delay = timeDiff < 10000 ? 10000 - timeDiff : 0;
	rec && clearTimeout(rec);
	rec = setTimeout(function () {
		connect(wsurl, accessToken);
	}, delay);
};

//设置关闭连接
let close = (code) => {
	if (!isConnect) {
		return;
	}
	uni.closeSocket({
		code: code,
		complete: (res) => {
			console.log("关闭websocket连接");
			isConnect = false;
		},
		fail: (e) => {
			console.log("关闭websocket连接失败", e);
		}
	});
};


//心跳设置
var heartCheck = {
	timeout: 10000, //每段时间发送一次心跳包 这里设置为30s
	timeoutObj: null, //延时发送消息对象（启动心跳新建这个对象，收到消息后重置对象）
	start: function () {
		if (isConnect) {
			console.log('发送WebSocket心跳')
			let heartBeat = {
				cmd: 1,
				data: {}
			};
			uni.sendSocketMessage({
				data: JSON.stringify(heartBeat),
				fail(res) {
					console.log(res);
				}
			})
		}
	},
	reset: function () {
		clearTimeout(this.timeoutObj);
		this.timeoutObj = setTimeout(function () {
			heartCheck.start();
		}, this.timeout);
	}

}

// 实际调用的方法
function sendMessage(agentData) {
	uni.sendSocketMessage({
		data: agentData
	})
}

let onConnect = (callback) => {
	connectCallBack = callback;
}


function onMessage(callback) {
	messageCallBack = callback;
}


function onClose(callback) {
	closeCallBack = callback;
}


// 将方法暴露出去
export {
	init,
	connect,
	reconnect,
	close,
	sendMessage,
	onConnect,
	onMessage,
	onClose
}
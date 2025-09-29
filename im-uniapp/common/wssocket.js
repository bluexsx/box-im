let accessToken = "";
let messageCallBack = null;
let closeCallBack = null;
let connectCallBack = null;
let isConnect = false; //连接标识 避免重复连接
let rec = null;
let lastConnectTime = new Date(); // 最后一次连接时间
let socketTask = null;

let connect = (wsurl, token) => {
	accessToken = token;
	if (isConnect) {
		return;
	}
	lastConnectTime = new Date();
	socketTask = uni.connectSocket({
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

	socketTask.onOpen((res) => {
		console.log("WebSocket连接已打开");
		isConnect = true;
		// 发送登录命令
		let loginInfo = {
			cmd: 0,
			data: {
				accessToken: accessToken
			}
		};
		socketTask.send({
			data: JSON.stringify(loginInfo)
		});
	})

	socketTask.onMessage((res) => {
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

	socketTask.onClose((res) => {
		console.log('WebSocket连接关闭')
		isConnect = false;
		closeCallBack && closeCallBack(res);
	})

	socketTask.onError((e) => {
		console.log("ws错误:",e)
		close(1006);
	})
}

//定义重连函数
let reconnect = (wsurl, accessToken) => {
	console.log("尝试重新连接");
	if (isConnect) {
		return;
	}
	// 延迟10秒重连  避免过多次过频繁请求重连
	let timeDiff = new Date().getTime() - lastConnectTime.getTime()
	let delay = timeDiff < 10000 ? 10000 - timeDiff : 0;
	rec && clearTimeout(rec);
	rec = setTimeout(function() {
		connect(wsurl, accessToken);
	}, delay);
};

//设置关闭连接
let close = (code) => {
	if (!isConnect) {
		return;
	}
	socketTask.close({
		code: code,
		complete: (res) => {
			console.log("关闭websocket连接");
			isConnect = false;
			if (code != 3099) {
				closeCallBack && closeCallBack(res);s
			}
		},
		fail: (e) => {
			console.log("关闭websocket连接失败", e);
		}
	});
};


// 心跳设置
let heartCheck = {
	timeout: 20000, // 每段时间发送一次心跳包 这里设置为20s
	timeoutObj: null, // 延时发送消息对象（启动心跳新建这个对象，收到消息后重置对象）
	start: function() {
		if (isConnect) {
			console.log('发送WebSocket心跳')
			let heartBeat = {
				cmd: 1,
				data: {}
			};
			sendMessage(JSON.stringify(heartBeat))
		}
	},
	reset: function() {
		clearTimeout(this.timeoutObj);
		this.timeoutObj = setTimeout(() => heartCheck.start(), this.timeout);
	}
};

let sendMessage = (message) => {
	socketTask.send({ data: message })
}

let onConnect = (callback) => {
	connectCallBack = callback;
}


let onMessage = (callback) => {
	messageCallBack = callback;
}


let onClose = (callback) => {
	closeCallBack = callback;
}


// 将方法暴露出去
export {
	connect,
	reconnect,
	close,
	sendMessage,
	onConnect,
	onMessage,
	onClose
}
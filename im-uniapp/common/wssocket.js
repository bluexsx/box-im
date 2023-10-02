let wsurl = "";
let accessToken = "";
let messageCallBack = null;
let openCallBack = null;
let isConnect = false; //连接标识 避免重复连接
let hasLogin = false;

let createWebSocket = (url, token) => {
	wsurl = url;
	accessToken = token;
	closeWebSocket().then(() => {
		initWebSocket();
	});

};

let initWebSocket = () => {
	console.log("初始化WebSocket");
	uni.connectSocket({
		url: wsurl,
		success: (res) => {
			console.log("websocket连接成功");
		},
		fail: (err) => {
			console.log(e);
			console.log("websocket连接失败");
			reConnect(); //如果无法连接上webSocket 那么重新连接！可能会因为服务器重新部署，或者短暂断网等导致无法创建连接
		}
	});

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
			hasLogin = true;
			heartCheck.start()
			console.log('WebSocket登录成功')
			// 登录成功才算连接完成
			openCallBack && openCallBack();
		} else if (sendInfo.cmd == 1) {
			// 重新开启心跳定时
			heartCheck.reset();
		} else {
			// 其他消息转发出去
			console.log("接收到消息",sendInfo);
			messageCallBack && messageCallBack(sendInfo.cmd, sendInfo.data)
		}
	})

	uni.onSocketClose((res) => {
		console.log(res)
		console.log('WebSocket连接关闭')
		isConnect = false; //断开后修改标识
		//reConnect();
	})

	uni.onSocketError((err) => {
		console.log(err)
		isConnect = false; //连接断开修改标识
		uni.showModal({
			content: '连接失败，可能是websocket服务不可用，请稍后再试',
			showCancel: false,
		})
	})

};

//定义重连函数
let reConnect = () => {
	console.log("尝试重新连接");
	if (isConnect) return; //如果已经连上就不在重连了
	rec && clearTimeout(rec);
	rec = setTimeout(function() { // 延迟5秒重连  避免过多次过频繁请求重连
		initWebSocket();
	}, 5000);
};

//设置关闭连接
let closeWebSocket = () => {
	return new Promise((resolve, reject) => {
		if (!isConnect) {
			resolve();
			return;
		}
		console.log("关闭websocket连接");
		uni.closeSocket({
			code: 1000,
			complete: (res) => {
				hasLogin = false;
				isConnect = false;
				resolve();
			}
		})
	})


};


//心跳设置
var heartCheck = {
	timeout: 10000, //每段时间发送一次心跳包 这里设置为30s
	timeoutObj: null, //延时发送消息对象（启动心跳新建这个对象，收到消息后重置对象）
	start: function() {
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
	reset: function() {
		clearTimeout(this.timeoutObj);
		this.timeoutObj = setTimeout(function() {
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


function onmessage(callback) {
	messageCallBack = callback;
}


function onopen(callback) {
	openCallBack = callback;
	if (hasLogin) {
		openCallBack();
	}
}


// 将方法暴露出去
export {
	createWebSocket,
	closeWebSocket,
	sendMessage,
	onmessage,
	onopen
}
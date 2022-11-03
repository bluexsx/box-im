var websock = null;
let rec; //断线重连后，延迟5秒重新创建WebSocket连接  rec用来存储延迟请求的代码
let isConnect = false; //连接标识 避免重复连接
let wsurl = "";
let $store = null;
let messageCallBack = null;
let openCallBack = null;
let hasLogin = false;

let createWebSocket = (url, store) => {
	$store = store;
	wsurl = url;
	initWebSocket();
};

let initWebSocket = () => {
	try {
		console.log("初始化WebSocket");
		hasLogin = false;
		websock = new WebSocket(wsurl);
		websock.onmessage = function(e) {
			let msg = JSON.parse(e.data)
			if (msg.cmd == 0) {
				hasLogin = true;
				heartCheck.start()
				console.log('WebSocket登录成功')
				// 登录成功才算连接完成
				openCallBack && openCallBack();
			}
			else if(msg.cmd==1){
				// 重新开启心跳定时
				heartCheck.reset();
			} else {
				// 其他消息转发出去
				messageCallBack && messageCallBack(JSON.parse(e.data))
			}
		}
		websock.onclose = function(e) {
			console.log('WebSocket连接关闭')
			isConnect = false; //断开后修改标识
		}
		websock.onopen = function() {
			console.log("WebSocket连接成功");
			isConnect = true;
			// 发送登录命令
			let loginInfo = {
				cmd: 0,
				data: {userId: $store.state.userStore.userInfo.id}
			};
			websock.send(JSON.stringify(loginInfo));
			
		}

		// 连接发生错误的回调方法
		websock.onerror = function() {
			console.log('WebSocket连接发生错误')
			isConnect = false; //连接断开修改标识
			reConnect();
		}
	} catch (e) {
		console.log("尝试创建连接失败");
		reConnect(); //如果无法连接上webSocket 那么重新连接！可能会因为服务器重新部署，或者短暂断网等导致无法创建连接
	}
};

//定义重连函数
let reConnect = () => {
	console.log("尝试重新连接");
	if (isConnect) return; //如果已经连上就不在重连了
	rec && clearTimeout(rec);
	rec = setTimeout(function() { // 延迟5秒重连  避免过多次过频繁请求重连
		initWebSocket(wsurl);
	}, 5000);
};
//设置关闭连接
let closeWebSocket = () => {
	websock.close();
};


//心跳设置
var heartCheck = {
	timeout: 5000, //每段时间发送一次心跳包 这里设置为20s
	timeoutObj: null, //延时发送消息对象（启动心跳新建这个对象，收到消息后重置对象）
	start: function() {
		if(isConnect){
			console.log('发送WebSocket心跳')
			let heartBeat = {
				cmd: 1,
				data: {
					userId: $store.state.userStore.userInfo.id
				}
			};
			websock.send(JSON.stringify(heartBeat))
		}
	},

	reset: function(){
		clearTimeout(this.timeoutObj);
		this.timeoutObj = setTimeout(function() {
			heartCheck.start();
		}, this.timeout);
		
	}
};



// 实际调用的方法
function sendMessage(agentData) {
	// console.log(globalCallback)
	if (websock.readyState === websock.OPEN) {
		// 若是ws开启状态
		websock.send(JSON.stringify(agentData))
	} else if (websock.readyState === websock.CONNECTING) {
		// 若是 正在开启状态，则等待1s后重新调用
		setTimeout(function() {
			sendMessage(agentData)
		}, 1000)
	} else {
		// 若未开启 ，则等待1s后重新调用
		setTimeout(function() {
			sendMessage(agentData)
		}, 1000)
	}
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

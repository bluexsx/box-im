var websock = null;
let rec; //断线重连后，延迟5秒重新创建WebSocket连接  rec用来存储延迟请求的代码
let isConnect = false; //连接标识 避免重复连接
let isCompleteConnect = false; //完全连接标识（接收到心跳）
let wsurl = "";
let $store = null;
let messageCallBack = null;
let openCallBack = null;


let createWebSocket = (url, store) => {
	$store = store;
	wsurl = url;
	initWebSocket();
};

let initWebSocket = () => {
	try {
		console.log("初始化WebSocket");
		isCompleteConnect = false;
		websock = new WebSocket(wsurl);
		websock.onmessage = function(e) {
			let msg = JSON.parse(decodeUnicode(e.data))
			if (msg.cmd == 0) {
				if(!isCompleteConnect){
					// 第一次上传心跳成功才算连接完成
					isCompleteConnect = true;
					openCallBack && openCallBack();
				}
				heartCheck.reset();
			} else {
				// 其他消息转发出去
				messageCallBack && messageCallBack(JSON.parse(e.data))
			}
		}
		websock.onclose = function(e) {
			console.log('WebSocket连接关闭')
			isConnect = false; //断开后修改标识
			reConnect();
		}
		websock.onopen = function() {
			console.log("WebSocket连接成功");
			isConnect = true;
			heartCheck.start()
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
				cmd: 0,
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
	if (isCompleteConnect) {
		openCallBack();
	}
}


function decodeUnicode(str) {
	str = str.replace(/\\/g, "%");
	//转换中文
	str = unescape(str);
	//将其他受影响的转换回原来
	str = str.replace(/%/g, "\\");
	//对网址的链接进行处理
	str = str.replace(/\\/g, "");
	return str;

}


// 将方法暴露出去
export {
	createWebSocket,
	closeWebSocket,
	sendMessage,
	onmessage,
	onopen
}

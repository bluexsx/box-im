//设置环境(打包前修改此变量)
const ENV = "PROD";

const UNI_APP = {
}

if(ENV=="DEV"){
	// 开发环境
	UNI_APP.BASE_URL = "http://192.168.2.67:8888";
	UNI_APP.WS_URL = "ws://192.168.2.67:8878/im";
	// H5 走本地代理避免跨域问题
	// #ifdef H5
		UNI_APP.BASE_URL = "/api";
	// #endif
}

if(ENV=="PROD"){
	// 生产环境
	UNI_APP.BASE_URL = "https://www.boxim.online/api";
	UNI_APP.WS_URL = "wss://www.boxim.online:81/im";
}

export default UNI_APP
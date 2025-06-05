//设置环境(打包前修改此变量)
const ENV = "DEV";
const UNI_APP = {}
// 表情包路径
UNI_APP.EMO_URL = "/static/emoji/";
// #ifdef MP-WEIXIN
// 微信小程序的本地表情包经常莫名失效，建议将表情放到服务器中
// UNI_APP.EMO_URL = "https://www.boxim.online/emoji/";
// #endif

if(ENV=="DEV"){
	UNI_APP.BASE_URL = "http://127.0.0.1:8888";
	UNI_APP.WS_URL = "ws://127.0.0.1:8878/im";
	// H5 走本地代理解决跨域问题
	// #ifdef H5
	UNI_APP.BASE_URL = "/api";
	// #endif
}
if(ENV=="PROD"){
	UNI_APP.BASE_URL = "https://www.boxim.online/api";
	UNI_APP.WS_URL = "wss://www.boxim.online/im";
}
export default UNI_APP
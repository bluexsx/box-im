
// 是否普通消息
let isNormal = function(type){
	return type>=0 && type < 10;
}

// 是否状态消息
let isStatus = function(type){
	return type>=10 && type < 20;
}

// 是否提示消息
let isTip = function(type){
	return type>=20 && type < 30;
}

// 操作交互类消息
let isAction = function(type){
	return type>=40 && type < 50;
}

// 单人通话信令
let isRtcPrivate = function(type){
	return type>=100 && type < 300;
}

// 多人通话信令
let isRtcGroup = function(type){
	return type>=200 && type < 400;
}


export {
	isNormal,
	isStatus,
	isTip,
	isAction,
	isRtcPrivate,
	isRtcGroup
}
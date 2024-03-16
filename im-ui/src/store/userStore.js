import http from '../api/httpRequest.js'
import {RTC_STATE} from "../api/enums.js"
export default {
	
	state: {
		userInfo: {
			
		},
		rtcInfo: {
			friend: {},  // 好友信息
			mode: "video", // 模式 video:视频 voice:语音
			state: 	RTC_STATE.FREE // FREE:空闲  WAIT_CALL:呼叫方等待 WAIT_ACCEPT: 被呼叫方等待接听  CHATING:聊天中 
		}
	},

	mutations: {
		setUserInfo(state, userInfo) {
			state.userInfo = userInfo
		},
		setRtcInfo(state, rtcInfo ){
		    state.rtcInfo = rtcInfo;
		},
		setRtcState(state,rtcState){
			state.rtcInfo.state = rtcState;
		},
		clear(state){
			state.userInfo = {};
			state.rtcInfo = {
				friend: {},
				mode: "video",
				state: RTC_STATE.FREE
			};
		}
	},
	actions:{
		loadUser(context){
			return new Promise((resolve, reject) => {
				http({
					url: '/user/self',
					method: 'GET'
				}).then((userInfo) => {
					context.commit("setUserInfo",userInfo);
					resolve();
				}).catch((res)=>{
					reject(res);
				});
			})
		}
	}
}
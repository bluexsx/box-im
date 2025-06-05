import { defineStore } from 'pinia';
import http from '../api/httpRequest.js'
import { RTC_STATE } from "../api/enums.js"

export default defineStore('userStore', {
	state: () => {
		return {
			userInfo: {},
			rtcInfo: {
				friend: {},  // 好友信息
				mode: "video", // 模式 video:视频 voice:语音
				state: RTC_STATE.FREE // FREE:空闲  WAIT_CALL:呼叫方等待 WAIT_ACCEPT: 被呼叫方等待接听  CHATING:聊天中 
			}
		}
	},
	actions: {
		setUserInfo(userInfo) {
			this.userInfo = userInfo
		},
		setRtcInfo(rtcInfo) {
			this.rtcInfo = rtcInfo;
		},
		setRtcState(rtcState) {
			this.rtcInfo.state = rtcState;
		},
		clear() {
			this.userInfo = {};
			this.rtcInfo = {
				friend: {},
				mode: "video",
				state: RTC_STATE.FREE
			};
		},
		loadUser() {
			return new Promise((resolve, reject) => {
				http({
					url: '/user/self',
					method: 'GET'
				}).then((userInfo) => {
					this.setUserInfo(userInfo);
					resolve();
				}).catch((res) => {
					reject(res);
				});
			})
		}
	}
});
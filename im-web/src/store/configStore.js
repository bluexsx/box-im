import { defineStore } from 'pinia';
import http from '../api/httpRequest.js'

export default defineStore('configStore', {
	state: () => {
		return {
			appInit: false,  // 应用是否完成初始化
			fullScreen: true, // 当前是否全屏
			webrtc: {}
		}
	},
	actions: {
		setConfig(config) {
			this.webrtc = config.webrtc;
		},
		setAppInit(appInit) {
			this.appInit = appInit;
		},
		setFullScreen(fullScreen) {
			this.fullScreen = fullScreen;
		},
		loadConfig() {
			return new Promise((resolve, reject) => {
				http({
					url: '/system/config',
					method: 'GET'
				}).then(config => {
					console.log("系统配置", config)
					this.setConfig(config);
					resolve();
				}).catch((res) => {
					reject(res);
				});
			})
		}
	}
});
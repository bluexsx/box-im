import { defineStore } from 'pinia';
import http from '../api/httpRequest.js'

export default defineStore('configStore', {
	state: () => {
		return {
			webrtc: {}
		}
	},
	actions: {
		setConfig(config) {
			this.webrtc = config.webrtc;
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
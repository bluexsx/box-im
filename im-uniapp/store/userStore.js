import { defineStore } from 'pinia';
import http from '../common/request'

export default defineStore('userStore', {
	state: () => {
		return {
			userInfo: {}
		}
	},
	actions: {
		setUserInfo(userInfo) {
			this.userInfo = userInfo;
		},
		clear() {
			this.userInfo = {};
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
})
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
		loadUser(context) {
			return new Promise((resolve, reject) => {
				http({
					url: '/user/self',
					method: 'GET'
				}).then((userInfo) => {
					console.log(userInfo)
					this.setUserInfo(userInfo);
					resolve();
				}).catch((res) => {
					reject(res);
				});
			})
		}
	}
})
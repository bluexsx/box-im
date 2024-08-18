import { defineStore } from 'pinia';
import http from '../common/request'
import { TERMINAL_TYPE } from '../common/enums.js'

export default defineStore('friendStore', {
	state: () => {
		return {
			friends: [],
			timer: null
		}
	},
	actions: {
		setFriends(friends) {
			friends.forEach((f) => {
				f.online = false;
				f.onlineWeb = false;
				f.onlineApp = false;
			})
			this.friends = friends;
		},
		updateFriend(friend) {
			let f = this.findFriend(friend.id);
			let copy = JSON.parse(JSON.stringify(f));
			Object.assign(f, friend);
			f.online = copy.online;
			f.onlineWeb = copy.onlineWeb;
			f.onlineApp = copy.onlineApp;
		},
		removeFriend(id) {
			this.friends.forEach((f, idx) => {
				if (f.id == id) {
					this.friends.splice(idx, 1)
				}
			})
		},
		addFriend(friend) {
			this.friends.push(friend);
		},
		setOnlineStatus(onlineTerminals) {
			this.friends.forEach((f) => {
				let userTerminal = onlineTerminals.find((o) => f.id == o.userId);
				if (userTerminal) {
					f.online = true;
					f.onlineWeb = userTerminal.terminals.indexOf(TERMINAL_TYPE.WEB) >= 0
					f.onlineApp = userTerminal.terminals.indexOf(TERMINAL_TYPE.APP) >= 0
				} else {
					f.online = false;
					f.onlineWeb = false;
					f.onlineApp = false;
				}
			});
		},
		refreshOnlineStatus() {
			if (this.friends.length > 0) {
				let userIds = [];
				this.friends.forEach(f => userIds.push(f.id));
				http({
					url: '/user/terminal/online?userIds=' + userIds.join(','),
					method: 'GET'
				}).then((onlineTerminals) => {
					this.setOnlineStatus(onlineTerminals);
				})
			}
			// 30s后重新拉取
			clearTimeout(this.timer);
			this.timer = setTimeout(() => {
				this.refreshOnlineStatus();
			}, 30000)
		},
		clear() {
			clearTimeout(this.timer);
			this.friends = [];
			this.timer = null;
		},
		loadFriend() {
			return new Promise((resolve, reject) => {
				http({
					url: '/friend/list',
					method: 'GET'
				}).then((friends) => {
					this.setFriends(friends);
					this.refreshOnlineStatus();
					resolve()
				}).catch((res) => {
					reject();
				})
			});
		}
	},
	getters: {
		findFriend: (state) => (id) => {
			return state.friends.find((f) => f.id == id);
		}
	}
})
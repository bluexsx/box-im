import { defineStore } from 'pinia';
import http from '../api/httpRequest.js'
import { TERMINAL_TYPE } from "../api/enums.js"

export default defineStore('friendStore', {
	state: () => {
		return {
			friends: [], // 好友列表
			timer: null
		}
	},
	actions: {
		setFriends(friends) {
			this.friends = friends;
		},
		updateFriend(friend) {
			this.friends.forEach((f, index) => {
				if (f.id == friend.id) {
					// 拷贝属性
					let online = this.friends[index].online;
					Object.assign(this.friends[index], friend);
					this.friends[index].online = online;
				}
			})
		},
		removeFriend(id) {
			this.friends.filter(f => f.id == id).forEach(f => f.deleted = true);
		},
		addFriend(friend) {
			if (this.friends.some(f => f.id == friend.id)) {
				this.updateFriend(friend)
			} else {
				this.friends.unshift(friend);
			}
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
			let userIds = this.friends.filter((f) => !f.deleted).map((f) => f.id);
			if (userIds.length == 0) {
				return;
			}
			http({
				url: '/user/terminal/online?userIds=' + userIds.join(','),
				method: 'GET'
			}).then((onlineTerminals) => {
				this.setOnlineStatus(onlineTerminals);
			})
			// 30s后重新拉取
			clearTimeout(this.timer);
			this.timer = setTimeout(() => {
				this.refreshOnlineStatus();
			}, 30000)
		},
		setDnd(id, isDnd) {
			let friend = this.findFriend(id);
			friend.isDnd = isDnd;
		},
		clear() {
			this.timer && clearTimeout(this.timer);
			this.friends = [];
			this.timer = null;
		},
		loadFriend() {
			return new Promise((resolve, reject) => {
				http({
					url: '/friend/list',
					method: 'GET'
				}).then(async (friends) => {
					this.setFriends(friends);
					this.refreshOnlineStatus();
					resolve();
				}).catch(e => {
					reject(e);
				})
			});
		}
	},
	getters: {
		isFriend: (state) => (userId) => {
			return state.friends.filter((f) => !f.deleted).some((f) => f.id == userId);
		},
		findFriend: (state) => (userId) => {
			return state.friends.find((f) => f.id == userId);
		}
	}
});
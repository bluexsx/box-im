import { defineStore } from 'pinia';
import http from '../common/request'
import { TERMINAL_TYPE } from '../common/enums.js'

export default defineStore('friendStore', {
	state: () => {
		return {
			friendMap: new Map(),
			timer: null
		}
	},
	actions: {
		setFriends(friends) {
			this.friendMap.clear();
			friends.forEach(f => this.friendMap.set(f.id, f))
		},
		updateFriend(friend) {
			let f = this.findFriend(friend.id);
			friend.online = f.online;
			friend.onlineWeb = f.onlineWeb;
			friend.onlineApp = f.onlineApp;
			this.friendMap.set(friend.id, friend)
		},
		removeFriend(id) {
			this.friendMap.get(id).deleted = true;
		},
		addFriend(friend) {
			this.friendMap.set(friend.id, friend)
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
			this.friendMap.get(id).isDnd = isDnd;
		},
		clear() {
			this.friendMap.clear();
			clearTimeout(this.timer);
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
		friends: (state) => {
			return Array.from(state.friendMap.values());
		},
		findFriend: (state) => (userId) => {
			return state.friendMap.get(userId);
		},
		isFriend: (state) => (userId) => {
			let f = state.findFriend(userId)
			return f && !f.deleted
		}
	}
})
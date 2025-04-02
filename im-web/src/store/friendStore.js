import http from '../api/httpRequest.js'
import { TERMINAL_TYPE } from "../api/enums.js"

export default {

	state: {
		friends: [],
		timer: null
	},
	mutations: {
		setFriends(state, friends) {
			friends.forEach((f) => {
				f.online = false;
				f.onlineWeb = false;
				f.onlineApp = false;
			})
			state.friends = friends;
		},
		updateFriend(state, friend) {
			state.friends.forEach((f, index) => {
				if (f.id == friend.id) {
					// 拷贝属性
					let online = state.friends[index].online;
					Object.assign(state.friends[index], friend);
					state.friends[index].online = online;
				}
			})
		},
		removeFriend(state, id) {
			state.friends.filter(f => f.id == id).forEach(f => f.deleted = true);
		},
		addFriend(state, friend) {
			if (state.friends.some((f) => f.id == friend.id)) {
				this.commit("updateFriend", friend)
			} else {
				state.friends.unshift(friend);
			}
		},
		refreshOnlineStatus(state) {
			let userIds = state.friends.filter((f) => !f.deleted).map((f) => f.id);
			if (userIds.length == 0) {
				return;
			}
			http({
				url: '/user/terminal/online',
				method: 'get',
				params: { userIds: userIds.join(',') }
			}).then((onlineTerminals) => {
				this.commit("setOnlineStatus", onlineTerminals);
			})
			// 30s后重新拉取
			state.timer && clearTimeout(state.timer);
			state.timer = setTimeout(() => {
				this.commit("refreshOnlineStatus");
			}, 30000)
		},
		setOnlineStatus(state, onlineTerminals) {
			state.friends.forEach((f) => {
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
			// 在线的在前面
			state.friends.sort((f1, f2) => {
				if (f1.online && !f2.online) {
					return -1;
				}
				if (f2.online && !f1.online) {
					return 1;
				}
				return 0;
			});
		},
		clear(state) {
			state.timer && clearTimeout(state.timer);
			state.friends = [];
			state.timer = null;
		}
	},
	actions: {
		loadFriend(context) {
			return new Promise((resolve, reject) => {
				http({
					url: '/friend/list',
					method: 'GET'
				}).then((friends) => {
					context.commit("setFriends", friends);
					context.commit("refreshOnlineStatus");
					resolve()
				}).catch(() => {
					reject();
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
}

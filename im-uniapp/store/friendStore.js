import http from '../common/request'
import {TERMINAL_TYPE} from '../common/enums.js'

export default {

	state: {
		friends: [],
		timer: null
	},
	mutations: {
		setFriends(state, friends) {
			state.friends = friends;
		},
		updateFriend(state, friend) {
			state.friends.forEach((f, index) => {
				if (!f.delete && f.id == friend.id) {
					// 拷贝属性
					let online = state.friends[index].online;
					Object.assign(state.friends[index], friend);
					state.friends[index].online = online;
				}
			})
		},
		removeFriend(state, id) {
			let friend = this.getters.findFriend(id);
			if(friend){
				friend.delete = true;
			}
		},
		addFriend(state, friend) {
			let f = this.getters.findFriend(friend.id);
			if(f){
				Object.assign(f, friend);
				f.delete = false;
			}else{
				state.friends.push(friend);
			}
		},
		setOnlineStatus(state, onlineUsers) {
			state.friends.forEach((f) => {
				let onlineUser = onlineUsers.find((o) => f.id == o.userId);
				f.online = !!onlineUser
			});
		},
		refreshOnlineStatus(state) {
			if (state.friends.length > 0) {
				let userIds = [];
				state.friends.forEach((f) => {
					userIds.push(f.id)
				});
				http({
					url: '/user/terminal/online?userIds=' + userIds.join(','),
					method: 'GET'
				}).then((onlineUsers) => {
					this.commit("setOnlineStatus", onlineUsers);
				})
			}
			// 30s后重新拉取
			clearTimeout(state.timer);
			state.timer = setTimeout(() => {
				this.commit("refreshOnlineStatus");
			}, 30000)
		},
		clear(state) {
			clearTimeout(state.timer);
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
				}).catch((res) => {
					reject();
				})
			});
		}
	},
	getters:{
		findFriend: (state) => (id) => {
			return state.friends.find((f)=>f.id==id);
		}
	}
}
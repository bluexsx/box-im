import request from '../common/request';

export default {

	state: {
		friends: [],
		activeIndex: -1
	},
	mutations: {
		setFriends(state, friends) {
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
		activeFriend(state, index) {
			state.activeIndex = index;
		},
		removeFriend(state, index) {
			state.friends.splice(index, 1);
			if (state.activeIndex >= state.friends.length) {
				state.activeIndex = state.friends.length - 1;
			}
		},
		addFriend(state, friend) {
			state.friends.push(friend);
		},
		
		setOnlineStatus(state, onlineIds) {
			console.log("setOnlineStatus")
			state.friends.forEach((f) => {
				let onlineFriend = onlineIds.find((id) => f.id == id);
				f.online = onlineFriend != undefined;
			});

			let activeFriend = state.friends[state.activeIndex];
			state.friends.sort((f1, f2) => {
				if (f1.online && !f2.online) {
					return -1;
				}
				if (f2.online && !f1.online) {
					return 1;
				}
				return 0;
			});

			// 重新排序后，activeIndex指向的好友可能会变化，需要重新指定
			if (state.activeIndex >= 0) {
				state.friends.forEach((f, i) => {
					if (f.id == activeFriend.id) {
						state.activeIndex = i;
					}
				})
			}
		}
	},
	actions: {
		loadFriend(context) {
			return new Promise((resolve, reject) => {
				request({
					url: '/friend/list',
					method: 'GET'
				}).then((friends) => {
					context.commit("setFriends", friends);
					context.dispatch("refreshOnlineStatus");
					resolve()
				}).catch((res) => {
					reject();
				})
			});
		},
		refreshOnlineStatus(context) {
			if (context.state.friends.length > 0 ) {
				let userIds = [];
				context.state.friends.forEach((f) => {
					userIds.push(f.id)
				});
				request({
					url: '/user/online?userIds='+ userIds.join(','),
					method: 'GET'
				}).then((onlineIds) => {
					context.commit("setOnlineStatus", onlineIds);
				})
			}
			// 30s后重新拉取
			clearTimeout(context.timer);
			context.timer = setTimeout(() => {
				context.dispatch("refreshOnlineStatus");
			}, 30000)
		},
	}
}
import httpRequest from '../api/httpRequest.js'

export default {

	state: {
		groups: [],
		activeIndex: -1,
	},
	mutations: {
		initGroupStore(state, userInfo) {
			httpRequest({
				url: '/api/friends/list',
				method: 'get'
			}).then((friendsList) => {
				this.commit("setFriendsList",friendsList);
				this.commit("refreshOnlineStatus");
			})
		},
}
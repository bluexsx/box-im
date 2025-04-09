import http from '../api/httpRequest.js'

export default {
	state: {
		groups: []
	},
	mutations: {
		setGroups(state, groups) {
			state.groups = groups;
		},
		addGroup(state, group) {
			if (state.groups.some((g) => g.id == group.id)) {
				this.commit("updateGroup", group)
			} else {
				state.groups.unshift(group);
			}
		},
		removeGroup(state, id) {
			state.groups.filter(g => g.id == id).forEach(g => g.quit = true);
		},
		updateGroup(state, group) {
			state.groups.forEach((g, idx) => {
				if (g.id == group.id) {
					// 拷贝属性
					Object.assign(state.groups[idx], group);
				}
			})
		},
		clear(state) {
			state.groups = [];
		}
	},
	actions: {
		loadGroup(context) {
			return new Promise((resolve, reject) => {
				http({
					url: '/group/list',
					method: 'GET'
				}).then((groups) => {
					context.commit("setGroups", groups);
					resolve();
				}).catch((res) => {
					reject(res);
				})
			});
		}
	},
	getters: {
		findGroup: (state) => (id) => {
			return state.groups.find((g) => g.id == id);
		}
	}
}
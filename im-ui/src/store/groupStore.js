import http from '../api/httpRequest.js'

export default {

	state: {
		groups: [],
		activeGroup: null,
	},
	mutations: {
		setGroups(state, groups) {
			state.groups = groups;
		},
		activeGroup(state, idx) {
			state.activeGroup = state.groups[idx];
		},
		addGroup(state, group) {
			state.groups.unshift(group);
		},
		removeGroup(state, groupId) {
			state.groups.forEach((g, idx) => {
				if (g.id == groupId) {
					state.groups.splice(idx, 1);
				}
			})
			if (state.activeGroup.id == groupId) {
				state.activeGroup = null;
			}
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
			state.activeGroup = null;
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
	}
}
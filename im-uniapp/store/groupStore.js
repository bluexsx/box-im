import { defineStore } from 'pinia';
import http from '@/common/request';

export default defineStore('groupStore', {
	state: () => {
		return {
			groupMap: new Map()
		}
	},
	actions: {
		setGroups(groups) {
			this.groupMap.clear();
			groups.forEach(g => this.groupMap.set(g.id, g))
		},
		addGroup(group) {
			this.groupMap.set(group.id, group);
		},
		removeGroup(id) {
			this.groupMap.get(id).quit = true;
		},
		updateGroup(group) {
			this.groupMap.set(group.id, group);
		},
		setDnd(id, isDnd) {
			this.groupMap.get(id).isDnd = isDnd;
		},
		clear() {
			this.groupMap.clear();
		},
		loadGroup() {
			return new Promise((resolve, reject) => {
				http({
					url: '/group/list',
					method: 'GET'
				}).then((groups) => {
					this.setGroups(groups);
					resolve();
				}).catch((res) => {
					reject(res);
				})
			});
		}
	},
	getters: {
		groups: (state) => {
			return Array.from(state.groupMap.values());
		},
		findGroup: (state) => (id) => {
			return state.groupMap.get(id);
		},
		isGroup: (state) => (id) => {
			let group = state.findGroup(id);
			return group && !group.quit
		}
	}
})
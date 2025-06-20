import { defineStore } from 'pinia';
import http from '@/common/request';

export default defineStore('groupStore', {
	state: () => {
		return {
			groups: []
		}
	},
	actions: {
		setGroups(groups) {
			this.groups = groups;
		},
		addGroup(group) {
			if (this.groups.some((g) => g.id == group.id)) {
				this.updateGroup(group);
			} else {
				this.groups.unshift(group);
			}
		},
		removeGroup(id) {
			this.groups.filter(g => g.id == id).forEach(g => g.quit = true);
		},
		updateGroup(group) {
			let g = this.findGroup(group.id);
			Object.assign(g, group);
		},
		setDnd(id, isDnd) {
			let group = this.findGroup(id);
			group.isDnd = isDnd;
		},
		clear() {
			this.groups = [];
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
		findGroup: (state) => (id) => {
			return state.groups.find((g) => g.id == id);
		}
	}
})
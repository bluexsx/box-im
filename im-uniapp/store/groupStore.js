import { defineStore } from 'pinia';
import http from '@/common/request';

export default defineStore('groupStore', {
	state: () => {
		return {
			groups: [],
			activeIndex: -1
		}
	},
	actions: {
		setGroups(groups) {
			this.groups = groups;
		},
		activeGroup(index) {
			this.activeIndex = index;
		},
		addGroup(group) {
			this.groups.unshift(group);
		},
		removeGroup(groupId) {
			this.groups.forEach((g, index) => {
				if (g.id == groupId) {
					this.groups.splice(index, 1);
					if (this.activeIndex >= this.groups.length) {
						this.activeIndex = this.groups.length - 1;
					}
				}
			})
		},
		updateGroup(group) {
			this.groups.forEach((g, idx) => {
				if (g.id == group.id) {
					// 拷贝属性
					Object.assign(this.groups[idx], group);
				}
			})
		},
		clear() {
			this.groups = [];
			this.activeGroup = -1;
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
	}
})
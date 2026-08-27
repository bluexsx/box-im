import { defineStore } from 'pinia';
import { toRaw } from 'vue';
import http from '@/common/request';
import { getDB } from '@/db/index.js';
import { isToday } from '@/common/date.js';

export default defineStore('groupStore', {
	state: () => {
		return {
			groups: [],
			groupMap: new Map()
		}
	},
	actions: {
		init(groups) {
			this.groups = groups;
			this.groupMap.clear();
			groups.forEach(group => this.groupMap.set(group.id, group));
		},
		append(groups) {
			groups.forEach(group => {
				if (this.groupMap.has(group.id)) {
					const g = this.groupMap.get(group.id);
					group.members = g.members;
					Object.assign(g, group);
				} else {
					this.groups.push(group);
					this.groupMap.set(group.id, group);
				}
			});
		},
		resetMembers(groups) {
			groups.forEach(group => group.members = [])
		},
		async addGroup(group) {
			if (this.groupMap.has(group.id)) {
				this.updateGroup(group)
			} else {
				group.members = [];
				group.rtcInfo = {};
				this.groups.unshift(group);
				this.groupMap.set(group.id, group);
				await getDB().saveGroup(toRaw(group));
			}
		},
		async removeGroup(id) {
			const group = this.findGroup(id);
			group.quit = true;
			await getDB().saveGroup(toRaw(group));
		},
		async updateGroup(group) {
			const g = this.findGroup(group.id);
			// 成员列表保持不变
			group.members = g.members;
			// 拷贝属性
			Object.assign(g, group);
			await getDB().saveGroup(toRaw(g));
		},
		async updateTopMessage(id, topMessage) {
			const group = this.findGroup(id);
			if (group) {
				group.topMessage = topMessage;
				await getDB().saveGroup(toRaw(group));
			}
		},
		async setDnd(id, isDnd) {
			const group = this.findGroup(id);
			group.isDnd = isDnd;
			await getDB().saveGroup(toRaw(group));
		},
		async setTop(id, isTop) {
			const group = this.findGroup(id);
			group.isTop = isTop;
			await getDB().saveGroup(toRaw(group));
		},
		async refreshMember(id) {
			const group = this.findGroup(id);
			// 成员最大版本号
			const version = Math.max(0, ...group.members.map(m => m.version || 0));
			const members = await http({ url: `/group/members/${id}?version=${version}`, });
			if (!group.members.length) {
				// 全量更新
				group.members = members;
			} else {
				// 增量更新
				members.forEach(m1 => {
					const member = group.members.find(m2 => m1.userId == m2.userId);
					if (member) {
						Object.assign(member, m1);
					} else {
						group.members.push(m1)
					}
				})
				// 更新成员在线状态
				this.refreshMemberOnline(id);
			}
			await getDB().saveGroup(toRaw(group));
		},
		async refreshMemberOnline(id) {
			const group = this.findGroup(id);
			const userIds = await http({ url: `/group/members/online/${id}` });
			group.members.forEach(m => m.online = userIds.some(userId => m.userId == userId));
			this.refreshMmeberSort(id);
		},
		refreshMmeberSort(id) {
			const group = this.findGroup(id);
			group.members.sort((m1, m2) => {
				// 在线的放前面
				if (m1.online && !m2.online) {
					return -1
				}
				if (!m1.online && m2.online) {
					return 1
				}
				// 群主在前面
				if (m1.userId == group.ownerId) {
					return -1;
				}
				if (m2.userId == group.ownerId) {
					return 1;
				}
				// 管理员在前面
				if (m1.isManager && !m2.isManager) {
					return -1;
				}
				if (!m1.isManager && m2.isManager) {
					return 1;
				}
				return 0;
			})
		},
		clear() {
			this.groups = [];
			this.groupMap.clear();
		},
		async pullGroups() {
			const version = Math.max(0, ...this.groups.map(g => g.version || 0));
			const groups = await http({ url: '/group/list?version=' + version });
			this.resetMembers(groups);
			await getDB().saveGroups(groups);
			this.append(groups);
		},
		async loadGroup() {
			const lastSyncTime = await getDB().findLastSyncGroupsTime();
			if (!lastSyncTime || !isToday(new Date(lastSyncTime))) {
				// 每日首次登录进行全量同步
				const groups = await http({ url: '/group/list' });
				this.resetMembers(groups);
				await getDB().syncAllGroups(groups);
				this.init(groups);
				console.log("全量同步群聊信息")
			} else {
				// 从本地数据库加载
				const groups = await getDB().findAllGroups();

				this.init(groups);
				// 从服务器增量拉取
				await this.pullGroups();
				console.log("增量同步群聊信息")
			}
		}
	},
	getters: {
		findGroup: (state) => (id) => {
			return state.groupMap.get(id);
		},
		isGroup: (state) => (id) => {
			let group = state.findGroup(id);
			return group && !group.quit
		}
	}
})
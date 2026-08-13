import { defineStore } from 'pinia';
import { ref } from 'vue';
import { listGroup, listGroupMembers, listGroupOnlineMembers } from '@/api/group';
import type { GroupVO } from '@/api/group/types';
import { getDB } from '@/db';
import { isToday } from '@/utils/date';

export const useGroupStore = defineStore('group', () => {
  const groups = ref<GroupVO[]>([]);
  const groupMap = ref(new Map<number, GroupVO>());
  const findGroup = (id?: number) => {
    if (id == null) return undefined;
    return groupMap.value.get(id);
  };

  const isGroup = (id?: number) => {
    const group = findGroup(id);
    return !!(group && !group.quit);
  };

  const init = (list: GroupVO[]) => {
    groups.value = list;
    groupMap.value.clear();
    list.forEach((group) => {
      if (group.id != null) groupMap.value.set(group.id, group);
    });
  };

  const append = (list: GroupVO[]) => {
    list.forEach((group) => {
      if (group.id == null) return;
      if (groupMap.value.has(group.id)) {
        const g = groupMap.value.get(group.id)!;
        group.members = g.members;
        Object.assign(g, group);
      } else {
        groups.value.push(group);
        groupMap.value.set(group.id, group);
      }
    });
  };

  const resetMembers = (list: GroupVO[]) => {
    list.forEach((group) => {
      group.members = [];
    });
  };

  const addGroup = async (group: GroupVO) => {
    if (group.id != null && groupMap.value.has(group.id)) {
      await updateGroup(group);
    } else {
      group.members = [];
      groups.value.unshift(group);
      if (group.id != null) groupMap.value.set(group.id, group);
      await getDB().saveGroup(group);
    }
  };

  const removeGroup = async (id?: number) => {
    const group = findGroup(id)!;
    group.quit = true;
    await getDB().saveGroup(group);
  };

  const updateGroup = async (group: GroupVO) => {
    const g = findGroup(group.id)!;
    group.members = g.members;
    Object.assign(g, group);
    await getDB().saveGroup(group);
  };

  const setDnd = async (id: number, isDnd: boolean) => {
    const group = findGroup(id)!;
    group.isDnd = isDnd;
    await getDB().saveGroup(group);
  };

  const setTop = async (id: number, isTop: boolean) => {
    const group = findGroup(id)!;
    group.isTop = isTop;
    await getDB().saveGroup(group);
  };

  const refreshMember = async (id: number) => {
    const group = findGroup(id)!;
    const version = Math.max(0, ...(group.members || []).map((m) => m.version || 0));
    const members = await listGroupMembers(id, version);
    if (!group.members?.length) {
      group.members = members;
    } else {
      members.forEach((m1) => {
        const member = group.members!.find((m2) => m1.userId == m2.userId);
        if (member) {
          Object.assign(member, m1);
        } else {
          group.members!.push(m1);
        }
      });
      await refreshMemberOnline(id);
    }
    await getDB().saveGroup(group);
  };

  const refreshMemberOnline = async (id: number) => {
    const group = findGroup(id)!;
    const userIds = await listGroupOnlineMembers(id);
    (group.members || []).forEach((m) => {
      m.online = userIds.some((userId) => m.userId == userId);
    });
    refreshMmeberSort(id);
  };

  const refreshMmeberSort = (id: number) => {
    const group = findGroup(id)!;
    (group.members || []).sort((m1, m2) => {
      if (m1.online && !m2.online) {
        return -1;
      }
      if (!m1.online && m2.online) {
        return 1;
      }
      if (m1.userId == group.ownerId) {
        return -1;
      }
      if (m2.userId == group.ownerId) {
        return 1;
      }
      return 0;
    });
  };

  const clear = () => {
    groups.value = [];
    groupMap.value.clear();
  };

  const pullGroups = async () => {
    const version = Math.max(0, ...groups.value.map((g) => g.version || 0));
    const list = await listGroup(version);
    resetMembers(list);
    append(list);
    await getDB().saveGroups(list);
  };

  const loadGroup = async () => {
    const lastSyncTime = await getDB().findLastSyncGroupsTime();
    if (!lastSyncTime || !isToday(new Date(lastSyncTime))) {
      const list = await listGroup();
      resetMembers(list);
      init(list);
      await getDB().syncAllGroups(list);
      console.log('全量同步群聊信息');
    } else {
      const list = (await getDB().findAllGroups()) as GroupVO[];
      init(list);
      await pullGroups();
      console.log('增量同步群聊信息');
    }
  };
  return {
    groups,
    groupMap,
    init,
    append,
    resetMembers,
    addGroup,
    removeGroup,
    updateGroup,
    setDnd,
    setTop,
    refreshMember,
    refreshMemberOnline,
    refreshMmeberSort,
    clear,
    pullGroups,
    loadGroup,
    findGroup,
    isGroup
  };
});

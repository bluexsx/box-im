import { defineStore } from 'pinia';
import { ref } from 'vue';
import { listFriend, listFriendOnline } from '@/api/friend';
import type { FriendVO, UserOnlineVO } from '@/api/friend/types';
import { getDB } from '@/db';
import { TERMINAL_TYPE } from '@/utils/enums';
import { isToday } from '@/utils/date';

export const useFriendStore = defineStore('friend', () => {
  const friends = ref<FriendVO[]>([]);
  const friendMap = ref(new Map<number, FriendVO>());
  const findFriend = (userId?: number) => {
    if (userId == null) return undefined;
    return friendMap.value.get(userId);
  };

  const isFriend = (userId?: number) => {
    const f = findFriend(userId);
    return !!(f && !f.deleted);
  };

  const init = (list: FriendVO[]) => {
    friends.value = list;
    friendMap.value.clear();
    list.forEach((f) => {
      if (f.id != null) friendMap.value.set(f.id, f);
    });
  };

  const append = (list: FriendVO[]) => {
    list.forEach((f) => {
      if (f.id == null) return;
      if (friendMap.value.has(f.id)) {
        const friend = friendMap.value.get(f.id)!;
        Object.assign(friend, f);
      } else {
        friends.value.push(f);
        friendMap.value.set(f.id, f);
      }
    });
  };

  const resetOnline = (list: FriendVO[]) => {
    list.forEach((f) => {
      f.online = false;
      f.onlineWeb = false;
      f.onlineApp = false;
    });
  };

  const updateFriend = async (friend: FriendVO) => {
    const f = findFriend(friend.id)!;
    friend.online = f.online;
    friend.onlineWeb = f.onlineWeb;
    friend.onlineApp = f.onlineApp;
    Object.assign(f, friend);
    await getDB().saveFriend(friend);
  };

  const removeFriend = async (id?: number) => {
    const friend = findFriend(id)!;
    friend.deleted = true;
    await getDB().saveFriend(friend);
  };

  const addFriend = async (friend: FriendVO) => {
    if (friend.id != null && friendMap.value.has(friend.id)) {
      await updateFriend(friend);
    } else {
      friends.value.unshift(friend);
      friendMap.value.set(friend.id, friend);
      await getDB().saveFriend(friend);
    }
  };

  const updateOnlineStatus = (onlineData: UserOnlineVO) => {
    const friend = findFriend(onlineData.userId);
    if (!friend) return;
    if (onlineData.terminal == TERMINAL_TYPE.WEB) {
      friend.onlineWeb = onlineData.online;
    } else if (onlineData.terminal == TERMINAL_TYPE.APP) {
      friend.onlineApp = onlineData.online;
    }
    friend.online = !!(friend.onlineWeb || friend.onlineApp);
  };

  const setDnd = async (id: number, isDnd: boolean) => {
    const friend = findFriend(id);
    if (!friend) return;
    friend.isDnd = isDnd;
    await getDB().saveFriend(friend);
  };

  const setTop = async (id: number, isTop: boolean) => {
    const friend = findFriend(id);
    if (!friend) return;
    friend.isTop = isTop;
    await getDB().saveFriend(friend);
  };

  const clear = () => {
    friends.value = [];
    friendMap.value.clear();
  };

  const pullFriends = async () => {
    const version = Math.max(0, ...friends.value.map((f) => f.version || 0));
    const list = await listFriend(version);
    resetOnline(list);
    append(list);
    await getDB().saveFriends(list);
  };

  const refreshOnline = async () => {
    const onlines = await listFriendOnline();
    resetOnline(friends.value);
    onlines.forEach((online) => updateOnlineStatus(online));
  };

  const loadFriend = async () => {
    const lastSyncTime = await getDB().findLastSyncFriendsTime();
    if (!lastSyncTime || !isToday(new Date(lastSyncTime))) {
      const list = await listFriend();
      resetOnline(list);
      init(list);
      await getDB().syncAllFriends(list);
      console.log('全量同步好友信息');
    } else {
      const list = (await getDB().findAllFriends()) as FriendVO[];
      resetOnline(list);
      init(list);
      await pullFriends();
      console.log('增量同步好友信息');
    }
    refreshOnline();
  };
  return {
    friends,
    friendMap,
    init,
    append,
    resetOnline,
    updateFriend,
    removeFriend,
    addFriend,
    updateOnlineStatus,
    setDnd,
    setTop,
    clear,
    pullFriends,
    refreshOnline,
    loadFriend,
    findFriend,
    isFriend
  };
});

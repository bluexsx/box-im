import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getSelf } from '@/api/user';
import type { UserVO } from '@/api/user/types';

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserVO>({ id: 0, nickName: '' });
  const isInRtc = ref(false); // 是否正在通话中

  const setUserInfo = (info: UserVO) => {
    userInfo.value = info;
  };

  const setInRtc = (value: boolean) => {
    isInRtc.value = value;
  };

  const clear = () => {
    userInfo.value = { id: 0, nickName: '' };
    isInRtc.value = false;
  };

  const loadUser = async () => {
    const info = await getSelf();
    setUserInfo(info);
    if (info.isBanned) {
      throw new Error('账户已被封禁');
    }
  };

  return {
    userInfo,
    isInRtc,
    setUserInfo,
    setInRtc,
    clear,
    loadUser
  };
});

<template>
  <div class="group-member-card" @click.stop="onShowUserInfo">
    <HeadImage :id="member.userId" :name="member.showNickName" :url="member.headImage" :size="40" :online="member.online" />
    <div class="name">{{ member.showNickName }}</div>
  </div>
</template>

<script setup lang="ts">
import HeadImage from '@/components/common/HeadImage.vue';
import { findUser } from '@/api/user';
import type { GroupMemberVO } from '@/api/group/types';
import eventBus from '@/utils/eventBus';

const props = defineProps({
  member: {
    type: Object as () => GroupMemberVO,
    required: true
  }
});

const onShowUserInfo = async (e: MouseEvent) => {
  if (!props.member.userId) {
    return;
  }
  const user = await findUser(props.member.userId);
  eventBus.emit('openUserInfo', {
    user,
    pos: { x: e.clientX + 30, y: e.clientY }
  });
};
</script>

<style lang="scss" scoped>
.group-member-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 54px;
  cursor: pointer;

  .name {
    font-size: 12px;
    text-align: center;
    width: 100%;
    height: 30px;
    line-height: 30px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
}
</style>

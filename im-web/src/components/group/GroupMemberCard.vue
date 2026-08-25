<template>
  <div class="group-member-card">
    <HeadImage :id="member.userId" :name="member.showNickName" :url="member.headImage" :size="40" :online="member.online" />
    <div class="name">{{ member.showNickName }}</div>
  </div>
</template>

<script setup lang="ts">
import HeadImage from '@/components/common/HeadImage.vue';
import type { GroupMemberVO } from '@/api/group/types';

defineProps({
  member: {
    type: Object as () => GroupMemberVO,
    required: true
  }
});
</script>

<style lang="scss" scoped>
.group-member-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 54px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
  }

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

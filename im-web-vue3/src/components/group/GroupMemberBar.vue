<template>
  <div ref="itemRef" class="group-member-bar" :class="active ? 'active' : ''" :style="{ height: height + 'px' }">
    <HeadImage :size="headImageSize" :id="member.userId" :name="member.showNickName" :url="member.headImage" :online="member.online" />
    <div class="name" :style="{ 'line-height': height + 'px' }">
      <div class="name-text" :title="member.showNickName">{{ member.showNickName }}</div>
      <el-tag v-if="mine.id == member.userId" type="primary">{{ '我' }}</el-tag>
      <el-tag v-if="member.userId == group.ownerId" type="danger">{{ '群主' }}</el-tag>
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import HeadImage from '@/components/common/HeadImage.vue';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import { useUserStore } from '@/stores/user';

const props = defineProps({
  group: {
    type: Object as () => GroupVO,
    required: true
  },
  member: {
    type: Object as () => GroupMemberVO,
    required: true
  },
  height: {
    type: Number,
    default: 50
  },
  active: {
    type: Boolean,
    default: false
  }
});

const userStore = useUserStore();
const { userInfo: mine } = storeToRefs(userStore);
const headImageSize = computed(() => Math.ceil(props.height * 0.75));
</script>

<style lang="scss" scoped>
.group-member-bar {
  display: flex;
  position: relative;
  padding: 0 15px;
  align-items: center;
  white-space: nowrap;
  box-sizing: border-box;
  border-radius: 5px;
  margin: 0 1px;
  cursor: pointer;

  &:hover {
    background-color: var(--im-background-active);
  }

  &.active {
    background-color: var(--im-background-active-dark);
  }

  .name {
    display: flex;
    align-items: center;
    flex: 1;
    height: 100%;
    padding: 10px;
    box-sizing: border-box;
    overflow: hidden;

    .name-text {
      text-align: left;
      font-size: var(--im-font-size);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}
</style>

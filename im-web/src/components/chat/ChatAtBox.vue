<template>
  <VirtualScroller
    v-show="show && showMembers.length"
    ref="scrollerRef"
    class="chat-at-box"
    :style="{ left: pos.x + 'px', top: pos.y - 300 + 'px' }"
    :items="showMembers">
    <template #default="{ item: member }">
      <div @click="onSelectMember(member)">
        <GroupMemberItem
          :group="group"
          :group-members="members"
          :member="member"
          :height="40"
          :active="!!(activeMember && member.userId === activeMember.userId)"
          :menu="false" />
      </div>
    </template>
  </VirtualScroller>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import VirtualScroller from '@/components/common/VirtualScroller.vue';
import GroupMemberItem from '@/components/group/GroupMemberItem.vue';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import { useUserStore } from '@/stores/user';

const props = defineProps({
  searchText: {
    type: String,
    default: ''
  },
  group: {
    type: Object as () => GroupVO,
    required: true
  },
  members: {
    type: Array as () => GroupMemberVO[],
    default: () => []
  }
});

const emit = defineEmits<{
  select: [member: GroupMemberVO];
}>();

const userStore = useUserStore();
const { userInfo: mine } = storeToRefs(userStore);
const scrollerRef = ref<{ init: () => void; scrollbar?: { $el: HTMLElement } }>();
const show = ref(false);
const pos = ref({ x: 0, y: 0 });
const activeIdx = ref(0);

const showMembers = computed(() => {
  const list: GroupMemberVO[] = [];
  const userId = mine.value.id;
  const allName = '全体成员';
  const isOwner = props.group?.ownerId == userId;
  // 群主可@全体成员
  if (isOwner && allName.startsWith(props.searchText)) {
    list.push({ userId: -1, showNickName: allName });
  }
  props.members.forEach((m) => {
    if (m.userId != userId && !m.quit && m.showNickName.startsWith(props.searchText)) {
      list.push(m);
    }
  });
  return list;
});

const activeMember = computed(() => showMembers.value[activeIdx.value]);

const scrollWrap = () => {
  const s = scrollerRef.value;
  if (!s?.scrollbar) {
    return null;
  }
  return s.scrollbar.$el?.querySelector('.el-scrollbar__wrap') as HTMLElement | null;
};

const init = () => {
  activeIdx.value = showMembers.value.length ? 0 : -1;
  nextTick(() => {
    const scroller = scrollerRef.value;
    if (scroller) {
      scroller.init();
      const wrap = scrollWrap();
      if (wrap) {
        wrap.scrollTop = 0;
      }
    }
  });
};

const open = (p: { x: number; y: number }) => {
  show.value = true;
  pos.value = p;
  init();
};

const close = () => {
  show.value = false;
};

const scrollToActive = () => {
  nextTick(() => {
    const wrap = scrollWrap();
    if (!wrap) {
      return;
    }
    const top = activeIdx.value * 40;
    if (top - wrap.clientHeight > wrap.scrollTop) {
      wrap.scrollTop = Math.min(wrap.scrollTop + 140, wrap.scrollHeight);
    } else if (top < wrap.scrollTop) {
      wrap.scrollTop = Math.max(wrap.scrollTop - 140, 0);
    }
  });
};

const moveUp = () => {
  if (activeIdx.value > 0) {
    activeIdx.value--;
    scrollToActive();
  }
};

const moveDown = () => {
  if (activeIdx.value < showMembers.value.length - 1) {
    activeIdx.value++;
    scrollToActive();
  }
};

const select = () => {
  if (activeMember.value) {
    onSelectMember(activeMember.value);
  }
  close();
};

const onSelectMember = (member: GroupMemberVO) => {
  emit('select', member);
  show.value = false;
};

watch(
  () => props.searchText,
  () => {
    init();
  }
);

defineExpose({ open, close, moveUp, moveDown, select });
</script>

<style scoped lang="scss">
.chat-at-box {
  position: fixed;
  width: 200px;
  height: 300px;
  background-color: #fff;
  box-shadow: var(--im-box-shadow);
  border-radius: 6px;
  overflow: hidden;
}
</style>

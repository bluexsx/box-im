<template>
  <div>
    <div class="group-item" :class="itemClass" @contextmenu.prevent="showRightMenu($event)">
      <div class="group-avatar">
        <HeadImage :size="headImageSize" :name="group.showGroupName" :url="group.headImageThumb" />
      </div>
      <div class="group-name">{{ group.showGroupName }}</div>
      <slot />
    </div>
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import HeadImage from '@/components/common/HeadImage.vue';
import RightMenu, { type RightMenuItem } from '@/components/common/RightMenu.vue';
import type { GroupVO } from '@/api/group/types';
import { useUserStore } from '@/stores/user';

const props = defineProps({
  group: {
    type: Object as () => GroupVO,
    required: true
  },
  active: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'normal'
  },
  menu: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['chat', 'dissolve', 'quit']);

const userStore = useUserStore();
const rightMenuRef = ref<InstanceType<typeof RightMenu>>();

const headImageSize = computed(() => (props.size == 'small' ? 36 : 42));

const itemClass = computed(() => {
  let clz = '';
  if (props.active) clz += 'active';
  if (props.size == 'small') clz += ' small';
  return clz;
});

const isOwner = computed(() => props.group.ownerId == userStore.userInfo.id);

const menuItems = computed<RightMenuItem[]>(() => {
  const items: RightMenuItem[] = [{ key: 'CHAT', name: '发送消息' }];
  if (isOwner.value) {
    items.push({ key: 'DISSOLVE', name: '解散群聊', danger: true });
  } else {
    items.push({ key: 'QUIT', name: '退出群聊', danger: true });
  }
  return items;
});

const onSelectMenu = (item: RightMenuItem) => {
  emit(String(item.key).toLowerCase() as 'chat' | 'dissolve' | 'quit');
};

const showRightMenu = (e: MouseEvent) => {
  if (props.menu) {
    rightMenuRef.value?.open(e, menuItems.value);
  }
};
</script>

<style lang="scss" scoped>
.group-item {
  height: 60px;
  display: flex;
  position: relative;
  border-radius: 10px;
  margin: 0 3px;
  padding: 5px 8px;
  cursor: pointer;

  &:hover {
    background-color: var(--im-background-active);
  }

  &.active {
    background-color: var(--im-background-active-dark);
  }

  &.small {
    height: 48px;
    padding: 3px 10px;
  }

  .group-name {
    flex: 1;
    display: flex;
    align-items: center;
    padding-left: 10px;
    height: 100%;
    text-align: left;
    white-space: nowrap;
    overflow: hidden;
    font-size: var(--im-font-size);
  }
}
</style>

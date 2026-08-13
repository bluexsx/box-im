<template>
  <div v-if="show" class="right-menu-mask" @click.stop="close" @contextmenu.prevent="close">
    <div class="right-menu" :style="{ left: pos.x + 'px', top: pos.y + 'px' }">
      <div class="menu-container">
        <div v-for="item in items" :key="item.key" class="menu-item" :class="{ danger: item.danger }" @click.stop="onSelectMenu(item)">
          <i v-if="item.icon" :class="item.icon" class="menu-icon" />
          <span class="menu-text">{{ item.name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';

export type RightMenuItem = {
  key: string | number;
  name: string;
  icon?: string;
  danger?: boolean;
  [k: string]: unknown;
};

const emit = defineEmits(['select']);
const show = ref(false);
const pos = reactive({ x: 0, y: 0 });
const items = ref<RightMenuItem[]>([]);

const rejustPos = () => {
  const menuH = items.value.length * 40 + 16; // 增加内边距
  const menuW = 140; // 增加宽度
  if (pos.y > window.innerHeight - menuH) {
    pos.y = window.innerHeight - menuH;
  }
  if (pos.x > window.innerWidth - menuW) {
    pos.x = window.innerWidth - menuW;
  }
};

const open = (p: { x: number; y: number }, menuItems: RightMenuItem[]) => {
  pos.x = p.x;
  pos.y = p.y;
  items.value = menuItems;
  show.value = true;
  rejustPos();
};

const close = () => {
  show.value = false;
};

const onSelectMenu = (item: RightMenuItem) => {
  emit('select', item);
  close();
};

defineExpose({ open, close });
</script>

<style lang="scss">
.right-menu-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
}

.right-menu {
  position: fixed;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 10000;

  .menu-container {
    padding: 8px 0;
    min-width: 120px;

    .menu-item {
      display: flex;
      align-items: center;
      padding: 8px 16px;
      cursor: pointer;
      transition: all 0.2s ease;
      position: relative;
      font-size: 14px;
      color: var(--im-text-color);

      &:hover {
        background: var(--im-background-active);
        color: var(--im-color-primary);
      }

      &.danger {
        color: var(--im-color-danger);

        &:hover {
          background: rgba(245, 108, 108, 0.1);
          color: var(--im-color-danger);
        }

        &:active {
          background: rgba(245, 108, 108, 0.2);
          transform: scale(0.98);
        }
      }

      .menu-icon {
        font-size: 16px;
        margin-right: 8px;
        width: 16px;
        text-align: center;
      }

      .menu-text {
        flex: 1;
        font-weight: 500;
      }
    }
  }
}
</style>

<template>
  <div class="tabs">
    <div v-for="(item, idx) in items" :key="idx" class="tab" :class="current == idx ? 'active' : ''" @click="onClickItem(idx)">
      {{ item }}
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps({
  items: {
    type: Array as () => string[],
    required: true
  },
  current: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['change']);

const onClickItem = (idx: number) => {
  if (props.current != idx) {
    emit('change', idx);
  }
};
</script>

<style lang="scss" scoped>
.tabs {
  height: 52px;
  display: flex;
  align-items: center;
  border-radius: 8px;
  background: #fafafa;
  padding: 4px;
  position: relative;

  .tab {
    color: var(--im-text-color-secondary);
    padding: 8px 16px;
    border-radius: 6px;
    font-weight: 500;
    font-size: 14px;
    flex: 1;
    text-align: center;
    white-space: nowrap;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    z-index: 1;

    &:hover {
      color: var(--im-text-color-primary);
      background: var(--im-background-active);
    }

    &.active {
      color: var(--im-color-primary);
      background: var(--im-background-active-dark);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      font-weight: 600;
      transform: translateY(-1px);
      // 添加下划线指示器

      &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 50%;
        transform: translateX(-50%);
        width: 24px;
        height: 3px;
        background: var(--im-color-primary);
        border-radius: 2px;
        animation: slideIn 0.3s ease-out;
      }
    }

    &:active {
      transform: translateY(0) scale(0.98);
      transition: transform 0.1s ease;
    }

    // 第一个和最后一个tab的特殊处理

    &:first-child {
      margin-right: 2px;
    }

    &:last-child {
      margin-left: 2px;
    }

    // 中间的tab

    &:not(:first-child):not(:last-child) {
      margin: 0 2px;
    }
  }
}

// 下划线出现动画

@keyframes slideIn {
  0% {
    width: 0;
    opacity: 0;
  }

  50% {
    opacity: 1;
  }

  100% {
    width: 24px;
    opacity: 1;
  }
}
</style>

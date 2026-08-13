<template>
  <el-aside :style="{ width: asideWidth + 'px' }" class="resizable-aside">
    <slot />
    <!-- 拖拽条 -->
    <div class="resize-handle" :class="{ resizing: isResizing }" :title="'拖拽调整宽度'" @mousedown="startResize">
      <div class="resize-line" />
    </div>
  </el-aside>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';

const props = defineProps({
  // 默认宽度
  defaultWidth: {
    type: Number,
    default: 260
  },
  // 最小宽度
  minWidth: {
    type: Number,
    default: 200
  },
  // 最大宽度
  maxWidth: {
    type: Number,
    default: 500
  },
  // localStorage存储key
  storageKey: {
    type: String,
    required: true
  }
});

const asideWidth = ref(props.defaultWidth);
const isResizing = ref(false);
const startX = ref(0);
const startWidth = ref(0);

// 拖拽相关方法
const startResize = (e: MouseEvent) => {
  isResizing.value = true;
  startX.value = e.clientX;
  startWidth.value = asideWidth.value;
  document.body.style.cursor = 'col-resize';
  document.body.style.userSelect = 'none';
  e.preventDefault();
};

const handleResize = (e: MouseEvent) => {
  if (!isResizing.value) return;
  const deltaX = e.clientX - startX.value;
  let newWidth = startWidth.value + deltaX;
  // 限制宽度范围
  newWidth = Math.max(props.minWidth, Math.min(props.maxWidth, newWidth));
  asideWidth.value = newWidth;
};

const stopResize = () => {
  if (!isResizing.value) return;
  isResizing.value = false;
  document.body.style.cursor = '';
  document.body.style.userSelect = '';
  // 保存宽度到localStorage
  localStorage.setItem(props.storageKey, asideWidth.value.toString());
};

onMounted(() => {
  // 从localStorage恢复宽度设置
  const savedWidth = localStorage.getItem(props.storageKey);
  if (savedWidth) {
    asideWidth.value = parseInt(savedWidth, 10);
  }
  // 添加全局事件监听
  document.addEventListener('mousemove', handleResize);
  document.addEventListener('mouseup', stopResize);
});

onBeforeUnmount(() => {
  // 清理事件监听
  document.removeEventListener('mousemove', handleResize);
  document.removeEventListener('mouseup', stopResize);
});
</script>

<style scoped lang="scss">
.resizable-aside {
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  position: relative;
  // 拖拽条样式

  .resize-handle {
    position: absolute;
    top: 0;
    right: -3px;
    width: 6px;
    height: 100%;
    cursor: col-resize;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s ease;

    .resize-line {
      width: 2px;
      height: 100%;
      background-color: var(--im-background-active-dark);
      border-radius: 1px;
      transition: all 0.2s ease;
    }

    &:hover .resize-line,
    &.resizing .resize-line {
      width: 3px;
    }
  }
}
</style>

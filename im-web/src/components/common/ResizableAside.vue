<template>
  <el-aside :style="{ width: asideWidth + 'px' }" class="resizable-aside">
    <slot></slot>
    <!-- 拖拽条 -->
    <div class="resize-handle" @mousedown="startResize" :class="{ 'resizing': isResizing }" title="拖拽调整宽度">
      <div class="resize-line"></div>
    </div>
  </el-aside>
</template>

<script>
export default {
  name: "ResizableAside",
  props: {
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
  },
  data() {
    return {
      asideWidth: this.defaultWidth,
      isResizing: false,
      startX: 0,
      startWidth: 0
    }
  },
  mounted() {
    // 从localStorage恢复宽度设置
    const savedWidth = localStorage.getItem(this.storageKey);
    if (savedWidth) {
      this.asideWidth = parseInt(savedWidth);
    }
    
    // 添加全局事件监听
    document.addEventListener('mousemove', this.handleResize);
    document.addEventListener('mouseup', this.stopResize);
  },
  beforeDestroy() {
    // 清理事件监听
    document.removeEventListener('mousemove', this.handleResize);
    document.removeEventListener('mouseup', this.stopResize);
  },
  methods: {
    // 拖拽相关方法
    startResize(e) {
      this.isResizing = true;
      this.startX = e.clientX;
      this.startWidth = this.asideWidth;
      document.body.style.cursor = 'col-resize';
      document.body.style.userSelect = 'none';
      e.preventDefault();
    },
    handleResize(e) {
      if (!this.isResizing) return;
      
      const deltaX = e.clientX - this.startX;
      let newWidth = this.startWidth + deltaX;
      
      // 限制宽度范围
      newWidth = Math.max(this.minWidth, Math.min(this.maxWidth, newWidth));
      
      this.asideWidth = newWidth;
    },
    stopResize() {
      if (!this.isResizing) return;
      
      this.isResizing = false;
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
      
      // 保存宽度到localStorage
      localStorage.setItem(this.storageKey, this.asideWidth.toString());
    }
  }
}
</script>

<style lang="scss" scoped>
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

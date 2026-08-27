<template>
  <el-scrollbar ref="scrollbarRef">
    <slot name="top" />
    <div v-for="(item, idx) in items" :key="idx">
      <slot v-if="idx < showMaxIdx" :item="item" />
    </div>
  </el-scrollbar>
</template>
<script setup lang="ts" generic="T">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
const props = withDefaults(
  defineProps<{
    items: T[];
    size?: number;
  }>(),
  {
    size: 30
  }
);
defineSlots<{
  default(props: { item: T }): unknown;
  top(): unknown;
}>();
const scrollbarRef = ref();
const page = ref(1);
const isInitEvent = ref(false);
const lockTip = ref(false);
const showMaxIdx = computed(() => Math.min(page.value * props.size, props.items.length));

const showTip = () => {
  if (!lockTip.value) {
    ElMessage.success('已滚动到底部');
    lockTip.value = true;
    setTimeout(() => {
      lockTip.value = false;
    }, 3000);
  }
};

const onScroll = (e: Event) => {
  const scrollbar = e.target as HTMLElement;
  // 滚到底部
  if (scrollbar.scrollTop + scrollbar.clientHeight >= scrollbar.scrollHeight - 30) {
    if (showMaxIdx.value >= props.items.length) {
      showTip();
    } else {
      page.value++;
    }
  }
};

const initEvent = () => {
  if (!isInitEvent.value && scrollbarRef.value) {
    const scrollWrap = scrollbarRef.value.$el.querySelector('.el-scrollbar__wrap') as HTMLElement | null;
    if (scrollWrap) {
      scrollWrap.addEventListener('scroll', onScroll);
      isInitEvent.value = true;
    }
  }
};

const init = () => {
  page.value = 1;
  initEvent();
};

onMounted(() => {
  initEvent();
});

defineExpose({ init, page, size: props.size, scrollbar: scrollbarRef });
</script>

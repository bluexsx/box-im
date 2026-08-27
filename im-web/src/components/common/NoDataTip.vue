<template>
  <div class="no-data-tip">
    <img class="image" src="@/assets/image/no_data.png" />
    <div class="text">{{ displayTip }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  tip: {
    type: String,
    default: ''
  }
});

const displayTip = computed(() => props.tip || '没有数据');
</script>

<style lang="scss">
.no-data-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .image {
    height: 120px;
  }

  .text {
    text-align: center;
    font-size: var(--im-font-size);
    color: var(--im-text-color-light);
    margin-top: 5px;
  }
}
</style>

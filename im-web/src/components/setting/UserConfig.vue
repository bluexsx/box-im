<template>
  <div class="user-config">
    <div class="form-section">
      <h4 class="section-title">
        <el-icon><Sunny /></el-icon>
        {{ '主色调' }}
      </h4>
      <div class="setting-item theme-accent-item">
        <div class="setting-label">
          <el-icon><Opportunity /></el-icon>
          <span>{{ '自定义界面主色' }}</span>
        </div>
        <div class="setting-content accent-list">
          <div
            v-for="item in themeAccents"
            :key="item.id"
            class="accent-item"
            :class="{ active: primaryColor === item.color }"
            :style="{ '--accent': item.color }"
            :title="item.name"
            @click="onThemeAccentChange(item.color)">
            <el-icon v-if="primaryColor === item.color"><Check /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Check, Opportunity, Sunny } from '@element-plus/icons-vue';
import { useConfigStore } from '@/stores/config';
import { themeAccents, themeDefaultColor } from '@/utils/theme';

const configStore = useConfigStore();
const primaryColor = ref(themeDefaultColor);
const init = () => {
  primaryColor.value = configStore.themeAccent;
};

const onThemeAccentChange = (color: string | null) => {
  if (!color) return;
  primaryColor.value = configStore.setThemeAccent(color);
};
defineExpose({ init });
</script>

<style scoped lang="scss">
.user-config {
  padding: 15px;

  .form-section {
    background: #fff;
    border-radius: 12px;
    padding: 16px 20px;
    margin-bottom: 16px;

    .section-title {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0 0 12px;
      font-size: 15px;
    }

    .setting-item {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .setting-label {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }

    .accent-list {
      display: flex;
      gap: 8px;

      .accent-item {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: var(--accent);
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;

        &.active {
          outline: 2px solid var(--accent);
          outline-offset: 2px;
        }
      }
    }
  }
}
</style>

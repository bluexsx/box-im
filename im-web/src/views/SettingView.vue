<template>
  <el-container class="setting-page">
    <el-aside width="230px" class="setting-aside" :class="{ fullscreen: configStore.fullScreen }">
      <div class="aside-header">
        <span class="aside-title">{{ '设置' }}</span>
      </div>
      <div class="nav-menu">
        <div
          v-for="(item, index) in menuItems"
          :key="item.component"
          class="nav-item"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </el-aside>
    <el-container class="setting-main">
      <div class="content-header">
        <div class="header-title">{{ currentTabItem.label }}</div>
      </div>
      <div class="content-body">
        <div v-show="currentComponent === 'personalInfo'" class="tab-content">
          <PersonalInfo ref="personalInfoRef" />
        </div>
        <div v-show="currentComponent === 'userConfig'" class="tab-content">
          <UserConfig ref="userConfigRef" />
        </div>
        <div v-show="currentComponent === 'modifyPassword'" class="tab-content">
          <ModifyPassword ref="modifyPasswordRef" />
        </div>
      </div>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, nextTick, onActivated, ref, type Component } from 'vue';
import { useRoute } from 'vue-router';
import { Lock, Setting, User } from '@element-plus/icons-vue';
import ModifyPassword from '@/components/setting/ModifyPassword.vue';
import PersonalInfo from '@/components/setting/PersonalInfo.vue';
import UserConfig from '@/components/setting/UserConfig.vue';
import { useConfigStore } from '@/stores/config';

interface MenuItem {
  label: string;
  icon: Component;
  component: string;
}

interface TabExpose {
  init?: () => void;
}

const route = useRoute();
const configStore = useConfigStore();
const currentTab = ref(0);
const personalInfoRef = ref<TabExpose>();
const userConfigRef = ref<TabExpose>();
const modifyPasswordRef = ref<TabExpose>();
const menuItems = computed<MenuItem[]>(() => [
  { label: '个人资料', icon: User, component: 'personalInfo' },
  { label: '通用设置', icon: Setting, component: 'userConfig' },
  { label: '修改密码', icon: Lock, component: 'modifyPassword' }
]);
const currentTabItem = computed(() => menuItems.value[currentTab.value] || menuItems.value[0]);
const currentComponent = computed(() => currentTabItem.value?.component || '');
const refMap: Record<string, () => TabExpose | undefined> = {
  personalInfo: () => personalInfoRef.value,
  userConfig: () => userConfigRef.value,
  modifyPassword: () => modifyPasswordRef.value
};

const onClickTab = () => {
  const name = currentComponent.value;
  const comp = name ? refMap[name]?.() : undefined;
  comp?.init?.();
};

const switchTab = (index: number) => {
  currentTab.value = index;
  nextTick(() => onClickTab());
};

const init = () => {
  const tab = route.query.tab;
  if (tab != null) {
    currentTab.value = parseInt(String(tab), 10);
  }
  nextTick(() => onClickTab());
};
onActivated(() => {
  init();
});
</script>

<style lang="scss" scoped>
.setting-page {
  height: 100vh;
  background: #f5f6fa;

  .setting-aside {
    display: flex;
    flex-direction: column;
    height: 100%;
    border-right: 1px solid rgba(0, 0, 0, 0.08);
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
    background: white;

    &.fullscreen {
      width: 260px !important;
    }

    .aside-header {
      height: 60px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      padding: 0 20px;
      border-bottom: var(--im-border);

      .aside-title {
        font-size: var(--im-font-size-larger);
        font-weight: 600;
        color: #333;
      }
    }

    .nav-menu {
      flex: 1;
      overflow-y: auto;
      padding: 5px 0;

      .nav-item {
        display: flex;
        align-items: center;
        gap: 10px;
        height: 60px;
        margin: 0 3px;
        padding: 5px 12px;
        cursor: pointer;
        border-radius: 10px;
        white-space: nowrap;

        &:hover {
          background-color: var(--im-background-active);
        }

        &.active {
          background-color: var(--im-background-active-dark);
          color: var(--im-color-primary);

          .el-icon {
            color: var(--im-color-primary);
          }
        }

        .el-icon {
          font-size: 18px;
          width: 20px;
          text-align: center;
        }

        span {
          font-size: var(--im-font-size);
        }
      }
    }
  }

  .setting-main {
    display: flex;
    flex-direction: column;
    background: #fcfdff;
    overflow: hidden;

    .content-header {
      height: 60px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      padding: 0 20px;
      border-bottom: var(--im-border);
      background: white;

      .header-title {
        font-size: var(--im-font-size-larger);
        font-weight: 600;
        color: #333;
      }
    }

    .content-body {
      flex: 1;
      overflow-y: auto;
      background: #fafbfc;
      min-height: 0;
    }

    .tab-content {
      min-height: 100%;
    }
  }
}
</style>

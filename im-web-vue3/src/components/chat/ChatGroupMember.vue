<template>
  <div class="chat-group-member">
    <div class="member-card-container">
      <div class="header">
        <div class="header-content">
          <el-icon class="back-btn" @click="onBack()" :title="'返回'"><ArrowLeft /></el-icon>
          <h3 class="title">{{ '群成员' }}</h3>
          <span class="member-count">{{ `${groupMembers.length}人` }}</span>
        </div>
      </div>
      <div v-show="!group.quit" class="search-section">
        <div class="search-box">
          <el-input v-model="searchText" :placeholder="'搜索群成员'" clearable>
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div class="member-list">
        <VirtualScroller class="scroll-box" :items="showMembers">
          <template #default="{ item }">
            <GroupMemberItemComp :member="item" :group="group" :group-members="groupMembers" @click="onShowUserInfo($event, item)" />
          </template>
        </VirtualScroller>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { ArrowLeft, Search } from '@element-plus/icons-vue';
import VirtualScroller from '@/components/common/VirtualScroller.vue';
import GroupMemberItemComp from '@/components/group/GroupMemberItem.vue';
import { findUser } from '@/api/user';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import eventBus from '@/utils/eventBus';
const props = defineProps({
  group: {
    type: Object as () => GroupVO,
    required: true
  },
  groupMembers: {
    type: Array as () => GroupMemberVO[],
    default: () => []
  }
});
const emit = defineEmits(['back']);
const searchText = ref('');
const showMembers = computed(() => props.groupMembers.filter((m) => m.showNickName.includes(searchText.value)));

const onBack = () => {
  emit('back');
};

const onShowUserInfo = async (e: MouseEvent, member: GroupMemberVO) => {
  if (!member.userId) {
    return;
  }
  const user = await findUser(member.userId);
  eventBus.emit('openUserInfo', { user, pos: { x: e.clientX + 30, y: e.clientY } });
};
</script>
<style lang="scss" scoped>
.chat-group-member {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);

  .member-card-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 12px;
    border: 1px solid rgba(0, 0, 0, 0.05);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    box-sizing: border-box;
  }

  .header {
    padding: 16px 10px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.08);

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;

      .back-btn {
        font-size: 18px;
        color: var(--im-color-primary);
        cursor: pointer;
        border-radius: 50%;
        transition: all 0.3s ease;

        &:hover {
          background: var(--im-background-active);
          transform: translateX(-2px);
        }
      }

      .title {
        margin: 0;
        font-size: var(--im-font-size);
        font-weight: 600;
        color: var(--im-text-color);
        flex: 1;
      }

      .member-count {
        font-size: var(--im-font-size-small);
        color: var(--im-text-color-light);
        background: var(--im-background-active-dark);
        padding: 4px 8px;
        border-radius: 12px;
        font-weight: 500;
      }
    }
  }

  .search-section {
    padding: 16px 20px;
  }

  .member-list {
    flex: 1;
    padding: 0 10px 10px;
    overflow: hidden;

    .scroll-box {
      height: 100%;
    }
  }
}
</style>

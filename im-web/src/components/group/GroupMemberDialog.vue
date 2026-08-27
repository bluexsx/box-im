<template>
  <el-dialog v-model="show" :title="'群成员'" width="70%" :close-on-click-modal="false" @close="onClose">
    <div class="members-dialog-content">
      <div class="member-tools-bar">
        <div class="member-tools" @click="onInviteMember">
          <div class="tool-btn" :title="'邀请好友进群聊'">
            <el-icon><Plus /></el-icon>
          </div>
          <div class="tool-text">{{ '邀请' }}</div>
        </div>
        <div v-if="isOwner" class="member-tools" @click="onRemoveMember">
          <div class="tool-btn" :title="'选择成员移出群聊'">
            <el-icon><Minus /></el-icon>
          </div>
          <div class="tool-text">{{ '移除' }}</div>
        </div>
      </div>
      <div class="member-list-container" @scroll="onScroll">
        <div class="member-items">
          <div v-for="(member, idx) in showMembers" :key="member.userId">
            <GroupMemberItem
              v-if="idx < showMaxIdx"
              class="member-item"
              :group="group"
              :group-members="groupMembers"
              :member="member"
              type="card"
              :menu="false" />
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { Minus, Plus } from '@element-plus/icons-vue';
import { storeToRefs } from 'pinia';
import GroupMemberItem from './GroupMemberItem.vue';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import { useUserStore } from '@/stores/user';

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

const emit = defineEmits(['close', 'invite-member', 'remove-member']);

const userStore = useUserStore();
const { userInfo: mine } = storeToRefs(userStore);
const show = ref(false);
const showMaxIdx = ref(50);

const isOwner = computed(() => props.group.ownerId == mine.value.id);
const showMembers = computed(() => props.groupMembers.filter((m) => !m.quit));

const open = () => {
  show.value = true;
};

const close = () => {
  show.value = false;
};

const onClose = () => {
  emit('close');
};

const onScroll = (e: Event) => {
  const container = e.target as HTMLElement;
  if (container.scrollTop + container.clientHeight >= container.scrollHeight - 30) {
    loadMoreMembers();
  }
};

const onInviteMember = () => {
  emit('invite-member');
};

const onRemoveMember = () => {
  emit('remove-member');
};

const loadMoreMembers = () => {
  if (showMaxIdx.value < showMembers.value.length) {
    showMaxIdx.value += 50;
  }
};

defineExpose({ open, close });
</script>

<style lang="scss" scoped>
.members-dialog-content {
  .member-tools-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24px;
    margin-bottom: 20px;
    padding: 16px;
    background: var(--im-background-active);
    border-radius: 8px;

    .member-tools {
      display: flex;
      flex-direction: column;
      align-items: center;
      transition: all 0.3s ease;
      cursor: pointer;
      padding: 8px;
      border-radius: 12px;

      &:hover {
        background: rgba(0, 0, 0, 0.04);
        transform: translateY(-2px);
      }

      .tool-btn {
        width: 44px;
        height: 44px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--im-background-active);
        border: none;
        font-size: 20px;
        cursor: pointer;
        border-radius: 12px;
        transition: all 0.3s ease;
        color: var(--im-color-primary);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .icon {
          font-size: 20px;
        }

        &:hover {
          background: var(--im-color-primary-light-2);
          color: white;
          transform: scale(1.05);
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        }
      }

      .tool-text {
        font-size: 12px;
        text-align: center;
        margin-top: 8px;
        color: var(--im-text-color-secondary);
        font-weight: 500;
        white-space: nowrap;
      }
    }
  }

  .member-list-container {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 4px;
  }

  .member-items {
    padding: 10px;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 16px;

    .member-item {
      transition: all 0.3s ease;
      border-radius: 8px;
      padding: 6px;
      background: rgba(255, 255, 255, 0.6);
      border: 1px solid rgba(0, 0, 0, 0.04);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      &:hover {
        background: rgba(255, 255, 255, 0.9);
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
        border-color: var(--im-color-primary-light-3);
      }
    }
  }
}
</style>

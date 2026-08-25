<template>
  <div v-if="show">
    <div class="chat-group-readed-mask" @click.self="close()">
      <div class="chat-group-readed" :style="{ left: pos.x + 'px', top: pos.y + 'px' }" @click.prevent="">
        <div class="readed-header">
          <div class="header-title">
            <el-icon><View /></el-icon>
            <span>{{ '消息已读状态' }}</span>
          </div>
          <div class="close-btn" @click="close()">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        <div class="tabs-container">
          <Tabs :items="tabItems" :current="currentTab" @change="onTabChange" />
          <div class="tab-content">
            <div v-show="currentTab === 0" class="member-list">
              <div v-if="readedMembers.length === 0" class="empty-state">
                <el-icon><CircleCheck /></el-icon>
                <p>{{ '暂无已读成员' }}</p>
              </div>
              <VirtualScroller v-else class="scroll-box" :items="readedMembers">
                <template #default="{ item }">
                  <GroupMemberBar :member="item" :group="group" />
                </template>
              </VirtualScroller>
            </div>
            <div v-show="currentTab === 1" class="member-list">
              <div v-if="unreadMembers.length === 0" class="empty-state">
                <el-icon><Warning /></el-icon>
                <p>{{ '暂无未读成员' }}</p>
              </div>
              <VirtualScroller v-else class="scroll-box" :items="unreadMembers">
                <template #default="{ item }">
                  <GroupMemberBar :member="item" :group="group" />
                </template>
              </VirtualScroller>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { CircleCheck, Close, View, Warning } from '@element-plus/icons-vue';
import Tabs from '@/components/common/Tabs.vue';
import VirtualScroller from '@/components/common/VirtualScroller.vue';
import GroupMemberBar from '@/components/group/GroupMemberBar.vue';
import { findGroupReadedUsers } from '@/api/groupMessage';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import type { ChatMessage } from '@/types';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import { CONVERSATION_TYPE } from '@/utils/enums';
const props = defineProps({
  group: {
    type: Object as () => GroupVO,
    required: true
  },
  message: {
    type: Object as () => ChatMessage,
    required: true
  }
});
const chatStore = useChatStore();
const show = ref(false);
const pos = reactive({ x: 0, y: 0, arrowY: 0 });
const readedMembers = ref<GroupMemberVO[]>([]);
const unreadMembers = ref<GroupMemberVO[]>([]);
const currentTab = ref(0);
const groupMembers = computed(() => props.group.members || []);
const tabItems = computed(() => [`${'已读'}(${readedMembers.value.length})`, `${'未读'}(${unreadMembers.value.length})`]);

const close = () => {
  show.value = false;
};

const onTabChange = (index: number) => {
  currentTab.value = index;
};

const loadReadedUser = async () => {
  if (!props.message.id) {
    return;
  }
  const userIds = await findGroupReadedUsers(props.message.groupId!, props.message.id);
  readedMembers.value = [];
  unreadMembers.value = [];
  groupMembers.value.forEach((member) => {
    // 发送者和已退群的不显示
    if (member.userId == props.message.sendId || member.quit) {
      return;
    }
    // 区分已读还是未读
    if (userIds.find((userId) => member.userId == userId)) {
      readedMembers.value.push(member);
    } else {
      unreadMembers.value.push(member);
    }
  });
  // 更新已读人数
  const updateMessage = {
    localId: props.message.localId,
    readedCount: readedMembers.value.length
  };
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, props.message.groupId!);
  if (chatStore.isActive(convKey)) {
    await chatStore.updateMessage(convKey, updateMessage as ChatMessage);
  }
};

const open = (rect: DOMRect) => {
  show.value = true;
  pos.arrowY = 200;
  // 计算窗口位置
  if (props.message.selfSend) {
    // 自己发的消息弹出在消息的左边
    pos.x = rect.left - 300;
  } else {
    // 别人发的消息弹窗在消息右边
    pos.x = rect.right + 20;
  }
  pos.y = rect.top + rect.height / 2 - 265;
  // 防止窗口溢出
  if (pos.y < 0) {
    pos.arrowY += pos.y;
    pos.y = 0;
  }
  loadReadedUser();
};
defineExpose({ open, close });
</script>
<style lang="scss">
.chat-group-readed-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
}

.chat-group-readed {
  position: fixed;
  width: 280px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: slideIn 0.3s ease-out;

  .readed-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background: linear-gradient(135deg, rgba(var(--im-color-primary-rgb), 0.05) 0%, rgba(255, 255, 255, 0.8) 100%);
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      font-weight: 600;
      color: var(--im-text-color-primary);

      .el-icon {
        color: var(--im-color-primary);
        font-size: 16px;
      }
    }

    .close-btn {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      cursor: pointer;
      transition: all 0.2s ease;
      color: var(--im-text-color-light);

      &:hover {
        background: rgba(0, 0, 0, 0.08);
        color: var(--im-text-color-primary);
        transform: scale(1.1);
      }

      .el-icon {
        font-size: 14px;
      }
    }
  }

  .tabs-container {
    .tab-content {
      padding: 0;
      background: white;

      .member-list {
        position: relative;
        min-height: 300px;

        .empty-state {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 200px;
          color: var(--im-text-color-light);
          text-align: center;

          .el-icon {
            font-size: 32px;
            margin-bottom: 12px;
            opacity: 0.6;
          }

          p {
            margin: 0;
            font-size: 14px;
            font-weight: 500;
          }
        }

        .scroll-box {
          height: 300px;
          padding: 8px 0;
        }
      }
    }
  }
}

// 弹窗进入动画

@keyframes slideIn {
  0% {
    opacity: 0;
    transform: scale(0.9) translateY(-10px);
  }

  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>

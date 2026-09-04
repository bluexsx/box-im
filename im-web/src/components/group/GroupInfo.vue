<template>
  <div v-if="show" ref="rootRef" class="group-info" :style="{ left: pos.x + 'px', top: pos.y + 'px' }" @click.stop>
    <div class="container">
      <div class="avatar">
        <HeadImage :name="group.name" :url="group.headImageThumb" :size="60" :id="group.id" radius="15%" @click="showFullImage()" />
      </div>
      <div class="info-card">
        <div class="header">
          <div class="name">{{ group.name }}</div>
          <el-tag v-if="group.isBanned" type="danger">{{ '已封禁' }}</el-tag>
        </div>
        <div class="info-item">{{ '群主' }}: {{ ownerName }}</div>
      </div>
    </div>
    <!-- 成员预览区域 -->
    <div class="member-preview">
      <div class="preview-header">
        <span class="preview-title">{{ '群成员' }}</span>
        <span class="preview-count">{{ `${memberSize}人` }}</span>
      </div>
      <div class="member-avatars">
        <div v-for="member in previewMembers" :key="member.userId" class="member-avatar" :title="member.showNickName">
          <HeadImage :id="member.userId" :name="member.showNickName" :url="member.headImage" :size="32" :is-show-user-info="true" />
        </div>
        <div v-if="memberSize > maxPreviewCount" class="more-members">
          <span class="more-count">+{{ memberSize - maxPreviewCount }}</span>
        </div>
      </div>
    </div>
    <div v-if="isInGroup" class="btn-group">
      <el-button type="primary" :icon="Position" @click="onSendMessage()">
        {{ '发消息' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Position } from '@element-plus/icons-vue';
import HeadImage from '@/components/common/HeadImage.vue';
import { listGroupMembers } from '@/api/group';
import type { GroupMemberVO, GroupVO } from '@/api/group/types';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import { useGroupStore } from '@/stores/group';
import { CONVERSATION_TYPE } from '@/utils/enums';
import eventBus from '@/utils/eventBus';

const router = useRouter();
const chatStore = useChatStore();
const groupStore = useGroupStore();
const show = ref(false);
const group = ref<GroupVO>({ id: 0, name: '', showGroupName: '' });
const groupMembers = ref<GroupMemberVO[]>([]);
const pos = reactive({ x: 0, y: 0 });
const rootRef = ref<HTMLElement>();
const maxPreviewCount = 6; // 最多显示6个成员头像

const isInGroup = computed(() => groupStore.isGroup(group.value.id));

const ownerName = computed(() => {
  const member = groupMembers.value.find((m) => m.userId == group.value.ownerId);
  return member?.showNickName;
});

const memberSize = computed(() => groupMembers.value.filter((m) => !m.quit).length);
// 过滤掉已退群的成员，并限制显示数量
const previewMembers = computed(() => groupMembers.value.filter((m) => !m.quit).slice(0, maxPreviewCount));

const open = (g: GroupVO, p: { x: number; y: number }) => {
  show.value = true;
  group.value = g;
  groupMembers.value = [];
  pos.x = p.x;
  pos.y = p.y;
  nextTick(() => {
    const el = rootRef.value;
    if (!el) return;
    const pad = 8;
    const w = document.documentElement.clientWidth;
    const h = document.documentElement.clientHeight;
    pos.x = Math.max(pad, Math.min(p.x, w - el.offsetWidth - pad));
    pos.y = Math.max(pad, Math.min(p.y, h - el.offsetHeight - pad));
  });
  loadGroupMembers();
};

const close = () => {
  show.value = false;
};

const onSendMessage = async () => {
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, group.value.id);
  await chatStore.openChat({
    type: CONVERSATION_TYPE.GROUP,
    targetId: group.value.id,
    showName: group.value.showGroupName,
    headImage: group.value.headImageThumb,
    isDnd: group.value.isDnd,
    isTop: group.value.isTop
  });
  await chatStore.moveTop(convKey);
  chatStore.setActive(convKey);
  if (router.currentRoute.value.path != '/home/chat') {
    await router.push('/home/chat');
  }
  show.value = false;
};

const showFullImage = () => {
  if (group.value.headImage) {
    eventBus.emit('openFullImage', group.value.headImage);
  }
};

const loadGroupMembers = () => {
  listGroupMembers(group.value.id).then((members) => {
    groupMembers.value = members;
  });
};

defineExpose({ open, close });
</script>

<style lang="scss" scoped>
.group-info {
  position: fixed;
  width: 320px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 9999;
  overflow: hidden;

  .container {
    padding: 20px;
    display: flex;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 20px;

    .avatar {
      position: relative;
      flex-shrink: 0;
    }

    .info-card {
      flex: 1;
      min-width: 0;

      .header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        flex-wrap: wrap;

        .name {
          font-size: 18px;
          font-weight: 700;
          color: var(--im-text-color-primary);
          line-height: 1.2;
        }
      }

      .info-item {
        font-size: 14px;
        color: var(--im-text-color);
        margin-bottom: 8px;
        word-break: break-word;
        line-height: 1.5;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  .member-preview {
    padding: 0 20px 20px 20px;

    .preview-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .preview-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--im-text-color-primary);
      }

      .preview-count {
        font-size: 12px;
        color: var(--im-text-color-light);
        background: var(--im-background-active);
        padding: 2px 8px;
        border-radius: 10px;
      }
    }

    .member-avatars {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .member-avatar {
        position: relative;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
          transform: scale(1.1);
          z-index: 10;
        }
      }

      .more-members {
        width: 32px;
        height: 32px;
        background: var(--im-background-active);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2px solid rgba(255, 255, 255, 0.8);
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
          background: var(--im-color-primary-light-2);
          transform: scale(1.1);

          .more-count {
            color: white;
          }
        }

        .more-count {
          font-size: 11px;
          font-weight: 600;
          color: var(--im-text-color-light);
        }
      }
    }
  }

  .btn-group {
    padding: 0 20px 20px 20px;
    text-align: center;

    .el-button {
      width: 100%;
      padding: 10px 16px;
      border-radius: 8px;
      font-weight: 500;
      transition: all 0.3s ease;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      }
    }
  }
}
</style>

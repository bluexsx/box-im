<template>
  <div v-if="show" class="user-info" :style="{ left: pos.x + 'px', top: pos.y + 'px' }" @click.stop>
    <div class="user-card">
      <div class="user-header">
        <div class="avatar-section" @click="showFullImage">
          <HeadImage :name="user.nickName" :url="user.headImageThumb" :size="70" :online="user.online" :is-show-user-info="false" radius="50%" />
        </div>
        <div class="user-basic-info">
          <div class="user-name-row">
            <span class="nick-name">{{ user.nickName }}</span>
            <el-icon v-if="user.sex == 0" class="gender-icon male"><Male /></el-icon>
            <el-icon v-if="user.sex == 1" class="gender-icon female"><Female /></el-icon>
          </div>
          <div class="user-id">
            <span>ID: {{ user.userName }}</span>
            <el-icon class="copy-btn" :title="'复制用户ID'" @click="copyUserName"><CopyDocument /></el-icon>
          </div>
        </div>
      </div>
      <div class="signature-section">
        <div class="signature-label">{{ '个性签名' }}</div>
        <div class="signature-content">
          {{ user.signature ? user.signature : '这个人很懒，什么都没有留下' }}
        </div>
      </div>
      <div class="action-section">
        <el-button v-if="isFriend" type="primary" class="action-btn" :icon="Position" @click="onSendMessage">
          {{ '发消息' }}
        </el-button>
        <el-button v-else-if="user.id != mine.id" type="success" class="action-btn" :icon="Plus" @click="onAddFriend">
          {{ '加为好友' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { CopyDocument, Female, Male, Plus, Position } from '@element-plus/icons-vue';
import { storeToRefs } from 'pinia';
import HeadImage from '@/components/common/HeadImage.vue';
import { addFriend } from '@/api/friend';
import type { UserVO } from '@/api/user/types';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import { useFriendStore } from '@/stores/friend';
import { useUserStore } from '@/stores/user';
import { CONVERSATION_TYPE } from '@/utils/enums';
import eventBus from '@/utils/eventBus';
import type { Pos } from '@/utils/eventBus';

const router = useRouter();
const userStore = useUserStore();
const friendStore = useFriendStore();
const chatStore = useChatStore();
const { userInfo: mine } = storeToRefs(userStore);
const show = ref(false);
const user = ref<UserVO>({ id: 0, nickName: '' });
const pos = ref<Pos>({ x: 0, y: 0 });
const isFriend = computed(() => friendStore.isFriend(user.value.id));
const friendInfo = computed(() => friendStore.findFriend(user.value.id));

const open = (u: UserVO, position: Pos) => {
  show.value = true;
  user.value = u;
  const w = document.documentElement.clientWidth;
  const h = document.documentElement.clientHeight;
  pos.value.x = Math.min(position.x, w - 350);
  pos.value.y = Math.min(position.y, h - 200);
};

const close = () => {
  show.value = false;
};

const onSendMessage = async () => {
  const u = user.value;
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, u.id);
  await chatStore.openChat({
    type: CONVERSATION_TYPE.PRIVATE,
    targetId: u.id,
    showName: u.nickName,
    headImage: u.headImageThumb,
    isDnd: friendInfo.value?.isDnd,
    isTop: friendInfo.value?.isTop
  });
  await chatStore.moveTop(convKey);
  chatStore.setActive(convKey);
  if (router.currentRoute.value.path != '/home/chat') {
    await router.push('/home/chat');
  }
  show.value = false;
};

const onAddFriend = async () => {
  await addFriend(user.value.id);
  ElMessage.success(`添加成功，'${user.value.nickName}'已成为您的好友`);
  await friendStore.addFriend({
    id: user.value.id,
    nickName: user.value.nickName,
    headImage: user.value.headImageThumb,
    online: user.value.online,
    deleted: false,
    version: 0
  });
};

const showFullImage = () => {
  if (user.value.headImage) {
    eventBus.emit('openFullImage', user.value.headImage);
  }
};

const copyUserName = () => {
  const userName = user.value.userName;
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard
      .writeText(userName!)
      .then(() => {
        ElMessage.success(`内容'${userName}'已复制到剪贴板`);
      })
      .catch(() => {
        ElMessage.error('复制失败，请手动复制');
      });
  } else {
    ElMessage.error('复制失败，请手动复制');
  }
};
defineExpose({ open, close });
</script>

<style lang="scss">
.user-info {
  position: fixed;
  width: 320px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 9999;
  overflow: hidden;

  .user-card {
    padding: 20px;

    .user-header {
      position: relative;
      display: flex;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 20px;

      .avatar-section {
        position: relative;
        cursor: pointer;
      }

      .user-basic-info {
        display: flex;
        flex-direction: column;
        justify-content: center;
        flex: 1;
        min-height: 70px;
        overflow: hidden;

        .user-name-row {
          gap: 8px;
          margin-bottom: 8px;

          .nick-name {
            font-size: var(--im-font-size-large);
            font-weight: 700;
            color: var(--im-text-color-primary);
            line-height: 1.2;
            word-break: break-all;
          }

          .gender-icon {
            font-size: 16px;
            border-radius: 50%;
            padding: 2px;
            margin-left: 3px;

            &.male {
              color: #1890ff;
              background: rgba(24, 144, 255, 0.1);
            }

            &.female {
              color: #f5222d;
              background: rgba(245, 34, 45, 0.1);
            }
          }
        }

        .user-id {
          font-size: var(--im-font-size);
          color: var(--im-text-color);
          display: flex;
          align-items: center;
          word-break: break-all;
          gap: 6px;

          .copy-btn {
            font-size: 12px;
            color: var(--im-color-primary);
            cursor: pointer;
            opacity: 0.7;

            &:hover {
              opacity: 1;
            }
          }
        }
      }
    }

    .signature-section {
      margin-bottom: 20px;
      padding: 16px;
      background: rgba(0, 0, 0, 0.02);
      border-radius: 12px;

      .signature-label {
        font-size: 12px;
        color: var(--im-text-color-light);
        margin-bottom: 8px;
      }

      .signature-content {
        font-size: 14px;
        color: var(--im-text-color);
        line-height: 1.5;
        word-break: break-word;
        white-space: pre-wrap;
        max-height: 150px;
        overflow-y: auto;
      }
    }

    .action-section {
      text-align: center;

      .action-btn {
        width: 100%;
        padding: 10px 16px;
        border-radius: 8px;
        font-weight: 500;
      }
    }
  }
}
</style>

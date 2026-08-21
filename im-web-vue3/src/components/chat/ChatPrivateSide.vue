<template>
  <div class="chat-private-side">
    <div v-if="userInfo" class="friend-info-section">
      <div class="friend-card">
        <div class="friend-avatar">
          <HeadImage :size="80" :url="userInfo.headImageThumb" radius="50%" :name="userInfo.nickName" />
        </div>
        <div class="friend-details">
          <div class="friend-name-row">
            <h4 class="friend-name">{{ userInfo.nickName }}</h4>
            <div class="gender-icons">
              <el-icon v-if="userInfo.sex == 0" class="gender-icon male"><Male /></el-icon>
              <el-icon v-if="userInfo.sex == 1" class="gender-icon female"><Female /></el-icon>
            </div>
          </div>
          <div class="friend-id-row">
            <span class="friend-id">ID: {{ userInfo.userName }}</span>
            <el-icon class="copy-btn" title="复制用户名" @click="copyUserName"><CopyDocument /></el-icon>
          </div>
        </div>
      </div>
      <div class="personal-setting">
        <div class="switch-item">
          <div class="label">
            <span>消息免打扰</span>
          </div>
          <el-switch v-model="isDnd" @change="onDndChange" />
        </div>
        <div class="switch-item">
          <div class="label">
            <span>会话置顶</span>
          </div>
          <el-switch v-model="isTop" @change="onTopChange" />
        </div>
      </div>
      <div class="action-menu">
        <div class="action-item" @click="onSearchChatHistory">
          <div class="label">
            <span>查找聊天记录</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      <div class="btn-group">
        <div class="text-btn" @click="onCleanMessage">清空聊天记录</div>
        <div v-if="isFriend" class="text-btn" @click="onDeleteFriend">删除好友</div>
      </div>
    </div>
    <CleanMessageConfirm ref="cleanMessageConfirmRef" />
  </div>
</template>
<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowRight, Bell, CopyDocument, Female, Male, Top } from '@element-plus/icons-vue';
import { deleteFriend, setFriendDnd, setFriendTop } from '@/api/friend';
import { deletePrivateChat } from '@/api/privateMessage';
import type { UserVO } from '@/api/user/types';
import CleanMessageConfirm from '@/components/common/CleanMessageConfirm.vue';
import HeadImage from '@/components/common/HeadImage.vue';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import type { Conversation } from '@/types';
import { useFriendStore } from '@/stores/friend';
import { CONVERSATION_TYPE } from '@/utils/enums';

const props = defineProps({
  userInfo: {
    type: Object as () => UserVO,
    required: false
  },
  conversation: {
    type: Object as () => Conversation,
    required: false
  }
});
const emit = defineEmits(['show-chat-history', 'close']);
const friendStore = useFriendStore();
const chatStore = useChatStore();
const isDnd = ref(false);
const isTop = ref(false);
const cleanMessageConfirmRef = ref<InstanceType<typeof CleanMessageConfirm>>();
const isFriend = computed(() => (props.userInfo?.id ? friendStore.isFriend(props.userInfo.id) : false));

const onDndChange = async (value: boolean) => {
  if (!props.userInfo?.id) {
    return;
  }
  try {
    await setFriendDnd({ friendId: props.userInfo.id, isDnd: value });
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, props.userInfo.id);
    await friendStore.setDnd(props.userInfo.id, value);
    await chatStore.setDnd(convKey, value);
    ElMessage.success(value ? '已开启免打扰' : '已关闭免打扰');
  } catch {
    ElMessage.error('操作失败');
    isDnd.value = !value;
  }
};

const onTopChange = async (value: boolean) => {
  if (!props.userInfo?.id) {
    return;
  }
  try {
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, props.userInfo.id);
    await setFriendTop({ friendId: props.userInfo.id, isTop: value });
    if (friendStore.isFriend(props.userInfo.id)) {
      await friendStore.setTop(props.userInfo.id, value);
    }
    await chatStore.setTop(convKey, value);
    ElMessage.success(value ? '已置顶' : '已取消置顶');
  } catch {
    ElMessage.error('操作失败');
    isTop.value = !value;
  }
};

const onCleanMessage = async () => {
  if (!props.userInfo?.id || !props.conversation) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认清空与'${props.userInfo.nickName}'的聊天记录吗?`, '清空聊天记录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deletePrivateChat({ chatId: props.userInfo.id });
    await chatStore.cleanMessage(props.conversation.key);
    ElMessage.success(`与'${props.userInfo.nickName}'的聊天记录已清空`);
  } catch {}
};

const onDeleteFriend = async () => {
  if (!props.userInfo?.id) {
    return;
  }
  try {
    const isCleanMessage = await cleanMessageConfirmRef.value!.open({
      title: '删除好友',
      message: `确认删除'${props.userInfo.nickName}'吗?`
    });
    const userId = props.userInfo.id;
    await deleteFriend(userId);
    await friendStore.removeFriend(userId);
    if (isCleanMessage) {
      await deletePrivateChat({ chatId: userId });
      const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, userId);
      await chatStore.remove(convKey);
    }
    ElMessage.success('删除好友成功');
    emit('close');
  } catch {}
};

const onSearchChatHistory = () => {
  emit('show-chat-history');
};

const copyUserName = () => {
  const userName = props.userInfo?.userName;
  if (!userName) {
    return;
  }
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard
      .writeText(userName)
      .then(() => ElMessage.success(`已复制: ${userName}`))
      .catch(() => ElMessage.error('复制失败'));
  } else {
    ElMessage.error('复制失败');
  }
};

watch(
  () => props.conversation,
  (conv) => {
    isDnd.value = !!conv?.isDnd;
    isTop.value = !!conv?.isTop;
  },
  { immediate: true }
);
</script>
<style scoped lang="scss">
.chat-private-side {
  padding: 10px;
  position: relative;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;

  %card-style {
    padding: 5px 16px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 10px;
    border: 1px solid rgba(0, 0, 0, 0.05);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .friend-info-section {
    .friend-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
      margin-bottom: 12px;

      @extend %card-style;
      .friend-avatar {
        flex-shrink: 0;
      }

      .friend-details {
        flex: 1;

        .friend-name-row {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .friend-name {
            margin: 0;
            font-size: var(--im-font-size-larger);
            font-weight: 600;
            color: var(--im-text-color);
          }

          .gender-icons {
            .gender-icon {
              font-size: 16px;
              border-radius: 50%;
              padding: 2px;
              transition: all 0.3s ease;

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
        }

        .friend-id-row {
          display: flex;
          align-items: center;
          gap: 8px;

          .friend-id {
            margin: 0;
            font-size: var(--im-font-size);
            color: var(--im-text-color-light);
            text-align: left;
          }

          .copy-btn {
            font-size: 22px;
            color: var(--im-color-primary);
            cursor: pointer;
            padding: 4px;
            border-radius: 4px;
            transition: all 0.3s ease;
            opacity: 0.8;

            &:hover {
              background: rgba(var(--im-color-primary-rgb), 0.1);
              opacity: 1;
            }
          }
        }
      }
    }

    .personal-setting {
      margin-bottom: 12px;

      @extend %card-style;
      .switch-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 6px 0;
        border-bottom: 1px solid rgba(0, 0, 0, 0.08);
        transition: all 0.2s ease;

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          background: var(--im-background-active);
          border-radius: 8px;
          padding: 6px 8px;
          margin: 0 -8px;
        }

        .label {
          display: flex;
          align-items: center;
          gap: 10px;
          font-size: 14px;
          font-weight: 500;
          color: var(--im-text-color-primary);

          .el-icon {
            color: var(--im-color-primary);
            font-size: 16px;
            width: 18px;
            text-align: center;
          }
        }
      }
    }

    .action-menu {
      margin-bottom: 12px;

      @extend %card-style;
      .action-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 0;
        border-bottom: 1px solid rgba(0, 0, 0, 0.08);
        cursor: pointer;
        transition: all 0.2s ease;

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          background: var(--im-background-active);
          border-radius: 8px;
          padding: 12px 8px;
          margin: 0 -8px;
        }

        .label {
          display: flex;
          align-items: center;
          font-size: 14px;
          font-weight: 500;
          color: var(--im-text-color-primary);
        }

        .el-icon {
          color: var(--im-text-color-light);
          font-size: 14px;
        }
      }
    }

    .btn-group {
      text-align: center;

      @extend %card-style;
      .text-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        padding: 12px 16px;
        font-size: var(--im-font-size);
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s ease;
        border-radius: 6px;
        color: var(--im-color-danger);
        background: transparent;
        border: none;
        box-sizing: border-box;
        border-bottom: 1px solid rgba(0, 0, 0, 0.08);

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          background: var(--im-background-active);
        }
      }
    }
  }
}
</style>

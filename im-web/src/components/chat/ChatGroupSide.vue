<template>
  <div class="chat-group-side">
    <ChatGroupMember v-if="showAllMembers && !group.quit" :group="group" :group-members="showMembers" @back="showAllMembers = false" />
    <div v-else>
      <div v-if="!group.quit" class="member-area">
        <div class="member-header">
          <div class="member-title">群成员</div>
          <div class="more-member-btn" @click="onShowMoreMember">查看全部 {{ showMembers.length }} 人</div>
        </div>
        <div class="member-items">
          <div v-for="member in showGridMembers" :key="member.userId">
            <GroupMemberItemComp class="member-item" :group="group" :group-members="groupMembers" :member="member" type="card" />
          </div>
          <div class="member-tools">
            <div class="tool-btn" title="邀请好友进群聊" @click="onInvite">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="tool-text">邀请</div>
            <GroupMemberInvite ref="groupMemberInviteRef" :group-id="group.id" :members="groupMembers" @reload="emit('reload')" />
          </div>
          <div v-if="isOwner" class="member-tools">
            <div class="tool-btn" title="选择成员移出群聊" @click="onRemove">
              <el-icon><Minus /></el-icon>
            </div>
            <div class="tool-text">移除</div>
            <GroupMemberSelector ref="removeSelectorRef" title="选择成员进行移除" :group="group" @complete="onRemoveComplete" />
          </div>
        </div>
      </div>
      <div class="switch-setting">
        <div class="switch-item">
          <div class="label">
            <span>消息免打扰</span>
          </div>
          <el-switch v-model="group.isDnd" @change="onDndChange" />
        </div>
        <div class="switch-item">
          <div class="label">
            <span>会话置顶</span>
          </div>
          <el-switch v-model="group.isTop" @change="onTopChange" />
        </div>
      </div>
      <div class="group-info-section">
        <div class="info-item">
          <div class="info-label">
            <span>群聊名称</span>
          </div>
          <div class="info-content">
            <el-input
              v-if="isEditingGroupName"
              ref="groupNameInputRef"
              v-model="groupNameValue"
              placeholder="请输入群聊名称"
              maxlength="20"
              show-word-limit
              @blur="saveGroupName"
              @keyup.enter="saveGroupName" />
            <div v-else class="info-display" @click="startEditGroupName">
              <span v-if="group.name">{{ group.name }}</span>
              <span v-else class="info-placeholder">点击设置群聊名称</span>
            </div>
          </div>
        </div>
        <div class="info-item">
          <div class="info-label">
            <span>备注</span>
          </div>
          <div class="info-content">
            <el-input
              v-if="isEditingRemark"
              ref="remarkInputRef"
              v-model="remarkValue"
              maxlength="32"
              show-word-limit
              @blur="saveRemark"
              @keyup.enter="saveRemark" />
            <div v-else class="info-display" @click="startEditRemark">
              <span v-if="group.remarkGroupName">{{ group.remarkGroupName }}</span>
              <span v-else class="info-placeholder">点击设置群聊备注</span>
            </div>
          </div>
        </div>
        <div class="info-item">
          <div class="info-label">
            <span>我在本群的昵称</span>
          </div>
          <div class="info-content">
            <el-input
              v-if="isEditingNickName"
              ref="nickNameInputRef"
              v-model="nickNameValue"
              placeholder="请输入昵称"
              maxlength="20"
              show-word-limit
              @blur="saveNickName"
              @keyup.enter="saveNickName" />
            <div v-else class="info-display" @click="startEditNickName">
              <span v-if="group.remarkNickName">{{ group.remarkNickName }}</span>
              <span v-else class="info-placeholder">点击设置本群昵称</span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="group.notice || isOwner" class="notice-section">
        <div class="notice-header">
          <span>群公告</span>
        </div>
        <div class="notice-content">
          <div class="notice-display" @click="openNoticeDialog">
            <div v-if="group.notice" class="notice-text">{{ group.notice }}</div>
            <div v-else class="notice-placeholder">点击设置群公告</div>
          </div>
        </div>
      </div>
      <el-dialog
        v-model="noticeDialogVisible"
        title="编辑群公告"
        width="500px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        draggable
        destroy-on-close>
        <div class="notice-dialog-content">
          <el-input
            ref="noticeDialogInputRef"
            v-model="noticeValue"
            type="textarea"
            placeholder="请输入群公告"
            maxlength="1024"
            show-word-limit
            :rows="6" />
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="cancelNoticeEdit">取消</el-button>
            <el-button type="primary" @click="saveNotice">保存</el-button>
          </div>
        </template>
      </el-dialog>
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
        <div v-if="!isOwner && !group.quit" class="text-btn" @click="onQuit">退出群聊</div>
        <div v-if="isOwner && !group.quit" class="text-btn" @click="onDissolve">解散群聊</div>
      </div>
    </div>
    <CleanMessageConfirm ref="cleanMessageConfirmRef" />
  </div>
</template>
<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowRight, Minus, Plus } from '@element-plus/icons-vue';
import { deleteGroup, modifyGroup, quitGroup, removeGroupMembers, setGroupDnd, setGroupTop } from '@/api/group';
import type { GroupMemberVO, GroupVO } from '@/api/group/types';
import { deleteGroupChat } from '@/api/groupMessage';
import ChatGroupMember from '@/components/chat/ChatGroupMember.vue';
import CleanMessageConfirm from '@/components/common/CleanMessageConfirm.vue';
import GroupMemberInvite from '@/components/group/GroupMemberInvite.vue';
import GroupMemberItemComp from '@/components/group/GroupMemberItem.vue';
import GroupMemberSelector from '@/components/group/GroupMemberSelector.vue';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import { useConfigStore } from '@/stores/config';
import { useGroupStore } from '@/stores/group';
import { useUserStore } from '@/stores/user';
import type { Conversation } from '@/types';
import { CONVERSATION_TYPE } from '@/utils/enums';
const props = defineProps({
  groupId: {
    type: Number,
    required: true
  },
  groupMembers: {
    type: Array as () => GroupMemberVO[],
    default: () => []
  },
  conversation: {
    type: Object as () => Conversation,
    required: false
  }
});
const emit = defineEmits(['reload', 'show-chat-history', 'close']);
const userStore = useUserStore();
const groupStore = useGroupStore();
const chatStore = useChatStore();
const configStore = useConfigStore();
const { userInfo: mine } = storeToRefs(userStore);
const { fullScreen } = storeToRefs(configStore);
const showAllMembers = ref(false);
const group = ref<GroupVO>({ id: 0, name: '', showGroupName: '' });
const isEditingGroupName = ref(false);
const isEditingRemark = ref(false);
const isEditingNickName = ref(false);
const noticeDialogVisible = ref(false);
const groupNameValue = ref('');
const noticeValue = ref('');
const remarkValue = ref('');
const nickNameValue = ref('');
const groupNameInputRef = ref();
const remarkInputRef = ref();
const nickNameInputRef = ref();
const noticeDialogInputRef = ref();
const cleanMessageConfirmRef = ref<InstanceType<typeof CleanMessageConfirm>>();
const groupMemberInviteRef = ref<InstanceType<typeof GroupMemberInvite>>();
const removeSelectorRef = ref<InstanceType<typeof GroupMemberSelector>>();
const isOwner = computed(() => group.value.ownerId == mine.value.id);
const showMembers = computed(() => props.groupMembers.filter((m) => !m.quit));

const showMaxIdx = computed(() => {
  let idx = fullScreen.value ? 12 : 9;
  idx -= 1;
  if (isOwner.value) {
    idx -= 1;
  }
  return idx;
});

const showGridMembers = computed(() => showMembers.value.slice(0, showMaxIdx.value));

const onDndChange = async (value: boolean) => {
  try {
    await setGroupDnd({ groupId: props.groupId, isDnd: value });
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, props.groupId);
    await groupStore.setDnd(props.groupId, value);
    await chatStore.setDnd(convKey, value);
    ElMessage.success(value ? '已开启免打扰' : '已关闭免打扰');
  } catch {
    ElMessage.error('操作失败');
    group.value.isDnd = !value;
  }
};

const onTopChange = async (value: boolean) => {
  try {
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, props.groupId);
    await setGroupTop({ groupId: props.groupId, isTop: value });
    await groupStore.setTop(props.groupId, value);
    await chatStore.setTop(convKey, value);
    ElMessage.success(value ? '已置顶' : '已取消置顶');
  } catch {
    ElMessage.error('操作失败');
    group.value.isTop = !value;
  }
};

const onInvite = () => {
  groupMemberInviteRef.value?.open();
};

const onRemove = () => {
  const hideIds = [group.value.ownerId!, mine.value.id];
  removeSelectorRef.value?.open(50, [], [], hideIds);
};

const onRemoveComplete = (members: GroupMemberVO[]) => {
  const userIds = members.map((m) => m.userId);
  removeGroupMembers({
    groupId: group.value.id,
    userIds
  }).then(() => {
    emit('reload');
    ElMessage.success(`已移除${userIds.length}位成员`);
  });
};

const onShowMoreMember = () => {
  showAllMembers.value = true;
};

const onQuit = async () => {
  try {
    const isCleanMessage = await cleanMessageConfirmRef.value!.open({
      title: '确认退出?',
      message: `确认退出'${group.value.showGroupName}'吗？`
    });
    await quitGroup(group.value.id);
    await groupStore.removeGroup(group.value.id);
    if (isCleanMessage) {
      await deleteGroupChat({ chatId: group.value.id });
      const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, group.value.id);
      await chatStore.remove(convKey);
    }
    ElMessage.success(`您已退出'${group.value.name}'`);
    emit('close');
  } catch {}
};

const onDissolve = async () => {
  try {
    await ElMessageBox.confirm(`确认解散'${group.value.name}'吗？`, '确认解散?', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteGroup(group.value.id);
    await groupStore.removeGroup(group.value.id);
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, group.value.id);
    await chatStore.remove(convKey);
    ElMessage.success(`群聊'${group.value.name}'已解散`);
    emit('close');
  } catch {}
};

const startEditGroupName = () => {
  if (!isOwner.value) {
    return;
  }
  isEditingGroupName.value = true;
  groupNameValue.value = group.value.name;
  nextTick(() => {
    groupNameInputRef.value?.focus();
  });
};

const saveGroupName = () => {
  const newName = groupNameValue.value.trim();
  if (newName === group.value.name) {
    isEditingGroupName.value = false;
    return;
  }
  group.value.name = newName;
  void saveGroupInfo();
};

const openNoticeDialog = () => {
  if (!isOwner.value) {
    return;
  }
  noticeValue.value = group.value.notice || '';
  noticeDialogVisible.value = true;
  nextTick(() => {
    noticeDialogInputRef.value?.focus();
  });
};

const saveNotice = () => {
  const newNotice = noticeValue.value.trim();
  if (newNotice === (group.value.notice || '')) {
    noticeDialogVisible.value = false;
    return;
  }
  group.value.notice = newNotice;
  void saveGroupInfo();
};

const cancelNoticeEdit = () => {
  noticeDialogVisible.value = false;
  noticeValue.value = group.value.notice || '';
};

const startEditRemark = () => {
  isEditingRemark.value = true;
  remarkValue.value = group.value.remarkGroupName || '';
  nextTick(() => {
    remarkInputRef.value?.focus();
  });
};

const saveRemark = () => {
  const newRemark = remarkValue.value.trim();
  if (newRemark === (group.value.remarkGroupName || '')) {
    isEditingRemark.value = false;
    return;
  }
  group.value.remarkGroupName = newRemark;
  void saveGroupInfo();
};

const startEditNickName = () => {
  isEditingNickName.value = true;
  nickNameValue.value = group.value.remarkNickName || '';
  nextTick(() => {
    nickNameInputRef.value?.focus();
  });
};

const saveNickName = () => {
  const newNickName = nickNameValue.value.trim();
  if (newNickName === (group.value.remarkNickName || '')) {
    isEditingNickName.value = false;
    return;
  }
  group.value.remarkNickName = newNickName;
  void saveGroupInfo();
};

const saveGroupInfo = async () => {
  try {
    const updatedGroup = await modifyGroup(group.value);
    await chatStore.updateFromGroup(updatedGroup);
    await groupStore.updateGroup(updatedGroup);
    group.value = JSON.parse(JSON.stringify(updatedGroup));
    ElMessage.success('修改成功');
    if (noticeDialogVisible.value) {
      noticeDialogVisible.value = false;
    }
  } finally {
    isEditingGroupName.value = false;
    isEditingRemark.value = false;
    isEditingNickName.value = false;
  }
};

const onCleanMessage = async () => {
  if (!props.conversation) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认清空与'${group.value.name}'的聊天记录吗?`, '清空聊天记录', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteGroupChat({ chatId: group.value.id });
    await chatStore.cleanMessage(props.conversation.key);
    ElMessage.success(`与'${group.value.name}'的聊天记录已清空`);
  } catch {}
};

const onSearchChatHistory = () => {
  emit('show-chat-history');
};
onMounted(() => {
  const sourceGroup = groupStore.findGroup(props.groupId);
  if (sourceGroup) {
    group.value = JSON.parse(JSON.stringify(sourceGroup));
  }
});
</script>
<style lang="scss">
.chat-group-side {
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

  .member-area {
    margin-bottom: 12px;
    padding: 12px;

    @extend %card-style;
    .member-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.08);

      .member-title {
        font-size: var(--im-font-size);
        font-weight: 500;
        color: var(--im-text-color-primary);
      }

      .more-member-btn {
        font-size: var(--im-font-size-smaller);
        color: var(--im-color-primary-light-2);
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 4px;
        transition: all 0.2s ease;

        &:hover {
          color: var(--im-color-primary);
        }
      }
    }

    .member-items {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      text-align: center;
      gap: 20px;

      .member-tools {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 54px;

        .tool-btn {
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          border: var(--im-border);
          border-radius: 50%;
          font-size: 18px;
          cursor: pointer;
          color: var(--im-text-color-light);
          box-sizing: border-box;
          font-weight: 800;
          background: var(--im-background-active);
          transition: all 0.3s ease;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

          &:hover {
            border-color: var(--im-color-primary-light-2);
            color: var(--im-color-primary-light-2);
            background: var(--im-background-active);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(var(--im-color-primary-rgb), 0.2);
          }
        }

        .tool-text {
          font-size: 11px;
          color: var(--im-text-color);
          margin-top: 6px;
          text-align: center;
          font-weight: 500;
          width: 100%;
          height: 20px;
          line-height: 20px;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
        }
      }
    }
  }

  .switch-setting {
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
        border-radius: 6px;
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

  .group-info-section {
    margin-bottom: 12px;

    @extend %card-style;
    .info-item {
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
        border-radius: 6px;
        padding: 6px 8px;
        margin: 0 -8px;
      }

      .info-label {
        display: flex;
        align-items: center;
        gap: 6px;
        font-weight: 500;
        color: var(--im-text-color-primary);
        font-size: var(--im-font-size);
        flex-shrink: 0;
        width: 125px;
      }

      .info-content {
        flex: 1;
        margin-left: 12px;

        .info-display {
          display: flex;
          align-items: center;
          justify-content: flex-start;
          padding: 3px;
          cursor: pointer;
          transition: all 0.2s ease;
          min-height: 32px;
          border-radius: 8px;

          &:hover {
            background: var(--im-background-active);
          }

          span {
            font-size: var(--im-font-size);
            color: var(--im-text-color-light);
            text-align: left;
            word-break: break-all;

            &.info-placeholder {
              color: var(--im-text-color-light);
              font-style: italic;
            }
          }
        }
      }
    }
  }

  .notice-section {
    margin-bottom: 12px;

    @extend %card-style;
    .notice-header {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 8px;
      font-weight: 500;
      color: var(--im-text-color-primary);
      font-size: var(--im-font-size);
    }

    .notice-content {
      .notice-display {
        min-height: 40px;
        padding: 10px;
        border-radius: 10px;
        cursor: pointer;
        transition: all 0.2s ease;
        position: relative;
        font-style: italic;
        background: var(--im-background-active);

        &:hover {
          transform: translateY(-2px);
        }

        .notice-text {
          font-size: var(--im-font-size-small);
          color: var(--im-text-color-light);
          line-height: 1.4;
          word-break: break-word;
          text-align: left;
        }

        .notice-placeholder {
          font-size: var(--im-font-size-small);
          color: var(--im-text-color-light);
          line-height: 1.4;
          text-align: left;
        }
      }
    }
  }

  .notice-dialog-content {
    padding: 10px 0;
  }

  .dialog-footer {
    text-align: right;

    .el-button {
      margin-left: 10px;
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
</style>

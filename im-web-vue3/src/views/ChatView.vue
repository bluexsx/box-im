<template>
  <el-container class="chat-page">
    <ResizableAside :default-width="260" :min-width="200" :max-width="500" storage-key="chat-aside-width">
      <div class="header">
        <el-input v-model="searchText" class="search-text" :placeholder="'搜索'">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button plain class="add-btn" :icon="Plus" :title="'更多'" @click="onClickAddMenu" />
      </div>
      <div v-if="reconnecting" class="chat-status-bar is-reconnect">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>连接断开，正在尝试重新连接...</span>
      </div>
      <div v-else-if="loading" class="chat-status-bar is-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>消息接收中...</span>
      </div>
      <div v-if="!loading" class="chat-filter-bar">
        <div v-for="(item, idx) in menuItems" :key="idx" class="filter-item" :class="{ active: menuIdx === idx }" @click="onMenuChange(idx)">
          <span class="filter-label">{{ item.label }}</span>
          <span v-if="showMenuCount(item)" class="filter-count" :class="{ emphasis: isMenuCountEmphasis(item, idx) }">
            {{ formatMenuCount(item.count) }}
          </span>
        </div>
      </div>
      <VirtualScroller class="scroll-box" :items="showConversations">
        <template #default="{ item }">
          <ChatItem
            :conversation="item"
            :active="item === activeConv"
            @click="onActiveItem(item)"
            @delete="onDelItem(item)"
            @info="onShowInfo(item)"
            @dnd="onDnd(item)"
            @top="onTop(item)" />
        </template>
      </VirtualScroller>
    </ResizableAside>
    <el-container class="chat-box">
      <ChatBox v-if="activeConv" :conversation="activeConv" />
    </el-container>
    <AddFriend :dialog-visible="showAddFriend" @close="onCloseAddFriend" />
    <GroupMemberInvite ref="groupMemberInviteRef" @success="onCreateGroupSuccess" />
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
  </el-container>
</template>

<script setup lang="ts">
import { computed, inject, ref, type Ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Loading } from '@element-plus/icons-vue';
import ResizableAside from '@/components/common/ResizableAside.vue';
import VirtualScroller from '@/components/common/VirtualScroller.vue';
import RightMenu, { type RightMenuItem } from '@/components/common/RightMenu.vue';
import ChatItem from '@/components/chat/ChatItem.vue';
import ChatBox from '@/components/chat/ChatBox.vue';
import AddFriend from '@/components/friend/AddFriend.vue';
import GroupMemberInvite from '@/components/group/GroupMemberInvite.vue';
import { setFriendDnd, setFriendTop } from '@/api/friend';
import { setGroupDnd, setGroupTop } from '@/api/group';
import type { GroupVO } from '@/api/group/types';
import { deletePrivateChat } from '@/api/privateMessage';
import { deleteGroupChat } from '@/api/groupMessage';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import type { Conversation } from '@/types';
import { useFriendStore } from '@/stores/friend';
import { useGroupStore } from '@/stores/group';
import { CONVERSATION_TYPE } from '@/utils/enums';

type MenuItem = {
  label: string;
  count: number;
  alwaysShowCount?: boolean;
  emphasis?: boolean;
};

type FrozenFilter = {
  menuIdx: number;
  keys: string[];
};

const router = useRouter();
const chatStore = useChatStore();
const friendStore = useFriendStore();
const groupStore = useGroupStore();
const { activeConversation, conversations, loading } = storeToRefs(chatStore);
const reconnecting = inject<Ref<boolean>>('wsReconnecting', ref(false));
const searchText = ref('');
const menuIdx = ref(0);
// 未读/@我 tab 快照：{ menuIdx, keys }，进入 tab 时记录会话 key，切换 tab 时刷新
const frozenFilter = ref<FrozenFilter>();
const showAddFriend = ref(false);
const rightMenuRef = ref<InstanceType<typeof RightMenu>>();
const groupMemberInviteRef = ref<InstanceType<typeof GroupMemberInvite>>();

const activeConv = computed(() => activeConversation.value);
const isPrivate = (conv: Conversation) => conv.type == CONVERSATION_TYPE.PRIVATE;
const isGroup = (conv: Conversation) => conv.type == CONVERSATION_TYPE.GROUP;

const isShow = (conv: Conversation) => {
  return !searchText.value || conv.showName.includes(searchText.value);
};

const searchableConversations = computed(() => conversations.value.filter((conv) => isShow(conv)));

const isUnreadConv = (conv: Conversation) => {
  return !conv.isDnd && conv.unreadCount > 0;
};

const matchMenuFilter = (conv: Conversation, idx?: number) => {
  const current = idx === undefined ? menuIdx.value : idx;
  if (current === 1) {
    return isUnreadConv(conv);
  }
  if (current === 2) {
    return !!(conv.atMe || conv.atAll);
  }
  return true;
};

const isInFrozenTabList = (conv: Conversation) => {
  if (!frozenFilter.value) {
    return false;
  }
  // 快照保留已读会话，同时并入新未读/@我会话
  return frozenFilter.value.keys.includes(conv.key) || matchMenuFilter(conv, menuIdx.value);
};

// 快照内会话（含已读）+ 停留期间新符合 filter 的会话
const countFrozenTabConversations = (idx: number) => {
  if (!frozenFilter.value) {
    return 0;
  }
  const frozenKeys = new Set(frozenFilter.value.keys);
  return searchableConversations.value.filter((conv) => {
    return frozenKeys.has(conv.key) || matchMenuFilter(conv, idx);
  }).length;
};

// 各 tab 角标数量：当前 tab 用快照计数，其他 tab 用实时过滤
const getMenuCount = (idx: number) => {
  // 正在查看的未读/@我 tab：快照 + 新进来的会话
  if (frozenFilter.value && frozenFilter.value.menuIdx === idx) {
    return countFrozenTabConversations(idx);
  }
  const convs = searchableConversations.value;
  // 非当前 tab 或未读/@我 tab：实时统计，用于角标红色强调
  if (idx === 1) {
    return convs.filter((conv) => isUnreadConv(conv)).length;
  }
  if (idx === 2) {
    return convs.filter((conv) => conv.atMe || conv.atAll).length;
  }
  return convs.length;
};

// 进入未读/@我 tab 时快照当前符合条件的会话，避免点开后因已读状态变化而从列表消失
const refreshFrozenFilter = () => {
  if (menuIdx.value === 1 || menuIdx.value === 2) {
    const keys = searchableConversations.value.filter((conv) => matchMenuFilter(conv, menuIdx.value)).map((conv) => conv.key);
    frozenFilter.value = { menuIdx: menuIdx.value, keys };
  } else {
    frozenFilter.value = undefined;
  }
};

const showConversations = computed(() => {
  return conversations.value.filter((conv) => {
    if (!isShow(conv)) {
      return false;
    }
    if (menuIdx.value === 0) {
      return true;
    }
    // 未读/@我 tab 使用快照列表，全部 tab 实时过滤
    if (frozenFilter.value && frozenFilter.value.menuIdx === menuIdx.value) {
      return isInFrozenTabList(conv);
    }
    return matchMenuFilter(conv);
  });
});

const menuItems = computed((): MenuItem[] => {
  return [
    {
      label: '全部',
      count: getMenuCount(0),
      alwaysShowCount: true
    },
    {
      label: '未读',
      count: getMenuCount(1),
      emphasis: true
    },
    {
      label: '@我',
      count: getMenuCount(2),
      emphasis: true
    }
  ];
});

const addMenuItems = computed((): RightMenuItem[] => {
  return [
    { key: 'ADD_FRIEND', name: '添加好友' },
    { key: 'CREATE_GROUP', name: '发起群聊' }
  ];
});

const showMenuCount = (item: MenuItem) => {
  return !!(item.alwaysShowCount || item.count > 0);
};

const isMenuCountEmphasis = (item: MenuItem, idx: number) => {
  return !!(item.emphasis && item.count > 0 && menuIdx.value !== idx);
};

const formatMenuCount = (count: number) => {
  return count > 99 ? '99+' : count;
};

const onMenuChange = (idx: number) => {
  if (menuIdx.value !== idx) {
    menuIdx.value = idx;
    // 切换 tab 时重建快照（全部 tab 清空快照）
    refreshFrozenFilter();
  }
};

const onClickAddMenu = (e: MouseEvent) => {
  rightMenuRef.value?.open({ x: e.x, y: e.y }, addMenuItems.value);
};

const onSelectMenu = (item: RightMenuItem) => {
  if (item.key === 'ADD_FRIEND') {
    showAddFriend.value = true;
  } else if (item.key === 'CREATE_GROUP') {
    groupMemberInviteRef.value?.openCreate();
  }
};

const onCloseAddFriend = () => {
  showAddFriend.value = false;
};

const onCreateGroupSuccess = async (group: GroupVO) => {
  await groupStore.addGroup(group);
  await groupStore.refreshMember(group.id);
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, group.id);
  await chatStore.openChat({
    type: CONVERSATION_TYPE.GROUP,
    targetId: group.id,
    showName: group.showGroupName,
    headImage: group.headImageThumb,
    isDnd: group.isDnd,
    isTop: group.isTop
  });
  await chatStore.moveTop(convKey);
  chatStore.setActive(convKey);
};

const onActiveItem = (conv: Conversation) => {
  chatStore.setActive(conv.key);
};

const onDelItem = async (conv: Conversation) => {
  try {
    await ElMessageBox.confirm(`删除后记录将清空,确认删除与'${conv.showName}'的聊天 ?`, '删除会话', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });
    if (isPrivate(conv)) {
      await deletePrivateChat({ chatId: conv.targetId });
    } else if (isGroup(conv)) {
      await deleteGroupChat({ chatId: conv.targetId });
    }
    await chatStore.remove(conv.key);
  } catch {}
};

const onShowInfo = (conv: Conversation) => {
  if (isPrivate(conv)) {
    void router.push('/home/friend?id=' + conv.targetId);
  } else if (isGroup(conv)) {
    if (!groupStore.isGroup(conv.targetId)) {
      ElMessage.error('您已不在群聊中，无法查看群资料');
      return;
    }
    void router.push('/home/group?id=' + conv.targetId);
  }
};

const onDnd = (conv: Conversation) => {
  if (isPrivate(conv)) {
    void doSetFriendDnd(conv, conv.targetId, !conv.isDnd);
  } else if (isGroup(conv)) {
    void doSetGroupDnd(conv, conv.targetId, !conv.isDnd);
  } else {
    void chatStore.setDnd(conv.key, !conv.isDnd);
  }
};

const doSetFriendDnd = async (conv: Conversation, friendId: number, isDnd: boolean) => {
  try {
    await setFriendDnd({ friendId, isDnd });
    await friendStore.setDnd(friendId, isDnd);
    await chatStore.setDnd(conv.key, isDnd);
  } catch {
    ElMessage.error('操作失败');
  }
};

const doSetGroupDnd = async (conv: Conversation, groupId: number, isDnd: boolean) => {
  try {
    await setGroupDnd({ groupId, isDnd });
    await groupStore.setDnd(groupId, isDnd);
    await chatStore.setDnd(conv.key, isDnd);
  } catch {
    ElMessage.error('操作失败');
  }
};

const onTop = (conv: Conversation) => {
  if (isPrivate(conv)) {
    void doSetFriendTop(conv, conv.targetId, !conv.isTop);
  } else if (isGroup(conv)) {
    void doSetGroupTop(conv, conv.targetId, !conv.isTop);
  } else {
    void chatStore.setTop(conv.key, !conv.isTop);
  }
};

const doSetFriendTop = async (conv: Conversation, friendId: number, isTop: boolean) => {
  try {
    await setFriendTop({ friendId, isTop });
    await friendStore.setTop(friendId, isTop);
    await chatStore.setTop(conv.key, isTop);
  } catch {
    ElMessage.error('操作失败');
  }
};

const doSetGroupTop = async (conv: Conversation, groupId: number, isTop: boolean) => {
  try {
    await setGroupTop({ groupId, isTop });
    await groupStore.setTop(groupId, isTop);
    await chatStore.setTop(conv.key, isTop);
  } catch {
    ElMessage.error('操作失败');
  }
};
</script>

<style scoped lang="scss">
.chat-page {
  height: 100%;
  width: 100%;

  .header {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 12px;

    .search-text {
      flex: 1;
    }

    .add-btn {
      padding: 8px;
      margin: 5px;
      font-size: 16px;
      border-radius: 50%;
      background: var(--im-background-active);
      color: var(--im-color-primary);
      transition: all 0.3s ease;
      font-weight: 600;
      border: var(--im-border);
      flex-shrink: 0;

      &:hover {
        background: var(--im-background-active-dark);
        transform: scale(1.05);
      }
    }
  }

  .chat-filter-bar {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 0 16px 10px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);

    .filter-item {
      position: relative;
      display: inline-flex;
      align-items: baseline;
      gap: 4px;
      padding-bottom: 6px;
      cursor: pointer;
      user-select: none;

      .filter-label {
        font-size: 13px;
        color: var(--im-text-color-light);
        transition: color 0.2s ease;
      }

      .filter-count {
        font-size: 12px;
        color: var(--im-text-color-lighter);
        transition: color 0.2s ease;

        &.emphasis {
          color: var(--im-color-danger);
        }
      }

      &:hover {
        .filter-label {
          color: var(--im-text-color);
        }
      }

      &.active {
        .filter-label {
          color: var(--im-color-primary);
          font-weight: 600;
        }

        .filter-count {
          color: var(--im-color-primary);
          opacity: 0.85;
        }

        .filter-count.emphasis {
          color: var(--im-color-danger);
          opacity: 1;
        }

        &::after {
          content: '';
          position: absolute;
          left: 0;
          right: 0;
          bottom: 0;
          height: 2px;
          border-radius: 1px;
          background: var(--im-color-primary);
        }
      }
    }
  }

  .scroll-box {
    flex: 1;
    min-height: 0;
  }

  .chat-status-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    flex-shrink: 0;
    height: 36px;
    padding: 0 12px;
    font-size: 12px;
    line-height: 1;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
    .el-icon {
      font-size: 14px;
    }
    &.is-loading {
      color: var(--im-text-color-light);
      background: var(--im-color-primary-light-9, #f5f7fa);
      .el-icon {
        color: var(--im-color-primary);
      }
    }
    &.is-reconnect {
      color: var(--im-color-danger);
      background: color-mix(in srgb, var(--im-color-danger) 8%, #fff);
      .el-icon {
        color: var(--im-color-danger);
      }
    }
  }

  .chat-box {
    flex: 1;
    min-width: 0;
    height: 100%;
  }
}
</style>

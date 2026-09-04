<template>
  <div>
    <div class="chat-item" :class="{ active }" @click="emit('click')" @contextmenu.prevent="showRightMenu">
      <div class="chat-left">
        <HeadImage
          :url="conversation.headImage"
          :name="conversation.showName"
          :size="45"
          :online="online"
          :id="conversation.targetId" />
        <div v-show="!conversation.isDnd && conversation.unreadCount > 0" class="unread-text">
          {{ conversation.unreadCount }}
        </div>
      </div>
      <div class="chat-right">
        <div class="chat-name">
          <div class="chat-name-text" :title="conversation.showName">{{ conversation.showName }}</div>
          <div class="chat-tag">
            <el-tag v-if="isSystem" type="danger">{{ '官方' }}</el-tag>
          </div>
          <div class="chat-time-text">{{ showTime }}</div>
        </div>
        <div class="chat-content">
          <div class="chat-at-text">{{ atText }}</div>
          <div v-show="isShowSendName" class="chat-send-name">{{ conversation.sendNickName }}:&nbsp;</div>
          <div class="chat-content-text" v-html="transform(html2Escape(conversation.lastContent), 'emoji-small')" />
          <div v-if="conversation.isDnd" class="icon iconfont icon-dnd" />
        </div>
      </div>
      <div v-if="conversation.isTop" class="chat-top-badge" :title="'该会话已置顶'">
        <i class="icon iconfont icon-fixed" />
      </div>
    </div>
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import HeadImage from '@/components/common/HeadImage.vue';
import RightMenu, { type RightMenuItem } from '@/components/common/RightMenu.vue';
import type { Conversation } from '@/types';
import { useFriendStore } from '@/stores/friend';
import { CONVERSATION_TYPE } from '@/utils/enums';
import { toTimeText } from '@/utils/date';
import { transform } from '@/utils/emotion';
import { html2Escape } from '@/utils/str';

const props = defineProps({
  conversation: {
    type: Object as () => Conversation,
    required: true
  },
  active: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['click', 'top', 'info', 'dnd', 'delete']);

const friendStore = useFriendStore();
const rightMenuRef = ref<InstanceType<typeof RightMenu>>();

const isPrivate = computed(() => props.conversation.type == CONVERSATION_TYPE.PRIVATE);
const isGroup = computed(() => props.conversation.type == CONVERSATION_TYPE.GROUP);
const isSystem = computed(() => props.conversation.type == CONVERSATION_TYPE.SYSTEM);
const isShowSendName = computed(() => !!props.conversation.sendNickName);
const showTime = computed(() => (props.conversation.lastSendTime ? toTimeText(props.conversation.lastSendTime, true) : ''));

const atText = computed(() => {
  if (props.conversation.atMe) {
    return '[有人@我]';
  }
  if (props.conversation.atAll) {
    return '[@全体成员]';
  }
  return '';
});

const online = computed(() => {
  if (!isPrivate.value) {
    return false;
  }
  const friend = friendStore.findFriend(props.conversation.targetId);
  return !!(friend && friend.online);
});

const menuItems = computed((): RightMenuItem[] => {
  const items: RightMenuItem[] = [];
  if (!props.conversation.isTop) {
    items.push({ key: 'TOP', name: '置顶' });
  } else {
    items.push({ key: 'TOP', name: '取消置顶' });
  }
  if (isPrivate.value || isGroup.value) {
    items.push({ key: 'INFO', name: '查看资料' });
  }
  if (props.conversation.isDnd) {
    items.push({ key: 'DND', name: '新消息提醒' });
  } else {
    items.push({ key: 'DND', name: '消息免打扰' });
  }
  items.push({ key: 'DELETE', name: '删除聊天', danger: true });
  return items;
});

const showRightMenu = (e: MouseEvent) => {
  rightMenuRef.value?.open({ x: e.x, y: e.y }, menuItems.value);
};

const onSelectMenu = (item: RightMenuItem) => {
  const key = String(item.key).toLowerCase() as 'top' | 'info' | 'dnd' | 'delete';
  emit(key, props.conversation);
};
</script>

<style scoped lang="scss">
.chat-item {
  height: 64px;
  display: flex;
  position: relative;
  margin: 0 3px;
  padding: 5px 8px;
  align-items: center;
  white-space: nowrap;
  cursor: pointer;
  border-radius: 10px;

  &:hover {
    background-color: var(--im-background-active);
  }

  &.active {
    background-color: var(--im-background-active-dark);
  }

  .chat-left {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;

    .unread-text {
      position: absolute;
      background-color: var(--im-color-danger);
      right: -4px;
      top: -8px;
      color: white;
      border-radius: 30px;
      padding: 1px 5px;
      font-size: 10px;
      text-align: center;
      white-space: nowrap;
      border: 1px solid #f1e5e5;
    }
  }

  .chat-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    position: relative;
    padding-left: 10px;
    text-align: left;
    overflow: hidden;

    .chat-name {
      display: flex;
      align-items: center;
      line-height: 26px;
      height: 26px;

      .chat-name-text {
        font-size: var(--im-font-size);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .chat-tag {
        flex: 1;
        display: flex;
        align-items: center;
      }

      .chat-time-text {
        font-size: var(--im-font-size-smaller);
        text-align: right;
        color: var(--im-text-color-light);
        white-space: nowrap;
        overflow: hidden;
        padding-left: 2px;
        min-width: 60px;
      }
    }

    .chat-content {
      display: flex;
      line-height: 20px;
      height: 20px;
      margin-top: 2px;

      .chat-at-text {
        color: #c70b0b;
        font-size: var(--im-font-size-smaller);
      }

      .chat-send-name {
        font-size: var(--im-font-size-smaller);
        color: var(--im-text-color-light);
      }

      .chat-content-text {
        flex: 1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: var(--im-font-size-smaller);
        color: var(--im-text-color-light);
      }

      .icon {
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: var(--im-font-size-large);
        color: var(--im-text-color-lighter);
        margin-left: 5px;
        background: var(--im-background-active);
        border-radius: 50%;
        width: 20px;
        height: 20px;
      }
    }
  }

  .chat-top-badge {
    position: absolute;
    top: 0;
    right: 0;
    width: 22px;
    height: 22px;
    background: var(--im-color-primary-light-2);
    clip-path: polygon(0% 0%, 100% 100%, 100% 0%);
    border-radius: 0 8px 0 4px;
    z-index: 1;

    .icon {
      position: absolute;
      top: 2px;
      right: 2px;
      color: #fff;
      font-size: 10px;
      line-height: 1;
    }
  }
}
</style>

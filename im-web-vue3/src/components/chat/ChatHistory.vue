<template>
  <el-drawer v-model="show" :title="'聊天记录'" size="700px" direction="rtl" @close="onDrawerClose">
    <div class="chat-history" v-loading="loading" :element-loading-text="'正在加载聊天记录...'" element-loading-background="#F9F9F9">
      <div class="search-bar">
        <el-input v-model="searchText" class="search-text" :placeholder="'搜索聊天记录'" @input="onSearchTextChange">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div ref="chatTabsRef" class="chat-tabs">
        <el-tabs v-model="tabName" @tab-change="onTabChange">
          <el-tab-pane :label="'全部'" name="all">
            <el-scrollbar v-if="messageSize > 0" ref="allScrollBoxRef" :style="tabPaneStyle">
              <div v-for="(message, idx) in showMessages" :key="showMinIdx + idx">
                <ChatHistoryItem
                  :active="activeIdx == showMinIdx + idx"
                  :head-image="headImage(message)"
                  :show-name="showName(message)"
                  :message="message"
                  @click="onClickItem(showMinIdx + idx)"
                  @contextmenu.prevent="onRclickItem($event, showMinIdx + idx)"
                  @dblclick="onDblclickItem(showMinIdx + idx)" />
              </div>
            </el-scrollbar>
            <NoDataTip v-else :style="tabPaneStyle" :tip="noDataTip" />
          </el-tab-pane>
          <el-tab-pane :label="'文字'" name="text">
            <el-scrollbar v-if="messageSize > 0" ref="textScrollBoxRef" :style="tabPaneStyle">
              <div v-for="(message, idx) in showMessages" :key="showMinIdx + idx">
                <ChatHistoryItem
                  :active="activeIdx == showMinIdx + idx"
                  :head-image="headImage(message)"
                  :show-name="showName(message)"
                  :message="message"
                  @click="onClickItem(showMinIdx + idx)"
                  @contextmenu.prevent="onRclickItem($event, showMinIdx + idx)"
                  @dblclick="onDblclickItem(showMinIdx + idx)" />
              </div>
            </el-scrollbar>
            <NoDataTip v-else :style="tabPaneStyle" :tip="noDataTip" />
          </el-tab-pane>
          <el-tab-pane :label="'图片'" name="image">
            <el-scrollbar v-if="messageSize > 0" ref="imageScrollBoxRef" :style="tabPaneStyle">
              <div v-if="tabName == 'image'" class="chat-image-video-list">
                <div v-for="(message, idx) in showMessages" :key="showMinIdx + idx">
                  <div
                    class="chat-image-video"
                    :class="activeIdx == showMinIdx + idx ? 'active' : ''"
                    @contextmenu.prevent="onRclickItem($event, showMinIdx + idx)"
                    @dblclick="onDblclickItem(showMinIdx + idx)"
                    @click="onClickItem(showMinIdx + idx)">
                    <img class="image" :src="parseContent(message).thumbUrl" loading="lazy" @click.stop="showFullImageBox(message)" />
                    <span class="upload-text">{{ showName(message) }} {{ '上传于' }} {{ toTimeText(Number(message.sendTime), true) }}</span>
                  </div>
                </div>
              </div>
            </el-scrollbar>
            <NoDataTip v-else :style="tabPaneStyle" />
          </el-tab-pane>
          <el-tab-pane :label="'文件'" name="file">
            <el-scrollbar v-if="messageSize > 0" ref="fileScrollBoxRef" :style="tabPaneStyle">
              <div v-for="(message, idx) in showMessages" :key="showMinIdx + idx">
                <ChatHistoryItem
                  :active="activeIdx == showMinIdx + idx"
                  :head-image="headImage(message)"
                  :show-name="showName(message)"
                  :message="message"
                  @click="onClickItem(showMinIdx + idx)"
                  @contextmenu.prevent="onRclickItem($event, showMinIdx + idx)"
                  @dblclick="onDblclickItem(showMinIdx + idx)" />
              </div>
            </el-scrollbar>
            <NoDataTip v-else :style="tabPaneStyle" :tip="noDataTip" />
          </el-tab-pane>
          <el-tab-pane :label="'语音'" name="voice">
            <el-scrollbar v-if="messageSize > 0" ref="voiceScrollBoxRef" :style="tabPaneStyle">
              <div v-for="(message, idx) in showMessages" :key="showMinIdx + idx">
                <ChatHistoryItem
                  :active="activeIdx == showMinIdx + idx"
                  :head-image="headImage(message)"
                  :show-name="showName(message)"
                  :message="message"
                  @click="onClickItem(showMinIdx + idx)"
                  @contextmenu.prevent="onRclickItem($event, showMinIdx + idx)"
                  @dblclick="onDblclickItem(showMinIdx + idx)" />
              </div>
            </el-scrollbar>
            <NoDataTip v-else :style="tabPaneStyle" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
  </el-drawer>
</template>
<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Search } from '@element-plus/icons-vue';
import ChatHistoryItem from '@/components/chat/ChatHistoryItem.vue';
import NoDataTip from '@/components/common/NoDataTip.vue';
import RightMenu, { type RightMenuItem } from '@/components/common/RightMenu.vue';
import { getDB } from '@/db';
import type { Conversation, ChatMessage } from '@/types';
import type { FriendVO } from '@/api/friend/types';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import { useUserStore } from '@/stores/user';
import { toTimeText } from '@/utils/date';
import { CONVERSATION_TYPE, MESSAGE_STATUS, MESSAGE_TYPE } from '@/utils/enums';
import eventBus from '@/utils/eventBus';
import { isAction, isNormal } from '@/utils/messageType';
const props = defineProps({
  conversation: {
    type: Object as () => Conversation,
    required: true
  },
  friend: {
    type: Object as () => FriendVO,
    default: undefined
  },
  group: {
    type: Object as () => GroupVO,
    default: undefined
  },
  groupMemberMap: {
    type: Object as () => Map<number, GroupMemberVO>,
    default: () => new Map<number, GroupMemberVO>()
  }
});
const emit = defineEmits(['locateInChat']);
const userStore = useUserStore();
const show = ref(false);
const tabName = ref('all');
const showMinIdx = ref(0);
const activeIdx = ref(-1);
const searchText = ref('');
const tabPaneHeight = ref(500);
const messages = ref<ChatMessage[]>([]);
const loading = ref(false);
const chatTabsRef = ref<HTMLElement>();
const allScrollBoxRef = ref();
const textScrollBoxRef = ref();
const imageScrollBoxRef = ref();
const fileScrollBoxRef = ref();
const voiceScrollBoxRef = ref();
const rightMenuRef = ref<InstanceType<typeof RightMenu>>();
const menuItemKeys = [{ key: 'LOCATE_IN_CHAT', name: '在聊天中定位' }];
const mine = computed(() => userStore.userInfo);
const isGroup = computed(() => props.conversation.type == CONVERSATION_TYPE.GROUP);
const allMessage = computed(() => messages.value.filter((m) => isNormal(m.type) || isAction(m.type)));
const imageMessage = computed(() => messages.value.filter((m) => m.type == MESSAGE_TYPE.IMAGE));
const fileMessage = computed(() => messages.value.filter((m) => m.type == MESSAGE_TYPE.FILE));
const voiceMessage = computed(() => messages.value.filter((m) => m.type == MESSAGE_TYPE.AUDIO));
const textMessage = computed(() => messages.value.filter((m) => m.type == MESSAGE_TYPE.TEXT));

const tabMessages = computed(() => {
  const listMap: Record<string, ChatMessage[]> = {
    all: allMessage.value,
    text: textMessage.value,
    image: imageMessage.value,
    file: fileMessage.value,
    voice: voiceMessage.value
  };
  const list = listMap[tabName.value] || allMessage.value;
  const keyword = searchText.value.toLowerCase();
  if (!keyword) {
    return list;
  }
  // 只有文字和文件支持检索
  if (tabName.value != 'all' && tabName.value != 'text' && tabName.value != 'file') {
    return list;
  }
  return list.filter((m) => {
    if (m.type == MESSAGE_TYPE.TEXT) {
      return m.content.toLowerCase().includes(keyword);
    }
    if (m.type == MESSAGE_TYPE.FILE) {
      return String(parseContent(m).name || '')
        .toLowerCase()
        .includes(keyword);
    }
    return false;
  });
});

const showMessages = computed(() => tabMessages.value.slice(showMinIdx.value));
const messageSize = computed(() => (show.value ? tabMessages.value.length : 0));
const tabPaneStyle = computed(() => `height:${tabPaneHeight.value}px`);
const menuItems = computed<RightMenuItem[]>(() => menuItemKeys.map((item) => ({ key: item.key, name: item.name })));
const noDataTip = computed(() => (searchText.value ? `未搜索到与'${searchText.value}'相关的内容` : '没有数据'));

const parseContent = (message: ChatMessage) => {
  try {
    return JSON.parse(message.content || '{}');
  } catch {
    return {};
  }
};

const getScrollRef = () => {
  const refMap: Record<string, typeof allScrollBoxRef> = {
    all: allScrollBoxRef,
    text: textScrollBoxRef,
    image: imageScrollBoxRef,
    file: fileScrollBoxRef,
    voice: voiceScrollBoxRef
  };
  return refMap[tabName.value]?.value;
};

const getScrollWrap = () => {
  const scrollBox = getScrollRef();
  return scrollBox?.$el?.querySelector('.el-scrollbar__wrap') as HTMLElement | null;
};

const filterInvalidMessage = (localMessages: ChatMessage[]) => {
  // 排除已经删除或撤回的消息
  return localMessages.filter((m) => !m.deleted && m.status != MESSAGE_STATUS.RECALL && m.type != MESSAGE_TYPE.RECALL);
};

const showName = (message?: ChatMessage) => {
  if (!message) {
    return '';
  }
  if (isGroup.value) {
    const member = props.groupMemberMap.get(Number(message.sendId));
    return member ? member.showNickName : String(message.sendNickName || '');
  }
  return message.selfSend ? mine.value.nickName : props.conversation.showName;
};

const headImage = (message: ChatMessage) => {
  if (isGroup.value) {
    const member = props.groupMemberMap.get(Number(message.sendId));
    return member ? member.headImage || '' : '';
  }
  return message.selfSend ? mine.value.headImageThumb || '' : props.conversation.headImage || '';
};

const resetShowMinIdx = () => {
  showMinIdx.value = messageSize.value > 30 ? messageSize.value - 30 : 0;
};

const onScroll = (e: Event) => {
  const scrollElement = e.target as HTMLElement;
  if (scrollElement.scrollTop < 30) {
    // 多展示20条信息
    showMinIdx.value = showMinIdx.value > 20 ? showMinIdx.value - 20 : 0;
  }
};

const initEvent = () => {
  if (!messageSize.value) {
    return;
  }
  nextTick(() => {
    const scrollWrap = getScrollWrap();
    if (!scrollWrap) {
      return;
    }
    scrollWrap.removeEventListener('scroll', onScroll);
    scrollWrap.addEventListener('scroll', onScroll);
  });
};

const scrollToBottom = () => {
  if (!messageSize.value) {
    return;
  }
  nextTick(() => {
    const scrollWrap = getScrollWrap();
    if (scrollWrap) {
      scrollWrap.scrollTop = scrollWrap.scrollHeight;
    }
  });
};

const open = async () => {
  show.value = true;
  searchText.value = '';
  tabName.value = 'all';
  loading.value = true;
  const localMessages = await getDB().findMessageByConvKey(props.conversation.key);
  messages.value = filterInvalidMessage(localMessages);
  loading.value = false;
  resetShowMinIdx();
  initEvent();
  scrollToBottom();
  nextTick(() => {
    if (chatTabsRef.value) {
      // 滚动条高度
      tabPaneHeight.value = chatTabsRef.value.offsetHeight - 80;
    }
  });
};

const onDrawerClose = () => {
  messages.value = [];
  loading.value = false;
};

const close = () => {
  show.value = false;
  onDrawerClose();
};

const onTabChange = () => {
  resetShowMinIdx();
  initEvent();
  scrollToBottom();
};

const onSearchTextChange = () => {
  resetShowMinIdx();
};

const onClickItem = (idx: number) => {
  activeIdx.value = idx;
};

const onDblclickItem = (idx: number) => {
  activeIdx.value = idx;
  onSelectMenu(menuItems.value[0]);
};

const onRclickItem = (e: MouseEvent, idx: number) => {
  activeIdx.value = idx;
  rightMenuRef.value?.open({ x: e.x, y: e.y }, menuItems.value);
};

const onSelectMenu = (item: RightMenuItem) => {
  // 菜单id转驼峰作为事件key
  const eventKey = String(item.key)
    .toLowerCase()
    .replace(/_([a-z])/g, (_g, c: string) => c.toUpperCase());
  if (eventKey == 'locateInChat') {
    emit('locateInChat', tabMessages.value[activeIdx.value]);
  }
};

const showFullImageBox = (message: ChatMessage) => {
  const imageUrl = parseContent(message).originUrl;
  if (!imageUrl) {
    return;
  }
  eventBus.emit('openFullImage', {
    convKey: props.conversation.key,
    url: imageUrl,
    seqNo: message.seqNo,
    localId: message.localId
  });
};
defineExpose({ open, close });
</script>
<style lang="scss">
.chat-history {
  display: flex;
  height: 100%;
  padding: 0 25px;
  flex-direction: column;

  .search-bar {
    margin-bottom: 10px;
  }

  .chat-tabs {
    flex: 1;

    .chat-image-video-list {
      display: flex;
      flex-wrap: wrap;

      .chat-image-video {
        display: flex;
        flex-direction: column;
        padding: 10px;
        width: 140px;
        border-radius: 5px;
        cursor: pointer;

        .image {
          width: 140px;
          height: 140px;
          border-radius: 5px;
          object-fit: cover;
        }

        .upload-text {
          color: var(--im-text-color-light);
          font-size: var(--im-font-size-small);
          margin-top: 5px;
          word-break: break-all;
        }

        &:hover {
          background: #f4f4f4;
        }

        &.active {
          background: #e1eaf7;
        }
      }
    }
  }
}
</style>

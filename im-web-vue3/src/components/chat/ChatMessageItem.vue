<template>
  <div class="chat-message-item" :class="active ? 'active' : ''">
    <div v-if="message.type == MESSAGE_TYPE.TIP_TEXT" class="message-tip" v-html="parsedTipContent" @click="onClickTipMessage" />
    <div v-else-if="message.type == MESSAGE_TYPE.TIP_TIME" class="message-tip">
      {{ toTimeText(Number(message.sendTime)) }}
    </div>
    <div v-else-if="isNormal || isAction" class="message-normal" :class="{ 'message-mine': mine }">
      <div class="avatar" @contextmenu.prevent.stop="showAvatarMenu">
        <HeadImage :name="showName" :size="38" :url="headImage" :id="message.sendId" />
      </div>
      <div class="content">
        <div v-if="message.groupId && !message.selfSend" class="top">
          <div class="show-name">{{ showName }}</div>
          <el-tag v-if="isGroupOwner(message.sendId)" size="small" type="danger">{{ '群主' }}</el-tag>
        </div>
        <div class="bottom" :class="{ fullscreen: configStore.fullScreen }" @contextmenu.prevent="showMessageMenu">
          <div ref="chatMsgBoxRef" class="message-content-wrapper">
            <div v-if="isTextMessage" class="message-text" v-html="htmlText" @click="onClickTextMessage" />
            <div v-else-if="message.type == MESSAGE_TYPE.IMAGE" class="message-image" @click="showFullImage">
              <div class="image-container" :style="imageStyle">
                <img class="send-image" :src="contentData.thumbUrl" loading="lazy" />
                <div class="image-overlay">
                  <el-icon><ZoomIn /></el-icon>
                </div>
              </div>
            </div>
            <div v-else-if="message.type == MESSAGE_TYPE.FILE" class="message-file">
              <div v-loading="sending" class="file-box">
                <div class="file-info">
                  <el-link class="file-name" :underline="true" type="primary" target="_blank" :href="contentData.url" :download="contentData.name">
                    {{ contentData.name }}
                  </el-link>
                  <div class="file-size">{{ fileSize }}</div>
                </div>
                <div class="file-icon icon iconfont icon-message-file" />
              </div>
            </div>
            <ChatVoiceMessage
              v-else-if="message.type == MESSAGE_TYPE.AUDIO"
              ref="voiceMessageRef"
              mode="chat"
              :url="contentData.url"
              :duration="contentData.duration"
              :mine="mine"
              @audioStateChange="onVoiceStateChange" />
            <div v-else-if="isAction" class="chat-action message-text">
              <span v-if="message.type == MESSAGE_TYPE.ACT_RT_VOICE" :title="'重新呼叫'" class="iconfont icon-chat-voice" @click="emit('call')" />
              <span v-if="message.type == MESSAGE_TYPE.ACT_RT_VIDEO" :title="'重新呼叫'" class="iconfont icon-chat-video" @click="emit('call')" />
              <span>{{ displayContentText }}</span>
            </div>
            <div v-else class="message-text">{{ '[暂不支持该消息类型]' }}</div>
            <div v-if="sending" class="sending" v-loading="sending" :title="'发送中'" />
            <div v-else-if="sendFail" class="send-fail" :title="'发送失败'" @click="emit('resend', message)">
              <el-icon><WarningFilled /></el-icon>
            </div>
          </div>
          <div v-if="!isAction && message.selfSend && !isGroupMessage" class="message-status">
            <span v-if="isReaded" class="chat-readed">{{ '已读' }}</span>
            <span v-else class="chat-unread">{{ '未读' }}</span>
          </div>
          <div v-if="message.receipt && message.selfSend" class="chat-receipt" @click="onShowReadedBox">
            <span v-if="message.receiptOk" class="icon iconfont icon-ok" :title="'全体已读'" />
            <span v-else>{{ `${message.readedCount}人已读` }}</span>
          </div>
        </div>
      </div>
    </div>
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
    <ChatGroupReaded v-if="group" ref="chatGroupReadedRef" :message="message" :group="group" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { WarningFilled, ZoomIn } from '@element-plus/icons-vue';
import HeadImage from '@/components/common/HeadImage.vue';
import RightMenu, { type RightMenuItem } from '@/components/common/RightMenu.vue';
import ChatGroupReaded from '@/components/chat/ChatGroupReaded.vue';
import ChatVoiceMessage from '@/components/chat/ChatVoiceMessage.vue';
import { findUser } from '@/api/user';
import type { ChatMessage, Conversation } from '@/types';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import { useConfigStore } from '@/stores/config';
import { useUserStore } from '@/stores/user';
import { MESSAGE_STATUS, MESSAGE_TYPE } from '@/utils/enums';
import { toTimeText } from '@/utils/date';
import * as msgType from '@/utils/messageType';
import { transform } from '@/utils/emotion';
import { html2Escape } from '@/utils/str';
import eventBus from '@/utils/eventBus';

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  mine: {
    type: Boolean,
    required: true
  },
  headImage: {
    type: String,
    default: ''
  },
  showName: {
    type: String,
    required: true
  },
  conversation: {
    type: Object as () => Conversation,
    required: true
  },
  message: {
    type: Object as () => ChatMessage,
    required: true
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

const emit = defineEmits(['resend', 'delete', 'recall', 'download', 'call', 'audioStateChange', 'copy', 'atMember']);

const userStore = useUserStore();
const configStore = useConfigStore();
const chatMsgBoxRef = ref<HTMLElement>();
const rightMenuRef = ref<InstanceType<typeof RightMenu>>();
const chatGroupReadedRef = ref<InstanceType<typeof ChatGroupReaded>>();
const voiceMessageRef = ref<InstanceType<typeof ChatVoiceMessage>>();
const avatarMenuEvent = ref<MouseEvent>();

const contentData = computed(() => {
  try {
    return JSON.parse(props.message.content || '{}');
  } catch {
    return {};
  }
});

const isTextMessage = computed(() => props.message.type == MESSAGE_TYPE.TEXT);
const isNormal = computed(() => msgType.isNormal(props.message.type));
const isAction = computed(() => msgType.isAction(props.message.type));
const isGroupMessage = computed(() => !!(props.message as ChatMessage & { groupId?: number }).groupId);
const sending = computed(() => props.message.status == MESSAGE_STATUS.SENDING);
const sendFail = computed(() => props.message.status == MESSAGE_STATUS.FAILED);
const isReaded = computed(() => props.message.status == MESSAGE_STATUS.READED || props.conversation.maxReadedId >= (props.message.id ?? 0));
const displayContentText = computed(() => props.message.content);

const parsedTipContent = computed(() => {
  const content = displayContentText.value;
  // 匹配格式：#{displayName:userId},正则表达式：#\{([^:]+):(\d+)\}
  const userMarkPattern = /#\{([^:]+):(\d+)\}/g;
  let lastIndex = 0;
  let result = '';
  let match: RegExpExecArray | null;
  while ((match = userMarkPattern.exec(content)) !== null) {
    // 添加匹配前的文本
    result += html2Escape(content.substring(lastIndex, match.index));
    let displayName = match[1];
    const userId = match[2];
    // 如果是当前登录用户，用"你"代替用户昵称
    if (userId == String(userStore.userInfo.id)) {
      displayName = '你';
    }
    // 渲染为可点击元素
    result += `<span class="tip-user-name" data-user-id="${userId}">${html2Escape(displayName)}</span>`;
    lastIndex = match.index + match[0].length;
  }
  // 添加剩余文本
  result += html2Escape(content.substring(lastIndex));
  return result;
});

const htmlText = computed(() => {
  let text = html2Escape(props.message.content);
  text = transform(text, 'emoji-normal');
  const atUserIds = props.message.atUserIds;
  if (atUserIds && atUserIds.length > 0) {
    let atIndex = 0;
    text = text.replace(/@([^\s@]+)/g, (match, nick) => {
      if (atIndex < atUserIds.length) {
        const userId = atUserIds[atIndex++];
        return `<span class="at-user-name" data-user-id="${userId}">@${nick}</span>`;
      }
      return match;
    });
  }
  return text;
});

const fileSize = computed(() => {
  const size = contentData.value.size || 0;
  if (size > 1024 * 1024) {
    return Math.round(size / 1024 / 1024) + 'M';
  }
  if (size > 1024) {
    return Math.round(size / 1024) + 'KB';
  }
  return size + 'B';
});

const imageStyle = computed(() => {
  // 计算图片的显示宽高，要求：任意边不能高于360px,不能低于60px,不能拉伸图片比例
  const maxSize = configStore.fullScreen ? 360 : 240;
  const minSize = 60;
  const width = contentData.value.width;
  const height = contentData.value.height;
  if (width && height) {
    const ratio = Math.min(width, height) / Math.max(width, height);
    const w = Math.max(Math.min(width > height ? maxSize : ratio * maxSize, width), minSize);
    const h = Math.max(Math.min(width > height ? ratio * maxSize : maxSize, height), minSize);
    return `width: ${w}px;height:${h}px;object-fit: cover;`;
  }
  // 兼容历史版本，历史数据没有记录宽高
  return `max-width: ${maxSize}px;min-width:60px;max-height: ${maxSize}px;min-height:60px;`;
});

const isGroupOwner = (userId?: number) => {
  return props.group?.ownerId == userId;
};

const openUserInfoCard = (event: MouseEvent, userId: number) => {
  findUser(userId).then((user) => {
    eventBus.emit('openUserInfo', {
      user,
      pos: { x: event.clientX + 30, y: event.clientY }
    });
  });
};

const onClickTipMessage = (event: MouseEvent) => {
  // 检查点击的是否是用户名元素
  const target = event.target as HTMLElement;
  const usernameEl = target.closest('.tip-user-name');
  if (usernameEl) {
    const userId = usernameEl.getAttribute('data-user-id');
    if (userId) {
      event.stopPropagation();
      openUserInfoCard(event, parseInt(userId));
    }
  }
};

const onClickTextMessage = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  const usernameEl = target.closest('.at-user-name');
  if (usernameEl) {
    const userId = usernameEl.getAttribute('data-user-id');
    if (userId && Number(userId) > 0) {
      event.stopPropagation();
      openUserInfoCard(event, parseInt(userId));
    }
  }
};

const showFullImage = () => {
  const imageUrl = contentData.value.originUrl;
  if (!imageUrl) {
    return;
  }
  eventBus.emit('openFullImage', {
    convKey: props.conversation.key,
    url: imageUrl,
    seqNo: props.message.seqNo,
    localId: props.message.localId
  });
};

const onVoiceStateChange = (state: string) => {
  emit('audioStateChange', state, props.message);
};

const onShowReadedBox = () => {
  const rect = chatMsgBoxRef.value?.getBoundingClientRect();
  if (rect) {
    chatGroupReadedRef.value?.open(rect);
  }
};

const showMessageMenu = (e: MouseEvent) => {
  const menuItems: RightMenuItem[] = [];
  if (isTextMessage.value) {
    menuItems.push({ key: 'COPY', name: '复制' });
  }
  menuItems.push({ key: 'DELETE', name: '删除', danger: true });
  if (props.message.selfSend && props.message.id && props.message.id > 0) {
    menuItems.push({ key: 'RECALL', name: '撤回' });
  }
  if (props.message.type == MESSAGE_TYPE.FILE) {
    menuItems.push({ key: 'DOWNLOAD', name: '下载' });
  }
  if (sendFail.value) {
    menuItems.push({ key: 'RESEND', name: '重新发送' });
  }
  rightMenuRef.value?.open({ x: e.clientX, y: e.clientY }, menuItems);
};

const showAvatarMenu = (e: MouseEvent) => {
  if (!props.message.groupId || props.message.selfSend) {
    return;
  }
  avatarMenuEvent.value = e;
  rightMenuRef.value?.open({ x: e.clientX, y: e.clientY }, [
    { key: 'AT_MEMBER', name: '@' + props.showName },
    { key: 'USER_INFO', name: '查看资料' }
  ]);
};

const onSelectMenu = (item: RightMenuItem) => {
  if (item.key === 'AT_MEMBER') {
    emit('atMember', { userId: props.message.sendId, showNickName: props.showName });
    return;
  }
  if (item.key === 'USER_INFO' && avatarMenuEvent.value) {
    openUserInfoCard(avatarMenuEvent.value, props.message.sendId!);
    return;
  }
  if (item.key === 'RESEND') {
    emit('resend', props.message);
    return;
  }
  // 菜单id转驼峰作为事件key
  const eventKey = String(item.key)
    .toLowerCase()
    .replace(/_([a-z])/g, (_g, c: string) => c.toUpperCase());
  emit(eventKey as 'copy', props.message);
};

const stopPlayAudio = () => {
  voiceMessageRef.value?.stopPlayAudio?.();
};

defineExpose({ stopPlayAudio });
</script>

<style scoped lang="scss">
.chat-message-item {
  padding: 3px 10px;
  border-radius: 10px;

  &.active {
    background: var(--im-background-active-dark);
  }

  .message-tip {
    display: table;
    margin: 8px auto;
    padding: 4px 12px;
    line-height: 22px;
    max-width: 80%;
    border-radius: 6px;
    background: rgba(255, 255, 255, 0.3);
    font-size: var(--im-font-size-small);
    color: var(--im-text-color-light);
    text-align: center;
    word-break: break-word;

    :deep(.tip-user-name) {
      color: var(--im-color-primary);
      cursor: pointer;
      padding: 2px 5px;
    }
  }

  .message-normal {
    position: relative;
    font-size: 0;
    padding-left: 53px;
    min-height: 50px;
    margin: 5px 0;

    .avatar {
      position: absolute;
      width: 40px;
      height: 40px;
      top: 0;
      left: 0;
    }

    .content {
      text-align: left;

      .top {
        display: flex;
        flex-wrap: nowrap;
        align-items: center;
        gap: 4px;

        .show-name {
          white-space: nowrap;
          max-width: 400px;
          overflow: hidden;
          line-height: 18px;
          font-size: var(--im-font-size-small);
          color: #888;
        }
      }

      .bottom {
        display: inline-block;
        padding-right: 30px;
        margin-top: 2px;

        &.fullscreen {
          padding-right: 240px;
        }

        .message-content-wrapper {
          position: relative;
          display: inline-flex;
          align-items: flex-end;

          .sending {
            width: 40px;
            height: 40px;

            :deep(.el-loading-mask) {
              background: inherit;
            }

            :deep(.circular) {
              width: 35px;
              height: 35px;
            }

            :deep(.el-loading-spinner) {
              margin-top: -15px;
            }
          }

          .send-fail {
            color: #e45050;
            font-size: 25px;
            cursor: pointer;
            margin: 0 5px;
            display: flex;
            align-items: center;
          }
        }

        .message-text {
          flex: 1;
          display: inline-block;
          position: relative;
          line-height: 26px;
          padding: 6px 10px;
          background: var(--im-background);
          border-radius: 10px;
          font-size: var(--im-font-size);
          text-align: left;
          white-space: pre-wrap;
          word-break: break-word;

          :deep(.at-user-name) {
            color: var(--im-color-primary);
            font-size: var(--im-font-size-small);
            font-weight: 600;
            cursor: pointer;
            padding: 2px;
            opacity: 0.9;

            &:hover {
              opacity: 1;
            }
          }
        }

        .message-image {
          border-radius: 12px;
          overflow: hidden;
          cursor: pointer;
          background: var(--im-background);
          box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
          transition: all 0.3s ease;
          position: relative;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
          }

          .image-container {
            position: relative;
            width: 100%;
            height: 100%;
            overflow: hidden;
            border-radius: 12px;

            .send-image {
              width: 100%;
              height: 100%;
              object-fit: cover;
              transition: transform 0.3s ease;
            }

            .image-overlay {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: rgba(0, 0, 0, 0.3);
              display: flex;
              align-items: center;
              justify-content: center;
              opacity: 0;
              transition: opacity 0.3s ease;
              color: white;
              font-size: 24px;
            }

            &:hover {
              .send-image {
                transform: scale(1.05);
              }

              .image-overlay {
                opacity: 1;
              }
            }
          }
        }

        .message-file {
          display: flex;
          flex-wrap: nowrap;
          flex-direction: row;
          align-items: center;
          cursor: pointer;
          margin-bottom: 2px;
          background: var(--im-background);

          .file-box {
            display: flex;
            flex-wrap: nowrap;
            align-items: center;
            min-height: 60px;
            box-shadow: var(--im-box-shadow-light);
            border-radius: 8px;
            padding: 15px;
            border: 2px solid #eee;
            transition: all 0.3s ease;
            background: white;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
            }

            .file-info {
              flex: 1;
              height: 100%;
              text-align: left;
              font-size: 14px;
              margin-right: 10px;

              .file-name {
                display: inline-block;
                min-width: 160px;
                max-width: 200px;
                font-size: 14px;
                margin-bottom: 4px;
                white-space: pre-wrap;
                word-break: break-all;
                color: #2830d3;

                &:hover {
                  text-decoration: underline;
                }
              }

              .file-size {
                font-size: var(--im-font-size-smaller);
                color: var(--im-text-color-light);
              }
            }

            .file-icon {
              font-size: 36px;
              color: #d42e07;
              transition: transform 0.3s ease;
            }

            &:hover .file-icon {
              transform: scale(1.1);
            }
          }
        }

        .chat-action {
          display: flex;
          align-items: center;

          .iconfont {
            cursor: pointer;
            font-size: 22px;
            padding-right: 8px;
          }
        }

        .message-status {
          margin-top: 3px;
          display: block;
          font-size: 11px;

          .chat-readed {
            color: var(--im-text-color-light);
          }

          .chat-unread {
            color: var(--im-color-danger);
          }
        }

        .chat-receipt {
          font-size: var(--im-font-size-smaller);
          cursor: pointer;
          color: var(--im-text-color-light);

          .icon-ok {
            font-size: 20px;
            color: var(--im-color-success);
          }
        }
      }
    }

    &.message-mine {
      text-align: right;
      padding-left: 0;
      padding-right: 53px;

      .avatar {
        left: auto;
        right: 0;
      }

      .content {
        text-align: right;

        .top {
          flex-direction: row-reverse;
        }

        .bottom {
          padding-left: 30px;
          padding-right: 0;

          &.fullscreen {
            padding-left: 240px;
          }

          .message-content-wrapper {
            flex-direction: row-reverse;
          }

          .message-text {
            background: var(--im-color-primary-light-2);
            color: white;

            :deep(.at-user-name) {
              color: white;
            }
          }

          .chat-action {
            flex-direction: row-reverse;

            .iconfont {
              transform: rotateY(180deg);
            }
          }
        }
      }
    }
  }
}
</style>

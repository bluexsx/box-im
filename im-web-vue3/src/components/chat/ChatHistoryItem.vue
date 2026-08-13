<template>
  <div class="chat-history-item" :class="active ? 'active' : ''">
    <div class="message-normal">
      <div class="avatar">
        <HeadImage :name="showName" :size="38" :url="headImage" :id="message.sendId" />
      </div>
      <div class="content">
        <div class="top">
          <span>{{ showName }}</span>
          <span>{{ toTimeText(Number(message.sendTime)) }}</span>
        </div>
        <div class="bottom">
          <div>
            <span v-if="message.type == MESSAGE_TYPE.TEXT" class="message-text" v-html="htmlText"></span>
            <div v-else-if="message.type == MESSAGE_TYPE.IMAGE" class="message-image">
              <div v-loading="sending" :element-loading-text="'发送中'" element-loading-background="rgba(0, 0, 0, 0.4)">
                <img class="send-image" :src="contentData.thumbUrl" loading="lazy" @click="showFullImageBox" />
              </div>
            </div>
            <div v-else-if="message.type == MESSAGE_TYPE.VIDEO" class="message-video">
              <video class="send-video" controls preload="none" :poster="contentData.coverUrl" :src="contentData.videoUrl" />
            </div>
            <div v-else-if="message.type == MESSAGE_TYPE.FILE" class="message-file">
              <div v-loading="sending" class="chat-file-box">
                <div class="chat-file-info">
                  <el-link class="chat-file-name" :underline="true" type="primary" target="_blank" :href="contentData.url" :download="contentData.name">
                    {{ contentData.name }}
                  </el-link>
                  <div class="chat-file-size">{{ fileSize }}</div>
                </div>
                <div class="chat-file-icon icon iconfont icon-message-file" />
              </div>
            </div>
            <ChatVoiceMessage v-else-if="message.type == MESSAGE_TYPE.AUDIO" mode="history" :url="contentData.url" :duration="contentData.duration" />
            <div v-else-if="message.type == MESSAGE_TYPE.ACT_RT_VOICE" class="message-text">{{ '[语音通话]' }}</div>
            <div v-else-if="message.type == MESSAGE_TYPE.ACT_RT_VIDEO" class="message-text">{{ '[视频通话]' }}</div>
            <div v-else class="message-text">{{ '[暂不支持该消息类型]' }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import HeadImage from '@/components/common/HeadImage.vue';
import ChatVoiceMessage from '@/components/chat/ChatVoiceMessage.vue';
import type { ChatMessage } from '@/types';
import { MESSAGE_STATUS, MESSAGE_TYPE } from '@/utils/enums';
import { toTimeText } from '@/utils/date';
import { transform } from '@/utils/emotion';
import eventBus from '@/utils/eventBus';
import { html2Escape } from '@/utils/str';
import { replaceURLWithHTMLLinks } from '@/utils/url';
const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  headImage: {
    type: String,
    default: ''
  },
  showName: {
    type: String,
    required: true
  },
  message: {
    type: Object as () => ChatMessage,
    required: true
  }
});
const sending = computed(() => props.message.status == MESSAGE_STATUS.SENDING);
const contentData = computed(() => JSON.parse(props.message.content || '{}'));

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

const htmlText = computed(() => {
  let text = html2Escape(props.message.content);
  text = replaceURLWithHTMLLinks(text, '');
  return transform(text, 'emoji-normal');
});

const showFullImageBox = () => {
  const imageUrl = contentData.value.originUrl;
  if (!imageUrl) {
    return;
  }
  eventBus.emit('openFullImage', {
    convKey: props.message.convKey,
    url: imageUrl,
    seqNo: props.message.seqNo,
    localId: props.message.localId
  });
};
</script>
<style lang="scss" scoped>
.chat-history-item {
  padding: 2px 8px;
  width: 620px;
  cursor: pointer;
  border-radius: 10px;
  margin-right: 20px;

  &:hover {
    background: var(--im-background-active);
  }

  &.active {
    background: var(--im-background-active-dark);
  }

  .message-normal {
    position: relative;
    font-size: 0;
    padding-left: 48px;
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
        color: var(--im-text-color-light);
        font-size: var(--im-font-size);
        line-height: 20px;

        span {
          margin-right: 12px;
        }
      }

      .bottom {
        padding-right: 100px;
        margin-top: 1px;

        .message-text {
          display: inline-block;
          position: relative;
          line-height: 26px;
          border-radius: 10px;
          font-size: var(--im-font-size);
          text-align: left;
          white-space: pre-wrap;
          word-break: break-word;
        }

        .message-image {
          display: flex;
          flex-wrap: nowrap;
          flex-direction: row;
          align-items: center;

          .send-image {
            min-width: 100px;
            min-height: 75px;
            max-width: 200px;
            max-height: 150px;
            border-radius: 8px;
          }
        }

        .message-video {
          display: flex;
          flex-wrap: nowrap;
          flex-direction: row;
          align-items: center;

          .send-video {
            min-width: 100px;
            min-height: 75px;
            max-width: 200px;
            max-height: 150px;
            border-radius: 8px;
            overflow: hidden;
            object-fit: contain;
          }
        }

        .message-file {
          display: flex;
          flex-wrap: nowrap;
          flex-direction: row;
          align-items: center;
          margin-bottom: 2px;

          .chat-file-box {
            display: flex;
            flex-wrap: nowrap;
            align-items: center;
            box-shadow: var(--im-box-shadow-light);
            border-radius: 4px;
            padding: 15px;

            .chat-file-info {
              flex: 1;
              height: 100%;
              text-align: left;
              font-size: 14px;
              margin-right: 10px;

              .chat-file-name {
                display: inline-block;
                min-width: 160px;
                max-width: 400px;
                font-size: 14px;
                margin-bottom: 4px;
                white-space: pre-wrap;
                word-break: break-all;
                color: #2830d3;

                &:hover {
                  text-decoration: underline;
                }
              }

              .chat-file-size {
                font-size: var(--im-font-size-smaller);
                color: var(--im-text-color-light);
              }
            }

            .chat-file-icon {
              font-size: 30px;
              color: #d42e07;
            }
          }
        }
      }
    }
  }
}
</style>

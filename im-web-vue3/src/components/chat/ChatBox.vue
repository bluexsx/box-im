<template>
  <div class="chat-box" @click="onClickChatBox" @mousemove="readedMessage">
    <el-container>
      <el-header height="60px">
        <div class="title">{{ title }}</div>
        <el-icon class="btn-side" :title="isGroup ? '群聊信息' : '聊天信息'" @click="onClickMore"><MoreFilled /></el-icon>
      </el-header>
      <el-main class="main-wrap">
        <el-container>
          <el-container class="content-box">
            <el-main class="im-chat-main">
              <div ref="scrollBoxRef" class="im-chat-scroll" @scroll="onScroll">
                <div class="im-chat-box">
                  <div v-if="chatStore.loadingMessage && messages.length" class="loading-message-tip">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>加载中...</span>
                  </div>
                  <div v-for="m in messages" :id="String(m.localId)" :key="m.localId">
                    <ChatMessageItem
                      :ref="(el) => setMessageItemRef(m.localId!, el as InstanceType<typeof ChatMessageItem> | null)"
                      :active="activeMessageLocalId == m.localId"
                      :mine="m.sendId == mine.id"
                      :head-image="headImage(m)"
                      :show-name="showName(m)"
                      :conversation="conversation"
                      :message="m"
                      :group="group"
                      :group-member-map="groupMemberMap"
                      @copy="onCopyMessage"
                      @resend="onResendMessage"
                      @delete="onDeleteMessage"
                      @recall="onRecallMessage"
                      @downloadFile="onDownloadFile"
                      @downloadImage="onDownloadImage"
                      @call="onCall(m.type)"
                      @atMember="onAtMember"
                      @audioStateChange="onAudioStateChange" />
                  </div>
                </div>
              </div>
            </el-main>
            <div v-if="conversation.atMe || conversation.atAll" class="locate-tip" @click="scrollToAtMessage">有人@我</div>
            <div v-else-if="!chatStore.isInBottom" class="locate-tip" @click="onScrollToBottom">
              {{ chatStore.newMessageSize > 0 ? `${chatStore.newMessageSize}条新消息` : '回到底部' }}
            </div>
            <el-footer height="220px" class="im-chat-footer">
              <div class="chat-tool-bar">
                <div ref="emotionRef" title="表情" class="icon iconfont icon-emoji" @click.stop="showEmotionBox" />
                <div title="发送图片">
                  <FileUpload
                    action="/image/upload"
                    :max-size="5 * 1024 * 1024"
                    :file-types="['image/jpeg', 'image/png', 'image/jpg', 'image/webp', 'image/gif']"
                    @before="onImageBefore"
                    @success="onImageSuccess"
                    @fail="onImageFail">
                    <div class="icon iconfont icon-picture" />
                  </FileUpload>
                </div>
                <div title="发送文件">
                  <FileUpload action="/file/upload" :max-size="10 * 1024 * 1024" @before="onFileBefore" @success="onFileSuccess" @fail="onFileFail">
                    <div class="icon iconfont icon-floder" />
                  </FileUpload>
                </div>
                <div title="发送语音" class="icon iconfont icon-microphone" @click="showRecordBox" />
                <div
                  v-show="isGroup"
                  title="回执消息"
                  class="icon iconfont icon-receipt"
                  :class="{ 'chat-tool-active': isReceipt }"
                  @click="onSwitchReceipt" />
                <div v-show="isPrivate" title="语音通话" class="icon iconfont icon-rtc-voice" @click="showPrivateVideo('voice')" />
                <div v-show="isGroup" title="多人通话" class="icon iconfont icon-rtc-video" @click="onGroupVideo" />
                <div v-show="isPrivate" title="视频通话" class="icon iconfont icon-rtc-video" @click="showPrivateVideo('video')" />
                <div title="聊天记录" class="icon iconfont icon-chat-history" @click="showHistoryBox" />
              </div>
              <div class="send-content-area">
                <ChatInput ref="chatInputRef" :group="isGroup ? group : undefined" :group-members="groupMembers" @submit="sendMessage" />
                <div class="send-btn-area">
                  <el-button type="primary" :icon="Promotion" @click="notifySend">发送</el-button>
                </div>
              </div>
              <div v-if="notAllowInputTip" class="chat-editer-mask">
                <el-icon><Warning /></el-icon>
                <span>{{ notAllowInputTip }}</span>
              </div>
            </el-footer>
          </el-container>
          <el-aside v-if="showSide" class="chat-side-box" :class="{ fullscreen: configStore.fullScreen }" width="265px">
            <ChatGroupSide
              v-if="isGroup && group"
              :conversation="conversation"
              :group-id="group.id"
              :group-members="groupMembers"
              @reload="loadGroup(group.id)"
              @show-chat-history="showHistoryBox"
              @close="showSide = false" />
            <ChatPrivateSide
              v-if="isPrivate && userInfo"
              :conversation="conversation"
              :user-info="userInfo"
              @show-chat-history="showHistoryBox"
              @close="showSide = false" />
          </el-aside>
        </el-container>
      </el-main>
    </el-container>
    <ChatEmotion ref="emoBoxRef" @emotion="onEmotion" />
    <ChatRecord ref="recordRef" @send="onSendRecord" />
    <ChatHistory
      ref="chatHistoryRef"
      :conversation="conversation"
      :friend="friend"
      :group="group"
      :group-member-map="groupMemberMap"
      @locateInChat="locateMessage" />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Loading, MoreFilled, Promotion, Warning } from '@element-plus/icons-vue';
import ChatMessageItem from '@/components/chat/ChatMessageItem.vue';
import ChatInput, { type ChatInputSubmitItem } from '@/components/chat/ChatInput.vue';
import ChatEmotion from '@/components/chat/ChatEmotion.vue';
import ChatGroupSide from '@/components/chat/ChatGroupSide.vue';
import ChatPrivateSide from '@/components/chat/ChatPrivateSide.vue';
import ChatRecord from '@/components/chat/ChatRecord.vue';
import request from '@/utils/request';
import ChatHistory from '@/components/chat/ChatHistory.vue';
import FileUpload from '@/components/common/FileUpload.vue';
import { findUser } from '@/api/user';
import { findGroup as fetchGroup } from '@/api/group';
import type { UserVO } from '@/api/user/types';
import { sendPrivateMessage, readedPrivateMessage, getPrivateMaxReadedId, deletePrivateMessage, recallPrivateMessage } from '@/api/privateMessage';
import { sendGroupMessage, readedGroupMessage, deleteGroupMessage, recallGroupMessage } from '@/api/groupMessage';
import type { PrivateMessageDTO } from '@/api/privateMessage/types';
import type { GroupMessageDTO } from '@/api/groupMessage/types';
import { useChatStore } from '@/stores/chat';
import type { ChatMessage, Conversation, SendMessageDTO } from '@/types';
import { useUserStore } from '@/stores/user';
import { useGroupStore } from '@/stores/group';
import { useFriendStore } from '@/stores/friend';
import { useConfigStore } from '@/stores/config';
import { CONVERSATION_TYPE, MESSAGE_STATUS, MESSAGE_TYPE } from '@/utils/enums';
import { isAction, isNormal } from '@/utils/messageType';
import eventBus from '@/utils/eventBus';
import { getDB } from '@/db';
import nextSnowflakeId from '@/utils/snowflake';

type UploadFile = File & {
  message?: SendMessageDTO;
  localMessage?: ChatMessage;
  conversation?: Conversation;
};

const props = defineProps({
  conversation: {
    type: Object as () => Conversation,
    required: true
  }
});

const chatStore = useChatStore();
const userStore = useUserStore();
const groupStore = useGroupStore();
const friendStore = useFriendStore();
const configStore = useConfigStore();
const { messages, loading, pendingLocateMessage } = storeToRefs(chatStore);
const scrollBoxRef = ref<HTMLElement>();
const chatInputRef = ref<InstanceType<typeof ChatInput>>();
const emoBoxRef = ref<InstanceType<typeof ChatEmotion>>();
const recordRef = ref<InstanceType<typeof ChatRecord>>();
const emotionRef = ref<HTMLElement>();
const chatHistoryRef = ref<InstanceType<typeof ChatHistory>>();
const lastScrollTime = ref(0);
const isSending = ref(false); // 是否正在发消息
const showSide = ref(false); // 是否显示群聊信息栏
const activeMessageLocalId = ref<string>(); //选中消息
const userInfo = ref<UserVO>();
const isReceipt = ref(false);
const playingAudio = ref<InstanceType<typeof ChatMessageItem>>();
const messageItemRefs = new Map<string, InstanceType<typeof ChatMessageItem>>();
const reqQueue = ref<
  Array<{
    send: () => Promise<ChatMessage>;
    resolve: (m: ChatMessage) => void;
    reject: (e: unknown) => void;
  }>
>([]);

const mine = computed(() => userStore.userInfo);
const isPrivate = computed(() => props.conversation.type == CONVERSATION_TYPE.PRIVATE);
const isGroup = computed(() => props.conversation.type == CONVERSATION_TYPE.GROUP);
const group = computed(() => (isGroup.value ? groupStore.findGroup(props.conversation.targetId) : undefined));
const groupMembers = computed(() => group.value?.members || []);
const groupMemberMap = computed(() => new Map(groupMembers.value.map((m) => [m.userId, m])));
const friend = computed(() => (userInfo.value?.id ? friendStore.findFriend(userInfo.value.id) : undefined));
const isFriend = computed(() => friendStore.isFriend(userInfo.value?.id));

const notAllowInputTip = computed(() => {
  if (isGroup.value && group.value) {
    if (group.value.dissolve) {
      return '群聊已解散';
    }
    if (group.value.quit) {
      return '您已不在群聊中';
    }
    if (group.value.isBanned) {
      return '群聊已被封禁' + (group.value.reason ? `，原因：${group.value.reason}` : '');
    }
  } else if (userInfo.value?.isBanned) {
    return '对方账号已被封禁' + (userInfo.value.reason ? `，原因：${userInfo.value.reason}` : '');
  }
  return '';
});

const title = computed(() => {
  let name = props.conversation.showName;
  if (isGroup.value) {
    const size = groupMembers.value.filter((m) => !m.quit).length;
    name += `(${size})`;
  }
  return name;
});

const setMessageItemRef = (localId: string, el: InstanceType<typeof ChatMessageItem> | null) => {
  if (el) {
    messageItemRefs.set(localId, el);
  } else {
    messageItemRefs.delete(localId);
  }
};

const resetEditor = () => {
  nextTick(() => {
    chatInputRef.value?.clear();
    chatInputRef.value?.focus();
  });
};

const onClickChatBox = () => {
  // 关闭表情窗口
  emoBoxRef.value?.close();
  // 取消消息选中
  activeMessageLocalId.value = '';
  // 停止语音播放
  playingAudio.value?.stopPlayAudio();
  playingAudio.value = undefined;
};

const onSwitchReceipt = () => {
  isReceipt.value = !isReceipt.value;
};

const scrollToBottom = async () => {
  await nextTick();
  const div = scrollBoxRef.value;
  if (div) {
    div.scrollTop = div.scrollHeight;
    chatStore.setIsInBottom(true);
  }
};

const headImage = (message: ChatMessage) => {
  if (isGroup.value) {
    const member = groupMemberMap.value.get(message.sendId!);
    return member ? member.headImage || '' : '';
  }
  return message.selfSend ? mine.value.headImageThumb || '' : props.conversation.headImage || '';
};

const showName = (message?: ChatMessage) => {
  if (!message) return '';
  if (isGroup.value) {
    const member = groupMemberMap.value.get(message.sendId!);
    return member ? member.showNickName : message.sendNickName || '';
  }
  if (message.sendId == mine.value.id) {
    return mine.value.nickName;
  }
  return props.conversation.showName;
};

const fillTargetId = (message: SendMessageDTO, targetId: number) => {
  if (isGroup.value) {
    message.groupId = targetId;
  } else {
    message.recvId = targetId;
  }
};

const buildLocalMessage = (message: SendMessageDTO): ChatMessage => {
  const m = JSON.parse(JSON.stringify(message)) as ChatMessage;
  m.id = 0;
  m.convKey = props.conversation.key;
  m.seqNo = Math.max(1, props.conversation.maxSeqNo);
  m.sendId = mine.value.id;
  m.sendTime = new Date().getTime();
  m.status = MESSAGE_STATUS.SENDING;
  m.selfSend = true;
  if (isGroup.value) {
    (m as ChatMessage & { readedCount?: number }).readedCount = 0;
  }
  return m;
};

const insertMessage = async (message: ChatMessage) => {
  if (!chatStore.isInBottom) {
    await chatStore.resetMessages(props.conversation.key);
  }
  await chatStore.insertMessage(props.conversation.key, message);
  await chatStore.moveTop(props.conversation.key);
  await scrollToBottom();
};

const processReqQueue = () => {
  if (!reqQueue.value.length || isSending.value) {
    return;
  }
  isSending.value = true;
  const reqData = reqQueue.value.shift()!;
  reqData
    .send()
    .then((res) => reqData.resolve(res))
    .catch((e) => reqData.reject(e))
    .finally(() => {
      isSending.value = false;
      // 发送下一条请求
      processReqQueue();
    });
};

const sendMessageRequest = (message: SendMessageDTO) => {
  return new Promise<ChatMessage>((resolve, reject) => {
    // 请求入队列，防止请求"后发先至"，导致消息错序
    const send = () => {
      if (isPrivate.value) {
        return sendPrivateMessage(message as PrivateMessageDTO) as Promise<ChatMessage>;
      }
      return sendGroupMessage(message as GroupMessageDTO) as Promise<ChatMessage>;
    };
    reqQueue.value.push({ send, resolve, reject });
    processReqQueue();
  });
};

const processSendMessage = async (conv: Conversation, message: SendMessageDTO, localMessage: ChatMessage) => {
  // 发送
  const m = await sendMessageRequest(message).catch(async () => {
    localMessage.status = MESSAGE_STATUS.FAILED;
    await chatStore.updateMessage(conv.key, localMessage);
    return null;
  });
  if (m) {
    // 更新本地消息
    m.selfSend = true;
    m.convKey = conv.key;
    await chatStore.updateMessage(conv.key, m);
  }
};

const sendTextMessage = async (sendText: string, atUserIds: string[] = []) => {
  if (!sendText.trim()) {
    return;
  }
  const message: SendMessageDTO = {
    localId: nextSnowflakeId(),
    content: sendText,
    type: MESSAGE_TYPE.TEXT
  };
  // 填充对方id
  fillTargetId(message, props.conversation.targetId);
  // 被@人员列表
  if (isGroup.value) {
    message.atUserIds = atUserIds.map((id) => Number(id));
    message.receipt = isReceipt.value;
  }
  // 本地消息
  const localMessage = buildLocalMessage(message);
  // 清空标志
  isReceipt.value = false;
  await insertMessage(localMessage);
  // 发送
  await processSendMessage(props.conversation, message, localMessage);
};

const getImageSize = (file: File): Promise<{ width: number; height: number }> => {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload = () => resolve({ width: img.width, height: img.height });
    img.onerror = () => resolve({ width: 0, height: 0 });
    img.src = URL.createObjectURL(file);
  });
};

const onImageBefore = async (file: UploadFile) => {
  const url = URL.createObjectURL(file);
  const data: Record<string, unknown> = { originUrl: url, thumbUrl: url };
  const message = {
    localId: nextSnowflakeId(),
    content: JSON.stringify(data),
    type: MESSAGE_TYPE.IMAGE,
    receipt: isReceipt.value
  };
  // 填充对方id
  fillTargetId(message, props.conversation.targetId);
  isReceipt.value = false;
  // 本地消息
  const localMessage = buildLocalMessage(message);
  // 插入消息
  await insertMessage(localMessage);
  // 借助file对象保存
  file.message = message;
  file.localMessage = localMessage;
  file.conversation = props.conversation;
  const size = await getImageSize(file);
  // 更新图片尺寸
  data.width = size.width;
  data.height = size.height;
  localMessage.content = JSON.stringify(data);
  await chatStore.updateMessage(props.conversation.key, localMessage);
};

const onImageSuccess = async (data: Record<string, unknown>, file: UploadFile) => {
  if (!file.message || !file.localMessage || !file.conversation) {
    return;
  }
  file.message.content = JSON.stringify(data);
  await processSendMessage(file.conversation, file.message, file.localMessage);
};

const onImageFail = async (_e: unknown, file: UploadFile) => {
  if (!file.localMessage) {
    return;
  }
  file.localMessage.status = MESSAGE_STATUS.FAILED;
  await chatStore.updateMessage(props.conversation.key, file.localMessage);
};

const onFileBefore = async (file: UploadFile) => {
  const url = URL.createObjectURL(file);
  const data = { name: file.name, size: file.size, url };
  const message = {
    localId: nextSnowflakeId(),
    content: JSON.stringify(data),
    type: MESSAGE_TYPE.FILE,
    receipt: isReceipt.value
  };
  // 填充对方id
  fillTargetId(message, props.conversation.targetId);
  isReceipt.value = false;
  // 本地消息
  const localMessage = buildLocalMessage(message);
  // 插入消息
  await insertMessage(localMessage);
  // 借助file对象保存
  file.message = message;
  file.localMessage = localMessage;
  file.conversation = props.conversation;
};

const onFileSuccess = async (uploadedUrl: string, file: UploadFile) => {
  if (!file.message || !file.localMessage || !file.conversation) {
    return;
  }
  const data = { name: file.name, size: file.size, url: uploadedUrl };
  file.message.content = JSON.stringify(data);
  await processSendMessage(file.conversation, file.message, file.localMessage);
};

const onFileFail = async (_e: unknown, file: UploadFile) => {
  if (!file.localMessage) {
    return;
  }
  file.localMessage.status = MESSAGE_STATUS.FAILED;
  await chatStore.updateMessage(props.conversation.key, file.localMessage);
};

const sendFileMessage = async (file: File) => {
  const uploadFile = file as UploadFile;
  await onFileBefore(uploadFile);
  const formData = new FormData();
  formData.append('file', file);
  try {
    const uploadedUrl = await request<string>({
      url: '/file/upload?isPermanent=false',
      data: formData,
      method: 'post',
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    await onFileSuccess(uploadedUrl, uploadFile);
  } catch (e) {
    await onFileFail(e, uploadFile);
  }
};

const sendMessage = async (fullList: ChatInputSubmitItem[]) => {
  resetEditor();
  readedMessage();
  const sendTextPrefix = isReceipt.value ? '【回执消息】' : '';
  for (const msg of fullList) {
    if (msg.type === 'text') {
      await sendTextMessage(sendTextPrefix + msg.content, msg.atUserIds);
    } else if (msg.type === 'image') {
      await sendImageMessage(msg.content.file);
    } else if (msg.type === 'file') {
      await sendFileMessage(msg.content.file);
    }
  }
};

const sendImageMessage = async (file: File) => {
  const uploadFile = file as UploadFile;
  await onImageBefore(uploadFile);
  const formData = new FormData();
  formData.append('file', file);
  try {
    const data = await request<Record<string, unknown>>({
      url: '/image/upload?isPermanent=false',
      data: formData,
      method: 'post',
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    await onImageSuccess(data, uploadFile);
  } catch (e) {
    await onImageFail(e, uploadFile);
  }
  nextTick(() => chatInputRef.value?.focus());
  await scrollToBottom();
};

const notifySend = () => {
  chatInputRef.value?.submit();
};

const onResendMessage = async (message: ChatMessage) => {
  if (message.type != MESSAGE_TYPE.TEXT) {
    ElMessage.error('该消息不支持自动重新发送，建议手动重新发送');
    return;
  }
  // 删除旧消息
  await chatStore.deleteMessage(props.conversation.key, message);
  // 重新推送
  const sendMessage = JSON.parse(JSON.stringify(message)) as SendMessageDTO;
  sendMessage.localId = nextSnowflakeId();
  const localMessage = buildLocalMessage(sendMessage);
  await insertMessage(localMessage);
  await processSendMessage(props.conversation, sendMessage, localMessage);
};

const onCopyMessage = (message: ChatMessage) => {
  // 使用现代浏览器的 Clipboard API
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard
      .writeText(message.content)
      .then(() => {
        ElMessage.success('内容已复制到剪贴板');
      })
      .catch(() => {
        ElMessage.error('复制失败，请手动复制');
      });
  } else {
    ElMessage.error('复制失败，请手动复制');
  }
};

const onDeleteMessage = (message: ChatMessage) => {
  ElMessageBox.confirm('确认删除消息?', '删除消息', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const convKey = props.conversation.key;
    if (message.id) {
      const data = { chatId: props.conversation.targetId, messageIds: [message.id] };
      if (isGroup.value) {
        await deleteGroupMessage(data);
      } else {
        await deletePrivateMessage(data);
      }
    }
    await chatStore.deleteMessage(convKey, message);
  });
};

const onRecallMessage = (message: ChatMessage) => {
  ElMessageBox.confirm('确认撤回消息?', '撤回消息', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const convKey = props.conversation.key;
    let m: ChatMessage;
    if (isGroup.value) {
      m = (await recallGroupMessage(message.id)) as ChatMessage;
    } else {
      m = (await recallPrivateMessage(message.id)) as ChatMessage;
    }
    ElMessage.success('消息已撤回');
    m.selfSend = true;
    await chatStore.recallMessage(convKey, m);
  });
};

const onDownloadFile = (message: ChatMessage) => {
  const data = JSON.parse(String(message.content || '{}'));
  const url = data.url;
  const name = data.name;
  download(url, name);
};

const genFileName = (fileUrl: string) => {
  try {
    const path = fileUrl.split('?')[0];
    const name = path.substring(path.lastIndexOf('/') + 1);
    if (name && /\.(png|jpe?g|gif|webp|bmp)$/i.test(name)) {
      return name;
    }
  } catch {
    // ignore
  }
  return `image_${Date.now()}.jpg`;
};

const download = (href: string, fileName: string) => {
  const a = document.createElement('a');
  a.href = href;
  a.download = fileName;
  a.target = '_blank';
  a.rel = 'noopener';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};

const onDownloadImage = async (message: ChatMessage) => {
  const data = JSON.parse(String(message.content || '{}'));
  const fileUrl = data.originUrl;
  const fileName = genFileName(fileUrl);
  try {
    const res = await fetch(fileUrl);
    if (!res.ok) throw new Error('fetch failed');
    const blob = await res.blob();
    const objectUrl = URL.createObjectURL(blob);
    download(objectUrl, fileName);
    URL.revokeObjectURL(objectUrl);
  } catch {
    download(fileUrl, fileName);
  }
};

const scrollToAtMessage = async () => {
  if (props.conversation.lastAtMessageId < 0) {
    return;
  }
  const atMessage = await getDB().findMessageById(props.conversation.lastAtMessageId!, props.conversation.key);
  if (!atMessage) {
    ElMessage.error('无法定位原消息');
    return;
  }
  await locateMessage(atMessage);
  await chatStore.resetAtMessage(props.conversation.key);
};

const showPrivateVideo = (mode: string) => {
  if (!isFriend.value) {
    ElMessage.error('你已不是对方好友,无法呼叫');
    return;
  }
  if (userStore.isInRtc) {
    ElMessage.error('您已在通话中');
    return;
  }
  eventBus.emit('openPrivateVideo', {
    mode,
    isHost: true,
    friend: friend.value
  }); // 通过home.vue打开单人视频窗口
};

const onGroupVideo = () => {
  // 开源版：打开多人通话说明弹窗（商业版引流入口）
  eventBus.emit('openGroupVideo');
};

const onCall = (type?: number) => {
  if (type == MESSAGE_TYPE.ACT_RT_VOICE) {
    showPrivateVideo('voice');
  } else if (type == MESSAGE_TYPE.ACT_RT_VIDEO) {
    showPrivateVideo('video');
  }
};

const onAtMember = (member: { userId: number; showNickName: string }) => {
  chatInputRef.value?.insertAtMember({
    userId: member.userId,
    showNickName: member.showNickName
  } as never);
};

const onAudioStateChange = (state: string, message: ChatMessage) => {
  if (state !== 'PLAYING') {
    return;
  }
  const item = messageItemRefs.get(message.localId!);
  if (item && item !== playingAudio.value) {
    playingAudio.value?.stopPlayAudio();
    playingAudio.value = item;
  }
};

const onClickMore = () => {
  showSide.value = !showSide.value;
  if (showSide.value && isGroup.value && group.value?.id) {
    // 刷新一下群和成员信息
    loadGroup(group.value.id);
  }
};

const showRecordBox = () => {
  recordRef.value?.open();
};

const onSendRecord = async (data: { duration: number; url: string }) => {
  const message = {
    localId: nextSnowflakeId(),
    content: JSON.stringify(data),
    type: MESSAGE_TYPE.AUDIO,
    receipt: isReceipt.value
  };
  // 填充对方id
  fillTargetId(message, props.conversation.targetId);
  isReceipt.value = false;
  // 本地消息
  const localMessage = buildLocalMessage(message);
  await insertMessage(localMessage);
  await processSendMessage(props.conversation, message, localMessage);
  chatInputRef.value?.focus();
};

const showEmotionBox = () => {
  const el = emotionRef.value;
  if (!el) {
    return;
  }
  const rect = el.getBoundingClientRect();
  emoBoxRef.value?.open({ x: rect.left + rect.width / 2, y: rect.top });
};

const onEmotion = (emotionText: string) => {
  chatInputRef.value?.insertEmoji(emotionText);
};

const showHistoryBox = () => {
  chatHistoryRef.value?.open();
};

const loadGroup = async (id: number) => {
  const groupData = await fetchGroup(id);
  await chatStore.updateFromGroup(groupData);
  groupStore.updateGroup(groupData);
  await groupStore.refreshMember(id);
};

const updateFriendInfo = async () => {
  if (isFriend.value && friend.value) {
    // store的数据不能直接修改，深拷贝一份store的数据
    const f = JSON.parse(JSON.stringify(friend.value));
    f.headImage = userInfo.value?.headImageThumb;
    f.nickName = userInfo.value?.nickName;
    await chatStore.updateFromFriend(f);
    friendStore.updateFriend(f);
  } else if (userInfo.value) {
    await chatStore.updateFromUser(userInfo.value);
  }
};

const loadFriend = async (friendId: number) => {
  // 获取好友信息
  userInfo.value = await findUser(friendId);
  await updateFriendInfo();
};

const locateMessage = async (message: ChatMessage) => {
  const localId = message.localId;
  const locateMsg = await getDB().findMessageByLocalId(localId!);
  if (!locateMsg || locateMsg.deleted || locateMsg.status == MESSAGE_STATUS.RECALL) {
    ElMessage.error('无法定位原消息');
    return;
  }
  await chatStore.locateToMessage(props.conversation.key, locateMsg);
  // 定位消息
  scrollToMessage(localId!, 100, 0);
  // 选中消息
  activeMessageLocalId.value = localId!;
  // 设置底部标记
  chatStore.setIsInBottom(!chatStore.hasMoreNextMessage);
  // 关闭聊天记录
  chatHistoryRef.value?.close();
};

const scrollToMessage = (id: string | number, delay: number, times: number) => {
  setTimeout(() => {
    const messageItem = document.getElementById(String(id));
    if (messageItem) {
      messageItem.scrollIntoView({ behavior: 'smooth' });
    } else if (times < 3) {
      scrollToMessage(id, delay * 3, times + 1);
    }
  }, delay);
};

const readedMessage = async () => {
  if (props.conversation.unreadCount > 0) {
    const convKey = props.conversation.key;
    const tid = props.conversation.targetId;
    const messageId = props.conversation.maxMessageId;
    await chatStore.resetUnreadCount(convKey);
    if (isGroup.value) {
      await readedGroupMessage(tid, messageId);
    } else {
      await readedPrivateMessage(tid, messageId);
    }
  }
};

const loadReaded = async (friendId: number) => {
  const messageId = await getPrivateMaxReadedId(friendId);
  await chatStore.readedMessage(props.conversation.key, messageId);
};

const onScroll = async (e: Event) => {
  const scrollElement = e.target as HTMLElement;
  const scrollTop = scrollElement.scrollTop;
  // 滚到顶部
  if (scrollTop < 30) {
    if (new Date().getTime() - lastScrollTime.value < 500) {
      return;
    }
    lastScrollTime.value = new Date().getTime();
    if (!chatStore.hasMoreLastMessage) {
      ElMessage.success('没有更多历史消息');
      return;
    }
    const hst = scrollElement.scrollHeight;
    await chatStore.loadLastPageMessage(props.conversation.key, 30);
    await nextTick();
    // 恢复滚动条位置
    scrollElement.scrollTop = scrollElement.scrollHeight - hst;
    // 清除底部标志
    chatStore.setIsInBottom(false);
  }
  // 滚到底部
  if (scrollTop + scrollElement.clientHeight >= scrollElement.scrollHeight - 30) {
    if (new Date().getTime() - lastScrollTime.value < 500) {
      return;
    }
    lastScrollTime.value = new Date().getTime();
    if (chatStore.hasMoreNextMessage) {
      // 向下翻页
      await chatStore.loadNextPageMessage(props.conversation.key, 30);
    }
    // 设置底部标志
    chatStore.setIsInBottom(!chatStore.hasMoreNextMessage);
  }
};

const onScrollToBottom = async () => {
  await chatStore.resetMessages(props.conversation.key);
  await scrollToBottom();
};

const onNewMessage = async (message: ChatMessage) => {
  // 收到新消息,则滚动至底部
  if (isNormal(message.type) || isAction(message.type)) {
    // 新消息来时，如果用户本来就在底部不远位置，则直接拉到底部
    if (chatStore.isInBottom || message.selfSend) {
      await scrollToBottom();
    }
  }
};

const initConversation = async () => {
  userInfo.value = undefined;
  showSide.value = false;
  isReceipt.value = false;
  emoBoxRef.value?.close();
  if (isGroup.value) {
    await loadGroup(props.conversation.targetId);
  } else if (isPrivate.value) {
    await loadFriend(props.conversation.targetId);
    // 加载已读状态
    await loadReaded(props.conversation.targetId);
  }
  // 重置消息
  await chatStore.resetMessages(props.conversation.key);
  // 滚到底部
  await scrollToBottom();
  // 有时页面渲染得慢，会导致无法正常滚到底部，这里再滚一次
  setTimeout(() => scrollToBottom(), 100);
  // 消息已读
  await readedMessage();
};

watch(
  () => props.conversation.key,
  async (newKey) => {
    await initConversation();
    const locateMsg = pendingLocateMessage.value;
    if (locateMsg && locateMsg.convKey == newKey) {
      await locateMessage(locateMsg);
    }
    chatStore.removePendingLocateMessage();
  },
  { immediate: true }
);

watch(loading, async (newLoading) => {
  if (newLoading) return;
  // 断线重连后，需要更新一下已读状态
  if (isPrivate.value) {
    await loadReaded(props.conversation.targetId);
  }
  // 如果用户所在的会话拉到了新的离线消息，需重置会话内的消息，否则新消息会不显示
  if (chatStore.hasMoreLastMessage) {
    await chatStore.resetMessages(props.conversation.key);
    await scrollToBottom();
  }
});

onMounted(() => {
  eventBus.on('newMessage', onNewMessage);
  eventBus.on('locateChatMessage', locateMessage);
});

onUnmounted(() => {
  eventBus.off('newMessage', onNewMessage);
  eventBus.off('locateChatMessage', locateMessage);
});
</script>

<style scoped lang="scss">
.chat-box {
  position: relative;
  width: 100%;
  height: 100%;

  :deep(> .el-container) {
    height: 100%;
  }

  .main-wrap {
    padding: 0;
  }

  :deep(.el-header) {
    display: flex;
    padding: 0 12px;
    line-height: 60px;
    border-bottom: var(--im-border);

    .title {
      font-size: var(--im-font-size-larger);
    }

    .btn-side {
      position: absolute;
      right: 20px;
      top: 0;
      height: 60px;
      font-size: 20px;
      cursor: pointer;
      color: var(--im-text-color-light);
    }
  }

  .content-box {
    position: relative;
    height: 100%;

    .im-chat-main {
      padding: 0;
      background: #f6f7f8;

      .im-chat-scroll {
        height: 100%;
        overflow-y: auto;
        padding: 0 10px;
      }

      .im-chat-box {
        position: relative;

        .loading-message-tip {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
          padding: 10px 0 6px;
          font-size: var(--im-font-size-small);
          color: var(--im-text-color-light);
        }
      }
    }

    .locate-tip {
      text-align: center;
      position: absolute;
      right: 20px;
      bottom: 230px;
      color: var(--im-color-primary);
      font-size: var(--im-font-size);
      font-weight: 600;
      background: white;
      padding: 8px 16px;
      border-radius: 18px;
      cursor: pointer;
      z-index: 99;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;
      border: 1px solid rgba(0, 0, 0, 0.06);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      }
    }

    .im-chat-footer {
      position: relative;
      display: flex;
      flex-direction: column;
      padding: 0;

      .chat-tool-bar {
        display: flex;
        position: relative;
        width: 100%;
        height: 44px;
        text-align: left;
        box-sizing: border-box;
        border-top: 2px solid #ebeef5;
        padding: 6px 8px;
        align-items: center;
        background: var(--im-background-active);
        color: black;
        gap: 14px;
        opacity: 0.85;
        > div,
        > .el-icon {
          font-size: 19px;
          cursor: pointer;
          width: 32px;
          height: 32px;
          line-height: 32px;
          text-align: center;
          border-radius: 6px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          display: flex;
          align-items: center;
          justify-content: center;
          position: relative;

          &.chat-tool-active {
            color: var(--im-color-primary);
            background: var(--im-background-active-dark);
            transform: scale(1.02);
          }

          &:hover {
            color: var(--im-color-primary);
            background: var(--im-background-active);
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }

          .el-icon,
          .iconfont {
            font-size: inherit;
          }

          .el-upload {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
          }
        }
      }

      .send-content-area {
        position: relative;
        display: flex;
        flex-direction: column;
        flex: 1;
        background-color: white !important;
        min-height: 140px;

        .send-btn-area {
          padding: 10px;
          position: absolute;
          bottom: 4px;
          right: 6px;
        }
      }

      .chat-editer-mask {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: #f8f8f8d0;
        font-size: var(--im-font-size-large);
        color: var(--im-text-color-light);
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 10px;
        border: 1px solid #ddd;
        z-index: 10;

        .icon {
          font-size: var(--im-font-size-larger);
          margin-right: 3px;
        }
      }
    }
  }

  .chat-side-box {
    border-left: var(--im-border);
    background: #f6f7f8;

    &.fullscreen {
      width: 335px !important;
    }
  }
}
</style>

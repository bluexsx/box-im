<template>
  <div class="home-page" @click="closeAllBox" @contextmenu="closeAllBox">
    <div class="app-container" :class="{ fullscreen: fullScreen }">
      <div class="navi-bar">
        <div class="navi-bar-box">
          <div class="top">
            <div class="avater">
              <HeadImage :name="mine.nickName" :size="42" :url="mine.headImageThumb" :is-show-user-info="false" @click="goSetting" />
            </div>
            <div class="menu">
              <router-link class="link" to="/home/chat">
                <div class="menu-item">
                  <span class="icon iconfont icon-chat" />
                  <div v-show="unreadCount > 0" class="unread-text">{{ unreadCount }}</div>
                </div>
              </router-link>
              <router-link class="link" to="/home/friend">
                <div class="menu-item">
                  <span class="icon iconfont icon-friend" />
                </div>
              </router-link>
              <router-link class="link" to="/home/group">
                <div class="menu-item">
                  <span class="icon iconfont icon-group" style="font-size: 28px" />
                </div>
              </router-link>
              <router-link class="link" to="/home/setting">
                <div class="menu-item">
                  <span class="icon iconfont icon-setting" style="font-size: 20px" />
                </div>
              </router-link>
            </div>
          </div>
          <div class="bottom">
            <div class="bottom-item" @click="onSwitchFullScreen">
              <el-icon><FullScreen /></el-icon>
            </div>
            <div class="bottom-item" :title="'退出'" @click="onExit">
              <span class="icon iconfont icon-exit" />
            </div>
          </div>
        </div>
      </div>
      <div class="content-box">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </div>
    </div>
    <UserInfo ref="userInfoRef" />
    <FullImage ref="fullImageRef" />
    <GroupInfo ref="groupInfoRef" />
    <RtcPrivateVideo ref="rtcPrivateVideoRef" />
    <RtcGroupVideo ref="rtcGroupVideoRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { ElMessage, ElMessageBox } from 'element-plus';
import { FullScreen } from '@element-plus/icons-vue';
import HeadImage from '@/components/common/HeadImage.vue';
import UserInfo from '@/components/common/UserInfo.vue';
import FullImage from '@/components/common/FullImage.vue';
import GroupInfo from '@/components/group/GroupInfo.vue';
import RtcPrivateVideo from '@/components/rtc/RtcPrivateVideo.vue';
import type { PrivateRtcInfo } from '@/components/rtc/RtcPrivateVideo.vue';
import RtcGroupVideo from '@/components/rtc/RtcGroupVideo.vue';
import { loadPrivateOfflineMessage } from '@/api/privateMessage';
import type { PrivateMessageVO } from '@/api/privateMessage/types';
import { loadGroupOfflineMessage } from '@/api/groupMessage';
import type { GroupMessageVO } from '@/api/groupMessage/types';
import type { GroupVO } from '@/api/group/types';
import { initDB, getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import type { ChatMessage, Conversation } from '@/types';
import type { FriendVO } from '@/api/friend/types';
import { useFriendStore } from '@/stores/friend';
import { useGroupStore } from '@/stores/group';
import { useUserStore } from '@/stores/user';
import { useConfigStore } from '@/stores/config';
import * as auth from '@/utils/auth';
import eventBus, { type EventBusEvents, type FullImagePayload } from '@/utils/eventBus';
import * as wsApi from '@/utils/wssocket';
import { MESSAGE_TYPE, MESSAGE_STATUS, CONVERSATION_TYPE } from '@/utils/enums';
import { isNormal, isTip, isAction, isRtcPrivate, isRtcGroup } from '@/utils/messageType';
import { previewContent } from '@/utils/messageUtil';
import { setTitleTip } from '@/utils/title';
import tipAudioUrl from '@/assets/audio/tip.mp3';

const router = useRouter();
const chatStore = useChatStore();
const friendStore = useFriendStore();
const groupStore = useGroupStore();
const userStore = useUserStore();
const configStore = useConfigStore();
const { fullScreen } = storeToRefs(configStore);
const { userInfo } = storeToRefs(userStore);
const mine = computed(() => userInfo.value);
const reconnecting = ref(false);
const privateMessagesBuffer = ref<ChatMessage[]>([]);
const groupMessagesBuffer = ref<ChatMessage[]>([]);
let audio: HTMLAudioElement | null = null;
let lastPlayAudioTime = Date.now() - 1000;
const rtcPrivateVideoRef = ref<InstanceType<typeof RtcPrivateVideo>>();
const rtcGroupVideoRef = ref<InstanceType<typeof RtcGroupVideo>>();
const userInfoRef = ref<InstanceType<typeof UserInfo>>();
const fullImageRef = ref<InstanceType<typeof FullImage>>();
const groupInfoRef = ref<InstanceType<typeof GroupInfo>>();

const unreadCount = computed(() => {
  let count = 0;
  chatStore.conversations.forEach((conv) => {
    if (!conv.isDnd) {
      count += conv.unreadCount;
    }
  });
  return count;
});

const unloadStore = () => {
  friendStore.clear();
  groupStore.clear();
  chatStore.clear();
  userStore.clear();
};

const onExit = () => {
  unloadStore();
  wsApi.close(3000);
  auth.clearLoginSession(true);
  try {
    getDB().close();
  } catch {
    // DB 未初始化时忽略
  }
  void router.push('/login');
};

const loadFriendInfo = (id: number): FriendVO => {
  const friend = friendStore.findFriend(id);
  if (!friend) {
    return {
      id,
      nickName: '未知用户',
      headImage: ''
    };
  }
  return friend;
};

const loadGroupInfo = (id: number): GroupVO => {
  const group = groupStore.findGroup(id);
  if (!group) {
    return {
      id,
      name: '未知群聊',
      showGroupName: '未知群聊',
      headImageThumb: ''
    };
  }
  return group;
};

const playAudioTip = () => {
  // 防止过于密集播放
  if (Date.now() - lastPlayAudioTime > 1000) {
    lastPlayAudioTime = Date.now();
    if (!audio) {
      audio = new Audio();
      audio.src = tipAudioUrl;
    }
    void audio.play();
  }
};

const handlePrivateOfflineMessage = async (messages: PrivateMessageVO[]) => {
  if (!messages?.length) {
    return;
  }
  // 会话信息
  const conversationMap = new Map<number, Conversation>();
  // 离线消息,map结构方便查询
  const messageMap = new Map(messages.map((m) => [m.id, m as ChatMessage]));
  // 处理过程中衍生的需要入库的事消息
  const tmpMessages: ChatMessage[] = [];
  const userId = mine.value.id;
  for (const raw of messages) {
    const m = raw as ChatMessage;
    // 标记这条消息是不是自己发的
    m.selfSend = m.sendId == userId;
    // 好友id
    const friendId = m.selfSend ? m.recvId! : m.sendId!;
    // 标记消息所属会话id
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, friendId);
    m.convKey = convKey;
    // 查询会话
    let conversation = conversationMap.get(friendId);
    if (!conversation) {
      // 查db
      const found = await getDB().findConversationByKey(convKey);
      if (found) {
        conversation = found;
      } else {
        // 创建新会话
        const friend = loadFriendInfo(friendId);
        conversation = {
          key: convKey,
          type: CONVERSATION_TYPE.PRIVATE,
          targetId: friend.id,
          showName: friend.nickName,
          headImage: friend.headImage,
          isDnd: friend.isDnd,
          isTop: friend.isTop,
          lastContent: '',
          lastSendTime: Date.now(),
          optTime: Date.now(),
          unreadCount: 0,
          lastAtMessageId: -1,
          lastTimeTip: 0,
          maxMessageId: 0,
          minSeqNo: 0,
          maxSeqNo: 0,
          maxReadedId: 0
        };
      }
      conversationMap.set(friendId, conversation);
    }
    // 会话时间
    conversation.lastSendTime = Number(m.sendTime);
    conversation.optTime = Number(m.sendTime);
    // 记录会话最大消息id
    conversation.maxMessageId = Math.max(conversation.maxMessageId, m.id ?? 0);
    conversation.maxSeqNo = Math.max(conversation.maxSeqNo, m.seqNo ?? 0);
    // 未读数量更新
    if (m.selfSend || m.status == MESSAGE_STATUS.READED) {
      // 会话中存在自己发的消息或已读消息，说明自己已经进入过会话
      conversation.unreadCount = 0;
    } else if (m.status != MESSAGE_STATUS.RECALL && m.type != MESSAGE_TYPE.TIP_TEXT) {
      // 未读加1
      conversation.unreadCount = conversation.unreadCount + 1;
    }
    // 撤回消息
    if (m.type == MESSAGE_TYPE.RECALL) {
      const recallPayload = JSON.parse(m.content as string);
      const recallMessageId = Number(recallPayload.id);
      const recallMessageTip = recallPayload.tip || '';
      let recallMessage = messageMap.get(recallMessageId);
      if (!recallMessage) {
        recallMessage = await getDB().findMessageById(recallMessageId, convKey);
        if (!recallMessage) {
          continue;
        }
        tmpMessages.push(recallMessage);
      }
      // 把原消息改造成一条提示消息
      recallMessage.status = MESSAGE_STATUS.PENDING;
      recallMessage.content = recallMessageTip;
      recallMessage.type = MESSAGE_TYPE.TIP_TEXT;
      // 会话提示语
      conversation.lastContent = previewContent(recallMessage);
      conversation.sendNickName = '';
    } else if (m.status != MESSAGE_STATUS.RECALL) {
      // 会话列表内容
      conversation.lastContent = previewContent(m);
    }
  }
  // 批量保存会话和消息
  const conversations = Array.from(conversationMap.values());
  await getDB().saveConversationAndMessage(conversations, (messages as ChatMessage[]).concat(tmpMessages));
  chatStore.append(conversations);
};

const handleGroupOfflineMessage = async (messages: GroupMessageVO[]) => {
  if (!messages?.length) {
    return;
  }
  // 会话信息
  const conversationMap = new Map<number, Conversation>();
  // 离线消息,map结构方便查询
  const messageMap = new Map(messages.map((m) => [m.id, m as ChatMessage]));
  // 处理过程中衍生的需要入库的事消息
  const tmpMessages: ChatMessage[] = [];
  const userId = mine.value.id;
  for (const raw of messages) {
    const m = raw as ChatMessage;
    // 标记这条消息是不是自己发的
    m.selfSend = m.sendId == userId;
    const groupId = m.groupId!;
    // 标记消息所属会话id
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, groupId);
    m.convKey = convKey;
    // 查询会话
    let conversation = conversationMap.get(groupId);
    if (!conversation) {
      // 查db
      conversation = await getDB().findConversationByKey(convKey);
      if (!conversation) {
        // 创建新会话
        const group = loadGroupInfo(groupId);
        conversation = {
          key: convKey,
          type: CONVERSATION_TYPE.GROUP,
          targetId: group.id,
          showName: group.showGroupName,
          headImage: group.headImageThumb,
          isDnd: group.isDnd,
          isTop: group.isTop,
          lastContent: '',
          lastSendTime: Date.now(),
          optTime: Date.now(),
          unreadCount: 0,
          atMe: false,
          atAll: false,
          lastAtMessageId: -1,
          lastTimeTip: 0,
          maxMessageId: 0,
          minSeqNo: 0,
          maxSeqNo: 0,
          maxReadedId: 0
        };
      }
      conversationMap.set(groupId, conversation);
    }
    // 会话时间
    conversation.lastSendTime = Number(m.sendTime);
    conversation.optTime = Number(m.sendTime);
    // 记录会话最大消息id
    conversation.maxMessageId = Math.max(conversation.maxMessageId, m.id ?? 0);
    conversation.maxSeqNo = Math.max(conversation.maxSeqNo, m.seqNo ?? 0);
    // 未读数量更新
    if (m.selfSend || m.status == MESSAGE_STATUS.READED) {
      // 会话中存在自己发的消息或已读消息，说明自己已经进入过会话
      conversation.unreadCount = 0;
    } else if (m.status != MESSAGE_STATUS.RECALL && m.type != MESSAGE_TYPE.TIP_TEXT) {
      // 未读加1
      conversation.unreadCount = conversation.unreadCount + 1;
    }
    // 是否有人@我（已读、已撤回的消息不再设置）
    if (!m.selfSend && m.atUserIds && m.status != MESSAGE_STATUS.READED && m.status != MESSAGE_STATUS.RECALL) {
      if (userId != null && m.atUserIds.indexOf(userId) >= 0) {
        conversation.atMe = true;
        if (m.id != null) {
          conversation.lastAtMessageId = m.id;
        }
      }
      if (m.atUserIds.indexOf(-1) >= 0) {
        conversation.atAll = true;
        if (m.id != null) {
          conversation.lastAtMessageId = m.id;
        }
      }
    }
    // 撤回消息
    if (m.type == MESSAGE_TYPE.RECALL) {
      const recallPayload = JSON.parse(m.content as string);
      const recallMessageId = Number(recallPayload.id);
      const recallMessageTip = recallPayload.tip || '';
      let recallMessage = messageMap.get(recallMessageId);
      if (!recallMessage) {
        recallMessage = await getDB().findMessageById(recallMessageId, convKey);
        if (!recallMessage) {
          continue;
        }
        tmpMessages.push(recallMessage);
      }
      // 撤回的若是@我消息，清除标记
      if (conversation.lastAtMessageId == recallMessageId) {
        conversation.atMe = false;
        conversation.atAll = false;
        conversation.lastAtMessageId = -1;
      }
      // 改造成一条提示消息
      recallMessage.status = MESSAGE_STATUS.PENDING;
      recallMessage.content = recallMessageTip;
      recallMessage.type = MESSAGE_TYPE.TIP_TEXT;
      // 会话提示语
      conversation.lastContent = previewContent(recallMessage);
      conversation.sendNickName = '';
    } else if (m.status != MESSAGE_STATUS.RECALL) {
      // 会话列表内容
      conversation.lastContent = previewContent(m);
      // 其他成员发的消息显示发送昵称
      conversation.sendNickName = m.selfSend ? '' : m.sendNickName;
    }
  }
  const conversations = Array.from(conversationMap.values());
  await getDB().saveConversationAndMessage(conversations, (messages as ChatMessage[]).concat(tmpMessages));
  chatStore.append(conversations);
};

const insertPrivateMessage = async (friend: FriendVO, m: ChatMessage) => {
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, friend.id);
  // 打开会话
  await chatStore.openChat({
    type: CONVERSATION_TYPE.PRIVATE,
    targetId: friend.id,
    showName: friend.nickName,
    headImage: friend.headImage,
    isDnd: friend.isDnd,
    isTop: friend.isTop
  });
  // 插入消息
  await chatStore.insertMessage(convKey, m as ChatMessage);
  // 通知 chat-box 滚动
  if (chatStore.isActive(convKey)) {
    eventBus.emit('newMessage', m);
  }
  if (!friend.isDnd && !m.selfSend && m.type != null && isNormal(m.type) && m.status != MESSAGE_STATUS.READED) {
    // 播放提示音
    playAudioTip();
  }
};

const insertGroupMessage = async (group: GroupVO, m: ChatMessage) => {
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, group.id);
  // 打开会话
  await chatStore.openChat({
    type: CONVERSATION_TYPE.GROUP,
    targetId: group.id,
    showName: group.showGroupName,
    headImage: group.headImageThumb,
    isDnd: group.isDnd,
    isTop: group.isTop
  });
  // 插入消息
  await chatStore.insertMessage(convKey, m as ChatMessage);
  // 通知 chat-box 滚动
  if (chatStore.isActive(convKey)) {
    eventBus.emit('newMessage', m);
  }
  // 提示音
  if (!group.isDnd && !m.selfSend && !chatStore.loading && m.type != null && isNormal(m.type) && m.status != MESSAGE_STATUS.READED) {
    // 播放提示音
    playAudioTip();
  }
};

const handlePrivateMessage = async (m: ChatMessage) => {
  // 标记这条消息是不是自己发的
  m.selfSend = m.sendId == mine.value.id;
  // 好友id
  const friendId = m.selfSend ? m.recvId! : m.sendId!;
  // 会话信息
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, friendId);
  // 消息已读处理，清空已读数量
  if (m.type == MESSAGE_TYPE.READED) {
    await chatStore.resetUnreadCount(convKey);
    return;
  }
  // 消息回执处理,更新对方已读位置
  if (m.type == MESSAGE_TYPE.RECEIPT) {
    const messageId = JSON.parse(m.content).id;
    if (messageId > 0) {
      await chatStore.readedMessage(convKey, messageId);
    }
    return;
  }
  // 消息撤回
  if (m.type == MESSAGE_TYPE.RECALL) {
    await chatStore.recallMessage(convKey, m as ChatMessage);
    return;
  }
  // 新增好友
  if (m.type == MESSAGE_TYPE.FRIEND_NEW) {
    await friendStore.addFriend(JSON.parse(m.content as string));
    return;
  }
  // 删除好友
  if (m.type == MESSAGE_TYPE.FRIEND_DEL) {
    await friendStore.removeFriend(friendId);
    return;
  }
  // 好友在线状态
  if (m.type == MESSAGE_TYPE.FRIEND_ONLINE) {
    friendStore.updateOnlineStatus(JSON.parse(m.content as string));
    return;
  }
  // 对好友设置免打扰
  if (m.type == MESSAGE_TYPE.FRIEND_DND) {
    const isDnd = JSON.parse(m.content as string);
    await friendStore.setDnd(friendId, isDnd);
    await chatStore.setDnd(convKey, isDnd);
    return;
  }
  // 对好友设置会话置顶
  if (m.type == MESSAGE_TYPE.FRIEND_TOP) {
    const isTop = JSON.parse(m.content as string);
    await friendStore.setTop(friendId, isTop);
    await chatStore.setTop(convKey, isTop);
    return;
  }
  // 单人webrtc 信令
  if (m.type != null && isRtcPrivate(m.type)) {
    rtcPrivateVideoRef.value?.onRTCMessage(m as never);
    return;
  }
  // 消息插入
  if (m.type != null && (isNormal(m.type) || isTip(m.type) || isAction(m.type))) {
    const friend = loadFriendInfo(friendId);
    await insertPrivateMessage(friend, m);
  }
};

const handleGroupMessage = async (m: ChatMessage) => {
  // 标记这条消息是不是自己发的
  m.selfSend = m.sendId == mine.value.id;
  // 会话信息
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, m.groupId!);
  // 消息已读处理
  if (m.type == MESSAGE_TYPE.READED) {
    // 我已读对方的消息，清空已读数量
    await chatStore.resetUnreadCount(convKey);
    await chatStore.resetAtMessage(convKey);
    return;
  }
  // 消息回执处理
  if (m.type == MESSAGE_TYPE.RECEIPT) {
    // 更新消息已读人数
    await chatStore.updateMessage(convKey, {
      localId: m.localId,
      readedCount: m.readedCount,
      receiptOk: m.receiptOk
    } as ChatMessage);
    return;
  }
  // 消息撤回
  if (m.type == MESSAGE_TYPE.RECALL) {
    await chatStore.recallMessage(convKey, m as ChatMessage);
    return;
  }
  // 新增群
  if (m.type == MESSAGE_TYPE.GROUP_NEW) {
    await groupStore.addGroup(JSON.parse(m.content as string));
    return;
  }
  // 删除群
  if (m.type == MESSAGE_TYPE.GROUP_DEL) {
    await groupStore.removeGroup(m.groupId);
    return;
  }
  // 对群设置免打扰
  if (m.type == MESSAGE_TYPE.GROUP_DND) {
    const isDnd = JSON.parse(m.content as string);
    await groupStore.setDnd(m.groupId!, isDnd);
    await chatStore.setDnd(convKey, isDnd);
    return;
  }
  // 对群设置会话置顶
  if (m.type == MESSAGE_TYPE.GROUP_TOP) {
    const isTop = JSON.parse(m.content as string);
    await groupStore.setTop(m.groupId!, isTop);
    await chatStore.setTop(convKey, isTop);
    return;
  }
  // 群视频信令（开源版仅占位，不处理）
  if (m.type != null && isRtcGroup(m.type)) {
    rtcGroupVideoRef.value?.onRTCMessage();
    return;
  }
  // 插入群聊消息
  if (m.type != null && (isNormal(m.type) || isTip(m.type) || isAction(m.type))) {
    const group = loadGroupInfo(m.groupId!);
    await insertGroupMessage(group, m);
  }
};

const pullOfflineMessage = () => {
  const timeStamp = Date.now();
  chatStore.setLoading(true);
  const maxPrivateMessageId = chatStore.findMaxMessageId(CONVERSATION_TYPE.PRIVATE);
  const maxGroupMessageId = chatStore.findMaxMessageId(CONVERSATION_TYPE.GROUP);
  Promise.all([loadPrivateOfflineMessage(maxPrivateMessageId), loadGroupOfflineMessage(maxGroupMessageId)])
    .then(async (messages) => {
      // 处理离线消息
      await handlePrivateOfflineMessage(messages[0] || []);
      await handleGroupOfflineMessage(messages[1] || []); // 处理缓冲区收到的实时消息
      for (const m of privateMessagesBuffer.value) {
        await handlePrivateMessage(m);
      }
      for (const m of groupMessagesBuffer.value) {
        await handleGroupMessage(m);
      } // 清空缓冲区
      privateMessagesBuffer.value = [];
      groupMessagesBuffer.value = []; // 关闭加载离线标记
      chatStore.setLoading(false);
      // 打印耗时
      const size = (messages[0]?.length || 0) + (messages[1]?.length || 0);
      console.log('加载离线消息耗时:', Date.now() - timeStamp, ',消息数量:', size);
    })
    .catch((e) => {
      console.log(e);
      ElMessage.error('拉取离线消息失败');
      onExit();
    });
};

const onReconnectWs = () => {
  // 重连成功
  reconnecting.value = false;
  // 重新加载群和好友
  Promise.all([friendStore.pullFriends(), groupStore.pullGroups()])
    .then(() => {
      // 拉取离线消息
      pullOfflineMessage();
      // 刷新好友在线状态
      void friendStore.refreshOnline();
      ElMessage.success('重新连接成功');
    })
    .catch(() => {
      ElMessage.error('初始化失败');
      onExit();
    });
};

const reconnectWs = () => {
  // 记录标志
  reconnecting.value = true;
  // 重新加载一次个人信息，目的是为了保证网络已经正常且token有效
  userStore
    .loadUser()
    .then(() => {
      // 断线重连
      ElMessage.error('连接断开，正在尝试重新连接...');
      wsApi.reconnect(import.meta.env.VITE_APP_WS_URL, sessionStorage.getItem('accessToken') || '');
    })
    .catch(() => {
      // 10s后重试
      setTimeout(() => reconnectWs(), 10000);
    });
};

const loadStore = async () => {
  await userStore.loadUser();
  const db = await initDB();
  if (!userStore.userInfo.id) {
    throw new Error('用户信息异常');
  }
  db.open(userStore.userInfo.id);
  // 加载好友要在加载会话前面，否则好友在线状态不显示
  await friendStore.loadFriend();
  await Promise.all([groupStore.loadGroup(), configStore.loadConfig(), chatStore.loadConversations()]);
};

const initRealtime = () => {
  const token = sessionStorage.getItem('accessToken') || '';
  // ws初始化
  wsApi.onConnect(() => {
    if (reconnecting.value) {
      onReconnectWs();
    } else {
      // 加载离线消息
      pullOfflineMessage();
    }
  });
  wsApi.onMessage((cmd, data) => {
    const message = data as ChatMessage;
    if (cmd === 2) {
      // 关闭ws
      wsApi.close(3000);
      // 异地登录，强制下线
      void ElMessageBox.alert('您已在其他地方登录，将被强制下线', '强制下线通知', {
        confirmButtonText: '确定',
        callback: () => onExit()
      });
    } else if (cmd === 3) {
      if (chatStore.loading) {
        // 如果正在拉取离线消息，先放进缓存区，等待消息拉取完成再处理，防止消息乱序
        privateMessagesBuffer.value.push(message);
      } else {
        // 插入私聊消息
        void handlePrivateMessage(message);
      }
    } else if (cmd === 4) {
      if (chatStore.loading) {
        // 如果正在拉取离线消息，先放进缓存区，等待消息拉取完成再处理，防止消息乱序
        groupMessagesBuffer.value.push(message);
      } else {
        // 插入群聊消息
        void handleGroupMessage(message);
      }
    } else if (cmd === 5) {
      // 处理系统消息
      handleSystemMessage(message);
    }
  });
  wsApi.onClose((e) => {
    const event = e as CloseEvent;
    if (event?.code !== 3000) {
      // 断线重连
      reconnectWs();
    }
  });
  wsApi.connect(import.meta.env.VITE_APP_WS_URL, token);
};

const goSetting = () => {
  void router.push('/home/setting');
};

const closeAllBox = () => {
  userInfoRef.value?.close();
  groupInfoRef.value?.close();
};

const onOpenUserInfo = ({ user, pos }: EventBusEvents['openUserInfo']) => {
  // 打开用户卡片
  nextTick(() => userInfoRef.value?.open(user, pos));
};

const onOpenGroupInfo = ({ group, pos }: EventBusEvents['openGroupInfo']) => {
  // 打开群卡片
  nextTick(() => groupInfoRef.value?.open(group, pos));
};

const onOpenFullImage = (payload: FullImagePayload) => {
  // 图片大图：支持单图 url，或 { convKey, url } 会话图库
  nextTick(() => fullImageRef.value?.open(payload));
};

const onOpenPrivateVideo = (rtcInfo: unknown) => {
  // 进入单人视频通话
  nextTick(() => rtcPrivateVideoRef.value?.open(rtcInfo as PrivateRtcInfo));
};

const onOpenGroupVideo = () => {
  // 开源版多人通话入口（商业版说明）
  nextTick(() => rtcGroupVideoRef.value?.open());
};

const handleSystemMessage = (msg: ChatMessage) => {
  // 用户被封禁
  if (msg.type == MESSAGE_TYPE.USER_BANNED) {
    wsApi.close(3000);
    void ElMessageBox.alert(`您的账号已被管理员封禁,原因:${msg.content || ''}`, '账号被封禁', {
      confirmButtonText: '确定',
      callback: () => onExit()
    });
  }
};

const onSwitchFullScreen = () => {
  configStore.setFullScreen(!fullScreen.value);
};

watch(
  unreadCount,
  (newCount) => {
    setTitleTip(newCount > 0 ? `${newCount}条未读` : '');
  },
  { immediate: true }
);

onMounted(() => {
  eventBus.on('openUserInfo', onOpenUserInfo);
  eventBus.on('openGroupInfo', onOpenGroupInfo);
  eventBus.on('openFullImage', onOpenFullImage);
  eventBus.on('openPrivateVideo', onOpenPrivateVideo);
  eventBus.on('openGroupVideo', onOpenGroupVideo);
  wsApi.close();
  void loadStore()
    .then(() => {
      initRealtime();
    })
    .catch(() => {
      ElMessage.error('初始化失败');
      onExit();
    });
});

onUnmounted(() => {
  eventBus.off('openUserInfo', onOpenUserInfo);
  eventBus.off('openGroupInfo', onOpenGroupInfo);
  eventBus.off('openFullImage', onOpenFullImage);
  eventBus.off('openPrivateVideo', onOpenPrivateVideo);
  eventBus.off('openGroupVideo', onOpenGroupVideo);
  wsApi.close();
});
</script>

<style scoped lang="scss">
.home-page {
  height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  overflow: hidden;
  background: var(--im-color-primary-light-9);

  .app-container {
    width: 62vw;
    height: 80vh;
    display: flex;
    min-height: 660px;
    min-width: 970px;
    position: absolute;
    border-radius: 4px;
    overflow: hidden;
    box-shadow: var(--im-box-shadow-dark);
    transition: 0.2s;

    &.fullscreen {
      transition: 0.2s;
      width: 100vw;
      height: 100vh;
    }
  }

  .navi-bar {
    --icon-font-size: 22px;
    --width: 70px;
    width: var(--width);
    background: linear-gradient(180deg, var(--im-color-primary-light-1) 0%, var(--im-color-primary-light-2) 100%);
    padding-top: 25px;
    position: relative;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
    // 添加顶部装饰线

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, var(--im-color-primary) 0%, var(--im-color-primary-light-3) 50%, var(--im-color-primary) 100%);
    }

    .navi-bar-box {
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .bottom {
        margin-bottom: 25px;
      }
    }

    .avater {
      display: flex;
      justify-content: center;
      margin-bottom: 10px;
      // 为头像添加容器样式

      :deep(.head-image) {
        border: 3px solid rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transition: all 0.3s ease;
        cursor: pointer;

        &:hover {
          border-color: rgba(255, 255, 255, 0.4);
          transform: scale(1.05);
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
        }
      }
    }

    .menu {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-content: center;
      flex-wrap: wrap;
      margin-top: 25px;
      gap: 8px;

      .link {
        text-decoration: none;
        display: flex;
        justify-content: center;
      }

      .router-link-active .menu-item {
        color: white;
        background: linear-gradient(135deg, var(--im-color-primary-light-2) 0%, var(--im-color-primary) 100%);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        transform: translateX(2px);

        &::before {
          opacity: 1;
          transform: scale(1);
        }
      }

      .link:not(.router-link-active) .menu-item:hover {
        background: linear-gradient(135deg, var(--im-color-primary) 0%, var(--im-color-primary-light-2) 100%);
        transform: scale(1.08) translateX(2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
        color: white;
      }

      .menu-item {
        position: relative;
        color: rgba(255, 255, 255, 0.8);
        width: 50px;
        height: 50px;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 10px;
        border-radius: 12px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        cursor: pointer;
        // 添加左侧指示条

        &::before {
          content: '';
          position: absolute;
          left: -5px;
          width: 3px;
          height: 20px;
          background: white;
          border-radius: 2px;
          opacity: 0;
          transition: all 0.3s ease;
        }

        .icon {
          font-size: var(--icon-font-size);
          transition: all 0.3s ease;
        }

        .unread-text {
          position: absolute;
          background: var(--im-color-danger);
          left: 32px;
          top: 3px;
          color: white;
          border-radius: 10px;
          padding: 1px 6px;
          font-size: 10px;
          font-weight: 600;
          text-align: center;
          white-space: nowrap;
          border: 1px solid rgba(255, 255, 255, 0.9);
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
          min-width: 16px;
          height: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 1;
        }
      }
    }

    .bottom-item {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 45px;
      width: 100%;
      cursor: pointer;
      color: rgba(255, 255, 255, 0.7);
      font-size: var(--icon-font-size);
      border-radius: 8px;
      margin: 4px 0;
      transition: all 0.3s ease;

      .icon {
        font-size: var(--icon-font-size);
        transition: all 0.3s ease;
      }

      &:hover {
        color: white;
        background: rgba(255, 255, 255, 0.1);
        transform: scale(1.05);

        .icon {
          transform: scale(1.1);
        }
      }

      &:active {
        transform: scale(0.95);
      }
    }
  }

  .content-box {
    flex: 1;
    padding: 0;
    background: var(--im-background);
    text-align: center;
    overflow: hidden;
  }
}
</style>

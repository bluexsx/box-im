import { defineStore } from 'pinia';
import { ref } from 'vue';
import { historyPrivateMessage } from '@/api/privateMessage';
import { historyGroupMessage } from '@/api/groupMessage';
import type { UserVO } from '@/api/user/types';
import { getDB } from '@/db';
import { useUserStore } from '@/stores/user';
import { useFriendStore } from '@/stores/friend';
import type { FriendVO } from '@/api/friend/types';
import { useGroupStore } from '@/stores/group';
import type { GroupVO } from '@/api/group/types';
import { MESSAGE_TYPE, MESSAGE_STATUS, CONVERSATION_TYPE } from '@/utils/enums';
import { previewContent } from '@/utils/messageUtil';
import nextSnowflakeId from '@/utils/snowflake';
import type { ChatInfo, ChatMessage, Conversation } from '@/types';

export const useChatStore = defineStore('chat', () => {
  const activeConversation = ref<Conversation>(); // 当前选中的会话
  const conversations = ref<Conversation[]>([]); // 全部会话列表
  const conversationMap = ref(new Map<string, Conversation>()); // 全部会话map
  const loading = ref(true); // 是否正在加载离线消息
  const loadingMessage = ref(false); // 是否正在从本地数据库拉取消息
  const messages = ref<ChatMessage[]>([]); // 当前会话展示的消息列表
  const hasMoreLastMessage = ref(true); // 当前会话是否还有更多上翻消息
  const hasMoreNextMessage = ref(false); // 当前会话是否还有更多下翻消息
  const isInBottom = ref(true); // 滚动条是否在消息底部
  const newMessageSize = ref(0); // 新消息数量
  const minSeqNo = ref(0); // 最小消息序号
  const maxSeqNo = ref(0); // 最大消息序号
  const pendingLocateMessage = ref<ChatMessage>(); // 待定位消息
  let sortTimer: ReturnType<typeof setInterval> | null = null;

  // 兼容历史数据缺失的字段
  const normalizeConversation = (conv: Conversation) => {
    conv.showName = conv.showName ?? '';
    conv.optTime = conv.optTime ?? 0;
    conv.unreadCount = conv.unreadCount ?? 0;
    conv.lastAtMessageId = conv.lastAtMessageId ?? -1;
    conv.lastTimeTip = conv.lastTimeTip ?? 0;
    conv.maxMessageId = conv.maxMessageId ?? 0;
    conv.minSeqNo = conv.minSeqNo ?? 0;
    conv.maxSeqNo = conv.maxSeqNo ?? 0;
    conv.maxReadedId = conv.maxReadedId ?? 0;
    conv.isDnd = !!conv.isDnd;
    conv.isTop = !!conv.isTop;
  };

  const findIdx = (convKey: string) => {
    for (const idx in conversations.value) {
      if (conversations.value[Number(idx)].key == convKey) {
        return Number(idx);
      }
    }
    return -1;
  };

  const findTopSize = () => {
    return conversations.value.filter((conv) => conv.isTop).length;
  };

  const findMaxMessageId = (type: number) => {
    let maxId = 0;
    conversations.value.forEach((conv) => {
      if (conv.maxMessageId && conv.type == type) {
        maxId = Math.max(maxId, conv.maxMessageId);
      }
    });
    return maxId;
  };

  const findMaxSeqNo = (type: number) => {
    let max = 0;
    conversations.value.forEach((conv) => {
      if (conv.maxSeqNo && conv.type == type) {
        max = Math.max(max, conv.maxSeqNo);
      }
    });
    return max;
  };

  const findByFriend = (friendId: number) => {
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, friendId);
    return conversationMap.value.get(convKey);
  };

  const findByGroup = (groupId: number) => {
    const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, groupId);
    return conversationMap.value.get(convKey);
  };

  const isActive = (convKey: string) => {
    return !!(activeConversation.value && activeConversation.value.key == convKey);
  };

  // 更新会话置顶和免打扰状态
  const refreshTopAndDnd = () => {
    const friendStore = useFriendStore();
    const groupStore = useGroupStore();
    conversations.value.forEach((conv) => {
      if (conv.type == CONVERSATION_TYPE.PRIVATE) {
        const friend = friendStore.findFriend(conv.targetId);
        if (friend) {
          conv.isDnd = friend.isDnd;
          conv.isTop = friend.isTop;
        }
      } else if (conv.type == CONVERSATION_TYPE.GROUP) {
        const group = groupStore.findGroup(conv.targetId);
        if (group) {
          conv.isDnd = group.isDnd;
          conv.isTop = group.isTop;
        }
      }
    });
  };

  const sort = () => {
    conversations.value.sort((conv1, conv2) => {
      if (conv1.isTop && !conv2.isTop) {
        return -1;
      } else if (conv2.isTop && !conv1.isTop) {
        return 1;
      } else {
        return conv2.optTime - conv1.optTime;
      }
    });
  };

  const startSortTimer = () => {
    sort();
    if (sortTimer) clearInterval(sortTimer);
    sortTimer = setInterval(() => sort(), 1000);
  };

  const init = (list: Conversation[]) => {
    list.forEach(normalizeConversation);
    conversations.value = list;
    conversationMap.value.clear();
    conversations.value.forEach((conv) => conversationMap.value.set(conv.key, conv));
    refreshTopAndDnd();
    startSortTimer();
  };

  const append = (list: Conversation[]) => {
    list.forEach((conv) => {
      normalizeConversation(conv);
      if (conversationMap.value.has(conv.key)) {
        // 更新
        const conversation = conversationMap.value.get(conv.key)!;
        Object.assign(conversation, conv);
      } else {
        // 新增
        conversations.value.push(conv);
        conversationMap.value.set(conv.key, conv);
      }
      // 当前会话拉到了新消息
      if (isActive(conv.key)) {
        hasMoreNextMessage.value = true;
      }
    });
    refreshTopAndDnd();
  };

  const setActive = (convKey: string) => {
    const conv = conversationMap.value.get(convKey);
    if (!activeConversation.value || convKey != activeConversation.value.key) {
      activeConversation.value = conv;
      messages.value = [];
      hasMoreNextMessage.value = false;
      hasMoreLastMessage.value = true;
      loadingMessage.value = false;
      minSeqNo.value = 0;
      maxSeqNo.value = 0;
    }
  };

  const setIsInBottom = (value: boolean) => {
    isInBottom.value = value;
    // 既然在底部，新消息已经显示了
    if (value) {
      newMessageSize.value = 0;
    }
  };

  const setPendingLocateMessage = (message?: ChatMessage) => {
    pendingLocateMessage.value = message;
  };

  const removePendingLocateMessage = () => {
    pendingLocateMessage.value = undefined;
  };

  const openChat = async (chatInfo: ChatInfo) => {
    const key = getDB().buildConversationKey(chatInfo.type, chatInfo.targetId);
    let conv = conversationMap.value.get(key);
    // 创建会话
    if (conv == null) {
      conv = {
        key, // 会话唯一key
        targetId: chatInfo.targetId, // 会话对象id
        type: chatInfo.type, // 会话类型 私聊|群聊|系统消息
        showName: chatInfo.showName, // 昵称
        headImage: chatInfo.headImage, // 头像
        isDnd: chatInfo.isDnd, // 会话是否开启免打扰
        isTop: chatInfo.isTop, // 会话是否置顶
        lastContent: '', // 会话最后一条消息的内容
        lastSendTime: new Date().getTime(), // 会话最后一条消息的发送时间
        optTime: new Date().getTime(), // 会话最后一次操作时间
        unreadCount: 0, // 会话未读消息数量
        atMe: false, // 会话是否有@我的消息
        atAll: false, // 会话是否有@所有人的消息
        lastAtMessageId: -1, // 最后一条@我的消息id
        lastTimeTip: 0, // 最后插入的时间提示
        maxMessageId: 0, // 会话最大消息id
        minSeqNo: 0, // 最小消息序号
        maxSeqNo: 0, // 最大消息序号
        maxReadedId: 0 // 已读最大消息id
      };
      conversations.value.push(conv);
      conversationMap.value.set(key, conv);
      await getDB().saveConversation(conv);
    }
  };

  const insertMessage = async (convKey: string, m: ChatMessage) => {
    const conv = conversationMap.value.get(convKey)!;
    conv.lastContent = previewContent(m);
    conv.lastSendTime = Number(m.sendTime);
    conv.optTime = Number(m.sendTime);
    // 其他成员发的消息显示发送昵称
    conv.sendNickName = m.selfSend ? '' : m.sendNickName;
    // 记录会话最大消息id
    if (m.id && m.seqNo) {
      conv.maxSeqNo = Math.max(conv.maxSeqNo, m.seqNo);
      conv.maxMessageId = Math.max(conv.maxMessageId, m.id);
    }
    // 会话未读加1
    if (!m.selfSend && m.status != MESSAGE_STATUS.READED && m.status != MESSAGE_STATUS.RECALL && m.type != MESSAGE_TYPE.TIP_TEXT) {
      conv.unreadCount = conv.unreadCount + 1;
    }
    // 是否有人@我（已读、已撤回的消息不再设置）
    if (!m.selfSend && m.atUserIds && m.status != MESSAGE_STATUS.READED && m.status != MESSAGE_STATUS.RECALL) {
      const userId = useUserStore().userInfo.id;
      if (userId != null && m.atUserIds.indexOf(userId) >= 0) {
        conv.atMe = true;
        if (m.id != null) {
          conv.lastAtMessageId = m.id;
        }
      }
      if (m.atUserIds.indexOf(-1) >= 0) {
        conv.atAll = true;
        if (m.id != null) {
          conv.lastAtMessageId = m.id;
        }
      }
    }
    // 间隔大于10分钟插入时间显示
    if (!conv.lastTimeTip || conv.lastTimeTip < Number(m.sendTime) - 600 * 1000) {
      conv.lastTimeTip = Number(m.sendTime);
      if (isActive(convKey)) {
        const timeTipMessage: ChatMessage = {
          id: 0,
          convKey: conv.key,
          localId: nextSnowflakeId(),
          sendTime: m.sendTime,
          type: MESSAGE_TYPE.TIP_TIME,
          content: ''
        };
        messages.value.push(timeTipMessage);
      }
    }
    // 标记所属会话
    m.convKey = conv.key;
    // 如果是当前打开的会话窗口
    if (isActive(convKey)) {
      if (!hasMoreNextMessage.value) {
        // 消息插入底部
        messages.value.push(m);
        maxSeqNo.value = conv.maxSeqNo;
        // 积压数量过大时，主动释放掉一部分消息，避免消息堆积占影响渲染效率
        if (isInBottom.value && messages.value.length > 100) {
          await resetMessages(convKey);
        }
      }
      if (!isInBottom.value) {
        // 滚动条不在底部，需要展示新消息数量
        newMessageSize.value++;
      }
    }
    await getDB().saveConversationAndMessage([conv], [m]);
  };

  const updateMessage = async (convKey: string, m: ChatMessage) => {
    const conv = conversationMap.value.get(convKey)!;
    // 查询原消息
    let message: ChatMessage | undefined = messages.value.find((m1) => m.localId == m1.localId);
    if (!message) {
      message = await getDB().findMessageByLocalId(m.localId!);
      if (!message) {
        return;
      }
    }
    // 通过属性拷贝的方式对字段更新
    Object.assign(message, m);
    await getDB().saveMessage(message);
    // 记录会话最大消息id
    if (m.id && m.seqNo) {
      conv.maxMessageId = Math.max(conv.maxMessageId, m.id);
      conv.maxSeqNo = Math.max(conv.maxSeqNo, m.seqNo);
      await getDB().saveConversation(conv);
    }
  };

  const deleteMessage = async (convKey: string, m: ChatMessage) => {
    // 删除旧消息
    await getDB().deleteMessageByLocalId(m.localId!);
    if (isActive(convKey)) {
      for (const idx in messages.value) {
        // 已经发送成功的，根据id删除
        if (messages.value[Number(idx)].localId == m.localId) {
          messages.value.splice(Number(idx), 1);
          break;
        }
      }
    }
    // 清空lastContent
    const conv = conversationMap.value.get(convKey)!;
    if (m.id && m.id == conv.maxMessageId) {
      conv.lastContent = '';
      conv.sendNickName = '';
      await getDB().saveConversation(conv);
    }
  };

  // 重置消息到底部30条
  const resetMessages = async (convKey: string) => {
    if (!isActive(convKey)) {
      return;
    }
    setIsInBottom(true);
    const conv = conversationMap.value.get(convKey)!;
    minSeqNo.value = 0;
    maxSeqNo.value = conv.maxSeqNo;
    hasMoreLastMessage.value = true;
    const size = 30;
    if (isActive(convKey) && !hasMoreNextMessage.value && messages.value.length) {
      // 消息已在内存里，就不去db查询了，降低并发压力
      const nextMin = Math.max(1, conv.minSeqNo, conv.maxSeqNo - size + 1);
      const idx = messages.value.findIndex((m) => m.seqNo == nextMin);
      if (idx >= 0) {
        messages.value.splice(0, idx);
        minSeqNo.value = nextMin;
        return;
      }
    }
    // 多加1是为了查询时修正边界值
    minSeqNo.value = conv.maxSeqNo + 1;
    hasMoreNextMessage.value = false;
    messages.value = [];
    await loadLastPageMessage(convKey, size);
  };

  const existMissMessage = (list: ChatMessage[], min: number, max: number) => {
    for (let i = min; i <= max; i++) {
      if (!list.some((m) => m.id && m.seqNo == i)) {
        return true;
      }
    }
    return false;
  };

  const filterInvalidMessage = (_convKey: string, localMessages: ChatMessage[]) => {
    // 排除已经删除或撤回的消息
    return localMessages.filter((m) => !m.deleted && m.status != MESSAGE_STATUS.RECALL && m.type != MESSAGE_TYPE.RECALL);
  };

  const appendTimeTipMessage = (convKey: string, localMessages: ChatMessage[]) => {
    const result: ChatMessage[] = [];
    let lastTimeTip = 0;
    localMessages.forEach((m) => {
      if (!m.deleted && Number(m.sendTime) - lastTimeTip > 600 * 1000) {
        const timeTipMessage: ChatMessage = {
          id: 0,
          localId: nextSnowflakeId(),
          convKey,
          sendTime: m.sendTime,
          type: MESSAGE_TYPE.TIP_TIME,
          content: ''
        };
        result.push(timeTipMessage);
        lastTimeTip = Number(m.sendTime);
      }
      result.push(m);
    });
    return result;
  };

  const resetAtMessage = async (convKey: string) => {
    const conv = conversationMap.value.get(convKey);
    if (conv) {
      conv.atAll = false;
      conv.atMe = false;
      conv.lastAtMessageId = -1;
      await getDB().saveConversation(conv);
    }
  };

  const refreshAtMessage = async (convKey: string, readedMessages: ChatMessage[]) => {
    const conv = conversationMap.value.get(convKey)!;
    if (!conv.atAll && !conv.atMe) {
      return;
    }
    if (readedMessages.some((m) => m.id == conv.lastAtMessageId)) {
      await resetAtMessage(convKey);
    }
  };

  const refreshMinSeqNo = async (convKey: string) => {
    const conv = conversationMap.value.get(convKey)!;
    for (const m of messages.value) {
      if (m.seqNo) {
        conv.minSeqNo = m.seqNo;
        await getDB().saveConversation(conv);
        break;
      }
    }
  };

  // 拉取正在发送中的消息
  const reloadSendingMessage = async (convKey: string, localMessages: ChatMessage[]) => {
    const conv = conversationMap.value.get(convKey)!;
    // 用户如果在消息发送过程中退出，消息会永远处于发送中状态，这里从服务器重新拉取这部分消息
    const sendingMessages = localMessages.filter((m) => m.status == MESSAGE_STATUS.SENDING);
    if (!sendingMessages.length) {
      return localMessages;
    }
    let remoteMessages: ChatMessage[] = [];
    const ids = sendingMessages.map((m) => m.localId!) as string[];
    if (conv.type == CONVERSATION_TYPE.PRIVATE) {
      remoteMessages = (await historyPrivateMessage({ friendId: conv.targetId, localIds: ids })) as ChatMessage[];
    } else {
      remoteMessages = (await historyGroupMessage({ groupId: conv.targetId, localIds: ids })) as ChatMessage[];
    }
    const userId = useUserStore().userInfo.id;
    remoteMessages.forEach((m) => {
      m.convKey = convKey;
      m.selfSend = m.sendId == userId;
      conv.maxMessageId = Math.max(conv.maxMessageId, m.id ?? 0);
      conv.maxSeqNo = Math.max(conv.maxSeqNo, m.seqNo ?? 0);
    });
    // 逐条更新
    const saveMessages: ChatMessage[] = [];
    for (const idx in sendingMessages) {
      const localMessage = sendingMessages[Number(idx)];
      const message = remoteMessages.find((m) => m.localId == localMessage.localId);
      if (message) {
        Object.assign(localMessage, message);
      } else {
        // 没拉取到这条消息，说明没发成功
        localMessage.status = MESSAGE_STATUS.FAILED;
      }
      saveMessages.push(localMessage);
    }
    await getDB().saveConversationAndMessage([conv], saveMessages);
    return localMessages;
  };

  // 从服务器拉取丢失的消息
  const reloadMissMessage = async (convKey: string, min: number, max: number, localMessages: ChatMessage[]) => {
    const conv = conversationMap.value.get(convKey)!;
    // 如果没有缺失的数据直接返回
    if (conv.maxSeqNo <= 0 || !existMissMessage(localMessages, min, max)) {
      return localMessages;
    }
    const userId = useUserStore().userInfo.id;
    // 从服务器重新拉取这一页数据
    let remoteMessages: ChatMessage[] = [];
    if (conv.type == CONVERSATION_TYPE.PRIVATE) {
      remoteMessages = (await historyPrivateMessage({ friendId: conv.targetId, minSeqNo: min, maxSeqNo: max })) as ChatMessage[];
    } else {
      remoteMessages = (await historyGroupMessage({ groupId: conv.targetId, minSeqNo: min, maxSeqNo: max })) as ChatMessage[];
    }
    const saveMessages: ChatMessage[] = [];
    remoteMessages.forEach((m) => {
      m.convKey = convKey;
      m.selfSend = m.sendId == userId;
      conv.maxMessageId = Math.max(conv.maxMessageId, m.id ?? 0);
      conv.maxSeqNo = Math.max(conv.maxSeqNo, m.seqNo ?? 0);
      // 只保存新拉到的消息,不能直接覆盖旧消息,否则被撤回的消息提示语会被覆盖
      if (!localMessages.some((localMessage) => localMessage.id == m.id)) {
        saveMessages.push(m);
      }
    });
    if (!saveMessages.length) {
      return localMessages;
    }
    await getDB().saveConversationAndMessage([conv], saveMessages);
    // 合并消息
    const messageMap = new Map<string | number, ChatMessage>();
    localMessages.forEach((m) => {
      if (m.localId != null) messageMap.set(m.localId, m);
    });
    saveMessages.forEach((m) => {
      if (m.localId != null) messageMap.set(m.localId, m);
    });
    return Array.from(messageMap.values()).sort((m1, m2) => {
      if (m1.seqNo != m2.seqNo) {
        return (m1.seqNo || 0) - (m2.seqNo || 0);
      }
      return Number(m1.sendTime) - Number(m2.sendTime);
    });
  };

  // 拉取上一页消息
  const loadLastPageMessage = async (convKey: string, size: number) => {
    if (!isActive(convKey)) {
      return;
    }
    // 防止滚动事件重复触发导致重复拉取
    if (loadingMessage.value || !hasMoreLastMessage.value) {
      return;
    }
    const conv = conversationMap.value.get(convKey)!;
    const nextMin = Math.max(1, conv.minSeqNo, minSeqNo.value - size);
    const nextMax = minSeqNo.value - 1;
    if (nextMax < nextMin) {
      hasMoreLastMessage.value = false;
      return;
    }
    loadingMessage.value = true;
    let pageMessages = await getDB().findPageMessage(convKey, nextMin, nextMax);
    pageMessages = await reloadSendingMessage(convKey, pageMessages);
    pageMessages = await reloadMissMessage(convKey, nextMin, nextMax, pageMessages);
    await refreshAtMessage(convKey, pageMessages);
    hasMoreLastMessage.value = nextMin > 1 && pageMessages.length > 0;
    pageMessages = filterInvalidMessage(convKey, pageMessages);
    pageMessages = appendTimeTipMessage(convKey, pageMessages);
    messages.value.unshift(...pageMessages);
    minSeqNo.value = nextMin;
    loadingMessage.value = false;
    // 如果上方已无可拉取的消息，标记最小消息序号，避免下次重复拉取
    if (!hasMoreLastMessage.value) {
      await refreshMinSeqNo(convKey);
    }
    // 防止用户删除了过多消息导致滚动条不出来
    if (messages.value.length < 20) {
      await loadLastPageMessage(convKey, 30);
    }
  };

  // 拉取下一页消息
  const loadNextPageMessage = async (convKey: string, size: number) => {
    if (!isActive(convKey)) {
      return;
    }
    // 防止滚动事件重复触发导致重复拉取
    if (loadingMessage.value || !hasMoreNextMessage.value) {
      return;
    }
    const conv = conversationMap.value.get(convKey)!;
    const nextMin = Math.min(conv.maxSeqNo, maxSeqNo.value + 1);
    const nextMax = Math.min(conv.maxSeqNo, maxSeqNo.value + size);
    loadingMessage.value = true;
    let pageMessages = await getDB().findPageMessage(convKey, nextMin, nextMax);
    pageMessages = await reloadSendingMessage(convKey, pageMessages);
    await refreshAtMessage(convKey, pageMessages);
    hasMoreNextMessage.value = nextMax < conv.maxSeqNo && pageMessages.length > 0;
    pageMessages = await reloadMissMessage(convKey, nextMin, nextMax, pageMessages);
    pageMessages = filterInvalidMessage(convKey, pageMessages);
    pageMessages = appendTimeTipMessage(convKey, pageMessages);
    messages.value.push(...pageMessages);
    maxSeqNo.value = nextMax;
    loadingMessage.value = false;
    // 防止用户删除了过多消息导致滚动条不出来
    if (messages.value.length < 20) {
      await loadNextPageMessage(convKey, 30);
    }
  };

  // 定位消息
  const locateToMessage = async (convKey: string, message: ChatMessage) => {
    if (!isActive(convKey)) {
      return;
    }
    const conv = conversationMap.value.get(convKey)!;
    // 向下取20条,向上取5条
    const nextMax = Math.min(conv.maxSeqNo, (message.seqNo ?? 0) + 20);
    const nextMin = Math.max(1, conv.minSeqNo, nextMax - 25);
    loadingMessage.value = true;
    let pageMessages = await getDB().findPageMessage(convKey, nextMin, nextMax);
    pageMessages = await reloadSendingMessage(convKey, pageMessages);
    pageMessages = await reloadMissMessage(convKey, nextMin, nextMax, pageMessages);
    await refreshAtMessage(convKey, messages.value);
    hasMoreLastMessage.value = nextMin > 1 && pageMessages.length > 0;
    hasMoreNextMessage.value = nextMax < conv.maxSeqNo && pageMessages.length > 0;
    pageMessages = filterInvalidMessage(convKey, pageMessages);
    pageMessages = appendTimeTipMessage(convKey, pageMessages);
    messages.value = pageMessages;
    maxSeqNo.value = nextMax;
    minSeqNo.value = nextMin;
    loadingMessage.value = false;
    // 防止用户删除了过多消息导致滚动条不出来
    if (messages.value.length < 20) {
      await loadLastPageMessage(convKey, 20);
      await loadNextPageMessage(convKey, 20);
    }
  };

  const recallMessage = async (convKey: string, message: ChatMessage) => {
    const batchMessages: ChatMessage[] = [];
    const conv = conversationMap.value.get(convKey)!;
    // 要撤回的消息id
    const recallMessageId = JSON.parse(message.content as string).id;
    const recallMessageTip = JSON.parse(message.content as string).tip;
    const recalled = await getDB().findMessageById(recallMessageId, convKey);
    if (!recalled) {
      return;
    }
    // 把原消息改造成一条提示消息
    recalled.status = MESSAGE_STATUS.PENDING;
    recalled.content = recallMessageTip;
    recalled.type = MESSAGE_TYPE.TIP_TEXT;
    batchMessages.push(recalled);
    // 撤回的若是@我消息，清除标记
    if (conv.lastAtMessageId == recallMessageId) {
      conv.atMe = false;
      conv.atAll = false;
      conv.lastAtMessageId = -1;
    }
    // 会话列表
    conv.lastContent = previewContent(recalled);
    conv.lastSendTime = Number(message.sendTime);
    conv.sendNickName = '';
    conv.maxMessageId = Math.max(conv.maxMessageId, message.id ?? 0);
    conv.maxSeqNo = Math.max(conv.maxSeqNo, message.seqNo ?? 0);
    if (!message.selfSend && message.status != MESSAGE_STATUS.READED) {
      conv.unreadCount = conv.unreadCount + 1;
    }
    if (isActive(convKey)) {
      messages.value.forEach((m1) => {
        if (m1.id == recallMessageId) {
          m1.status = MESSAGE_STATUS.RECALL;
          m1.content = recallMessageTip;
          m1.type = MESSAGE_TYPE.TIP_TEXT;
        }
      });
    }
    // 撤回指令也要入库，保证seqNo连续
    message.convKey = conv.key;
    batchMessages.push(message);
    await getDB().saveConversationAndMessage([conv], batchMessages);
  };

  const remove = async (convKey: string) => {
    const idx = findIdx(convKey);
    if (idx < 0) return;
    conversations.value.splice(idx, 1);
    conversationMap.value.delete(convKey);
    if (isActive(convKey)) {
      activeConversation.value = undefined;
      messages.value = [];
    }
    await getDB().deleteMessageByConvKey(convKey);
    await getDB().deleteConversationByKey(convKey);
  };

  const cleanMessage = async (convKey: string) => {
    const conv = conversationMap.value.get(convKey)!;
    conv.lastContent = '';
    conv.unreadCount = 0;
    conv.atMe = false;
    conv.atAll = false;
    conv.lastAtMessageId = -1;
    conv.sendNickName = '';
    if (isActive(convKey)) {
      messages.value = [];
    }
    await getDB().deleteMessageByConvKey(convKey);
    await getDB().saveConversation(conv);
  };

  const setDnd = async (convKey: string, isDnd: boolean) => {
    const conv = conversationMap.value.get(convKey);
    if (!conv) return;
    conv.isDnd = isDnd;
    await getDB().saveConversation(conv);
  };

  const moveTop = async (convKey: string) => {
    const idx = findIdx(convKey);
    const conv = conversationMap.value.get(convKey);
    if (idx < 0 || !conv) return;
    const insertIdx = conv.isTop ? 0 : findTopSize();
    if (idx != insertIdx) {
      conversations.value.splice(idx, 1);
      conversations.value.splice(insertIdx, 0, conv);
      conv.optTime = new Date().getTime();
      await getDB().saveConversation(conv);
    }
  };

  const setTop = async (convKey: string, isTop: boolean) => {
    const conv = conversationMap.value.get(convKey);
    if (!conv) return;
    conv.isTop = isTop;
    await moveTop(convKey);
    await getDB().saveConversation(conv);
  };

  const readedMessage = async (convKey: string, messageId?: number) => {
    const conv = conversationMap.value.get(convKey);
    // 没传messageId就是整个会话已读
    const id = messageId ?? conv?.maxMessageId ?? 0;
    if (conv && conv.maxReadedId < id) {
      conv.maxReadedId = Math.min(conv.maxMessageId, id);
      await getDB().saveConversation(conv);
    }
  };

  const resetUnreadCount = async (convKey: string) => {
    const conv = conversationMap.value.get(convKey);
    if (conv) {
      conv.unreadCount = 0;
      await getDB().saveConversation(conv);
    }
  };

  const updateFromFriend = async (friend: FriendVO) => {
    const conv = findByFriend(friend.id);
    // 更新会话中的昵称和头像
    if (conv && (conv.headImage != friend.headImage || conv.showName != friend.nickName)) {
      conv.headImage = friend.headImage;
      conv.showName = friend.nickName;
      await getDB().saveConversation(conv);
    }
  };

  const updateFromUser = async (user: UserVO) => {
    const conv = findByFriend(user.id);
    // 更新会话中的昵称和头像
    if (conv && user.nickName && (conv.headImage != user.headImageThumb || conv.showName != user.nickName)) {
      conv.headImage = user.headImageThumb;
      conv.showName = user.nickName;
      await getDB().saveConversation(conv);
    }
  };

  const updateFromGroup = async (group: GroupVO) => {
    const conv = findByGroup(group.id);
    if (conv && (conv.headImage != group.headImageThumb || conv.showName != group.showGroupName)) {
      // 更新会话中的群名称和头像
      conv.headImage = group.headImageThumb;
      conv.showName = group.showGroupName;
      await getDB().saveConversation(conv);
    }
  };

  const setLoading = (value: boolean) => {
    loading.value = value;
  };

  const clear = () => {
    activeConversation.value = undefined;
    conversations.value = [];
    conversationMap.value.clear();
    messages.value = [];
    loading.value = true;
    if (sortTimer) {
      clearInterval(sortTimer);
      sortTimer = null;
    }
  };

  const loadConversations = async () => {
    const list = await getDB().loadAllConversations();
    init(list ?? []);
  };

  return {
    activeConversation,
    conversations,
    conversationMap,
    loading,
    loadingMessage,
    messages,
    hasMoreLastMessage,
    hasMoreNextMessage,
    isInBottom,
    newMessageSize,
    minSeqNo,
    maxSeqNo,
    pendingLocateMessage,
    init,
    append,
    setActive,
    setIsInBottom,
    setPendingLocateMessage,
    removePendingLocateMessage,
    openChat,
    insertMessage,
    updateMessage,
    deleteMessage,
    resetMessages,
    loadLastPageMessage,
    loadNextPageMessage,
    locateToMessage,
    reloadSendingMessage,
    reloadMissMessage,
    filterInvalidMessage,
    appendTimeTipMessage,
    recallMessage,
    remove,
    cleanMessage,
    setDnd,
    setTop,
    moveTop,
    readedMessage,
    resetUnreadCount,
    resetAtMessage,
    refreshAtMessage,
    updateFromFriend,
    updateFromUser,
    updateFromGroup,
    refreshMinSeqNo,
    setLoading,
    refreshTopAndDnd,
    startSortTimer,
    sort,
    existMissMessage,
    clear,
    loadConversations,
    findIdx,
    findTopSize,
    findMaxMessageId,
    findMaxSeqNo,
    findByFriend,
    findByGroup,
    isActive
  };
});

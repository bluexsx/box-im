<template>
  <div v-if="show" class="full-image">
    <div class="mask" @click="close" />
    <div class="image-box" @click.stop>
      <img v-show="currentLoaded" :src="currentUrl" :style="imageStyle" @click.stop @load="onImageLoad" @error="onImageError" />
      <div v-if="currentLoading" class="state-tip">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>{{ '加载中...' }}</span>
      </div>
      <div v-else-if="currentLoadError" class="state-tip">{{ '无法加载此图片' }}</div>
    </div>
    <button class="close-btn" type="button" :title="'关闭'" @click.stop="close">
      <el-icon><Close /></el-icon>
    </button>
    <div class="toolbar" @click.stop>
      <button class="tool-btn" type="button" :disabled="!hasPrev" @click="move(-1)">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <button class="tool-btn" type="button" :disabled="!canZoomOut" @click="zoomOut">
        <el-icon><ZoomOut /></el-icon>
      </button>
      <button class="tool-btn" type="button" :disabled="!canZoomIn" @click="zoomIn">
        <el-icon><ZoomIn /></el-icon>
      </button>
      <button class="tool-btn" type="button" :disabled="!currentLoaded" @click="rotate">
        <el-icon><RefreshRight /></el-icon>
      </button>
      <button class="tool-btn" type="button" :disabled="!currentLoaded" :title="'下载'" @click="download">
        <el-icon><Download /></el-icon>
      </button>
      <button v-if="canLocate" class="tool-btn" type="button" :title="'在聊天中定位'" @click="locate">
        <el-icon><Aim /></el-icon>
      </button>
      <button class="tool-btn" type="button" :disabled="!hasNext" @click="move(1)">
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Aim, ArrowLeft, ArrowRight, Close, Download, Loading, RefreshRight, ZoomIn, ZoomOut } from '@element-plus/icons-vue';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import type { ChatMessage } from '@/types';
import { MESSAGE_STATUS, MESSAGE_TYPE } from '@/utils/enums';
import eventBus, { type FullImagePayload } from '@/utils/eventBus';

const PAGE_SIZE = 300;
const SCALE_STEP = 0.25;
const SCALE_MIN = 0.5;
const SCALE_MAX = 3;

type ImageItem = {
  url: string;
  seqNo: number;
  localId?: string | number;
  loaded?: boolean;
  loadError?: boolean;
};

const router = useRouter();
const chatStore = useChatStore();

const show = ref(false);
const items = ref<ImageItem[]>([]);
const index = ref(0);
const scale = ref(1);
const rotateDeg = ref(0);
const convKey = ref('');
const convMinSeqNo = ref(1);
const convMaxSeqNo = ref(0);
const scannedMinSeqNo = ref(0);
const scannedMaxSeqNo = ref(0);
const noMoreOlder = ref(true);
const noMoreNewer = ref(true);
const downloading = ref(false);

const currentUrl = computed(() => items.value[index.value]?.url || '');
const currentLoaded = computed(() => !!items.value[index.value]?.loaded);

const currentLoading = computed(() => {
  const item = items.value[index.value];
  return !!(item && !item.loaded && !item.loadError);
});

const currentLoadError = computed(() => !!items.value[index.value]?.loadError);

const imageStyle = computed(() => ({
  transform: `translate(-50%, -50%) scale(${scale.value}) rotate(${rotateDeg.value}deg)`
}));

const hasPrev = computed(() => index.value > 0 || !noMoreOlder.value);
const hasNext = computed(() => index.value < items.value.length - 1 || !noMoreNewer.value);
const canZoomIn = computed(() => currentLoaded.value && scale.value < SCALE_MAX);
const canZoomOut = computed(() => currentLoaded.value && scale.value > SCALE_MIN);
const canLocate = computed(() => !!items.value[index.value]?.localId);

const resetTransform = () => {
  scale.value = 1;
  rotateDeg.value = 0;
};

const unbindKey = () => {
  document.removeEventListener('keydown', onKeydown);
};

const bindKey = () => {
  document.addEventListener('keydown', onKeydown);
};

const reset = () => {
  unbindKey();
  items.value = [];
  index.value = 0;
  scale.value = 1;
  rotateDeg.value = 0;
  convKey.value = '';
  convMinSeqNo.value = 1;
  convMaxSeqNo.value = 0;
  scannedMinSeqNo.value = 0;
  scannedMaxSeqNo.value = 0;
  noMoreOlder.value = true;
  noMoreNewer.value = true;
  downloading.value = false;
};

const close = () => {
  show.value = false;
  reset();
};

const extractImages = (messages: ChatMessage[]) => {
  const list: ImageItem[] = [];
  messages.forEach((m) => {
    if (m.deleted || m.type !== MESSAGE_TYPE.IMAGE || m.status === MESSAGE_STATUS.RECALL || !m.seqNo) {
      return;
    }
    const url = JSON.parse(String(m.content)).originUrl;
    list.push({ url, seqNo: m.seqNo, localId: m.localId });
  });
  return list.sort((a, b) => a.seqNo - b.seqNo);
};

// 将本页图片并入列表，有新增返回 true
const mergeImages = (messages: ChatMessage[], isOlder: boolean) => {
  const exist = new Set(items.value.map((i) => i.seqNo));
  const images = extractImages(messages).filter((i) => !exist.has(i.seqNo));
  if (!images.length) {
    return false;
  }
  if (isOlder) {
    items.value = images.concat(items.value);
    index.value += images.length;
  } else {
    items.value = items.value.concat(images);
  }
  return true;
};

/**
 * 按消息页向更早/更新方向扫描，直到找到图片或到达会话边界
 * @param isOlder true=更早的消息
 */
const loadMore = async (isOlder: boolean) => {
  if (!convKey.value) return;
  const db = getDB();
  if (isOlder) {
    if (noMoreOlder.value) return;
    let cursor = scannedMinSeqNo.value - 1;
    while (cursor >= convMinSeqNo.value) {
      const min = Math.max(convMinSeqNo.value, cursor - PAGE_SIZE + 1);
      const messages = await db.findPageMessage(convKey.value, min, cursor);
      scannedMinSeqNo.value = min;
      if (mergeImages(messages, true) || min <= convMinSeqNo.value) {
        break;
      }
      cursor = min - 1;
    }
    if (scannedMinSeqNo.value <= convMinSeqNo.value) {
      noMoreOlder.value = true;
    }
  } else {
    if (noMoreNewer.value) return;
    let cursor = scannedMaxSeqNo.value + 1;
    while (cursor <= convMaxSeqNo.value) {
      const max = Math.min(convMaxSeqNo.value, cursor + PAGE_SIZE - 1);
      const messages = await db.findPageMessage(convKey.value, cursor, max);
      scannedMaxSeqNo.value = max;
      if (mergeImages(messages, false) || max >= convMaxSeqNo.value) {
        break;
      }
      cursor = max + 1;
    }
    if (scannedMaxSeqNo.value >= convMaxSeqNo.value) {
      noMoreNewer.value = true;
    }
  }
};

const openFromConversation = async (payload: { convKey: string; url: string; seqNo: number; localId?: string | number }) => {
  const conv = await getDB().findConversationByKey(payload.convKey);
  convKey.value = payload.convKey;
  convMinSeqNo.value = conv?.minSeqNo || 1;
  convMaxSeqNo.value = conv?.maxSeqNo ?? 0;
  items.value = [{ url: payload.url, seqNo: payload.seqNo, localId: payload.localId || '' }];
  index.value = 0;
  scannedMinSeqNo.value = payload.seqNo;
  scannedMaxSeqNo.value = payload.seqNo;
  noMoreOlder.value = false;
  noMoreNewer.value = false;
  show.value = true;
  bindKey();
  // 打开时双向预载
  await Promise.all([loadMore(true), loadMore(false)]);
};

const open = async (payload: FullImagePayload) => {
  reset();
  if (typeof payload === 'string') {
    items.value = payload ? [{ url: payload, seqNo: 0 }] : [];
  } else if (payload && payload.url && payload.convKey && payload.seqNo) {
    await openFromConversation({
      convKey: payload.convKey,
      url: payload.url,
      seqNo: payload.seqNo,
      localId: payload.localId
    });
    return;
  } else if (payload && payload.url) {
    items.value = [
      {
        url: payload.url,
        seqNo: payload.seqNo || 0,
        localId: payload.localId || ''
      }
    ];
  }
  if (!items.value.length) return;
  show.value = true;
  bindKey();
};

const move = async (step: number) => {
  const nextIdx = index.value + step;
  if (nextIdx >= 0 && nextIdx < items.value.length) {
    index.value = nextIdx;
    resetTransform();
    return;
  }
  // 已到列表边缘，继续按消息页补载
  if (step < 0) {
    if (noMoreOlder.value) return;
    await loadMore(true);
  } else {
    if (noMoreNewer.value) return;
    await loadMore(false);
  }
  const idx = index.value + step;
  if (idx >= 0 && idx < items.value.length) {
    index.value = idx;
    resetTransform();
  }
};

const onImageLoad = () => {
  const item = items.value[index.value];
  if (item) {
    item.loaded = true;
  }
};

const onImageError = () => {
  const item = items.value[index.value];
  if (item) {
    item.loadError = true;
    item.loaded = false;
  }
};

const zoomIn = () => {
  scale.value = Math.min(SCALE_MAX, +(scale.value + SCALE_STEP).toFixed(2));
};

const zoomOut = () => {
  scale.value = Math.max(SCALE_MIN, +(scale.value - SCALE_STEP).toFixed(2));
};

const rotate = () => {
  rotateDeg.value = (rotateDeg.value + 90) % 360;
};

const locate = async () => {
  const item = items.value[index.value];
  if (!item || !item.localId) return;
  const message = await getDB().findMessageByLocalId(item.localId);
  if (!message || message.deleted || message.status === MESSAGE_STATUS.RECALL) {
    ElMessage.error('无法定位原消息');
    return;
  }
  const key = message.convKey || convKey.value;
  close();
  if (chatStore.isActive(key)) {
    eventBus.emit('locateChatMessage', message);
    return;
  }
  if (!chatStore.conversationMap.get(key)) {
    ElMessage.error('无法定位原消息');
    return;
  }
  chatStore.setPendingLocateMessage(message);
  await chatStore.moveTop(key);
  chatStore.setActive(key);
  void router.push('/home/chat');
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

const triggerDownload = (href: string, fileName: string) => {
  const a = document.createElement('a');
  a.href = href;
  a.download = fileName;
  a.target = '_blank';
  a.rel = 'noopener';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};

const download = async () => {
  if (!currentLoaded.value) return;
  const fileUrl = currentUrl.value;
  if (!fileUrl || downloading.value) return;
  downloading.value = true;
  const fileName = genFileName(fileUrl);
  try {
    const res = await fetch(fileUrl);
    if (!res.ok) throw new Error('fetch failed');
    const blob = await res.blob();
    const objectUrl = URL.createObjectURL(blob);
    triggerDownload(objectUrl, fileName);
    URL.revokeObjectURL(objectUrl);
  } catch {
    triggerDownload(fileUrl, fileName);
  } finally {
    downloading.value = false;
  }
};

const onKeydown = (e: KeyboardEvent) => {
  if (!show.value) return;
  if (e.key === 'Escape') {
    close();
  } else if (e.key === 'ArrowLeft') {
    e.preventDefault();
    void move(-1);
  } else if (e.key === 'ArrowRight') {
    e.preventDefault();
    void move(1);
  }
};

onBeforeUnmount(() => {
  unbindKey();
});

defineExpose({ open, close });
</script>

<style lang="scss">
.full-image {
  position: fixed;
  inset: 0;
  z-index: 9999;

  .mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.85);
  }

  .image-box {
    position: relative;
    width: 100%;
    height: 100%;
    pointer-events: none;
    overflow: hidden;

    img {
      position: absolute;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);
      max-height: calc(100% - 80px);
      max-width: calc(100% - 40px);
      pointer-events: auto;
      user-select: none;
      transition: transform 0.15s ease;
      transform-origin: center center;
    }

    .state-tip {
      position: absolute;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      color: rgba(255, 255, 255, 0.65);
      font-size: 15px;
      pointer-events: none;
      user-select: none;
    }
  }

  .close-btn {
    position: fixed;
    top: 20px;
    right: 20px;
    width: 40px;
    height: 40px;
    border: none;
    border-radius: 50%;
    background: #333;
    opacity: 0.5;
    color: #fff;
    cursor: pointer;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;

    &:hover {
      opacity: 0.8;
    }
  }

  .toolbar {
    position: fixed;
    left: 50%;
    bottom: 28px;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    background: rgba(0, 0, 0, 0.55);
    border-radius: 24px;
    z-index: 2;

    .tool-btn {
      width: 36px;
      height: 36px;
      border: none;
      border-radius: 50%;
      background: transparent;
      color: #fff;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;

      &:hover:not(:disabled) {
        background: rgba(255, 255, 255, 0.15);
      }

      &:disabled {
        opacity: 0.3;
        cursor: default;
      }
    }
  }
}
</style>

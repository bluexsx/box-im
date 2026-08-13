<template>
  <div>
    <el-dialog
      v-model="showRoom"
      class="rtc-private-video-dialog"
      :title="title"
      :width="width"
      top="5vh"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      draggable
      destroy-on-close
      :before-close="onQuit">
      <div class="rtc-private-video">
        <div v-show="isVideo" class="rtc-video-box">
          <div
            v-loading="!isChating"
            class="rtc-video-friend"
            :element-loading-text="'等待对方接听...'"
            element-loading-background="rgba(0, 0, 0, 0.1)">
            <HeadImage
              class="friend-head-image"
              :id="friend.id"
              :size="80"
              :name="friend.nickName"
              :url="friend.headImage"
              :is-show-user-info="false"
              radius="0" />
            <video ref="remoteVideoRef" autoplay />
          </div>
          <div class="rtc-video-mine">
            <video ref="localVideoRef" autoplay />
          </div>
        </div>
        <div
          v-show="!isVideo"
          v-loading="!isChating"
          class="rtc-voice-box"
          :element-loading-text="'等待对方接听...'"
          element-loading-background="rgba(0, 0, 0, 0.1)">
          <HeadImage class="friend-head-image" :id="friend.id" :size="200" :name="friend.nickName" :url="friend.headImage" :is-show-user-info="false">
            <div class="rtc-voice-name">{{ friend.nickName }}</div>
          </HeadImage>
        </div>
        <div class="rtc-control-bar">
          <div class="icon iconfont icon-phone-reject reject" style="color: red" :title="'挂断'" @click="onQuit()" />
        </div>
      </div>
    </el-dialog>
    <RtcPrivateAcceptor v-if="!isHost && isWaiting" :friend="friend" :mode="mode" @accept="onAccept" @reject="onReject" />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import HeadImage from '@/components/common/HeadImage.vue';
import RtcPrivateAcceptor from './RtcPrivateAcceptor.vue';
import ImWebRtc from '@/utils/webrtc';
import ImCamera from '@/utils/camera';
import {
  callPrivate,
  acceptPrivate,
  rejectPrivate,
  cancelPrivate,
  failedPrivate,
  handupPrivate,
  candidatePrivate,
  heartbeatPrivate
} from '@/api/webrtcPrivate';
import { findFriend } from '@/api/friend';
import type { FriendVO } from '@/api/friend/types';
import { useConfigStore } from '@/stores/config';
import { MESSAGE_TYPE } from '@/utils/enums';
import type { ChatMessage } from '@/types';
import callWav from '@/assets/audio/call.wav';

export type PrivateRtcInfo = {
  mode: string;
  isHost: boolean;
  friend: FriendVO;
};

const configStore = useConfigStore();
const camera = new ImCamera();
const webrtc = new ImWebRtc();
const audio = new Audio();
const remoteVideoRef = ref<HTMLVideoElement>();
const localVideoRef = ref<HTMLVideoElement>();
const showRoom = ref(false);
const friend = ref<FriendVO>({ id: 0, nickName: '' });
const isHost = ref(false);
const state = ref<'CLOSE' | 'WAITING' | 'CHATING' | 'ERROR'>('CLOSE');
const mode = ref('video');
const localStream = ref<MediaStream | null>(null);
const videoTime = ref(0);
const candidates = ref<RTCIceCandidate[]>([]);
const offer = ref<RTCSessionDescriptionInit | null>(null);
let videoTimer: ReturnType<typeof setInterval> | null = null;
let heartbeatTimer: ReturnType<typeof setInterval> | null = null;
let waitTimer: ReturnType<typeof setTimeout> | null = null;
const isVideo = computed(() => mode.value == 'video');
const isChating = computed(() => state.value == 'CHATING');
const isWaiting = computed(() => state.value == 'WAITING');
const isClose = computed(() => state.value == 'CLOSE');
const modeText = computed(() => (isVideo.value ? '视频通话' : '语音通话'));
const width = computed(() => (isVideo.value ? '960px' : '360px'));

const configuration = computed(() => {
  const iceServers = (configStore.webrtc as { iceServers?: RTCIceServer[] }).iceServers;
  return { iceServers };
});

const currentTime = computed(() => {
  const min = Math.floor(videoTime.value / 60);
  const sec = videoTime.value % 60;
  return `${min < 10 ? '0' : ''}${min}:${sec < 10 ? '0' : ''}${sec}`;
});

const title = computed(() => {
  let strTitle = `${modeText.value}-${friend.value.nickName}`;
  if (isChating.value) {
    strTitle += `(${currentTime.value})`;
  } else if (isWaiting.value) {
    strTitle += `(${'呼叫中'})`;
  }
  return strTitle;
});

const initAudio = () => {
  audio.src = callWav;
  audio.loop = true;
};

const initRtc = () => {
  webrtc.init(configuration.value);
  webrtc.setupPeerConnection((stream) => {
    if (remoteVideoRef.value) {
      remoteVideoRef.value.srcObject = stream;
    }
  });
  webrtc.onIcecandidate((candidate) => {
    if (state.value == 'CHATING') {
      void candidatePrivate(friend.value.id, candidate);
    } else {
      candidates.value.push(candidate);
    }
  });
  webrtc.onStateChange((s) => {
    console.log('ICE连接状态变化:', s);
  });
};

const openStream = () => {
  return new Promise<MediaStream>((resolve, reject) => {
    if (isVideo.value) {
      camera
        .openVideo()
        .then((stream) => {
          localStream.value = stream;
          nextTick(() => {
            if (localVideoRef.value) {
              localVideoRef.value.srcObject = stream;
              localVideoRef.value.muted = true;
            }
          });
          resolve(stream);
        })
        .catch((e) => {
          ElMessage.error('打开摄像头失败');
          reject(e);
        });
    } else {
      camera
        .openAudio()
        .then((stream) => {
          localStream.value = stream;
          if (localVideoRef.value) {
            localVideoRef.value.srcObject = stream;
            localVideoRef.value.muted = true;
          }
          resolve(stream);
        })
        .catch((e) => {
          ElMessage.error('打开麦克风失败');
          reject(e);
        });
    }
  });
};

const checkDevEnable = () => {
  if (!camera.isEnable()) {
    ElMessage.error('访问摄像头失败');
    return false;
  }
  if (!webrtc.isEnable()) {
    ElMessage.error('初始化RTC失败，原因可能是: 1.服务器缺少ssl证书 2.您的设备不支持WebRTC');
    return false;
  }
  return true;
};

const startHeartBeat = () => {
  if (heartbeatTimer) clearInterval(heartbeatTimer);
  heartbeatTimer = setInterval(() => {
    void heartbeatPrivate(friend.value.id);
  }, 15000);
};

const startChatTime = () => {
  videoTime.value = 0;
  if (videoTimer) clearInterval(videoTimer);
  videoTimer = setInterval(() => {
    videoTime.value++;
  }, 1000);
};

const close = () => {
  showRoom.value = false;
  camera.close();
  webrtc.close();
  audio.pause();
  videoTime.value = 0;
  if (videoTimer) clearInterval(videoTimer);
  if (heartbeatTimer) clearInterval(heartbeatTimer);
  if (waitTimer) clearTimeout(waitTimer);
  videoTimer = null;
  heartbeatTimer = null;
  waitTimer = null;
  state.value = 'CLOSE';
  candidates.value = [];
  offer.value = null;
};

const onCall = () => {
  if (!checkDevEnable()) {
    close();
    return;
  }
  initRtc();
  startHeartBeat();
  openStream()
    .then(() => {
      webrtc.setStream(localStream.value);
      return webrtc.createOffer();
    })
    .then((sdp) => callPrivate(friend.value.id, mode.value, sdp))
    .then(() => {
      state.value = 'WAITING';
      void audio.play();
    })
    .catch(() => {
      close();
    });
};

const open = (rtcInfo: PrivateRtcInfo) => {
  showRoom.value = true;
  mode.value = rtcInfo.mode;
  isHost.value = rtcInfo.isHost;
  friend.value = rtcInfo.friend;
  if (isHost.value) {
    onCall();
  }
};

const onAccept = () => {
  if (!checkDevEnable()) {
    void failedPrivate(friend.value.id, '对方设备不支持通话');
    close();
    return;
  }
  showRoom.value = true;
  state.value = 'CHATING';
  audio.pause();
  initRtc();
  openStream().finally(() => {
    webrtc.setStream(localStream.value);
    if (!offer.value) return;
    webrtc.createAnswer(offer.value).then((answer) => {
      void acceptPrivate(friend.value.id, answer);
      startChatTime();
      if (waitTimer) clearTimeout(waitTimer);
    });
  });
};

const onReject = () => {
  void rejectPrivate(friend.value.id);
  close();
};

const onHandup = () => {
  void handupPrivate(friend.value.id);
  ElMessage.success('您已挂断,通话结束');
  close();
};

const onCancel = () => {
  void cancelPrivate(friend.value.id);
  ElMessage.success('已取消呼叫,通话结束');
  close();
};

const onQuit = () => {
  if (isChating.value) {
    onHandup();
  } else if (isWaiting.value) {
    onCancel();
  } else {
    close();
  }
};

const onRTCCall = (msg: ChatMessage, callMode: string) => {
  offer.value = JSON.parse(String(msg.content));
  isHost.value = false;
  mode.value = callMode;
  findFriend(msg.sendId!).then((f) => {
    friend.value = f;
    state.value = 'WAITING';
    void audio.play();
    startHeartBeat();
    waitTimer = setTimeout(() => {
      void failedPrivate(friend.value.id, '对方无应答');
      ElMessage.error('您未接听');
      close();
    }, 30000);
  });
};

const onRTCAccept = (msg: ChatMessage) => {
  if (msg.selfSend) {
    ElMessage.success('已在其他设备接听');
    close();
  } else {
    const answer = JSON.parse(String(msg.content));
    webrtc.setRemoteDescription(answer);
    state.value = 'CHATING';
    audio.pause();
    candidates.value.forEach((c) => {
      void candidatePrivate(friend.value.id, c);
    });
    startChatTime();
  }
};

const onRTCReject = (msg: ChatMessage) => {
  if (msg.selfSend) {
    ElMessage.success('已在其他设备拒绝');
  } else {
    ElMessage.error('对方拒绝了您的通话请求');
  }
  close();
};

const onRTCFailed = (msg: ChatMessage) => {
  ElMessage.error(String(msg.content));
  close();
};

const onRTCCancel = () => {
  ElMessage.success('对方取消了呼叫');
  close();
};

const onRTCHandup = () => {
  ElMessage.success('对方已挂断');
  close();
};

const onRTCCandidate = (msg: ChatMessage) => {
  const candidate = JSON.parse(String(msg.content));
  webrtc.addIceCandidate(candidate);
};

const onRTCMessage = (msg: ChatMessage) => {
  if (msg.type != MESSAGE_TYPE.RTC_CALL_VOICE && msg.type != MESSAGE_TYPE.RTC_CALL_VIDEO && isClose.value) {
    return;
  }
  switch (msg.type) {
    case MESSAGE_TYPE.RTC_CALL_VOICE:
      onRTCCall(msg, 'voice');
      break;
    case MESSAGE_TYPE.RTC_CALL_VIDEO:
      onRTCCall(msg, 'video');
      break;
    case MESSAGE_TYPE.RTC_ACCEPT:
      onRTCAccept(msg);
      break;
    case MESSAGE_TYPE.RTC_REJECT:
      onRTCReject(msg);
      break;
    case MESSAGE_TYPE.RTC_CANCEL:
      onRTCCancel();
      break;
    case MESSAGE_TYPE.RTC_FAILED:
      onRTCFailed(msg);
      break;
    case MESSAGE_TYPE.RTC_HANDUP:
      onRTCHandup();
      break;
    case MESSAGE_TYPE.RTC_CANDIDATE:
      onRTCCandidate(msg);
      break;
  }
};
onMounted(() => {
  initAudio();
  window.addEventListener('beforeunload', onQuit);
});
onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', onQuit);
  onQuit();
});
defineExpose({ open, onRTCMessage });
</script>

<style lang="scss" scoped>
.rtc-private-video {
  position: relative;

  .rtc-video-box {
    position: relative;
    background-color: #eeeeee;

    .rtc-video-friend {
      height: 70vh;

      .friend-head-image {
        position: absolute;
      }

      video {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transform: rotateY(180deg);
      }
    }

    .rtc-video-mine {
      position: absolute;
      z-index: 99999;
      width: 25vh;
      right: 0;
      bottom: -1px;

      video {
        width: 100%;
        object-fit: cover;
        transform: rotateY(180deg);
      }
    }
  }

  .rtc-voice-box {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 300px;
    background-color: var(--im-color-primary-light-9);

    .rtc-voice-name {
      text-align: center;
      font-size: 20px;
      font-weight: 600;
    }
  }

  .rtc-control-bar {
    display: flex;
    justify-content: space-around;
    padding: 10px;

    .icon {
      font-size: 50px;
      cursor: pointer;
    }
  }
}
</style>

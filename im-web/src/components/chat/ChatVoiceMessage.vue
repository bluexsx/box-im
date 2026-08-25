<template>
  <div class="chat-voice-message" :class="[mode, { playing: audioPlayState === 'PLAYING', mine }]" @click.stop="onPlayVoice">
    <div class="voice-body">
      <span class="iconfont icon-voice-play voice-icon"></span>
      <div class="voice-wave">
        <span v-for="i in 5" :key="i" class="wave-bar"></span>
      </div>
      <span class="voice-duration">{{ displayDuration }}"</span>
      <span v-if="audioPlayState === 'PLAYING'" class="iconfont icon-pause play-indicator"></span>
      <span v-else-if="audioPlayState === 'PAUSE'" class="iconfont icon-play play-indicator"></span>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
const props = defineProps({
  url: {
    type: String,
    default: ''
  },
  duration: {
    type: Number,
    default: 0
  },
  mine: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String as () => 'chat' | 'history',
    default: 'chat'
  }
});
const emit = defineEmits<{ audioStateChange: [state: string] }>();
const audioPlayState = ref<'STOP' | 'PLAYING' | 'PAUSE'>('STOP');
const audioEl = ref<HTMLAudioElement>();
const displayDuration = computed(() => props.duration || 0);

const emitAudioStateChange = () => {
  emit('audioStateChange', audioPlayState.value);
};

const stopPlayAudio = () => {
  if (audioEl.value) {
    audioEl.value.pause();
    audioEl.value = undefined;
  }
  audioPlayState.value = 'STOP';
};

const onPlayVoice = () => {
  if (!props.url) {
    return;
  }
  if (audioPlayState.value === 'STOP') {
    if (audioEl.value) {
      audioEl.value.pause();
    }
    audioEl.value = new Audio(props.url);
    audioEl.value.onended = () => {
      audioPlayState.value = 'STOP';
      emitAudioStateChange();
    };
    audioEl.value.onerror = () => {
      audioPlayState.value = 'STOP';
      emitAudioStateChange();
    };
    audioEl.value.play();
    audioPlayState.value = 'PLAYING';
  } else if (audioPlayState.value === 'PLAYING') {
    audioEl.value?.pause();
    audioPlayState.value = 'PAUSE';
  } else if (audioPlayState.value === 'PAUSE') {
    audioEl.value?.play();
    audioPlayState.value = 'PLAYING';
  }
  emitAudioStateChange();
};
onBeforeUnmount(() => {
  stopPlayAudio();
});
defineExpose({ stopPlayAudio });
</script>
<style lang="scss" scoped>
.chat-voice-message {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.85;
  }

  .voice-body {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .voice-icon {
    font-size: 19px;
    flex-shrink: 0;
  }

  .voice-wave {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 3px;
    width: 36px;
    height: 20px;
    flex-shrink: 0;

    .wave-bar {
      width: 3px;
      height: 8px;
      border-radius: 2px;
      background: currentcolor;
      opacity: 0.45;
      flex-shrink: 0;
    }
  }

  &.playing .voice-wave .wave-bar {
    animation: voice-wave-bounce 0.8s ease-in-out infinite;

    &:nth-child(1) {
      animation-delay: 0s;
    }

    &:nth-child(2) {
      animation-delay: 0.1s;
    }

    &:nth-child(3) {
      animation-delay: 0.2s;
    }

    &:nth-child(4) {
      animation-delay: 0.3s;
    }

    &:nth-child(5) {
      animation-delay: 0.4s;
    }
  }

  .voice-duration {
    font-size: var(--im-font-size-small);
    flex-shrink: 0;
    white-space: nowrap;
  }

  .play-indicator {
    font-size: 17px;
    flex-shrink: 0;
    opacity: 0.9;
  }

  &.chat {
    padding: 8px 12px;
    line-height: 26px;
    background: var(--im-background);
    border-radius: 10px;
    font-size: var(--im-font-size);
    text-align: left;

    &.mine {
      background: var(--im-color-primary-light-2);
      color: white;

      .voice-body {
        flex-direction: row-reverse;
      }

      .voice-icon {
        transform: scaleX(-1);
      }
    }
  }

  &.history {
    padding: 8px 12px;
    line-height: 26px;
    background: var(--im-background);
    border-radius: 10px;
    font-size: var(--im-font-size);
    text-align: left;
  }
}

@keyframes voice-wave-bounce {
  0%,
  100% {
    height: 6px;
    opacity: 0.35;
  }

  50% {
    height: 16px;
    opacity: 1;
  }
}
</style>

<template>
  <div class="head-image" :style="{ cursor: isShowUserInfo ? 'pointer' : undefined }" @click="showUserInfo">
    <img v-show="url" class="avatar-image" :src="url" :style="avatarImageStyle" loading="lazy" />
    <div v-show="!url" class="avatar-text" :style="avatarTextStyle">{{ avatarText }}</div>
    <div v-show="online" class="online" :title="'用户当前在线'" />
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { findUser } from '@/api/user';
import eventBus from '@/utils/eventBus';

const props = defineProps({
  id: {
    type: Number,
    required: true
  },
  size: {
    type: Number,
    default: 42
  },
  width: {
    type: Number
  },
  height: {
    type: Number
  },
  radius: {
    type: String,
    default: '50%'
  },
  url: {
    type: String
  },
  name: {
    type: String
  },
  online: {
    type: Boolean,
    default: false
  },
  isShowUserInfo: {
    type: Boolean,
    default: false
  }
});

const colors = ['#5daa31', '#c7515a', '#e03697', '#85029b', '#c9b455', '#326eb6'];

const isChinese = (charCode: number) => {
  return charCode >= 0x4e00 && charCode <= 0x9fa5;
};

const avatarImageStyle = computed(() => {
  const w = props.width ?? props.size;
  const h = props.height ?? props.size;
  return `width:${w}px;height:${h}px;border-radius:${props.radius};`;
});

const avatarTextStyle = computed(() => {
  const w = props.width ?? props.size;
  const h = props.height ?? props.size;
  return `width:${w}px;height:${h}px;background:linear-gradient(145deg,#ffffff20 25%,#00000060),${textColor.value};font-size:${w * 0.4}px;border-radius:${props.radius};`;
});

const avatarText = computed(() => {
  if (!props.name) {
    return '';
  }
  if (isChinese(props.name.charCodeAt(0))) {
    return props.name.charAt(0);
  }
  return props.name.charAt(0).toUpperCase() + props.name.charAt(1);
});

const textColor = computed(() => {
  return colors[props.id % colors.length];
});

const showUserInfo = async (e: MouseEvent) => {
  if (!props.isShowUserInfo) {
    return;
  }
  if (props.id && props.id > 0) {
    const user = await findUser(props.id);
    const pos = { x: e.x + 30, y: e.y };
    eventBus.emit('openUserInfo', { user, pos });
  }
};
</script>

<style scoped lang="scss">
.head-image {
  position: relative;

  .avatar-image {
    position: relative;
    overflow: hidden;
    display: block;
    cursor: pointer;
  }

  .avatar-text {
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .online {
    position: absolute;
    right: -4px;
    bottom: 0;
    width: 22%;
    height: 22%;
    min-width: 10px;
    min-height: 10px;
    background: var(--im-color-success);
    border-radius: 50%;
    border: 2px solid white;
  }
}
</style>

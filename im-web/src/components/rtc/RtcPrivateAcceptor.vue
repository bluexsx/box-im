<template>
  <div class="rtc-private-acceptor">
    <div class="acceptor-container">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-container">
          <HeadImage :id="friend.id" :name="friend.nickName" :url="friend.headImage" :size="80" />
          <div class="avatar-pulse" />
        </div>
      </div>
      <div class="info-section">
        <div class="caller-name">{{ friend.nickName }}</div>
        <div class="call-type">{{ modeText }}</div>
        <div class="call-status">{{ '来电中...' }}</div>
      </div>
      <div class="button-section">
        <div class="action-buttons">
          <div class="action-btn accept-btn iconfont icon-phone-accept" :title="'接受'" @click="emit('accept')" />
          <div class="action-btn reject-btn iconfont icon-phone-reject" :title="'拒绝'" @click="emit('reject')" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import HeadImage from '@/components/common/HeadImage.vue';
import type { FriendVO } from '@/api/friend/types';

const props = defineProps<{
  mode?: string;
  friend: FriendVO;
}>();

const emit = defineEmits<{
  accept: [];
  reject: [];
}>();

const modeText = computed(() => (props.mode == 'video' ? '视频通话' : '语音通话'));
</script>

<style scoped lang="scss">
.rtc-private-acceptor {
  position: fixed;
  bottom: 0;
  right: 0;
  z-index: 10000;
  width: 320px;

  .acceptor-container {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 30px 25px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.2);
    text-align: center;

    .avatar-section {
      margin-bottom: 25px;

      .avatar-container {
        position: relative;
        display: inline-block;

        .avatar-pulse {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 100px;
          height: 100px;
          border-radius: 50%;
          background: rgba(76, 175, 80, 0.2);
          animation: pulse-ring 2s infinite;

          &::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 120px;
            height: 120px;
            border-radius: 50%;
            background: rgba(76, 175, 80, 0.1);
            animation: pulse-ring 2s infinite 0.5s;
          }
        }
      }
    }

    .info-section {
      margin-bottom: 30px;

      .caller-name {
        font-size: 22px;
        font-weight: 700;
        color: var(--im-text-color-primary);
        margin-bottom: 8px;
      }

      .call-type {
        font-size: 16px;
        color: var(--im-text-color);
        margin-bottom: 6px;
        font-weight: 500;
      }

      .call-status {
        font-size: 14px;
        color: var(--im-text-color-light);
        font-weight: 500;
      }
    }

    .button-section {
      .action-buttons {
        display: flex;
        justify-content: center;
        gap: 50px;

        .action-btn {
          border-radius: 50%;
          cursor: pointer;
          transition: all 0.3s ease;
          font-size: 60px;

          &:hover {
            transform: scale(1.1);
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
          }

          &.reject-btn {
            color: var(--im-color-danger);
          }

          &.accept-btn {
            color: var(--im-color-success);
            animation: accept-pulse 1.5s infinite;
          }
        }
      }
    }
  }
}

@keyframes pulse-ring {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }

  100% {
    transform: translate(-50%, -50%) scale(1.3);
    opacity: 0;
  }
}

@keyframes accept-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.4);
  }

  70% {
    box-shadow: 0 0 0 20px rgba(76, 175, 80, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0);
  }
}
</style>

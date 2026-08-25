<template>
  <el-dialog v-model="isShow" title="多人通话" top="8vh" width="580px" :close-on-click-modal="false" :close-on-press-escape="false" draggable destroy-on-close>
    <div class="rtc-group-video">
      <div class="preview">
        <div class="preview-icon">
          <el-icon :size="28"><VideoCamera /></el-icon>
        </div>
        <div class="preview-title">当前版本暂不支持多人音视频通话</div>
        <div class="preview-sub">开源版仅提供入口展示，完整能力请升级商业版</div>
      </div>
      <div class="content">
        <p class="desc">多人通话为商业版专属功能。如需在群组内发起语音或视频通话，可查阅商业版说明文档了解功能详情与购买方式。</p>
        <ul class="feature-list">
          <li>
            <el-icon class="check"><Check /></el-icon>
            多人同时语音 / 视频通话
          </li>
          <li>
            <el-icon class="check"><Check /></el-icon>
            群内一键发起，成员可随时加入
          </li>
          <li>
            <el-icon class="check"><Check /></el-icon>
            通话状态实时同步至群聊
          </li>
        </ul>
        <a class="doc-link" :href="DOC_URL" target="_blank" rel="noopener noreferrer">
          <el-icon><Document /></el-icon>
          <span>盒子IM 商业版付费说明</span>
          <el-icon><ArrowRight /></el-icon>
        </a>
      </div>
    </div>
    <template #footer>
      <el-button @click="isShow = false">我知道了</el-button>
      <el-button type="primary" @click="openDoc">查看说明</el-button>
    </template>
  </el-dialog>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { ArrowRight, Check, Document, VideoCamera } from '@element-plus/icons-vue';
const DOC_URL = 'https://www.yuque.com/u1475064/imk5n2/qtezcg32q1d0dr29';
const isShow = ref(false);
const open = () => {
  isShow.value = true;
};

const openDoc = () => {
  window.open(DOC_URL, '_blank');
};

const onRTCMessage = () => {
  // 开源版不处理多人通话信令
};
defineExpose({ open, onRTCMessage });
</script>
<style lang="scss" scoped>
.rtc-group-video {
  .preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 28px 24px;
    text-align: center;
    background: var(--im-color-primary-light-9);
    border-radius: 8px;

    .preview-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 60px;
      height: 60px;
      margin-bottom: 14px;
      color: var(--im-color-primary);
      background: var(--im-background);
      border-radius: 50%;
      box-shadow: var(--im-box-shadow-lighter);
    }

    .preview-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--im-text-color);
    }

    .preview-sub {
      margin-top: 6px;
      font-size: 13px;
      line-height: 1.5;
      color: var(--im-text-color-lighter);
    }
  }

  .content {
    padding: 18px 4px 4px;

    .desc {
      margin: 0 0 18px;
      font-size: 14px;
      line-height: 1.8;
      color: var(--im-text-color-light);
    }

    .feature-list {
      margin: 0 0 20px;
      padding: 0;
      list-style: none;

      li {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 0;
        font-size: 14px;
        color: var(--im-text-color);

        .check {
          font-size: 14px;
          color: var(--im-color-success);
        }
      }
    }

    .doc-link {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 14px 16px;
      font-size: 14px;
      color: var(--im-color-primary);
      text-decoration: none;
      background: var(--im-background-active);
      border: 1px solid #ededed;
      border-radius: 8px;
      transition: all 0.2s;

      span {
        flex: 1;
      }

      &:hover {
        color: var(--im-color-primary-light-2);
        border-color: var(--im-color-primary-light-5);
        background: var(--im-color-primary-light-9);
      }
    }
  }
}
</style>

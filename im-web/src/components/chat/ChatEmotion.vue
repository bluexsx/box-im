<template>
  <div v-show="show" @click="close()">
    <div class="chat-emotion" :style="{ left: x + 'px', top: y + 'px' }" @click.stop>
      <div class="emotion-main">
        <el-scrollbar class="emotion-scroll">
          <div class="emotion-section">
            <div v-if="recentEmojiList.length" class="emotion-group">
              <div class="emotion-group-title">最近使用</div>
              <div class="emotion-items">
                <div
                  v-for="(emoText, i) in recentEmojiList"
                  :key="'recent-' + emoText + '-' + i"
                  class="emotion-item"
                  :title="emoText"
                  @click="onClickEmo(emoText)">
                  <img :src="emojiUrl(emoText)" :alt="emoText" :title="emoText" class="emoji-large" />
                </div>
              </div>
            </div>
            <div class="emotion-group">
              <div class="emotion-group-title">全部表情</div>
              <div class="emotion-items">
                <div
                  v-for="(emoText, i) in emo.emoTextList"
                  :key="'default-' + i"
                  class="emotion-item"
                  :title="emoText"
                  @click="onClickEmo(emoText)">
                  <img :src="emojiUrl(emoText)" :alt="emoText" :title="emoText" class="emoji-large" />
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
      <div class="album-tabs">
        <div class="album-tabs-nav">
          <div class="album-tab-item active" title="默认表情">
            <span class="album-tab-thumb">
              <i class="icon iconfont icon-emoji" />
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { getDB } from '@/db';
import * as emo from '@/utils/emotion';

const emit = defineEmits<{ emotion: [string] }>();
const show = ref(false);
const pos = ref({ x: 0, y: 0 });
const recentEmojiList = ref<string[]>([]);
const x = computed(() => pos.value.x - 22);
const y = computed(() => pos.value.y - 332);
const emojiUrl = (emoText: string) => emo.textToUrl(emo.formatEmoji(emoText));

const loadRecentEmojis = async () => {
  const list = await getDB().findRecentEmojis();
  recentEmojiList.value = emo.filterRecentEmojis(list);
};

const onClickEmo = (emoText: string) => {
  void getDB().addRecentEmoji(emoText);
  void loadRecentEmojis();
  emit('emotion', emo.formatEmoji(emoText));
  close();
};

const open = (p: { x: number; y: number }) => {
  pos.value = p;
  void loadRecentEmojis();
  show.value = true;
};

const close = () => {
  show.value = false;
};
defineExpose({ open, close });
</script>

<style scoped lang="scss">
.chat-emotion {
  position: fixed;
  width: 480px;
  box-sizing: border-box;
  background: #f5f6f7;
  box-shadow: var(--im-box-shadow-lighter);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 2000;

  .emotion-main {
    flex: 1;
    background: #fff;
    border-radius: 4px 4px 0 0;
    overflow: hidden;
    padding: 8px 4px 0;
  }

  .emotion-scroll {
    height: 270px;
  }

  .emotion-group {
    &:not(:last-child) {
      margin-bottom: 10px;
    }

    .emotion-group-title {
      font-size: 11px;
      color: var(--im-text-color-light);
      margin-bottom: 8px;
      padding: 0 4px;
      text-align: left;
    }
  }

  .emotion-items {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    padding: 4px 4px 0;
    gap: 5px;

    .emotion-item {
      display: flex;
      justify-content: center;
      align-items: center;
      text-align: center;
      cursor: pointer;
      margin: 5px;
      border-radius: 4px;

      &:hover {
        background: var(--im-background-active-dark);
      }
    }
  }

  .album-tabs {
    display: flex;
    align-items: flex-start;
    margin: 0 -8px;
    padding: 0 10px;
    min-height: 46px;
    background: #f5f6f7;
    gap: 4px;

    .album-tabs-nav {
      flex: 1;
      display: flex;
      align-items: flex-start;
      justify-content: flex-start;
      gap: 0;
      overflow: hidden;
      height: 44px;
    }

    .album-tab-item {
      box-sizing: border-box;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 50px;
      height: 44px;
      cursor: default;
      transition: background 0.15s;
      border-radius: 0 0 5px 5px;

      .album-tab-thumb {
        flex-shrink: 0;
        width: 30px;
        height: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;

        i {
          font-size: 25px;
          line-height: 1;
          color: #333;
        }
      }

      &.active {
        background: #fff;
      }
    }
  }
}
</style>

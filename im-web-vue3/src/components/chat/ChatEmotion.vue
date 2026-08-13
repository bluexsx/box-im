<template>
  <div v-show="show" @click="close()">
    <div class="emotion-box" :style="{ left: x + 'px', top: y + 'px' }" @click.stop>
      <el-scrollbar class="emotion-scroll">
        <div class="emotion-content">
          <div v-if="recentEmojiList.length" class="emotion-group">
            <div class="emotion-group-title">{{ '最近使用' }}</div>
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
            <div v-if="recentEmojiList.length" class="emotion-group-title">{{ '全部表情' }}</div>
            <div class="emotion-items">
              <div v-for="(emoText, i) in emo.emoTextList" :key="'default-' + i" class="emotion-item" :title="emoText" @click="onClickEmo(emoText)">
                <img :src="emojiUrl(emoText)" :alt="emoText" :title="emoText" class="emoji-large" />
              </div>
            </div>
          </div>
        </div>
      </el-scrollbar>
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
const y = computed(() => pos.value.y - 284);
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
.emotion-box {
  position: fixed;
  width: 480px;
  box-sizing: border-box;
  padding: 8px;
  background-color: #fff;
  box-shadow: var(--im-box-shadow-lighter);
  border-radius: 6px;
  z-index: 2000;

  .emotion-scroll {
    height: 270px;
  }

  .emotion-content {
    padding: 8px 4px;
  }

  .emotion-group {
    &:not(:last-child) {
      margin-bottom: 10px;
    }

    .emotion-group-title {
      font-size: 12px;
      color: #999;
      margin-bottom: 6px;
      padding-left: 4px;
    }

    .emotion-items {
      display: flex;
      flex-wrap: wrap;

      .emotion-item {
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        border-radius: 4px;

        &:hover {
          background: #f5f5f5;
        }

        .emoji-large {
          width: 28px;
          height: 28px;
        }
      }
    }
  }
}
</style>

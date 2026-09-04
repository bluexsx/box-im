<template>
  <div>
    <div class="friend-item" :class="itemClass" @contextmenu.prevent="showRightMenu($event)">
      <div class="friend-avatar">
        <HeadImage :size="headImageSize" :id="friend.id" :name="friend.nickName" :url="friend.headImage" :online="friend.online" :is-show-user-info="true" />
      </div>
      <div class="friend-info">
        <div class="friend-name">
          <div class="friend-name-text" :title="friend.nickName">{{ friend.nickName }}</div>
        </div>
        <div class="friend-online">
          <el-icon v-show="friend.onlineWeb" class="online" :title="'电脑设备在线'">
            <Monitor />
            <span class="online-icon" />
          </el-icon>
          <el-icon v-show="friend.onlineApp" class="online" :title="'移动设备在线'">
            <Iphone />
            <span class="online-icon" />
          </el-icon>
        </div>
      </div>
      <slot />
    </div>
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { Iphone, Monitor } from '@element-plus/icons-vue';
import HeadImage from '@/components/common/HeadImage.vue';
import RightMenu, { type RightMenuItem } from '@/components/common/RightMenu.vue';
import type { FriendVO } from '@/api/friend/types';

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  friend: {
    type: Object as () => FriendVO,
    required: true
  },
  menu: {
    type: Boolean,
    default: true
  },
  size: {
    type: String,
    default: 'normal'
  }
});

const emit = defineEmits(['chat', 'card', 'delete', 'del']);

const rightMenuRef = ref<InstanceType<typeof RightMenu>>();

const menuItems = computed<RightMenuItem[]>(() => [
  { key: 'CHAT', name: '发消息' },
  { key: 'CARD', name: '分享名片' },
  { key: 'DELETE', name: '删除好友', danger: true }
]);

const headImageSize = computed(() => (props.size == 'small' ? 36 : 42));

const itemClass = computed(() => {
  let clz = '';
  if (props.active) clz += 'active';
  if (props.size == 'small') clz += ' small';
  return clz;
});

const showRightMenu = (e: MouseEvent) => {
  if (props.menu) {
    rightMenuRef.value?.open(e, menuItems.value);
  }
};

const onSelectMenu = (item: RightMenuItem) => {
  emit(String(item.key).toLowerCase() as 'chat' | 'card' | 'delete');
};
</script>

<style scoped lang="scss">
.friend-item {
  height: 60px;
  display: flex;
  position: relative;
  align-items: center;
  white-space: nowrap;
  border-radius: 10px;
  margin: 0 3px;
  padding: 5px 8px;
  cursor: pointer;

  &:hover {
    background-color: var(--im-background-active);
  }

  &.active {
    background-color: var(--im-background-active-dark);
  }

  &.small {
    height: 48px;
    padding: 3px 10px;
  }

  .friend-avatar {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .friend-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding-left: 10px;
    text-align: left;
    overflow: hidden;

    .friend-name {
      display: flex;
      align-items: center;

      .friend-name-text {
        font-size: var(--im-font-size);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .friend-online {
      .online {
        font-weight: bold;
        padding-right: 2px;
        font-size: 16px;
        position: relative;
      }

      .online-icon {
        position: absolute;
        right: 0;
        bottom: 0;
        width: 6px;
        height: 6px;
        background: limegreen;
        border-radius: 50%;
        border: 1px solid white;
      }
    }
  }
}
</style>

<template>
  <el-container class="friend-page">
    <ResizableAside :default-width="260" :min-width="200" :max-width="500" storage-key="friend-aside-width">
      <div class="header">
        <el-input v-model="searchText" class="search-text" :placeholder="'搜索'">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button plain class="add-btn" :icon="Plus" :title="'添加好友'" @click="onShowAddFriend()" />
        <AddFriend :dialog-visible="showAddFriend" @close="onCloseAddFriend" />
      </div>
      <el-scrollbar class="friend-items">
        <div v-for="(friends, i) in friendValues" :key="friendKeys[i]">
          <div class="letter">{{ friendKeys[i] }}</div>
          <div v-for="friend in friends" :id="String(friend.id)" :key="friend.id">
            <FriendItem
              :friend="friend"
              :active="friend.id === activeFriend.id"
              @chat="onSendMessage(friend)"
              @delete="onDelFriend(friend)"
              @click="onActiveItem(friend)" />
          </div>
        </div>
      </el-scrollbar>
    </ResizableAside>
    <el-container v-show="userInfo.id" class="container">
      <div class="header">
        <div class="nick-name">{{ userInfo.nickName }}</div>
      </div>
      <div class="friend-info">
        <div class="info-card">
          <div class="avatar-section">
            <HeadImage
              :size="120"
              :id="userInfo.id"
              :name="userInfo.nickName"
              :url="userInfo.headImage"
              @click="showFullImage()" />
          </div>
          <div class="info-section">
            <div class="name-header">
              <h2 class="friend-name">{{ userInfo.nickName }}</h2>
              <div v-if="isFriend" class="more-btn" :title="'更多'" @click="onClickMore">
                <el-icon><MoreFilled /></el-icon>
              </div>
            </div>
            <div class="info-item-list">
              <div class="info-item">
                <span class="info-label">{{ '用户名(ID)' }}</span>
                <span class="info-value">
                  {{ userInfo.userName }}
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ '昵称' }}</span>
                <span class="info-value">{{ userInfo.nickName }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ '性别' }}</span>
                <span class="info-value">{{ userInfo.sex == 0 ? '男' : '女' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ '个性签名' }}</span>
                <span v-if="userInfo.signature" class="info-value">{{ userInfo.signature }}</span>
                <span v-else class="info-value light-text">{{ '这个人很懒，什么都没有留下' }}</span>
              </div>
            </div>
            <div class="btn-group">
              <el-button v-show="isFriend" :icon="Position" type="primary" @click="onSendMessage(activeFriend)">
                {{ '发消息' }}
              </el-button>
              <el-button v-show="!isFriend" :icon="Plus" type="primary" @click="onAddFriend()">
                {{ '加为好友' }}
              </el-button>
              <el-button v-show="isFriend" :icon="Delete" type="danger" @click="onDelFriend(activeFriend)">
                {{ '删除好友' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-container>
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
    <CleanMessageConfirm ref="cleanMessageConfirmRef" />
  </el-container>
</template>

<script setup lang="ts">
import { computed, nextTick, onActivated, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Delete, MoreFilled, Plus, Position, Search } from '@element-plus/icons-vue';
import { pinyin } from 'pinyin-pro';
import AddFriend from '@/components/friend/AddFriend.vue';
import FriendItem from '@/components/friend/FriendItem.vue';
import CleanMessageConfirm from '@/components/common/CleanMessageConfirm.vue';
import HeadImage from '@/components/common/HeadImage.vue';
import ResizableAside from '@/components/common/ResizableAside.vue';
import RightMenu from '@/components/common/RightMenu.vue';
import type { RightMenuItem } from '@/components/common/RightMenu.vue';
import { addFriend, deleteFriend } from '@/api/friend';
import { deletePrivateChat } from '@/api/privateMessage';
import { findUser } from '@/api/user';
import type { UserVO } from '@/api/user/types';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import { useFriendStore } from '@/stores/friend';
import type { FriendVO } from '@/api/friend/types';
import { CONVERSATION_TYPE } from '@/utils/enums';
import eventBus from '@/utils/eventBus';

const route = useRoute();
const router = useRouter();
const friendStore = useFriendStore();
const chatStore = useChatStore();

const searchText = ref('');
const showAddFriend = ref(false);
const activeFriend = ref<FriendVO>({ id: 0, nickName: '' });
const userInfo = ref<UserVO>({ id: 0, nickName: '' });
const rightMenuRef = ref<InstanceType<typeof RightMenu>>();
const cleanMessageConfirmRef = ref<InstanceType<typeof CleanMessageConfirm>>();

const menuItems = computed<RightMenuItem[]>(() => [
  { key: 'CHAT', name: '发消息' },
  { key: 'DELETE', name: '删除好友', danger: true }
]);

const friends = computed(() => friendStore.friends.filter((f) => !f.deleted));
const isFriend = computed(() => friendStore.isFriend(userInfo.value.id));

const firstLetter = (strText: string) => {
  const pyText = pinyin(strText, {
    toneType: 'none',
    type: 'array'
  } as never);
  return pyText[0];
};

const isEnglish = (character: string) => /^[A-Za-z]+$/.test(character);

const friendMap = computed(() => {
  // 按首字母分组
  const map = new Map<string, FriendVO[]>();
  friends.value.forEach((f) => {
    if (searchText.value && !f.nickName.includes(searchText.value)) {
      return;
    }
    let letter = firstLetter(f.nickName).toUpperCase();
    // 非英文一律为#组
    if (!isEnglish(letter)) {
      letter = '#';
    }
    if (f.online) {
      letter = '在线';
    }
    if (map.has(letter)) {
      map.get(letter)!.push(f);
    } else {
      map.set(letter, [f]);
    }
  });
  // 排序：在线优先，其次字母 A-Z，# 组最后
  const onlineKey = '在线';
  const arrayObj = Array.from(map);
  arrayObj.sort((a, b) => {
    if (a[0] === onlineKey) return -1;
    if (b[0] === onlineKey) return 1;
    if (a[0] == '#') return 1;
    if (b[0] == '#') return -1;
    return a[0].localeCompare(b[0]);
  });
  return new Map(arrayObj.map((i) => [i[0], i[1]]));
});

const friendKeys = computed(() => Array.from(friendMap.value.keys()));
const friendValues = computed(() => Array.from(friendMap.value.values()));

const onShowAddFriend = () => {
  showAddFriend.value = true;
};

const onCloseAddFriend = () => {
  showAddFriend.value = false;
};

const updateFriendInfo = async () => {
  if (isFriend.value) {
    // store的数据不能直接修改，深拷贝一份store的数据
    const friend = JSON.parse(JSON.stringify(activeFriend.value)) as FriendVO;
    friend.headImage = userInfo.value.headImageThumb;
    friend.nickName = userInfo.value.nickName;
    await chatStore.updateFromFriend(friend);
    await friendStore.updateFriend(friend);
  }
};

const loadUserInfo = async (id?: number) => {
  if (id == null) {
    return;
  }
  // 获取好友用户信息
  userInfo.value = await findUser(id);
  await updateFriendInfo();
};

const onActiveItem = (friend: FriendVO) => {
  activeFriend.value = friend;
  loadUserInfo(friend.id);
};

const onDelFriend = async (friend: FriendVO) => {
  try {
    const isCleanMessage = await cleanMessageConfirmRef.value!.open({
      title: '确认解除?',
      message: `确认删除'${friend.nickName}'吗?`
    });
    await deleteFriend(friend.id);
    await friendStore.removeFriend(friend.id);
    if (isCleanMessage) {
      await deletePrivateChat({ chatId: friend.id });
      const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, friend.id);
      await chatStore.remove(convKey);
    }
    ElMessage.success('删除好友成功');
  } catch {}
};

const onAddFriend = async () => {
  await addFriend(userInfo.value.id);
  ElMessage.success("添加成功，'{name}'已成为您的好友");
  await friendStore.addFriend({
    id: userInfo.value.id,
    nickName: userInfo.value.nickName,
    headImage: userInfo.value.headImageThumb,
    online: userInfo.value.online,
    deleted: false,
    version: 0
  });
};

const onSendMessage = async (friend: FriendVO) => {
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.PRIVATE, friend.id);
  await chatStore.openChat({
    type: CONVERSATION_TYPE.PRIVATE,
    targetId: friend.id,
    showName: friend.nickName,
    headImage: friend.headImage,
    isDnd: friend.isDnd,
    isTop: friend.isTop
  });
  await chatStore.moveTop(convKey);
  chatStore.setActive(convKey);
  router.push('/home/chat');
};

const onClickMore = (e: MouseEvent) => {
  rightMenuRef.value?.open(e, menuItems.value);
};

const onSelectMenu = (item: RightMenuItem) => {
  switch (item.key) {
    case 'CHAT':
      onSendMessage(activeFriend.value);
      break;
    case 'DELETE':
      onDelFriend(activeFriend.value);
      break;
  }
};

const locateItem = (id?: number) => {
  if (id == null) return;
  document.getElementById(String(id))?.scrollIntoView({ behavior: 'smooth' });
};

const showFullImage = () => {
  if (userInfo.value.headImage) {
    eventBus.emit('openFullImage', userInfo.value.headImage);
  }
};

onActivated(() => {
  const userId = route.query.id;
  if (userId) {
    const friend = friendStore.findFriend(parseInt(String(userId)));
    if (friend) {
      onActiveItem(friend);
      locateItem(friend.id);
    }
  }
});
</script>

<style lang="scss" scoped>
.friend-page {
  height: 100%;
  width: 100%;

  .header {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 12px;

    .search-text {
      flex: 1;
    }

    .add-btn {
      padding: 8px;
      margin: 5px;
      font-size: 16px;
      border-radius: 50%;
      background: var(--im-background-active);
      color: var(--im-color-primary);
      transition: all 0.3s ease;
      font-weight: 600;
      border: var(--im-border);

      &:hover {
        background: var(--im-background-active-dark);
        transform: scale(1.05);
      }
    }
  }

  .friend-items {
    flex: 1;

    .top-item {
      height: 60px;
      display: flex;
      position: relative;
      padding: 5px 10px;
      align-items: center;
      white-space: nowrap;
      cursor: pointer;
      border-radius: 12px;

      &:hover {
        background-color: var(--im-background-active);
      }

      &.active {
        background-color: var(--im-background-active-dark);
      }

      .top-item-avatar {
        display: flex;
        justify-content: center;
        align-items: center;

        .unread-text {
          position: absolute;
          background-color: var(--im-color-danger);
          right: -4px;
          top: -8px;
          color: white;
          border-radius: 30px;
          padding: 1px 5px;
          font-size: 10px;
          text-align: center;
          white-space: nowrap;
          border: 1px solid #f1e5e5;
        }
      }

      .top-item-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding-left: 10px;
        text-align: left;

        .top-item-name {
          font-size: var(--im-font-size);
          white-space: nowrap;
          overflow: hidden;
        }
      }
    }
  }

  .letter {
    text-align: left;
    font-size: 12px;
    font-weight: 600;
    padding: 8px 15px;
    color: var(--im-text-color-light);
    background: var(--im-background-active);
  }

  .container {
    display: flex;
    flex-direction: column;
    background: #fcfdff;

    .header {
      height: 60px;
      line-height: 60px;
      display: flex;
      align-items: center;
      padding: 0 12px;
      box-sizing: border-box;

      .nick-name {
        font-size: var(--im-font-size-larger);
        border-bottom: var(--im-border);
      }
    }

    .friend-info {
      flex: 1;
      display: flex;
      justify-content: center;
      padding: 20px;
      align-items: center;

      .info-card {
        border-radius: 20px;
        padding: 10px;
        position: relative;
        overflow: hidden;
        max-width: 600px;
        width: 100%;

        .avatar-section {
          text-align: center;
          margin-bottom: 20px;
        }

        .info-section {
          .name-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
            padding-bottom: 15px;
            border-bottom: 1px solid rgba(0, 0, 0, 0.06);

            .friend-name {
              font-size: 24px;
              font-weight: 600;
              color: var(--im-text-color-primary, var(--im-text-color));
              margin: 0;
              position: relative;

              &::after {
                content: '';
                position: absolute;
                bottom: -15px;
                left: 0;
                width: 40px;
                height: 2px;
                background: var(--im-color-primary);
                border-radius: 1px;
              }
            }

            .more-btn {
              font-size: 20px;
              cursor: pointer;
              padding: 8px;
              border-radius: 50%;
              transition: all 0.3s ease;
              background: var(--im-background-active);
              color: var(--im-color-primary);
              display: flex;
              align-items: center;
              justify-content: center;

              &:hover {
                background: var(--im-background-active-dark);
                transform: scale(1.1);
              }
            }
          }

          .info-item-list {
            margin-bottom: 30px;

            .info-item {
              display: flex;
              align-items: center;
              padding: 12px 0;
              border-bottom: 1px solid rgba(0, 0, 0, 0.04);

              &:last-child {
                border-bottom: none;
              }

              .info-label {
                font-weight: 500;
                color: var(--im-text-color-secondary, var(--im-text-color-light));
                font-size: 14px;
                min-width: 80px;
                margin-right: 20px;
              }

              .info-value {
                flex: 1;
                color: var(--im-text-color-primary, var(--im-text-color));
                font-size: 15px;
                line-height: 24px;
                display: flex;
                align-items: center;
                text-align: left;
                gap: 8px;

                &.light-text {
                  color: var(--im-text-color-light) !important;
                }
              }
            }
          }

          .btn-group {
            display: flex;
            gap: 12px;
            justify-content: center;
            flex-wrap: wrap;
          }
        }
      }
    }
  }
}
</style>

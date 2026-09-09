<template>
  <el-dialog
    :model-value="dialogVisible"
    class="add-friend"
    :title="'添加好友'"
    width="400px"
    :close-on-click-modal="false"
    draggable
    destroy-on-close
    @close="onClose">
    <el-input
      v-model="searchText"
      :placeholder="'输入用户名或昵称，最多展示20条'"
      class="input-with-select"
      @keyup.enter="onSearch()">
      <template #suffix>
        <el-icon class="el-input__icon" @click="onSearch()"><Search /></el-icon>
      </template>
    </el-input>
    <el-scrollbar style="height: 400px">
      <div v-for="user in users" :key="user.id" v-show="user.id != userStore.userInfo.id">
        <div class="item">
          <div class="avatar">
            <HeadImage :id="user.id" :name="user.nickName" :url="user.headImage" :online="user.online" :is-show-user-info="true" />
          </div>
          <div class="friend-info">
            <div class="nick-name">
              <div>{{ user.nickName }}</div>
              <div :class="user.online ? 'online-status online' : 'online-status'">
                {{ user.online ? '[在线]' : '[离线]' }}
              </div>
            </div>
            <div class="user-name">
              <div>{{ '用户名(ID)' }}:{{ user.userName }}</div>
            </div>
          </div>
          <el-button v-if="isFriend(user.id)" type="primary" size="small" :icon="Position" @click="onSendMessage(user)">
            发消息
          </el-button>
          <el-button v-else type="primary" plain size="small" @click="onAddFriend(user)">加为好友</el-button>
        </div>
      </div>
    </el-scrollbar>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Position, Search } from '@element-plus/icons-vue';
import HeadImage from '@/components/common/HeadImage.vue';
import { addFriend } from '@/api/friend';
import { searchUser } from '@/api/user';
import type { UserVO } from '@/api/user/types';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import { useFriendStore } from '@/stores/friend';
import { useUserStore } from '@/stores/user';
import { CONVERSATION_TYPE } from '@/utils/enums';

defineProps({
  dialogVisible: {
    type: Boolean,
    default: false
  }
});
const emit = defineEmits(['close']);
const router = useRouter();
const userStore = useUserStore();
const friendStore = useFriendStore();
const chatStore = useChatStore();
const users = ref<UserVO[]>([]);
const searchText = ref('');
const onClose = () => {
  emit('close');
};

const onSearch = async () => {
  if (!searchText.value) {
    users.value = [];
    return;
  }
  users.value = await searchUser(searchText.value);
};

const onSendMessage = async (user: UserVO) => {
  const friend = friendStore.findFriend(user.id);
  if (!friend) return;
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
  if (router.currentRoute.value.path != '/home/chat') {
    await router.push('/home/chat');
  }
  onClose();
};

const onAddFriend = async (user: UserVO) => {
  await addFriend(user.id);
  ElMessage.success(`添加成功，'${user.nickName}'已成为您的好友`);
  await friendStore.addFriend({
    id: user.id,
    nickName: user.nickName,
    headImage: user.headImageThumb,
    online: user.online,
    deleted: false,
    version: 0
  });
};

const isFriend = (userId?: number) => friendStore.isFriend(userId);
</script>

<style lang="scss">
.add-friend {
  .item {
    height: 65px;
    display: flex;
    position: relative;
    padding-left: 15px;
    align-items: center;
    padding-right: 25px;

    .friend-info {
      margin-left: 15px;
      flex: 3;
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      overflow: hidden;

      .nick-name {
        display: flex;
        flex-direction: row;
        font-weight: 600;
        font-size: 16px;
        line-height: 25px;

        .online-status {
          font-size: 12px;
          font-weight: 600;

          &.online {
            color: #5fb878;
          }
        }
      }

      .user-name {
        display: flex;
        flex-direction: row;
        font-size: 12px;
        line-height: 20px;
      }
    }
  }
}
</style>

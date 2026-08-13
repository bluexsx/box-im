<template>
  <el-dialog v-model="show" :title="dialogTitle" width="620px" draggable destroy-on-close :before-close="close">
    <div class="group-member-invite">
      <div class="left-box">
        <el-input v-model="searchText" :placeholder="'搜索好友'">
          <template #suffix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-scrollbar style="height: 400px">
          <div v-for="friend in friends" :key="friend.id">
            <FriendItem v-show="friend.nickName.includes(searchText)" :menu="false" :friend="friend" size="small" @click="onSwitchCheck(friend)">
              <el-checkbox v-model="friend.isCheck" :disabled="friend.disabled" class="checkbox" @click.stop />
            </FriendItem>
          </div>
        </el-scrollbar>
      </div>
      <div class="arrow">
        <el-icon><DArrowRight /></el-icon>
      </div>
      <div class="right-box">
        <div class="tip">{{ `已勾选${checkCount}位好友` }}</div>
        <el-scrollbar style="height: 400px">
          <div v-for="friend in friends" :key="friend.id">
            <FriendItem v-if="friend.isCheck && !friend.disabled" :friend="friend" size="small" :menu="false" @del="onRemoveFriend(friend)" />
          </div>
        </el-scrollbar>
      </div>
    </div>
    <template #footer>
      <el-button @click="close()">{{ '取消' }}</el-button>
      <el-button type="primary" :disabled="checkCount === 0 || loading" :loading="loading" @click="onOk()">
        {{ '确定' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { DArrowRight, Search } from '@element-plus/icons-vue';
import FriendItem from '@/components/friend/FriendItem.vue';
import { inviteGroup, newGroup } from '@/api/group';
import type { GroupVO } from '@/api/group/types';
import { useFriendStore } from '@/stores/friend';
import type { FriendVO } from '@/api/friend/types';
import type { GroupMemberVO } from '@/api/group/types';

interface InviteFriend extends FriendVO {
  disabled?: boolean;
  isCheck?: boolean;
}

const props = defineProps({
  groupId: {
    type: Number
  },
  members: {
    type: Array as () => GroupMemberVO[],
    default: () => []
  }
});

const emit = defineEmits(['reload', 'success']);

const friendStore = useFriendStore();
const show = ref(false);
const loading = ref(false);
const mode = ref<'invite' | 'create'>('invite');
const searchText = ref('');
const friends = ref<InviteFriend[]>([]);
const maxSelectSize = 50;

const isCreate = computed(() => mode.value === 'create');
const dialogTitle = computed(() => (isCreate.value ? '发起群聊' : '邀请好友进群'));
const checkCount = computed(() => friends.value.filter((f) => f.isCheck && !f.disabled).length);

const openCreate = () => {
  mode.value = 'create';
  initDialog();
};

const open = () => {
  mode.value = 'invite';
  initDialog();
};

const initDialog = () => {
  show.value = true;
  loading.value = false;
  searchText.value = '';
  friends.value = [];
  friendStore.friends.forEach((f) => {
    if (f.deleted) return;
    const friend: InviteFriend = JSON.parse(JSON.stringify(f));
    if (isCreate.value) {
      friend.disabled = false;
      friend.isCheck = false;
    } else {
      const m = props.members.filter((m) => !m.quit).find((m) => m.userId == f.id);
      if (m) {
        friend.disabled = true;
        friend.isCheck = true;
      } else {
        friend.disabled = false;
        friend.isCheck = false;
      }
    }
    friends.value.push(friend);
  });
};

const close = () => {
  show.value = false;
};

const onOk = () => {
  if (isCreate.value) {
    onCreateGroup();
  } else {
    onInviteFriends();
  }
};

const onCreateGroup = () => {
  const userIds = friends.value.filter((f) => f.isCheck).map((f) => f.id);
  if (userIds.length === 0) {
    ElMessage.warning('请至少选择1位好友');
    return;
  }
  loading.value = true;
  newGroup({ userIds })
    .then((group: GroupVO) => {
      emit('success', group);
      close();
    })
    .finally(() => {
      loading.value = false;
    });
};

const onInviteFriends = () => {
  const friendIds: number[] = [];
  friends.value.forEach((f) => {
    if (f.isCheck && !f.disabled && f.id != null) {
      friendIds.push(f.id);
    }
  });
  if (friendIds.length > 0 && props.groupId != null) {
    loading.value = true;
    inviteGroup({ groupId: props.groupId, friendIds })
      .then(() => {
        ElMessage.success('邀请成功');
        emit('reload');
        close();
      })
      .finally(() => {
        loading.value = false;
      });
  }
};

const onRemoveFriend = (friend: InviteFriend) => {
  friend.isCheck = false;
};

const onSwitchCheck = (friend: InviteFriend) => {
  if (friend.disabled) return;
  if (!friend.isCheck && checkCount.value >= maxSelectSize) {
    ElMessage.warning(`最多只能选择${maxSelectSize}位好友`);
    return;
  }
  friend.isCheck = !friend.isCheck;
};

defineExpose({ open, openCreate, close });
</script>

<style lang="scss" scoped>
.group-member-invite {
  display: flex;

  .left-box {
    flex: 1;
    overflow: hidden;
    border: var(--im-border);

    .checkbox {
      margin-right: 10px;
    }
  }

  .arrow {
    display: flex;
    align-items: center;
    font-size: 18px;
    padding: 10px;
    color: var(--im-color-primary);
  }

  .right-box {
    flex: 1;
    border: var(--im-border);

    .tip {
      text-align: left;
      height: 32px;
      line-height: 32px;
      text-indent: 10px;
      color: var(--im-text-color-light);
    }
  }
}
</style>

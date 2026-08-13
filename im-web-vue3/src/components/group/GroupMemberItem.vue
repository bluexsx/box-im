<template>
  <div>
    <div ref="itemRef" @contextmenu.prevent="showRightMenu($event)">
      <GroupMemberBar v-if="type == 'bar'" :group="group" :member="member" :height="height" :active="active">
        <slot />
      </GroupMemberBar>
      <GroupMemberCard v-else :member="member" />
    </div>
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { storeToRefs } from 'pinia';
import RightMenu, { type RightMenuItem } from '@/components/common/RightMenu.vue';
import GroupMemberBar from './GroupMemberBar.vue';
import GroupMemberCard from './GroupMemberCard.vue';
import { removeGroupMembers } from '@/api/group';
import { addFriend } from '@/api/friend';
import { findUser } from '@/api/user';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import { useFriendStore } from '@/stores/friend';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';
import { useUserStore } from '@/stores/user';
import { CONVERSATION_TYPE } from '@/utils/enums';
import eventBus from '@/utils/eventBus';

const props = defineProps({
  group: {
    type: Object as () => GroupVO,
    required: true
  },
  groupMembers: {
    type: Array as () => GroupMemberVO[],
    default: () => []
  },
  member: {
    type: Object as () => GroupMemberVO,
    required: true
  },
  height: {
    type: Number,
    default: 50
  },
  menu: {
    type: Boolean,
    default: true
  },
  active: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'bar'
  }
});

const router = useRouter();
const userStore = useUserStore();
const friendStore = useFriendStore();
const chatStore = useChatStore();
const { userInfo: mine } = storeToRefs(userStore);
const itemRef = ref<HTMLElement>();
const rightMenuRef = ref<InstanceType<typeof RightMenu>>();

const isOwner = computed(() => mine.value.id == props.group.ownerId);
const isFriend = computed(() => friendStore.isFriend(props.member.userId));

const showRightMenu = (e: MouseEvent) => {
  if (!props.menu || mine.value.id == props.member.userId) {
    return;
  }
  const menuItems: RightMenuItem[] = [];
  if (isFriend.value) {
    menuItems.push({ key: 'SEND_MESSAGE', name: '发送消息' });
  }
  menuItems.push({ key: 'USER_INFO', name: '查看资料' });
  if (!isFriend.value) {
    menuItems.push({ key: 'APPLY_FRIEND', name: '加为好友' });
  }
  // 群主可踢人
  if (isOwner.value && props.member.userId != props.group.ownerId) {
    menuItems.push({ key: 'KICK', name: '移出本群' });
  }
  rightMenuRef.value?.open(e, menuItems);
};

const onSelectMenu = (item: RightMenuItem) => {
  switch (item.key) {
    case 'USER_INFO':
      showUserInfo();
      break;
    case 'SEND_MESSAGE':
      sendMessage();
      break;
    case 'APPLY_FRIEND':
      applyFriend();
      break;
    case 'KICK':
      kick();
      break;
  }
};

const sendMessage = async () => {
  const friend = friendStore.findFriend(props.member.userId);
  if (!friend?.id) return;
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
};

const applyFriend = async () => {
  await addFriend(props.member.userId);
  ElMessage.success(`添加成功，'${props.member.showNickName}'已成为您的好友`);
};

const kick = () => {
  const member = props.member;
  ElMessageBox.confirm(`确定将成员'${member.showNickName}'移出群聊吗？`, '确认移出?', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    removeGroupMembers({
      groupId: props.group.id,
      userIds: [member.userId]
    }).then(() => {
      ElMessage.success(`您将'${member.showNickName}'移出了群聊`);
      member.quit = true;
    });
  });
};

const showUserInfo = () => {
  findUser(props.member.userId).then((user) => {
    const rect = itemRef.value?.getBoundingClientRect();
    const pos = {
      x: (rect?.left || 0) + 50,
      y: rect?.top || 0
    };
    eventBus.emit('openUserInfo', { user, pos });
  });
};
</script>

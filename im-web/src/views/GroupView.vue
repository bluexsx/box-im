<template>
  <el-container class="group-page">
    <ResizableAside :default-width="260" :min-width="200" :max-width="500" storage-key="group-aside-width">
      <div class="header" >
        <el-input v-model="searchText" class="search-text" :placeholder="'搜索'">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button plain class="add-btn" :icon="Plus" :title="'创建群聊'" @click="onCreateGroup()" />
      </div>
      <VirtualScroller v-if="useVirtual" class="group-items" :items="groupItems">
        <template #default="{ item }">
          <GroupItem
            :id="String(item.id)"
            :group="item"
            :active="item.id == activeGroup.id"
            @chat="onSendMessage(item)"
            @quit="onQuit(item)"
            @dissolve="onDissolve(item)"
            @click="onActiveItem(item)" />
        </template>
      </VirtualScroller>
      <el-scrollbar v-else class="group-items">
        <div v-for="(groupList, i) in groupValues" :key="i">
          <div class="letter">{{ groupKeys[i] }}</div>
          <div v-for="group in groupList" :key="group.id">
            <GroupItem
              :id="String(group.id)"
              :group="group"
              :active="group.id == activeGroup.id"
              @chat="onSendMessage(group)"
              @quit="onQuit(group)"
              @dissolve="onDissolve(group)"
              @click="onActiveItem(group)" />
          </div>
        </div>
      </el-scrollbar>
    </ResizableAside>
    <el-container v-show="activeGroup.id" class="container">
      <div class="header" >{{ activeGroup.showGroupName }}</div>
      <div class="container-box">
        <div class="unified-card">
          <!-- 头部区域 -->
          <div class="card-header">
            <div class="avatar-section">
              <FileUpload
                v-if="isOwner"
                class="avatar-uploader"
                :action="imageAction"
                :is-permanent="true"
                :show-loading="true"
                :max-size="5 * 1024 * 1024"
                :file-types="['image/jpeg', 'image/png', 'image/jpg', 'image/webp']"
                @success="onUploadSuccess">
                <HeadImage class="avatar" :size="80" :url="activeGroup.headImageThumb" radius="50%" :name="activeGroup.showGroupName" />
                <div class="upload-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>{{ '更换头像' }}</span>
                </div>
              </FileUpload>
              <HeadImage v-else class="avatar" :size="80" :url="activeGroup.headImageThumb" radius="50%" :name="activeGroup.showGroupName" />
              <div class="group-info">
                <div class="group-header">
                  <div class="group-name">
                    {{ activeGroup.name }}
                    <el-tag v-if="activeGroup.isBanned" type="danger">{{ '已封禁' }}</el-tag>
                  </div>
                  <div :title="'更多'" class="more-btn" @click="onClickMore">
                    <el-icon><MoreFilled /></el-icon>
                  </div>
                </div>
                <!-- 成员头像区域 -->
                <div class="members-preview">
                  <div class="members-avatars">
                    <div v-for="member in previewMembers" :key="member.userId" class="member-avatar">
                      <HeadImage :size="36" :url="member.headImage" radius="50%" :name="member.showNickName" :id="member.userId" />
                    </div>
                    <div v-if="showMembers.length > 6" class="more-members-btn" @click="openMemberDialog">
                      <span>+{{ showMembers.length - 6 }}</span>
                    </div>
                    <div class="view-all-btn" @click="openMemberDialog">
                      <span>{{ '查看全部成员' }}</span>
                      <el-icon><ArrowRight /></el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- 内容区域 -->
          <div class="card-content">
            <!-- 群信息 -->
            <div class="info-content">
              <el-form ref="groupFormRef" class="form" label-width="130px" :model="activeGroup">
                <el-form-item :label="'群主'">
                  <div class="value">{{ ownerName }}</div>
                </el-form-item>
                <el-form-item :label="'群名'">
                  <el-input
                    v-model="activeGroup.name"
                    :disabled="!isOwner"
                    :placeholder="activeGroup.name"
                    maxlength="32"
                    show-word-limit />
                </el-form-item>
                <el-form-item :label="'备注名'">
                  <el-input v-model="activeGroup.remarkGroupName" :placeholder="activeGroup.name" maxlength="32" show-word-limit />
                </el-form-item>
                <el-form-item :label="'我在本群的昵称'">
                  <el-input v-model="activeGroup.remarkNickName" maxlength="20" :placeholder="mine.nickName" show-word-limit />
                </el-form-item>
                <el-form-item :label="'群公告'">
                  <el-input
                    v-model="activeGroup.notice"
                    :disabled="!isOwner"
                    type="textarea"
                    :autosize="{ minRows: 3 }"
                    maxlength="512"
                    :placeholder="'群主未设置'"
                    show-word-limit />
                </el-form-item>
                <div class="btn-actions">
                  <el-button type="primary" :icon="Position" @click="onSendMessage(activeGroup)">{{ '发消息' }}</el-button>
                  <el-button type="success" @click="onSaveGroup()">{{ '保存' }}</el-button>
                  <el-button v-show="!isOwner" type="danger" @click="onQuit(activeGroup)">{{ '退出群聊' }}</el-button>
                  <el-button v-show="isOwner" type="danger" @click="onDissolve(activeGroup)">{{ '解散群聊' }}</el-button>
                </div>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </el-container>
    <RightMenu ref="rightMenuRef" @select="onSelectMenu" />
    <CleanMessageConfirm ref="cleanMessageConfirmRef" />
    <GroupMemberInvite
      ref="groupMemberInviteRef"
      :group-id="activeGroup.id"
      :members="groupMembers"
      @reload="reloadMembers()"
      @success="onCreateGroupSuccess" />
    <GroupMemberSelector ref="removeSelectorRef" :title="'选择成员进行移除'" :group="activeGroup" @complete="onRemoveComplete" />
    <!-- 成员列表弹窗 -->
    <GroupMemberDialog
      ref="groupMemberDialogRef"
      :group="activeGroup"
      :group-members="groupMembers"
      @invite-member="onInviteMember"
      @remove-member="onRemoveMember" />
  </el-container>
</template>

<script setup lang="ts">
import { computed, nextTick, onActivated, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance } from 'element-plus';
import { ArrowRight, Camera, MoreFilled, Plus, Position, Search } from '@element-plus/icons-vue';
import { pinyin } from 'pinyin-pro';
import { storeToRefs } from 'pinia';
import CleanMessageConfirm from '@/components/common/CleanMessageConfirm.vue';
import FileUpload from '@/components/common/FileUpload.vue';
import HeadImage from '@/components/common/HeadImage.vue';
import ResizableAside from '@/components/common/ResizableAside.vue';
import RightMenu, { type RightMenuItem } from '@/components/common/RightMenu.vue';
import VirtualScroller from '@/components/common/VirtualScroller.vue';
import GroupItem from '@/components/group/GroupItem.vue';
import GroupMemberDialog from '@/components/group/GroupMemberDialog.vue';
import GroupMemberInvite from '@/components/group/GroupMemberInvite.vue';
import GroupMemberSelector from '@/components/group/GroupMemberSelector.vue';
import type { UploadImageVO } from '@/api/file/types';
import { deleteGroup, modifyGroup, quitGroup, removeGroupMembers } from '@/api/group';
import type { GroupMemberVO, GroupVO } from '@/api/group/types';
import { deleteGroupChat } from '@/api/groupMessage';
import { getDB } from '@/db';
import { useChatStore } from '@/stores/chat';
import { useConfigStore } from '@/stores/config';
import { useGroupStore } from '@/stores/group';
import { useUserStore } from '@/stores/user';
import { CONVERSATION_TYPE } from '@/utils/enums';

const route = useRoute();
const router = useRouter();
const groupStore = useGroupStore();
const chatStore = useChatStore();
const userStore = useUserStore();
const configStore = useConfigStore();
const { userInfo: mine } = storeToRefs(userStore);

const searchText = ref('');
const activeGroup = ref<GroupVO>({ id: 0, name: '', showGroupName: '' });
const groupFormRef = ref<FormInstance>();
const rightMenuRef = ref<InstanceType<typeof RightMenu>>();
const cleanMessageConfirmRef = ref<InstanceType<typeof CleanMessageConfirm>>();
const groupMemberInviteRef = ref<InstanceType<typeof GroupMemberInvite>>();
const removeSelectorRef = ref<InstanceType<typeof GroupMemberSelector>>();
const groupMemberDialogRef = ref<InstanceType<typeof GroupMemberDialog>>();

const groups = computed(() => groupStore.groups.filter((g) => !g.quit));
const useVirtual = computed(() => groups.value.length > 1000);

const groupMembers = computed(() => {
  const group = groupStore.findGroup(activeGroup.value.id);
  return group ? group.members || [] : [];
});

const showMembers = computed(() => groupMembers.value.filter((m) => !m.quit));
const previewMembers = computed(() => showMembers.value.slice(0, 6));

const ownerName = computed(() => {
  const member = groupMembers.value.find((m) => m.userId == activeGroup.value.ownerId);
  return member && member.showNickName;
});

const isOwner = computed(() => activeGroup.value.ownerId == mine.value.id);

const imageAction = '/image/upload?thumbSize=20';

const firstLetter = (strText: string) => {
  // 使用pinyin-pro库将中文转换为拼音
  const pyText = pinyin(strText, { toneType: 'none' }); // 无声调
  return pyText[0];
};

const isEnglish = (character: string) => /^[A-Za-z]+$/.test(character);

const groupMap = computed(() => {
  // 按首字母分组
  const map = new Map<string, GroupVO[]>();
  groups.value.forEach((g) => {
    if (searchText.value && !g.showGroupName.includes(searchText.value)) {
      return;
    }
    let letter = firstLetter(g.showGroupName).toUpperCase();
    // 非英文一律为#组
    if (!isEnglish(letter)) {
      letter = '#';
    }
    if (map.has(letter)) {
      map.get(letter)!.push(g);
    } else {
      map.set(letter, [g]);
    }
  });
  // 排序
  const arrayObj = Array.from(map);
  arrayObj.sort((a, b) => {
    // #组在最后面
    if (a[0] == '#' || b[0] == '#') {
      return b[0].localeCompare(a[0]);
    }
    return a[0].localeCompare(b[0]);
  });
  return new Map(arrayObj.map((i) => [i[0], i[1]]));
});

const groupKeys = computed(() => Array.from(groupMap.value.keys()));
const groupValues = computed(() => Array.from(groupMap.value.values()));
const groupItems = computed(() => groupValues.value.flat());

const menuItems = computed<RightMenuItem[]>(() => {
  const items: RightMenuItem[] = [{ key: 'CHAT', name: '发送消息' }];
  if (isOwner.value) {
    items.push({ key: 'DISSOLVE', name: '解散群聊', danger: true });
  } else {
    items.push({ key: 'QUIT', name: '退出群聊', danger: true });
  }
  return items;
});

const onClickMore = (e: MouseEvent) => {
  rightMenuRef.value?.open(e, menuItems.value);
};

const onSelectMenu = (item: RightMenuItem) => {
  switch (item.key) {
    case 'CHAT':
      onSendMessage(activeGroup.value);
      break;
    case 'DISSOLVE':
      onDissolve(activeGroup.value);
      break;
    case 'QUIT':
      onQuit(activeGroup.value);
      break;
  }
};

const onCreateGroup = () => {
  groupMemberInviteRef.value?.openCreate();
};

const onCreateGroupSuccess = async (group: GroupVO) => {
  await groupStore.addGroup(group);
  await groupStore.refreshMember(group.id);
  await onSendMessage(group);
};

const onActiveItem = (group: GroupVO) => {
  // store数据不能直接修改，所以深拷贝一份内存
  activeGroup.value = JSON.parse(JSON.stringify(group));
  // 加载群成员
  reloadMembers();
};

const onInviteMember = () => {
  groupMemberInviteRef.value?.open();
};

const onRemoveMember = () => {
  // 群主不显示
  const hideIds = [activeGroup.value.ownerId!, mine.value.id];
  removeSelectorRef.value?.open(50, [], [], hideIds);
};

const onRemoveComplete = (members: GroupMemberVO[]) => {
  const userIds = members.map((m) => m.userId);
  removeGroupMembers({
    groupId: activeGroup.value.id,
    userIds
  }).then(() => {
    reloadMembers();
    ElMessage.success(`您移除了${userIds.length}位成员`);
  });
};

const onUploadSuccess = (data: UploadImageVO) => {
  activeGroup.value.headImage = data.originUrl;
  activeGroup.value.headImageThumb = data.thumbUrl;
};

const onSaveGroup = () => {
  groupFormRef.value?.validate((valid) => {
    if (valid) {
      modifyGroup(activeGroup.value).then((group) => {
        groupStore.updateGroup(group);
        ElMessage.success('修改成功');
      });
    }
  });
};

const onDissolve = async (group: GroupVO) => {
  try {
    const isCleanMessage = await cleanMessageConfirmRef.value!.open({
      title: '确认解散?',
      message: `确认要解散'${group.name}'吗?`
    });
    await deleteGroup(group.id);
    await groupStore.removeGroup(group.id);
    if (isCleanMessage) {
      await deleteGroupChat({ chatId: group.id });
      const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, group.id);
      await chatStore.remove(convKey);
    }
    ElMessage.success(`群聊'${group.name}'已解散`);
    reset();
  } catch {}
};

const onQuit = async (group: GroupVO) => {
  try {
    const isCleanMessage = await cleanMessageConfirmRef.value!.open({
      title: '确认退出?',
      message: `确认退出'${group.showGroupName}'吗？`
    });
    await quitGroup(group.id);
    await groupStore.removeGroup(group.id);
    if (isCleanMessage) {
      await deleteGroupChat({ chatId: group.id });
      const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, group.id);
      await chatStore.remove(convKey);
    }
    ElMessage.success(`您已退出'${group.name}'`);
    reset();
  } catch {}
};

const onSendMessage = async (group: GroupVO | GroupVO) => {
  const convKey = getDB().buildConversationKey(CONVERSATION_TYPE.GROUP, group.id);
  await chatStore.openChat({
    type: CONVERSATION_TYPE.GROUP,
    targetId: group.id,
    showName: group.showGroupName,
    headImage: group.headImageThumb,
    isDnd: group.isDnd,
    isTop: group.isTop
  });
  await chatStore.moveTop(convKey);
  chatStore.setActive(convKey);
  await router.push('/home/chat');
};

const locateItem = (id: number) => {
  document.getElementById(String(id))?.scrollIntoView({ behavior: 'smooth' });
};

const reloadMembers = () => {
  if (activeGroup.value.id != null) {
    groupStore.refreshMember(activeGroup.value.id);
  }
};

const reset = () => {
  activeGroup.value = { id: 0, name: '', showGroupName: '' };
};

const openMemberDialog = () => {
  groupMemberDialogRef.value?.open();
};

onActivated(() => {
  // 选中群聊
  const groupId = route.query.id;
  if (groupId) {
    const group = groupStore.findGroup(parseInt(String(groupId)));
    if (group) {
      onActiveItem(group);
      locateItem(group.id);
    }
  }
});
</script>

<style lang="scss" scoped>
.group-page {
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

  .group-items {
    flex: 1;

    .letter {
      text-align: left;
      font-size: 12px;
      font-weight: 600;
      padding: 8px 15px;
      color: var(--im-text-color-light);
      background: var(--im-background-active);
    }
  }

  .container {
    display: flex;
    flex-direction: column;
    background: #fcfdff;

    .header {
      display: flex;
      justify-content: space-between;
      padding: 0 12px;
      line-height: 60px;
      font-size: var(--im-font-size-larger);
      border-bottom: var(--im-border);
    }

    .container-box {
      overflow: auto;
      padding: 20px;
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;

      .unified-card {
        display: flex;
        flex-direction: column;
        width: 100%;
        border-radius: 20px;
        overflow: hidden;

        .card-header {
          padding: 20px 30px;
          border-bottom: 1px solid rgba(0, 0, 0, 0.06);

          .avatar-section {
            display: flex;
            align-items: center;
            padding: 0 20px;

            .avatar {
              position: relative;
            }

            .avatar-uploader {
              position: relative;
              display: inline-block;

              :deep(.el-upload) {
                border: none !important;
                border-radius: 50%;
                cursor: pointer;
                position: relative;
                overflow: hidden;
              }

              .upload-overlay {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.6);
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                color: white;
                font-size: 12px;
                border-radius: 50%;
                opacity: 0;
                transition: all 0.3s ease;

                i,
                .el-icon {
                  font-size: 18px;
                  margin-bottom: 4px;
                }

                span {
                  font-size: 10px;
                  font-weight: 500;
                }
              }

              &:hover .upload-overlay {
                opacity: 1;
              }
            }

            .group-info {
              flex: 1;
              display: flex;
              flex-direction: column;
              margin: 0 20px;

              .group-header {
                padding: 10px;
                display: flex;
                align-items: center;
                justify-content: space-between;

                .group-name {
                  font-size: 24px;
                  font-weight: 700;
                  color: var(--im-text-color-primary);
                  letter-spacing: -0.5px;
                  display: flex;
                  align-items: center;
                  gap: 12px;
                  margin: 0;
                }

                .more-btn {
                  font-size: 20px;
                  cursor: pointer;
                  padding: 8px;
                  border-radius: 50%;
                  transition: all 0.3s ease;
                  background: var(--im-background-active);
                  color: var(--im-color-primary);
                  margin: 0;
                  display: flex;
                  align-items: center;
                  justify-content: center;

                  &:hover {
                    background: var(--im-background-active-dark);
                    transform: scale(1.1);
                  }
                }
              }

              .members-preview {
                background: rgba(255, 255, 255, 0.6);
                border-radius: 12px;
                padding: 12px 16px;
                border: 1px solid rgba(0, 0, 0, 0.06);
                box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

                .members-avatars {
                  display: flex;
                  align-items: center;
                  gap: 8px;
                  flex-wrap: wrap;

                  .member-avatar {
                    position: relative;
                    transition: all 0.3s ease;

                    &:hover {
                      transform: scale(1.1);
                    }
                  }

                  .more-members-btn {
                    width: 36px;
                    height: 36px;
                    border-radius: 50%;
                    background: var(--im-background-active);
                    color: var(--im-color-primary);
                    border: 1px solid rgba(var(--im-color-primary-rgb), 0.3);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 12px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.3s ease;

                    &:hover {
                      background: var(--im-background-active-dark);
                      transform: scale(1.05);
                    }
                  }

                  .view-all-btn {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    padding: 6px 12px;
                    background: var(--im-color-primary-light-2);
                    color: white;
                    border-radius: 16px;
                    font-size: var(--im-font-size-smaller);
                    font-weight: 500;
                    cursor: pointer;
                    margin-left: 8px;

                    .el-icon {
                      font-size: 10px;
                    }
                  }
                }
              }
            }
          }
        }

        .card-content {
          padding: 12px;
          border-radius: 12px;

          .info-content {
            .form {
              padding: 10px;

              .el-form-item {
                text-align: left;

                .value {
                  color: var(--im-text-color-light);
                }
              }

              .btn-actions {
                margin-top: 40px;
                text-align: center;

                .el-button {
                  margin: 0 8px;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>

<template>
  <el-dialog v-model="show" :title="displayTitle" width="700px" draggable destroy-on-close>
    <div class="group-member-selector">
      <div class="left-box">
        <el-input v-model="searchText" :placeholder="'搜索'">
          <template #suffix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <VirtualScroller class="scroll-box" :items="showMembers">
          <template #default="{ item }">
            <GroupMemberBar :group="group" :member="item" @click="onClickMember(item)">
              <el-checkbox v-model="item.checked" :disabled="item.locked" @change="onChange(item)" @click.stop />
            </GroupMemberBar>
          </template>
        </VirtualScroller>
      </div>
      <div class="arrow">
        <el-icon><DArrowRight /></el-icon>
      </div>
      <div class="right-box">
        <div class="tip">{{ `已勾选${checkedMembers.length}位成员` }}</div>
        <el-scrollbar class="scroll-box">
          <div class="member-items">
            <div v-for="m in members" :key="m.userId">
              <GroupMemberCard v-if="m.checked" class="member-item" :member="m" />
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>
    <template #footer>
      <el-button @click="close()">{{ '取消' }}</el-button>
      <el-button type="primary" @click="ok()">{{ '确定' }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { DArrowRight, Search } from '@element-plus/icons-vue';
import VirtualScroller from '@/components/common/VirtualScroller.vue';
import GroupMemberBar from './GroupMemberBar.vue';
import GroupMemberCard from './GroupMemberCard.vue';
import { listGroupMembers } from '@/api/group';
import type { GroupVO, GroupMemberVO } from '@/api/group/types';

interface SelectableMember extends GroupMemberVO {
  checked?: boolean;
  locked?: boolean;
  hide?: boolean;
}

const props = defineProps({
  group: {
    type: Object as () => GroupVO,
    required: true
  },
  title: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['complete']);

const show = ref(false);
const searchText = ref('');
const maxSize = ref(-1);
const members = ref<SelectableMember[]>([]);

const displayTitle = computed(() => props.title || '选择成员');
const checkedMembers = computed(() => members.value.filter((m) => m.checked));
const showMembers = computed(() => members.value.filter((m) => !m.hide && !m.quit && m.showNickName.includes(searchText.value)));

const open = (size: number, checkedIds: number[], lockedIds: number[], hideIds: number[]) => {
  maxSize.value = size;
  show.value = true;
  loadGroupMembers(checkedIds, lockedIds, hideIds);
};

const loadGroupMembers = (checkedIds: number[], lockedIds: number[], hideIds: number[]) => {
  listGroupMembers(props.group.id).then((list) => {
    list.forEach((m) => {
      const member = m as SelectableMember;
      // 默认选择和锁定的用户
      member.checked = checkedIds.indexOf(m.userId) >= 0;
      member.locked = lockedIds.indexOf(m.userId) >= 0;
      member.hide = hideIds.indexOf(m.userId) >= 0;
    });
    members.value = list as SelectableMember[];
  });
};

const onClickMember = (m: SelectableMember) => {
  if (!m.locked) {
    m.checked = !m.checked;
  }
  if (maxSize.value > 0 && checkedMembers.value.length > maxSize.value) {
    ElMessage.error(`最多选择${maxSize.value}位成员`);
    m.checked = false;
  }
};

const onChange = (m: SelectableMember) => {
  if (maxSize.value > 0 && checkedMembers.value.length > maxSize.value) {
    ElMessage.error(`最多选择${maxSize.value}位成员`);
    m.checked = false;
  }
};

const ok = () => {
  emit('complete', checkedMembers.value);
  show.value = false;
};

const close = () => {
  show.value = false;
};

defineExpose({ open, close });
</script>

<style lang="scss" scoped>
.group-member-selector {
  display: flex;

  .scroll-box {
    height: 400px;
  }

  .left-box {
    width: 48%;
    overflow: hidden;
    border: var(--im-border);
  }

  .arrow {
    display: flex;
    align-items: center;
    font-size: 20px;
    padding: 10px;
    color: var(--im-color-primary);
  }

  .right-box {
    width: 48%;
    border: var(--im-border);

    .tip {
      text-align: left;
      height: 32px;
      line-height: 32px;
      text-indent: 10px;
      color: var(--im-text-color-light);
    }

    .member-items {
      padding: 10px;
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;

      .member-item {
        padding: 2px;
      }
    }
  }
}
</style>

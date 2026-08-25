<template>
  <el-dialog
    class="clean-message-confirm"
    :title="title"
    v-model="show"
    width="380px"
    append-to-body
    :close-on-click-modal="false"
    @close="onCancel">
    <p>{{ message }}</p>
    <el-checkbox v-model="isCleanMessage">{{ '同时清空聊天记录' }}</el-checkbox>
    <template #footer>
      <el-button @click="onCancel">{{ '取消' }}</el-button>
      <el-button type="primary" @click="onOk">{{ '确定' }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const show = ref(false);
const title = ref('');
const message = ref('');
const isCleanMessage = ref(true);
let resolveFn: ((v: boolean) => void) | null = null;
let rejectFn: ((r: string) => void) | null = null;

const open = (opts: { title: string; message: string }) => {
  title.value = opts.title;
  message.value = opts.message;
  isCleanMessage.value = true;
  show.value = true;
  return new Promise<boolean>((resolve, reject) => {
    resolveFn = resolve;
    rejectFn = reject;
  });
};

const onOk = () => {
  show.value = false;
  resolveFn?.(isCleanMessage.value);
  resolveFn = null;
  rejectFn = null;
};

const onCancel = () => {
  if (!show.value && !rejectFn) return;
  show.value = false;
  rejectFn?.('cancel');
  resolveFn = null;
  rejectFn = null;
};

defineExpose({ open });
</script>

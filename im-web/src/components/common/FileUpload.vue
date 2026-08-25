<template>
  <el-upload
    action="#"
    :http-request="onFileUpload"
    :accept="fileTypes == null ? '' : fileTypes.join(',')"
    :show-file-list="false"
    :disabled="disabled"
    :before-upload="beforeUpload"
    :multiple="true">
    <slot />
  </el-upload>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElLoading, ElMessage } from 'element-plus';
import type { UploadRequestOptions } from 'element-plus';
import request from '@/utils/request';

const props = defineProps({
  action: {
    type: String,
    default: '/file/upload'
  },
  fileTypes: {
    type: Array as () => string[]
  },
  maxSize: {
    type: Number
  },
  showLoading: {
    type: Boolean,
    default: false
  },
  isPermanent: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['before', 'success', 'fail']);
const loading = ref<ReturnType<typeof ElLoading.service>>();

const fileSizeStr = computed(() => {
  if (!props.maxSize) return '';
  if (props.maxSize > 1024 * 1024) {
    return Math.round(props.maxSize / 1024 / 1024) + 'M';
  }
  if (props.maxSize > 1024) {
    return Math.round(props.maxSize / 1024) + 'KB';
  }
  return props.maxSize + 'B';
});

const onFileUpload = (options: UploadRequestOptions) => {
  // 展示加载条
  if (props.showLoading) {
    loading.value = ElLoading.service({
      lock: true,
      text: '正在上传...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
  }
  const formData = new FormData();
  formData.append('file', options.file);
  let url = props.action;
  url += props.action.includes('?') ? '&' : '?';
  url += 'isPermanent=' + props.isPermanent;
  return request({
    url,
    data: formData,
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' }
  })
    .then((data) => {
      emit('success', data, options.file);
    })
    .catch((e) => {
      emit('fail', e, options.file);
    })
    .finally(() => {
      loading.value?.close();
      loading.value = undefined;
    });
};

const beforeUpload = (file: File) => {
  // 校验文件类型
  if (props.fileTypes && props.fileTypes.length > 0) {
    const tMatch = props.fileTypes.find((ft) => ft.toLowerCase() === file.type.toLowerCase());
    if (tMatch === undefined) {
      ElMessage.error(`文件格式错误，请上传以下格式的文件：${props.fileTypes.join(', ')}`);
      return false;
    }
  }
  // 校验大小
  if (props.maxSize && file.size > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${fileSizeStr.value}!`);
    return false;
  }
  emit('before', file);
  return true;
};
</script>

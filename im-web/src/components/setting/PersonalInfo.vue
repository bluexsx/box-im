<template>
  <div class="personal-info">
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-section">
          <div class="avatar-container">
            <FileUpload
              class="avatar-uploader"
              action="/image/upload?thumbSize=20"
              :is-permanent="true"
              :show-loading="true"
              :max-size="5 * 1024 * 1024"
              :file-types="['image/jpeg', 'image/png', 'image/jpg', 'image/webp']"
              @success="onUploadSuccess">
              <HeadImage
                class="avatar"
                :size="60"
                :id="userInfo.id"
                :url="userInfo.headImageThumb"
                radius="50%"
                :name="userInfo.nickName || userInfo.userName"
                :is-show-user-info="false" />
              <div class="upload-overlay">
                <el-icon><Camera /></el-icon>
                <span>{{ '更换头像' }}</span>
              </div>
            </FileUpload>
          </div>
        </div>
        <div class="user-basic-info">
          <h3 class="user-name">{{ userInfo.nickName || userInfo.userName || '未设置昵称' }}</h3>
          <p class="user-id">ID: {{ userInfo.userName || '未知用户' }}</p>
        </div>
      </div>
    </div>
    <div class="form-section">
      <h4 class="section-title">
        <el-icon><User /></el-icon>
        {{ '基本信息' }}
      </h4>
      <el-form ref="personalFormRef" :model="userInfo" label-width="80px" :rules="rules">
        <el-form-item prop="nickName" :label="'昵称'">
          <el-input v-model="userInfo.nickName" autocomplete="off" maxlength="20" show-word-limit :placeholder="'请输入昵称'" />
        </el-form-item>
        <el-form-item :label="'性别'">
          <el-radio-group v-model="userInfo.sex">
            <el-radio :value="0">{{ '男' }}</el-radio>
            <el-radio :value="1">{{ '女' }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>
    <div class="form-section">
      <h4 class="section-title">
        <el-icon><Edit /></el-icon>
        {{ '个性签名' }}
      </h4>
      <el-form :model="userInfo">
        <el-form-item>
          <el-input
            v-model="userInfo.signature"
            type="textarea"
            :rows="3"
            maxlength="64"
            show-word-limit
            :placeholder="'分享你的心情和想法...'" />
        </el-form-item>
      </el-form>
    </div>
    <div class="btn-group">
      <el-button type="primary" @click="onSubmit">{{ '提交' }}</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Camera, Edit, User } from '@element-plus/icons-vue';
import FileUpload from '@/components/common/FileUpload.vue';
import HeadImage from '@/components/common/HeadImage.vue';
import { updateUser } from '@/api/user';
import type { UserVO } from '@/api/user/types';
import type { UploadImageVO } from '@/api/file/types';
import { useConfigStore } from '@/stores/config';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const configStore = useConfigStore();
const personalFormRef = ref<FormInstance>();
const userInfo = reactive<UserVO>({ id: 0, nickName: '' });
const rules: FormRules = {
  nickName: [{ required: true, message: () => '请输入昵称', trigger: 'blur' }]
};
const init = () => {
  const mine = userStore.userInfo;
  if (!mine || !mine.id) {
    setTimeout(() => init(), 100);
    return;
  }
  Object.assign(userInfo, JSON.parse(JSON.stringify(mine)));
};

const onSubmit = () => {
  personalFormRef.value?.validate((valid) => {
    if (!valid) return;
    updateUser(userInfo).then(() => {
      void userStore.loadUser();
      ElMessage.success('修改成功');
    });
  });
};

const onUploadSuccess = (data: UploadImageVO) => {
  userInfo.headImage = data.originUrl;
  userInfo.headImageThumb = data.thumbUrl;
  userStore.userInfo.headImage = data.originUrl;
  userStore.userInfo.headImageThumb = data.thumbUrl;
};
defineExpose({ init });
</script>

<style lang="scss" scoped>
.personal-info {
  padding: 15px;
  background: #fafbfc;
  min-height: 400px;

  .profile-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;

    .profile-header {
      display: flex;
      align-items: center;
      gap: 16px;

      .avatar-container {
        .avatar-uploader {
          position: relative;
          display: inline-block;
          cursor: pointer;
          :deep(.el-upload) {
            border: none !important;
            border-radius: 50%;
            cursor: pointer;
            position: relative;
            overflow: hidden;
          }
          .upload-overlay {
            position: absolute;
            inset: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: rgba(0, 0, 0, 0.45);
            color: #fff;
            border-radius: 50%;
            opacity: 0;
            font-size: 12px;
          }
          &:hover .upload-overlay {
            opacity: 1;
          }
        }
      }

      .user-name {
        margin: 0 0 6px;
        font-size: 18px;
      }

      .user-id {
        margin: 0;
        color: var(--im-text-color-light);
      }
    }
  }

  .form-section {
    background: #fff;
    border-radius: 12px;
    padding: 16px 20px;
    margin-bottom: 16px;

    .section-title {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0 0 12px;
      font-size: 15px;
    }
  }

  .btn-group {
    text-align: center;
    padding: 10px 0 20px;
  }
}
</style>

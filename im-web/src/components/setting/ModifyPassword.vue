<template>
  <div class="modify-password">
    <!-- 密码修改表单 -->
    <div class="form-section">
      <h4 class="section-title">
        <el-icon><Lock /></el-icon>
        {{ '修改密码' }}
      </h4>
      <el-form ref="passwordFormRef" :model="formData" label-width="140px" :rules="rules">
        <el-form-item prop="oldPassword" :label="'原密码'">
          <el-input v-model="formData.oldPassword" type="password" autocomplete="off" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item prop="newPassword" :label="'新密码'">
          <el-input v-model="formData.newPassword" type="password" autocomplete="off" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item prop="confirmPassword" :label="'确认密码'">
          <el-input v-model="formData.confirmPassword" type="password" autocomplete="off" maxlength="20" show-word-limit />
        </el-form-item>
      </el-form>
      <div class="btn-group">
        <el-button type="primary" @click="onSubmit">{{ '提交' }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Lock } from '@element-plus/icons-vue';
import { modifyPwd } from '@/api/login';

const passwordFormRef = ref<FormInstance>();

const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const rules = computed<FormRules>(() => ({
  newPassword: [
    {
      validator: (_rule, value, callback) => {
        if (value === '') {
          return callback(new Error('请输入密码'));
        }
        callback();
      },
      trigger: 'blur'
    }
  ],
  oldPassword: [
    {
      validator: (_rule, value, callback) => {
        if (value === '') {
          return callback(new Error('请输入密码'));
        }
        callback();
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (value === '') {
          return callback(new Error('请输入密码'));
        }
        if (value != formData.newPassword) {
          return callback(new Error('两次密码输入不一致'));
        }
        callback();
      },
      trigger: 'blur'
    }
  ]
}));

const onSubmit = () => {
  passwordFormRef.value?.validate((valid) => {
    if (!valid) return;
    modifyPwd({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword
    }).then(() => {
      ElMessage.success('修改成功');
    });
  });
};

const init = () => {
  formData.oldPassword = '';
  formData.newPassword = '';
  formData.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
};

defineExpose({ init });
</script>

<style scoped lang="scss">
.modify-password {
  padding: 15px;
  background: #fafbfc;
  min-height: 400px;
  // 表单区域

  .form-section {
    background: white;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border: 1px solid #f0f0f0;

    .section-title {
      margin: 0 0 15px 0;
      font-size: var(--im-font-size-larger);
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 6px;
      padding-bottom: 8px;
      border-bottom: 1px solid #f5f5f5;

      .el-icon {
        color: var(--im-color-primary);
        font-size: 16px;
      }
    }
  }

  .btn-group {
    margin-top: 20px;

    .el-button {
      padding: 10px 28px;
      font-size: var(--im-font-size);
      font-weight: 600;
    }
  }
}
</style>

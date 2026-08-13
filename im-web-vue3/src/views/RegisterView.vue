<template>
  <el-container class="register">
    <AuthBackground />
    <div class="content">
      <el-form ref="registerFormRef" class="form" :model="dataForm" :rules="rules">
        <div class="form-header">
          <h1 class="form-title">{{ '用户名注册' }}</h1>
          <p class="form-subtitle">{{ '创建您的账户，开启聊天之旅' }}</p>
        </div>
        <div class="form-body">
          <el-form-item prop="userName">
            <el-input v-model="dataForm.userName" maxlength="20" :placeholder="'用户名'" show-word-limit :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="nickName">
            <el-input v-model="dataForm.nickName" maxlength="20" :placeholder="'昵称'" show-word-limit :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="dataForm.password" type="password" maxlength="20" :placeholder="'密码'" :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="dataForm.confirmPassword" type="password" maxlength="20" :placeholder="'确认密码'" :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="primary" @click="submitForm">{{ '注册并登录' }}</el-button>
          </el-form-item>
        </div>
        <div class="footer-links">
          <router-link class="link" to="/login">{{ '已有账号? 去登录' }}</router-link>
        </div>
      </el-form>
    </div>
    <Icp />
  </el-container>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Lock, User } from '@element-plus/icons-vue';
import AuthBackground from '@/components/auth/AuthBackground.vue';
import Icp from '@/components/common/Icp.vue';
import { login, register } from '@/api/login';
import * as auth from '@/utils/auth';
import { TERMINAL_TYPE } from '@/utils/enums';

interface RegisterForm {
  userName: string;
  nickName: string;
  password: string;
  confirmPassword: string;
}

const router = useRouter();
const registerFormRef = ref<FormInstance>();
const dataForm = reactive<RegisterForm>({
  userName: '',
  nickName: '',
  password: '',
  confirmPassword: ''
});
const rules = computed<FormRules<RegisterForm>>(() => ({
  userName: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback(new Error('请输入用户名'));
          return;
        }
        callback();
      },
      trigger: 'blur'
    }
  ],
  password: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback(new Error('请输入密码'));
          return;
        }
        callback();
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback(new Error('请输入密码'));
          return;
        }
        if (value != dataForm.password) {
          callback(new Error('两次输入密码不一致'));
          return;
        }
        callback();
      },
      trigger: 'blur'
    }
  ]
}));
const submitForm = async () => {
  if (!registerFormRef.value) return;
  const valid = await registerFormRef.value.validate().catch(() => false);
  if (!valid) return;
  await register({
    userName: dataForm.userName,
    password: dataForm.password,
    nickName: dataForm.nickName || undefined
  });
  const data = await login({
    terminal: TERMINAL_TYPE.WEB,
    userName: dataForm.userName,
    password: dataForm.password
  });
  auth.saveLoginSession(data, {
    autoLogin: true,
    userName: dataForm.userName
  });
  ElMessage.success(`注册成功，欢迎使用${dataForm.userName}！`);
  await router.push('/home/chat');
};
</script>

<style scoped lang="scss">
.register {
  position: relative;
  width: 100%;
  height: 100%;

  .content {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    z-index: 1;
  }

  .form {
    width: 380px;
    box-sizing: content-box;
    display: flex;
    flex-direction: column;
    padding: 32px 36px;
    background: rgba(255, 255, 255, 0.88);
    backdrop-filter: blur(24px);
    border-radius: 28px;
    border: 1px solid rgba(255, 255, 255, 0.95);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.12);
  }

  .form-header {
    margin-bottom: 20px;
  }

  .form-title {
    margin: 0 0 6px;
    font-size: 26px;
    font-weight: 700;
    color: var(--im-color-primary);
  }

  .form-subtitle {
    margin: 0;
    font-size: var(--im-font-size);
    color: var(--im-text-color-light);
  }

  .form-body :deep(.el-form-item) {
    margin-bottom: 18px;

    .el-input__wrapper {
      height: 48px;
      border-radius: 50px;
    }
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    border-radius: 50px;
    font-weight: 600;
  }

  .footer-links {
    margin-top: 16px;
    text-align: right;

    .link {
      text-decoration: none;
      color: var(--im-text-color-light);
      font-size: var(--im-font-size-small);

      &:hover {
        color: var(--im-color-primary);
      }
    }
  }
}
</style>

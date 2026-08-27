<template>
  <div class="login-view">
    <AuthBackground />
    <div class="content">
      <el-form
        ref="formRef"
        v-loading="loading"
        class="form"
        :model="loginForm"
        :rules="rules"
        :element-loading-text="'正在登陆...'"
        @keyup.enter="submitForm">
        <div class="form-header">
          <h1 class="form-welcome">{{ '欢迎回来' }}</h1>
          <p class="form-welcome-subtitle">{{ '登录您的账号，开启聊天之旅' }}</p>
        </div>
        <div class="form-body">
          <el-form-item prop="terminal" style="display: none">
            <el-input v-model.number="loginForm.terminal" />
          </el-form-item>
          <el-form-item prop="userName">
            <el-input v-model="loginForm.userName" :placeholder="'用户名'" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" :placeholder="'密码'" :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item>
            <div class="nav-tool-bar">
              <el-checkbox v-model="isAutoLogin">{{ '下次自动登录' }}</el-checkbox>
            </div>
          </el-form-item>
          <el-button class="submit-btn" type="primary" @click="submitForm">
            {{ '登录' }}
          </el-button>
        </div>
        <div class="footer-links">
          <span />
          <router-link class="link" to="/register">{{ '没有账号? 立即注册' }}</router-link>
        </div>
      </el-form>
    </div>
    <Icp />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Lock, User } from '@element-plus/icons-vue';
import AuthBackground from '@/components/auth/AuthBackground.vue';
import Icp from '@/components/common/Icp.vue';
import * as auth from '@/utils/auth';
import { TERMINAL_TYPE } from '@/utils/enums';
import { login } from '@/api/login';
import type { LoginDTO } from '@/api/login/types';
import { useConfigStore } from '@/stores/config';

const router = useRouter();
const configStore = useConfigStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const isAutoLogin = ref(true);
const loginForm = reactive<LoginDTO>({
  terminal: TERMINAL_TYPE.WEB,
  userName: '',
  password: ''
});

const rules = computed<FormRules<LoginDTO>>(() => ({
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
  ]
}));

const submitForm = async () => {
  if (!formRef.value) {
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }
  loading.value = true;
  try {
    const data = await login({ ...loginForm });
    auth.saveLoginSession(data, {
      autoLogin: isAutoLogin.value,
      userName: loginForm.userName
    });
    ElMessage.success('登录成功');
    await router.push('/home/chat');
  } finally {
    loading.value = false;
  }
};

const tryAutoLogin = async () => {
  loading.value = true;
  try {
    await auth.refreshLogin();
    await router.push('/home/chat');
  } catch {
    auth.clearLoginSession(true);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  isAutoLogin.value = auth.isAutoLoginEnabled();
  loginForm.userName = auth.getSavedUsername();
  void configStore.loadConfig().catch(() => undefined);
  if (isAutoLogin.value) {
    void tryAutoLogin();
  }
});
</script>

<style scoped lang="scss">
.login-view {
  position: relative;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  background: #fff;
  overflow: hidden;

  .content {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    z-index: 1;
  }

  .form {
    width: 380px;
    height: 460px;
    box-sizing: content-box;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 32px 36px;
    background: rgba(255, 255, 255, 0.88);
    backdrop-filter: blur(24px);
    border-radius: 28px;
    border: 1px solid rgba(255, 255, 255, 0.95);

    box-shadow:
      0 20px 50px rgba(0, 0, 0, 0.12),
      0 0 0 1px rgba(0, 0, 0, 0.04);
    overflow: hidden;
  }

  .form-header {
    flex-shrink: 0;
    text-align: left;
  }

  .form-welcome {
    margin: 0 0 6px;
    font-size: 26px;
    font-weight: 700;
    letter-spacing: 0.5px;
    line-height: 1.3;
    color: var(--im-color-primary);
  }

  .form-welcome-subtitle {
    margin: 0;
    font-size: var(--im-font-size);
    color: var(--im-text-color-light);
    line-height: 1.5;
  }

  .form-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-height: 0;
  }

  .form-body :deep(.el-form-item) {
    margin-bottom: 20px;

    .el-input__wrapper {
      height: 52px;
      border-radius: 50px;
      box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.06) inset;
      background: #fff;
    }

    .el-input__inner {
      font-size: var(--im-font-size-large);

      &::placeholder {
        color: var(--im-text-color-lighter);
        font-size: var(--im-font-size);
      }
    }

    .el-input__prefix {
      padding: 0 5px;
      color: var(--im-color-primary-light-3);
      font-size: 18px;
    }
  }

  .nav-tool-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  :deep(.nav-tool-bar .el-checkbox__label) {
    color: var(--im-text-color-light);
    font-size: var(--im-font-size-small);
  }

  .link {
    text-decoration: none;
    color: var(--im-text-color-light);
    font-size: var(--im-font-size-small);
    transition: color 0.2s ease;

    &:hover {
      color: var(--im-color-primary);
    }
  }

  .submit-btn {
    width: 100%;
    height: 52px;
    margin-top: 20px;
    border-radius: 50px;
    border: none;
    color: #fff;
    font-size: var(--im-font-size-larger);
    font-weight: 600;
    letter-spacing: 2px;

    transition:
      transform 0.2s ease,
      box-shadow 0.2s ease;
    &:hover {
      transform: translateY(-1px);
    }
  }

  .footer-links {
    flex-shrink: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    padding: 0 4px;
  }
}
</style>

import { createRouter, createWebHashHistory } from 'vue-router';
import * as auth from '@/utils/auth';

const whiteList = ['/login', '/register'];

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { title: '注册' }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true },
      redirect: '/home/chat',
      children: [
        {
          path: 'chat',
          name: 'Chat',
          component: () => import('@/views/ChatView.vue'),
          meta: { title: '聊天' }
        },
        {
          path: 'friend',
          name: 'Friend',
          component: () => import('@/views/FriendView.vue'),
          meta: { title: '好友', keepAlive: true }
        },
        {
          path: 'group',
          name: 'Group',
          component: () => import('@/views/GroupView.vue'),
          meta: { title: '群聊', keepAlive: true }
        },
        {
          path: 'setting',
          name: 'Setting',
          component: () => import('@/views/SettingView.vue'),
          meta: { title: '设置' }
        }
      ]
    }
  ]
});

router.beforeEach((to) => {
  if (whiteList.includes(to.path) || auth.isLoggedIn()) {
    return true;
  }
  return '/login';
});

export default router;

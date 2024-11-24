import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../view/Login'
import Register from '../view/Register'
import Home from '../view/Home'
// 安装路由
Vue.use(VueRouter);

// 配置导出路由
export default new VueRouter({
  routes: [{
    path: "/",
    redirect: "/login"
  },
  {
    name: "Login",
    path: '/login',
    component: Login
  },
  {
    name: "Register",
    path: '/register',
    component: Register
  },
  {
    name: "Home",
    path: '/home',
    component: Home,
    children: [
      {
        name: "Chat",
        path: "/home/chat",
        component: () => import("../view/Chat"),
      },
      {
        name: "Friend",
        path: "/home/friend",
        component: () => import("../view/Friend"),
      },
      {
        name: "GROUP",
        path: "/home/group",
        component: () => import("../view/Group"),
      }
    ]
  }
  ]

});

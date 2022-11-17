import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/iconfont/iconfont.css';
import httpRequest from './api/httpRequest';
import * as socketApi from './api/wssocket';
import emotion  from './api/emotion.js';
import element  from './api/element.js';
import store from './store';


Vue.use(ElementUI);
 
// 挂载全局
Vue.prototype.$wsApi = socketApi;
Vue.prototype.$http = httpRequest // http请求方法
Vue.prototype.$emo = emotion; // emo表情
Vue.prototype.$elm = element; // 元素操作

Vue.config.productionTip = false;

new Vue({
  el: '#app',
  // 配置路由
  router,
  store,
  render: h=>h(App)
})

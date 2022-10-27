import Vue from 'vue'
import App from './App'
import router from './router' // 自动扫描index.js
import axios from 'axios'
import VueAxios from 'vue-axios'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import httpRequest from './utils/httpRequest';

import globalApi from './common/globalApi';
import globalInfo from './common/globalInfo';
import * as socketApi from './api/wssocket'	;	
import store from './store';

console.log(store);
Vue.use(ElementUI);
 
// 挂载全局

Vue.prototype.$wsApi = socketApi;
Vue.prototype.$http = httpRequest // http请求方法
Vue.prototype.globalApi = globalApi; // 注册全局方法
Vue.prototype.globalInfo = globalInfo; // 注册全局变量

Vue.config.productionTip = false

new Vue({
  el: '#app',
  // 配置路由
  router,
  store,
  render: h=>h(App)
})

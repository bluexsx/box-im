import Vue from 'vue'
import App from './App'
import router from './router'
import axios from 'axios'
import VueAxios from 'vue-axios'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import httpRequest from './api/httpRequest';
import * as socketApi from './api/wssocket'	;	
import store from './store';


Vue.use(ElementUI);
 
// 挂载全局
Vue.prototype.$wsApi = socketApi;
Vue.prototype.$http = httpRequest // http请求方法


Vue.config.productionTip = false;

new Vue({
  el: '#app',
  // 配置路由
  router,
  store,
  render: h=>h(App)
})

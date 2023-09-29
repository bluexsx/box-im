import App from './App'
import request from './common/request';
import store from './store';
// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'

Vue.prototype.$http = request
console.log(Vue.prototype.$http)
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App,
  store
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  app.use(store);
  app.config.globalProperties.$http = request
  return {
    app
  }
}
// #endif
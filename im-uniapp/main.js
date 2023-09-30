import App from './App'
import request from './common/request';
import store from './store';
import { createSSRApp } from 'vue'

export function createApp() {
  const app = createSSRApp(App)
  app.use(store);
  app.config.globalProperties.$http = request
  return {
    app
  }
}

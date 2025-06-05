import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import './assets/style/im.scss';
import './assets/iconfont/iconfont.css';
import { createPinia, PiniaVuePlugin } from 'pinia'
import httpRequest from './api/httpRequest';
import * as socketApi from './api/wssocket';
import * as messageType from './api/messageType';
import emotion from './api/emotion.js';
import url from './api/url.js';
import element from './api/element.js';
import * as  enums from './api/enums.js';
import * as  date from './api/date.js';
import './utils/directive/dialogDrag';
import useChatStore from './store/chatStore.js'
import useFriendStore from './store/friendStore.js'
import useGroupStore from './store/groupStore.js'
import useUserStore from './store/userStore.js'
import useConfigStore from './store/configStore.js'
import useUiStore from './store/uiStore.js'


Vue.use(PiniaVuePlugin)
const pinia = createPinia()
Vue.use(ElementUI);
// 挂载全局
Vue.prototype.$wsApi = socketApi;
Vue.prototype.$msgType = messageType
Vue.prototype.$date = date;
Vue.prototype.$http = httpRequest // http请求方法
Vue.prototype.$emo = emotion; // emo表情
Vue.prototype.$url = url; // url转换
Vue.prototype.$elm = element; // 元素操作
Vue.prototype.$enums = enums; // 枚举
Vue.prototype.$eventBus = new Vue(); // 全局事件
Vue.config.productionTip = false;

new Vue({
  el: '#app',
  // 配置路由
  router,
  pinia,
  render: h => h(App)
})

// 挂载全局的pinia
Vue.prototype.chatStore = useChatStore();
Vue.prototype.friendStore = useFriendStore();
Vue.prototype.groupStore = useGroupStore();
Vue.prototype.userStore = useUserStore();
Vue.prototype.configStore = useConfigStore();
Vue.prototype.uiStore = useUiStore();
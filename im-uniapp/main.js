import App from './App'
import request from './common/request';
import emotion from './common/emotion.js';
import * as  enums from './common/enums.js';
import * as date from './common/date';
import * as socketApi from './common/wssocket';
import * as messageType from './common/messageType';
import { createSSRApp } from 'vue'
import uviewPlus from '@/uni_modules/uview-plus'
import * as pinia from 'pinia';
import useChatStore from '@/store/chatStore.js'
import useFriendStore from '@/store/friendStore.js'
import useGroupStore from '@/store/groupStore.js'
import useConfigStore from '@/store/configStore.js'
import useUserStore from '@/store/userStore.js'
	
// #ifdef H5
import * as recorder from './common/recorder-h5';
// #endif
// #ifndef H5
import * as recorder from './common/recorder-app';
// #endif


export function createApp() {
  const app = createSSRApp(App)
  app.use(uviewPlus);
  app.use(pinia.createPinia());
  app.config.globalProperties.$http = request;
  app.config.globalProperties.$wsApi = socketApi;
  app.config.globalProperties.$msgType = messageType;
  app.config.globalProperties.$emo = emotion;
  app.config.globalProperties.$enums = enums;
  app.config.globalProperties.$date = date;
  app.config.globalProperties.$rc = recorder;
  app.config.globalProperties.useChatStore = useChatStore;
  app.config.globalProperties.useFriendStore = useFriendStore;
  app.config.globalProperties.useGroupStore = useGroupStore;
  app.config.globalProperties.useConfigStore = useConfigStore;
  app.config.globalProperties.useUserStore = useUserStore;
  
  return {
    app,
	pinia
  }
}

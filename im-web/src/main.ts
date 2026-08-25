import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import { applyThemeAccent, getThemeAccent } from '@/utils/theme';
import '@/styles/index.scss';
import '@/assets/iconfont/iconfont.css';

applyThemeAccent(getThemeAccent());

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus, { locale: zhCn });

app.mount('#app');

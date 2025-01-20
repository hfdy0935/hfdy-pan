import App from './App.vue';
import router from './routes';
import pinia from '@/stores';

import '@/assets/main.css';
import '@arco-design/web-vue/dist/arco.css';
import '@/assets/icon/iconfont.css';
import ArcoVue from '@arco-design/web-vue';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';

const app = createApp(App);

app.use(pinia);
app.use(router);
app.use(ArcoVue);
app.use(ArcoVueIcon);

app.mount('#app');

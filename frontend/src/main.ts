import App from './App.vue';
import router from './routes';
import pinia from '@/stores';

import '@/assets/main.css';
import '@arco-design/web-vue/dist/arco.css';
import ArcoVue from '@arco-design/web-vue';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import { lazyImage } from './directive';

const app = createApp(App);

app.use(pinia).use(router).use(ArcoVue).use(ArcoVueIcon).directive('lazy', lazyImage).mount('#app');

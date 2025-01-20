import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);

export default pinia;

export { default as useUserStore } from './modules/user';
export { default as useAppStore } from './modules/app';
export { default as useFileStore } from './modules/file';

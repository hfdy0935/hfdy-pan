function appStore() {
  const isDark = ref<boolean>(false);
  const setDark = () => {
    // 设置为暗黑主题
    console.log(0);
    document.body.setAttribute('arco-theme', 'dark');
    isDark.value = true;
  };
  const setLight = () => {
    // 恢复亮色主题
    console.log(1);
    document.body.removeAttribute('arco-theme');
    isDark.value = false;
  };

  // 左侧菜单激活的索引
  const menuIdx = ref(0);
  // 左侧二级菜单（如果有）激活的索引
  const subMenuIdx = ref(0);

  return { isDark, setDark, setLight, menuIdx, subMenuIdx };
}

export default defineStore('appStore', appStore, {
  persist: true
});
